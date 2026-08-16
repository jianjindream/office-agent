package com.jianjin.assistant.domain.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Rewrites a history-dependent question into one standalone primary query, then
 * creates optional variants only when retrieval asks for expansion.
 */
public class LLMRewriter implements Rewriter {

    private static final Logger log = LoggerFactory.getLogger(LLMRewriter.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final int MAX_QUERY_CODE_POINTS = 50;

    private static final String PRIMARY_SYSTEM_PROMPT = """
            你是检索系统的查询改写助手。给定用户当前问题和最近对话历史，输出一条可独立检索的查询。
            若当前问题已经完整明确，原样返回；若包含指代或省略，则仅补全历史中已有的信息。
            不要扩展问题范围，不要编造实体、时间、地点或条件。

            输出严格 JSON，不要任何说明文字：
            {"query":"可独立检索的查询"}
            """;

    private static final String EXPAND_SYSTEM_TEMPLATE = """
            你是检索系统的查询扩展助手。给定一个已经自包含的主查询和最近对话历史，
            仅在确有助于扩大召回覆盖时生成 0 到 %d 条等价检索变体；不需要凑数量。

            type 只能是：
            - variant：同义词、语序或具体表述不同，但意图不变；
            - broad：在不丢失主查询关键约束的前提下，适度使用更泛化的表述。

            不要重复主查询或彼此重复；不要编造历史中不存在的事实；每条不超过 50 字。
            输出严格 JSON，不要任何说明文字：
            {"queries":[{"text":"查询变体","type":"variant"}]}
            """;

    private final BiFunction<String, String, String> generateFn;
    private final int maxQueries;

    public LLMRewriter(BiFunction<String, String, String> generateFn, int maxQueries) {
        this.generateFn = generateFn;
        this.maxQueries = Math.max(1, maxQueries);
    }

    @Override
    public QuerySpec rewritePrimary(String query, List<HistoryMessage> history) {
        String original = normalize(query);
        if (original.isEmpty()) return new QuerySpec("", QueryType.PRIMARY);
        if (history == null || history.isEmpty() || generateFn == null) {
            return new QuerySpec(original, QueryType.PRIMARY);
        }

        String raw;
        try {
            raw = generateFn.apply(PRIMARY_SYSTEM_PROMPT, buildInput(history, "当前问题：" + original));
        } catch (Exception e) {
            log.warn("Primary query rewrite failed; using original query: {}", e.getMessage());
            return new QuerySpec(original, QueryType.PRIMARY);
        }
        String rewritten = parsePrimary(raw);
        if (!isUsable(rewritten)) {
            log.warn("Primary query rewrite parse failed; using original query");
            return new QuerySpec(original, QueryType.PRIMARY);
        }
        return new QuerySpec(rewritten, QueryType.PRIMARY);
    }

    @Override
    public List<QuerySpec> expand(String primaryQuery, List<HistoryMessage> history) {
        String primary = normalize(primaryQuery);
        int maxVariants = maxQueries - 1;
        if (primary.isEmpty() || maxVariants <= 0 || generateFn == null) return List.of();

        String raw;
        try {
            raw = generateFn.apply(String.format(EXPAND_SYSTEM_TEMPLATE, maxVariants),
                    buildInput(history, "主查询：" + primary));
        } catch (Exception e) {
            log.warn("Query expansion failed; keeping primary query only: {}", e.getMessage());
            return List.of();
        }
        return parseExpansion(raw, primary, maxVariants);
    }

    private static String buildInput(List<HistoryMessage> history, String tail) {
        StringBuilder input = new StringBuilder();
        if (history == null || history.isEmpty()) {
            input.append("（无历史）\n");
        } else {
            input.append("最近对话历史（按时间顺序）：\n");
            int start = Math.max(0, history.size() - 6);
            for (int i = start; i < history.size(); i++) {
                HistoryMessage message = history.get(i);
                if (message == null) continue;
                String role = normalize(message.getRole());
                if (role.isEmpty()) role = "user";
                String content = limit(normalize(message.getContent()), 200);
                if (!content.isEmpty()) input.append('[').append(role).append("] ").append(content).append('\n');
            }
        }
        return input.append('\n').append(tail).toString();
    }

    private static String parsePrimary(String raw) {
        try {
            JsonNode root = mapper.readTree(stripCodeFence(raw));
            return normalize(root.path("query").asText(""));
        } catch (Exception ignored) {
            return "";
        }
    }

    private static List<QuerySpec> parseExpansion(String raw, String primary, int maxVariants) {
        List<QuerySpec> out = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        seen.add(dedupKey(primary));
        try {
            JsonNode queries = mapper.readTree(stripCodeFence(raw)).path("queries");
            if (!queries.isArray()) return out;
            for (JsonNode item : queries) {
                String text = normalize(item.path("text").asText(""));
                QueryType type = parseType(item.path("type").asText(""));
                if (!isUsable(text) || type == null || !seen.add(dedupKey(text))) continue;
                out.add(new QuerySpec(text, type));
                if (out.size() == maxVariants) break;
            }
        } catch (Exception e) {
            log.warn("Query expansion parse failed; keeping primary query only");
        }
        return out;
    }

    private static QueryType parseType(String value) {
        String normalized = normalize(value);
        if ("variant".equalsIgnoreCase(normalized)) return QueryType.VARIANT;
        if ("broad".equalsIgnoreCase(normalized)) return QueryType.BROAD;
        return null;
    }

    private static boolean isUsable(String value) {
        return !value.isEmpty() && value.codePointCount(0, value.length()) <= MAX_QUERY_CODE_POINTS;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static String dedupKey(String value) {
        return normalize(value).toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private static String limit(String value, int maxCodePoints) {
        if (value.codePointCount(0, value.length()) <= maxCodePoints) return value;
        int[] codePoints = value.codePoints().limit(maxCodePoints).toArray();
        return new String(codePoints, 0, codePoints.length) + "…";
    }

    private static String stripCodeFence(String raw) {
        if (raw == null) return "";
        String value = raw.trim();
        if (value.startsWith("```json")) value = value.substring(7);
        else if (value.startsWith("```")) value = value.substring(3);
        if (value.endsWith("```")) value = value.substring(0, value.length() - 3);
        return value.trim();
    }
}

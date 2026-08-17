package com.jianjin.assistant.service.rageval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jianjin.assistant.domain.rageval.GenerationMetrics;
import com.jianjin.assistant.service.llm.LlmService;
import com.jianjin.assistant.service.rag.RagService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** LLM-as-a-judge for the two generation dimensions required by the benchmark. */
@Component
public class GenerationJudge {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String SYSTEM = """
            You are a strict RAG evaluator. Score only from the supplied question, reference answer,
            retrieved context, and generated answer. Return strict JSON, with no markdown:
            {"faithfulness":0.0,"answerRelevance":0.0,"reason":"brief Chinese explanation"}
            Scores are numbers from 0 to 1. Faithfulness means every material answer claim is supported
            by the retrieved context; answerRelevance means the answer directly and sufficiently answers
            the user question. Do not use external knowledge.
            """;

    private final LlmService llm;

    public GenerationJudge(LlmService llm) { this.llm = llm; }

    public GenerationMetrics judge(String question, String referenceAnswer,
                                   List<RagService.ScoredChunk> contexts, String answer) {
        StringBuilder context = new StringBuilder();
        if (contexts != null) {
            for (int i = 0; i < contexts.size(); i++) {
                RagService.ScoredChunk item = contexts.get(i);
                if (item == null || item.chunk == null) continue;
                context.append("[").append(i).append("] ")
                        .append(item.chunk.getContent()).append("\n");
            }
        }
        String prompt = "问题：" + safe(question) + "\n\n参考答案：" + safe(referenceAnswer)
                + "\n\n检索上下文：\n" + context + "\n生成回答：" + safe(answer);
        String raw = llm.chat(SYSTEM, List.of(Map.of("role", "user", "content", prompt)));
        return parse(raw);
    }

    private static GenerationMetrics parse(String raw) {
        if (raw == null) return null;
        String json = raw.trim().replace("```json", "").replace("```", "").trim();
        try {
            JsonNode node = MAPPER.readTree(json);
            if (!node.has("faithfulness") || !node.has("answerRelevance")) return null;
            double faithfulness = node.get("faithfulness").asDouble(Double.NaN);
            double relevance = node.get("answerRelevance").asDouble(Double.NaN);
            if (!valid(faithfulness) || !valid(relevance)) return null;
            GenerationMetrics result = new GenerationMetrics();
            result.faithfulness = faithfulness;
            result.answerRelevance = relevance;
            result.reason = node.has("reason") ? node.get("reason").asText() : "";
            return result;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean valid(double value) { return !Double.isNaN(value) && value >= 0 && value <= 1; }
    private static String safe(String value) { return value == null ? "" : value; }
}

package com.jianjin.assistant.domain.rag;

import com.jianjin.assistant.model.Chunk;
import com.jianjin.assistant.service.rag.RagService.ScoredChunk;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class RewriterRerankerTest {

    @Test
    void primaryQuerySkipsLLMWithoutHistory() {
        AtomicBoolean called = new AtomicBoolean(false);
        Rewriter r = new LLMRewriter((sp, um) -> {
            called.set(true);
            return "{}";
        }, 3);

        QuerySpec out = r.rewritePrimary("你好", List.of());

        assertEquals("你好", out.text());
        assertEquals(QueryType.PRIMARY, out.type());
        assertFalse(called.get());
    }

    @Test
    void primaryQueryUsesHistoryToBecomeStandalone() {
        Rewriter r = new LLMRewriter(
                (sp, um) -> "{\"query\":\"小王的报销申请由谁审批？\"}", 3);

        QuerySpec out = r.rewritePrimary("那审批人是谁？",
                List.of(new HistoryMessage("user", "我们在讨论小王的报销申请")));

        assertEquals("小王的报销申请由谁审批？", out.text());
        assertEquals(QueryType.PRIMARY, out.type());
    }

    @Test
    void primaryQueryFallsBackOnInvalidJson() {
        Rewriter r = new LLMRewriter((sp, um) -> "not json", 3);

        QuerySpec out = r.rewritePrimary("原查询", List.of(new HistoryMessage("user", "历史")));

        assertEquals("原查询", out.text());
    }

    @Test
    void expansionDeduplicatesValidatesTypesAndCapsCount() {
        Rewriter r = new LLMRewriter((sp, um) -> """
                {"queries":[
                  {"text":"主查询","type":"variant"},
                  {"text":"未知类型","type":"other"},
                  {"text":"变体一","type":"variant"},
                  {"text":"变体一","type":"broad"},
                  {"text":"泛化变体","type":"broad"},
                  {"text":"%s","type":"variant"}
                ]}
                """.formatted("x".repeat(51)), 3);

        List<QuerySpec> out = r.expand("主查询", List.of());

        assertEquals(2, out.size());
        assertEquals(new QuerySpec("变体一", QueryType.VARIANT), out.get(0));
        assertEquals(new QuerySpec("泛化变体", QueryType.BROAD), out.get(1));
    }

    @Test
    void expansionCanReturnNoVariantsOnInvalidJson() {
        Rewriter r = new LLMRewriter((sp, um) -> "not json", 3);
        assertTrue(r.expand("主查询", List.of()).isEmpty());
    }

    @Test
    void rerankerReturnsOriginalWhenLLMNull() {
        Reranker r = new LLMReranker(null, 200);
        List<ScoredChunk> in = sample(5);
        List<ScoredChunk> out = r.rerank("query", in, 3);
        assertEquals(3, out.size());
    }

    @Test
    void rerankerReordersByLLMScores() {
        Reranker r = new LLMReranker(
                (sp, um) -> "{\"scores\":[{\"idx\":0,\"score\":1},{\"idx\":1,\"score\":10},{\"idx\":2,\"score\":5}]}",
                200);
        List<ScoredChunk> in = sample(3);
        List<ScoredChunk> out = r.rerank("query", in, 3);
        assertEquals(3, out.size());
        // idx=1 应排第一（分数最高）
        assertEquals(in.get(1).chunk.getId(), out.get(0).chunk.getId());
    }

    @Test
    void rerankerFallsBackOnInvalidJson() {
        Reranker r = new LLMReranker((sp, um) -> "garbage", 200);
        List<ScoredChunk> in = sample(5);
        List<ScoredChunk> out = r.rerank("query", in, 3);
        assertEquals(3, out.size());
    }

    private static List<ScoredChunk> sample(int n) {
        List<ScoredChunk> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(new ScoredChunk(new Chunk(i, "chunk content " + i), 0.5 - i * 0.05));
        }
        return list;
    }
}

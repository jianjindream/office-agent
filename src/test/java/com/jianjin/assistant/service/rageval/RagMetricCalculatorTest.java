package com.jianjin.assistant.service.rageval;

import com.jianjin.assistant.domain.rageval.ContextMetrics;
import com.jianjin.assistant.domain.rageval.StageMetrics;
import com.jianjin.assistant.model.Chunk;
import com.jianjin.assistant.service.rag.RagService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RagMetricCalculatorTest {
    @Test
    void calculatesRecallHitRateMrrAndNdcgForMultipleRelevantContexts() {
        List<RagService.ScoredChunk> ranked = List.of(hit(8), hit(5), hit(9), hit(7));
        StageMetrics metrics = RagMetricCalculator.evaluate(List.of(7L, 9L), ranked, List.of(1, 3, 10));

        assertEquals(0.0, metrics.recallAtK.get(1), 1e-9);
        assertEquals(0.5, metrics.recallAtK.get(3), 1e-9);
        assertEquals(1.0, metrics.recallAtK.get(10), 1e-9);
        assertEquals(0.0, metrics.hitRateAtK.get(1), 1e-9);
        assertEquals(1.0 / 3, metrics.mrrAtK.get(3), 1e-9);
        assertEquals(0.306573596, metrics.ndcgAtK.get(3), 1e-8);
    }

    @Test
    void treatsEmptyResultsAsZeroAndContextSetOverlapAsPrecisionRecall() {
        StageMetrics empty = RagMetricCalculator.evaluate(List.of(1L), List.of(), List.of(1, 5));
        assertEquals(0.0, empty.hitRateAtK.get(1), 1e-9);
        assertEquals(0.0, empty.ndcgAtK.get(5), 1e-9);

        ContextMetrics context = RagMetricCalculator.context(List.of(1L, 2L), List.of(hit(1), hit(9), hit(2)));
        assertEquals(2.0 / 3, context.precision, 1e-9);
        assertEquals(1.0, context.recall, 1e-9);
    }

    private static RagService.ScoredChunk hit(long id) {
        return new RagService.ScoredChunk(id, new Chunk((int) id, "context-" + id), 1.0, "test", 1, false);
    }
}

package com.jianjin.assistant.service.rageval;

import com.jianjin.assistant.domain.rageval.ContextMetrics;
import com.jianjin.assistant.domain.rageval.StageMetrics;
import com.jianjin.assistant.service.rag.RagService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Deterministic metrics over gold-labelled parent context IDs. */
public final class RagMetricCalculator {
    private RagMetricCalculator() {}

    public static StageMetrics evaluate(Collection<Long> relevantIds,
                                        List<RagService.ScoredChunk> ranked,
                                        List<Integer> topKs) {
        Set<Long> relevant = normalizedSet(relevantIds);
        List<Long> ids = rankedIds(ranked);
        StageMetrics metrics = new StageMetrics();
        for (int k : topKs) {
            int limit = Math.min(k, ids.size());
            int hits = 0;
            double dcg = 0;
            double reciprocalRank = 0;
            for (int i = 0; i < limit; i++) {
                if (relevant.contains(ids.get(i))) {
                    hits++;
                    if (reciprocalRank == 0) reciprocalRank = 1.0 / (i + 1);
                    dcg += 1.0 / log2(i + 2);
                }
            }
            double idcg = 0;
            for (int i = 0; i < Math.min(k, relevant.size()); i++) idcg += 1.0 / log2(i + 2);
            metrics.recallAtK.put(k, relevant.isEmpty() ? 0.0 : (double) hits / relevant.size());
            metrics.hitRateAtK.put(k, hits > 0 ? 1.0 : 0.0);
            metrics.mrrAtK.put(k, reciprocalRank);
            metrics.ndcgAtK.put(k, idcg == 0 ? 0.0 : dcg / idcg);
        }
        return metrics;
    }

    public static ContextMetrics context(Collection<Long> relevantIds, List<RagService.ScoredChunk> contexts) {
        Set<Long> relevant = normalizedSet(relevantIds);
        List<Long> ids = rankedIds(contexts);
        int hits = 0;
        for (Long id : ids) if (relevant.contains(id)) hits++;
        ContextMetrics metrics = new ContextMetrics();
        metrics.precision = ids.isEmpty() ? 0.0 : (double) hits / ids.size();
        metrics.recall = relevant.isEmpty() ? 0.0 : (double) hits / relevant.size();
        return metrics;
    }

    public static StageMetrics averageStages(List<StageMetrics> all, List<Integer> topKs) {
        StageMetrics out = new StageMetrics();
        for (int k : topKs) {
            out.recallAtK.put(k, average(all, m -> m.recallAtK.get(k)));
            out.hitRateAtK.put(k, average(all, m -> m.hitRateAtK.get(k)));
            out.mrrAtK.put(k, average(all, m -> m.mrrAtK.get(k)));
            out.ndcgAtK.put(k, average(all, m -> m.ndcgAtK.get(k)));
        }
        return out;
    }

    public static ContextMetrics averageContexts(List<ContextMetrics> all) {
        ContextMetrics out = new ContextMetrics();
        out.precision = average(all, m -> m.precision);
        out.recall = average(all, m -> m.recall);
        return out;
    }

    private static <T> double average(List<T> all, java.util.function.Function<T, Double> getter) {
        if (all == null || all.isEmpty()) return 0.0;
        double sum = 0;
        int count = 0;
        for (T value : all) {
            if (value == null) continue;
            Double number = getter.apply(value);
            if (number != null) { sum += number; count++; }
        }
        return count == 0 ? 0.0 : sum / count;
    }

    private static Set<Long> normalizedSet(Collection<Long> ids) {
        Set<Long> out = new LinkedHashSet<>();
        if (ids != null) for (Long id : ids) if (id != null && id >= 0) out.add(id);
        return out;
    }

    private static List<Long> rankedIds(List<RagService.ScoredChunk> ranked) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        if (ranked != null) {
            for (RagService.ScoredChunk item : ranked) {
                if (item != null && item.contextId >= 0) ids.add(item.contextId);
            }
        }
        return new ArrayList<>(ids);
    }

    private static double log2(int value) { return Math.log(value) / Math.log(2); }
}

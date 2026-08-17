package com.jianjin.assistant.domain.rageval;

import java.util.LinkedHashMap;
import java.util.Map;

/** Retrieval metrics, keyed by K for lossless JSON output. */
public class StageMetrics {
    public Map<Integer, Double> recallAtK = new LinkedHashMap<>();
    public Map<Integer, Double> hitRateAtK = new LinkedHashMap<>();
    public Map<Integer, Double> mrrAtK = new LinkedHashMap<>();
    public Map<Integer, Double> ndcgAtK = new LinkedHashMap<>();
}

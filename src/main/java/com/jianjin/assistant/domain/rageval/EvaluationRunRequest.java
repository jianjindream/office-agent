package com.jianjin.assistant.domain.rageval;

import java.util.ArrayList;
import java.util.List;

public class EvaluationRunRequest {
    public String datasetName;
    public String datasetVersion;
    public List<Integer> topKs = new ArrayList<>();
    /** Defaults to true: set false for a retrieval-only, zero-LLM-cost run. */
    public Boolean generationEvaluation;
}

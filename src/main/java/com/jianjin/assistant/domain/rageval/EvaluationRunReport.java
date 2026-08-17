package com.jianjin.assistant.domain.rageval;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EvaluationRunReport {
    public String runId;
    public String datasetName;
    public String datasetVersion;
    public String startedAt;
    public String completedAt;
    public String status;
    public boolean persisted;
    public List<Integer> topKs = new ArrayList<>();
    public Map<String, Object> configurationSnapshot = new LinkedHashMap<>();
    public StageMetrics retrievalMetrics;
    public StageMetrics rerankMetrics;
    public ContextMetrics contextMetrics;
    public GenerationMetrics generationMetrics;
    public List<EvaluationCaseResult> caseResults = new ArrayList<>();
    public List<String> warnings = new ArrayList<>();
}

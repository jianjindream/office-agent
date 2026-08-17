package com.jianjin.assistant.domain.rageval;

import java.util.ArrayList;
import java.util.List;

public class EvaluationCaseResult {
    public String caseId;
    public String question;
    public String category;
    public String difficulty;
    public String primaryQuery;
    public List<String> expandedQueries = new ArrayList<>();
    public List<Long> relevantContextIds = new ArrayList<>();
    public List<Long> primaryRetrievalContextIds = new ArrayList<>();
    public List<Long> retrievalContextIds = new ArrayList<>();
    public List<Long> rerankedContextIds = new ArrayList<>();
    public List<Long> finalContextIds = new ArrayList<>();
    public List<Long> missedContextIds = new ArrayList<>();
    public List<RankedContext> retrievalRanking = new ArrayList<>();
    public List<RankedContext> primaryRetrievalRanking = new ArrayList<>();
    public List<RankedContext> rerankRanking = new ArrayList<>();
    public List<RankedContext> finalContextRanking = new ArrayList<>();
    public StageMetrics retrievalMetrics;
    public StageMetrics rerankMetrics;
    public ContextMetrics contextMetrics;
    public GenerationMetrics generationMetrics;
    public String answer;
    public String warning;
}

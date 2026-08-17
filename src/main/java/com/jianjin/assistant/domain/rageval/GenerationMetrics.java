package com.jianjin.assistant.domain.rageval;

/** LLM-as-a-judge scores; null means the judge was not available. */
public class GenerationMetrics {
    public Double faithfulness;
    public Double answerRelevance;
    public String reason;
}

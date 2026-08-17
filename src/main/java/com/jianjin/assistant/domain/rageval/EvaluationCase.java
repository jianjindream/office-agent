package com.jianjin.assistant.domain.rageval;

import java.util.ArrayList;
import java.util.List;

/** One question plus the parent context IDs that support its answer. */
public class EvaluationCase {
    public String caseId;
    public String question;
    public String referenceAnswer;
    public List<Long> relevantContextIds = new ArrayList<>();
    public String category;
    public String difficulty;
    public List<String> tags = new ArrayList<>();
}

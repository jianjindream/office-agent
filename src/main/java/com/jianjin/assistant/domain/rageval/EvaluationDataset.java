package com.jianjin.assistant.domain.rageval;

import java.util.ArrayList;
import java.util.List;

/** A versioned, human-labelled RAG golden dataset. */
public class EvaluationDataset {
    public String name;
    public String version;
    public String description;
    public List<EvaluationCase> cases = new ArrayList<>();
}

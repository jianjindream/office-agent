package com.jianjin.assistant.domain.rag;

/** A normalized query plus its retrieval role. */
public record QuerySpec(String text, QueryType type) {
    public QuerySpec {
        if (text == null) text = "";
        if (type == null) type = QueryType.VARIANT;
    }
}

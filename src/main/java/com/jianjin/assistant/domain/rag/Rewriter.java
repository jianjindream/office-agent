package com.jianjin.assistant.domain.rag;

public interface Rewriter {
    /** Produces the one query used for the first retrieval pass. */
    QuerySpec rewritePrimary(String query, java.util.List<HistoryMessage> history);

    /** Produces optional expansion queries after the first retrieval pass is insufficient. */
    java.util.List<QuerySpec> expand(String primaryQuery, java.util.List<HistoryMessage> history);
}

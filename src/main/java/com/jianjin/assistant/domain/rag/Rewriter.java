package com.jianjin.assistant.domain.rag;

import java.util.List;

public interface Rewriter {
    List<String> rewrite(String query, List<HistoryMessage> history);
}

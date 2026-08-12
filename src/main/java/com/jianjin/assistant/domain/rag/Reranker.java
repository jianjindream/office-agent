package com.jianjin.assistant.domain.rag;

import com.jianjin.assistant.service.rag.RagService.ScoredChunk;

import java.util.List;

public interface Reranker {
    List<ScoredChunk> rerank(String query, List<ScoredChunk> results, int topK);
}

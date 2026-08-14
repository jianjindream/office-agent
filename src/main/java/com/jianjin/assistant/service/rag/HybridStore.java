package com.jianjin.assistant.service.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jianjin.assistant.config.AppConfig;
import com.jianjin.assistant.infrastructure.InfrastructureService;
import com.jianjin.assistant.model.Chunk;
import com.jianjin.assistant.service.graph.ChunkRef;
import com.jianjin.assistant.service.graph.GraphSearchResult;
import com.jianjin.assistant.service.graph.KGStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class HybridStore {

    private static final Logger log = LoggerFactory.getLogger(HybridStore.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final AppConfig cfg;
    private final InfrastructureService infra;
    private Function<String, List<Double>> embedFn;
    private KGStore kg;
    private String mode = "unavailable";

    public HybridStore(AppConfig cfg, InfrastructureService infra) {
        this.cfg = cfg;
        this.infra = infra;
        recomputeMode();
    }

    public void recomputeMode() {
        boolean milvusOK = "connected".equals(infra.getMilvusStatus());
        boolean esOK = "connected".equals(infra.getEsStatus());
        if (milvusOK && esOK) mode = "hybrid";
        else if (milvusOK) mode = "semantic";
        else if (esOK) mode = "keyword";
        else mode = "unavailable";
    }

    public void setEmbedFn(Function<String, List<Double>> fn) { this.embedFn = fn; }
    public void setKGStore(KGStore kg) { this.kg = kg; }
    public String getMode() { return mode; }

    /**
     * Persists large parent contexts and indexes only the small child chunks.
     * The returned references use PostgreSQL child IDs, which keeps graph search
     * and vector/keyword search on the same identifier space.
     */
    public IndexResult index(List<TextSplitter.ParentChunk> parents, String docContent) {
        String docHash = sha256(docContent).substring(0, 16);
        // Re-ingesting the same document must replace every old child/vector rather
        // than leaving stale rows when its chunking strategy changes.
        List<Long> oldChildIds = infra.deleteRAGChunksByDocHash(docHash);
        if (!oldChildIds.isEmpty()) {
            if ("connected".equals(infra.getEsStatus())) infra.deleteRAGChunksFromES(oldChildIds);
            if ("connected".equals(infra.getMilvusStatus())) infra.deleteRAGChunksFromMilvus(oldChildIds);
            if (kg != null && kg.available()) kg.deleteDocument(docHash);
        }
        List<Long> pgIds = new ArrayList<>();
        List<String> contents = new ArrayList<>();
        List<List<Float>> embeddings = new ArrayList<>();
        List<ChunkRef> childRefs = new ArrayList<>();

        for (TextSplitter.ParentChunk parent : parents) {
            long parentId = infra.saveRAGParentChunk(docHash, parent.getIndex(), parent.getContent());
            if (parentId < 0) {
                log.warn("RAG parent chunk save failed (index={})", parent.getIndex());
                continue;
            }
            for (Chunk child : parent.getChildren()) {
                List<Double> embedding = embedFn == null ? null : embedFn.apply(child.getContent());
                String embeddingJson = "null";
                if (embedding != null && !embedding.isEmpty()) {
                    try { embeddingJson = mapper.writeValueAsString(embedding); } catch (Exception ignored) { }
                }
                long childId = infra.saveRAGChunk(docHash, child.getId(), parentId,
                        child.getContent(), embeddingJson);
                if (childId < 0) {
                    log.warn("RAG child chunk save failed (index={})", child.getId());
                    continue;
                }
                childRefs.add(new ChunkRef(childId, child.getContent()));
                if ("connected".equals(infra.getEsStatus())) {
                    infra.indexRAGChunkInES(childId, child.getContent(), docHash, child.getId());
                }
                if ("connected".equals(infra.getMilvusStatus()) && embedding != null && !embedding.isEmpty()) {
                    pgIds.add(childId);
                    contents.add(child.getContent());
                    List<Float> embedding32 = new ArrayList<>(embedding.size());
                    for (Double value : embedding) embedding32.add(value.floatValue());
                    embeddings.add(embedding32);
                }
            }
        }
        if (!pgIds.isEmpty()) infra.insertRAGChunks(pgIds, contents, embeddings);
        return new IndexResult(docHash, childRefs);
    }

    public List<Long> delete(String docHash) {
        List<Long> pgIds = infra.deleteRAGChunksByDocHash(docHash);
        if (pgIds.isEmpty()) return pgIds;
        if ("connected".equals(infra.getEsStatus())) infra.deleteRAGChunksFromES(pgIds);
        if ("connected".equals(infra.getMilvusStatus())) infra.deleteRAGChunksFromMilvus(pgIds);
        return pgIds;
    }

    public void restoreChunks(List<Chunk> chunks) {
        // PostgreSQL remains the source of truth; parent context is loaded at query time.
    }

    public List<SearchResult> search(String query, int topK) {
        recomputeMode();
        return switch (mode) {
            case "hybrid" -> searchHybrid(query, topK);
            case "semantic" -> searchSemantic(query, topK);
            case "keyword" -> searchKeyword(query, topK);
            default -> Collections.emptyList();
        };
    }

    private List<SearchResult> searchHybrid(String query, int topK) {
        if (embedFn == null) return searchKeyword(query, topK);
        List<Double> embedding = embedFn.apply(query);
        if (embedding == null || embedding.isEmpty()) return searchKeyword(query, topK);

        List<Float> queryVector = new ArrayList<>(embedding.size());
        for (Double value : embedding) queryVector.add(value.floatValue());
        int fetchK = childFetchK(topK);
        List<InfrastructureService.MilvusHit> milvusHits = infra.milvusSearchWithScores(queryVector, fetchK);
        List<InfrastructureService.ESHit> esHits = infra.searchRAGChunks(query, fetchK);
        if (milvusHits.isEmpty() && esHits.isEmpty()) return Collections.emptyList();

        int k = cfg.getRag().getRrfConstantK() > 0 ? cfg.getRag().getRrfConstantK() : 60;
        Map<Long, Double> rrf = new HashMap<>();
        for (int i = 0; i < milvusHits.size(); i++) {
            rrf.merge(milvusHits.get(i).id, 1.0 / (k + i + 1), Double::sum);
        }
        for (int i = 0; i < esHits.size(); i++) {
            rrf.merge(esHits.get(i).pgId, 1.0 / (k + i + 1), Double::sum);
        }
        if (kg != null && kg.available()) {
            List<GraphSearchResult> kgHits = kg.search(query, fetchK);
            for (int i = 0; i < kgHits.size(); i++) {
                GraphSearchResult hit = kgHits.get(i);
                rrf.merge(hit.getChunkId(), hit.getScore() + 1.0 / (k + i + 1), Double::sum);
            }
        }

        List<Map.Entry<Long, Double>> ranked = new ArrayList<>(rrf.entrySet());
        ranked.sort((left, right) -> Double.compare(right.getValue(), left.getValue()));
        List<ChildHit> childHits = new ArrayList<>();
        for (Map.Entry<Long, Double> hit : ranked) childHits.add(new ChildHit(hit.getKey(), hit.getValue()));
        return expandToParentContexts(childHits, topK, "hybrid");
    }

    private List<SearchResult> searchSemantic(String query, int topK) {
        if (embedFn == null) return Collections.emptyList();
        List<Double> embedding = embedFn.apply(query);
        if (embedding == null || embedding.isEmpty()) return Collections.emptyList();
        List<Float> queryVector = new ArrayList<>(embedding.size());
        for (Double value : embedding) queryVector.add(value.floatValue());

        List<InfrastructureService.MilvusHit> hits = infra.milvusSearchWithScores(queryVector, childFetchK(topK));
        List<ChildHit> childHits = new ArrayList<>();
        // Milvus uses L2 distance (smaller is better); rank is a safe comparable relevance score.
        for (int i = 0; i < hits.size(); i++) childHits.add(new ChildHit(hits.get(i).id, 1.0 / (i + 1)));
        return expandToParentContexts(childHits, topK, "semantic");
    }

    private List<SearchResult> searchKeyword(String query, int topK) {
        List<InfrastructureService.ESHit> hits = infra.searchRAGChunks(query, childFetchK(topK));
        List<ChildHit> childHits = new ArrayList<>();
        for (InfrastructureService.ESHit hit : hits) childHits.add(new ChildHit(hit.pgId, hit.score));
        return expandToParentContexts(childHits, topK, "keyword");
    }

    /** Collapses retrieved children to parent contexts using max child relevance. */
    private List<SearchResult> expandToParentContexts(List<ChildHit> childHits, int topK, String source) {
        if (childHits.isEmpty()) return Collections.emptyList();
        List<Long> childIds = new ArrayList<>();
        for (ChildHit hit : childHits) childIds.add(hit.childId);

        Map<Long, InfrastructureService.RagContextRow> contextByChild = new HashMap<>();
        for (InfrastructureService.RagContextRow row : infra.loadRAGContextsByChildIDs(childIds)) {
            contextByChild.put(row.childId, row);
        }
        Map<Long, SearchResult> parentResults = new LinkedHashMap<>();
        for (ChildHit hit : childHits) {
            InfrastructureService.RagContextRow context = contextByChild.get(hit.childId);
            if (context == null || context.content == null) continue;
            SearchResult current = parentResults.get(context.contextId);
            if (current == null || hit.score > current.score) {
                parentResults.put(context.contextId,
                        new SearchResult(new Chunk(0, context.content), hit.score, source));
            }
        }
        List<SearchResult> results = new ArrayList<>(parentResults.values());
        results.sort((left, right) -> Double.compare(right.score, left.score));
        if (results.size() > topK) return new ArrayList<>(results.subList(0, topK));
        return results;
    }

    private int childFetchK(int parentTopK) {
        return Math.max(10, parentTopK * 4);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
    }

    public static class SearchResult {
        public Chunk chunk;
        public double score;
        public String source;

        public SearchResult(Chunk chunk, double score, String source) {
            this.chunk = chunk;
            this.score = score;
            this.source = source;
        }
    }

    public static class IndexResult {
        public final String docHash;
        public final List<ChunkRef> childRefs;

        public IndexResult(String docHash, List<ChunkRef> childRefs) {
            this.docHash = docHash;
            this.childRefs = childRefs;
        }
    }

    private static class ChildHit {
        private final long childId;
        private final double score;

        private ChildHit(long childId, double score) {
            this.childId = childId;
            this.score = score;
        }
    }
}

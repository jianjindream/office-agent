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
        List<GraphSearchResult> kgHits = Collections.emptyList();
        if (kg != null && kg.available()) {
            kgHits = kg.search(query, fetchK);
        }
        if (milvusHits.isEmpty() && esHits.isEmpty() && kgHits.isEmpty()) return Collections.emptyList();

        int k = rrfConstant();
        Map<Long, FusionHit> rrf = new HashMap<>();
        double activeWeight = 0;
        if (!milvusHits.isEmpty()) {
            double weight = sourceWeight(cfg.getRag().getSemanticWeight(), 0.7);
            activeWeight += weight;
            for (int i = 0; i < milvusHits.size(); i++) {
                mergeRrf(rrf, milvusHits.get(i).id, weight / (k + i + 1), SearchSource.MILVUS);
            }
        }
        if (!esHits.isEmpty()) {
            double weight = sourceWeight(cfg.getRag().getKeywordWeight(), 1.0);
            activeWeight += weight;
            for (int i = 0; i < esHits.size(); i++) {
                mergeRrf(rrf, esHits.get(i).pgId, weight / (k + i + 1), SearchSource.ES);
            }
        }
        if (!kgHits.isEmpty()) {
            // GraphSearchResult.score ranks graph hits internally; cross-source fusion uses rank only.
            double weight = sourceWeight(cfg.getNeo4j().getWeight(), 0.3);
            activeWeight += weight;
            for (int i = 0; i < kgHits.size(); i++) {
                mergeRrf(rrf, kgHits.get(i).getChunkId(), weight / (k + i + 1), SearchSource.NEO4J);
            }
        }
        if (activeWeight <= 0) return Collections.emptyList();

        List<Map.Entry<Long, FusionHit>> ranked = new ArrayList<>(rrf.entrySet());
        ranked.sort((left, right) -> Double.compare(right.getValue().score, left.getValue().score));
        List<ChildHit> childHits = new ArrayList<>();
        for (Map.Entry<Long, FusionHit> hit : ranked) {
            childHits.add(new ChildHit(hit.getKey(), hit.getValue().score / activeWeight, hit.getValue().sourceMask));
        }
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
        for (int i = 0; i < hits.size(); i++) {
            childHits.add(new ChildHit(hits.get(i).id, 1.0 / (rrfConstant() + i + 1), SearchSource.MILVUS.mask));
        }
        return expandToParentContexts(childHits, topK, "semantic");
    }

    private List<SearchResult> searchKeyword(String query, int topK) {
        List<InfrastructureService.ESHit> hits = infra.searchRAGChunks(query, childFetchK(topK));
        List<ChildHit> childHits = new ArrayList<>();
        for (int i = 0; i < hits.size(); i++) {
            childHits.add(new ChildHit(hits.get(i).pgId, 1.0 / (rrfConstant() + i + 1), SearchSource.ES.mask));
        }
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
            if (current == null) {
                parentResults.put(context.contextId, new SearchResult(
                        context.contextId, new Chunk(0, context.content), hit.score, source, hit.sourceMask));
            } else {
                current.sourceMask |= hit.sourceMask;
                current.sourceSupportCount = Integer.bitCount(current.sourceMask);
                if (hit.score > current.score) {
                    current.chunk = new Chunk(0, context.content);
                    current.score = hit.score;
                    current.source = source;
                }
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

    private int rrfConstant() {
        return cfg.getRag().getRrfConstantK() > 0 ? cfg.getRag().getRrfConstantK() : 60;
    }

    private static double sourceWeight(double configured, double fallback) {
        return configured > 0 ? configured : fallback;
    }

    private static void mergeRrf(Map<Long, FusionHit> rrf, long id, double contribution, SearchSource source) {
        FusionHit hit = rrf.computeIfAbsent(id, ignored -> new FusionHit());
        hit.score += contribution;
        hit.sourceMask |= source.mask;
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
        public long contextId;
        public Chunk chunk;
        public double score;
        public String source;
        public int sourceSupportCount;
        private int sourceMask;

        public SearchResult(long contextId, Chunk chunk, double score, String source, int sourceMask) {
            this.contextId = contextId;
            this.chunk = chunk;
            this.score = score;
            this.source = source;
            this.sourceMask = sourceMask;
            this.sourceSupportCount = Integer.bitCount(sourceMask);
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
        private final int sourceMask;

        private ChildHit(long childId, double score, int sourceMask) {
            this.childId = childId;
            this.score = score;
            this.sourceMask = sourceMask;
        }
    }

    private static class FusionHit {
        private double score;
        private int sourceMask;
    }

    private enum SearchSource {
        MILVUS(1), ES(2), NEO4J(4);

        private final int mask;

        SearchSource(int mask) {
            this.mask = mask;
        }
    }
}

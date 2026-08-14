package com.jianjin.assistant.service.rag;

import com.jianjin.assistant.model.Chunk;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unicode-safe text splitter with sliding window overlap.
 */
@Component
public class TextSplitter {

    private int chunkSize = 200;
    private int overlap = 50;
    private int parentChunkSize = 800;
    private int parentOverlap = 100;

    public void setChunkSize(int chunkSize) { this.chunkSize = chunkSize; }
    public void setOverlap(int overlap) { this.overlap = overlap; }
    public void setParentChunkSize(int parentChunkSize) { this.parentChunkSize = parentChunkSize; }
    public void setParentOverlap(int parentOverlap) { this.parentOverlap = parentOverlap; }

    public List<Chunk> split(String text) {
        return split(text, chunkSize, overlap, 0);
    }

    /**
     * Splits a document into larger parent contexts and small retrievable children.
     * Child identifiers are unique within the document, while each parent keeps its
     * own ordinal for persistence.
     */
    public List<ParentChunk> splitParentChild(String text) {
        if (text == null || text.isEmpty()) return Collections.emptyList();

        int effectiveParentSize = parentChunkSize > 0
                ? parentChunkSize : Math.max(chunkSize * 4, 600);
        int effectiveParentOverlap = parentOverlap >= 0 ? parentOverlap : overlap * 2;
        int parentStep = effectiveParentSize - effectiveParentOverlap;
        if (parentStep <= 0) parentStep = effectiveParentSize;

        int[] codePoints = text.codePoints().toArray();
        List<ParentChunk> parents = new ArrayList<>();
        int childId = 0;
        int parentIdx = 0;
        for (int start = 0; start < codePoints.length; start += parentStep) {
            int end = Math.min(start + effectiveParentSize, codePoints.length);
            String parentContent = new String(codePoints, start, end - start);
            List<Chunk> children = split(parentContent, chunkSize, overlap, childId);
            childId += children.size();
            parents.add(new ParentChunk(parentIdx++, parentContent, children));
            if (end >= codePoints.length) break;
        }
        return parents;
    }

    private List<Chunk> split(String text, int size, int chunkOverlap, int firstId) {
        List<Chunk> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) return chunks;
        int step = size - chunkOverlap;
        if (step <= 0) step = size;

        int[] codePoints = text.codePoints().toArray();
        int id = firstId;
        for (int i = 0; i < codePoints.length; i += step) {
            int end = Math.min(i + size, codePoints.length);
            String content = new String(codePoints, i, end - i);
            chunks.add(new Chunk(id++, content));
            if (end >= codePoints.length) break;
        }
        return chunks;
    }

    public static class ParentChunk {
        private final int index;
        private final String content;
        private final List<Chunk> children;

        public ParentChunk(int index, String content, List<Chunk> children) {
            this.index = index;
            this.content = content;
            this.children = children;
        }

        public int getIndex() { return index; }
        public String getContent() { return content; }
        public List<Chunk> getChildren() { return children; }
    }
}

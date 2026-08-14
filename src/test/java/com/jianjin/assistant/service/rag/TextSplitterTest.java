package com.jianjin.assistant.service.rag;

import com.jianjin.assistant.model.Chunk;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextSplitterTest {

    @Test
    void splitsAnEightHundredCharacterParentIntoFiveFullChildren() {
        TextSplitter splitter = configuredSplitter();
        List<TextSplitter.ParentChunk> parents = splitter.splitParentChild("x".repeat(800));

        assertEquals(1, parents.size());
        List<Chunk> children = parents.get(0).getChildren();
        assertEquals(5, children.size());
        for (Chunk child : children) assertEquals(200, child.getContent().length());
    }

    @Test
    void assignsUniqueChildIdsAcrossOverlappingParents() {
        TextSplitter splitter = configuredSplitter();
        List<TextSplitter.ParentChunk> parents = splitter.splitParentChild("x".repeat(1500));

        assertEquals(2, parents.size());
        List<Chunk> first = parents.get(0).getChildren();
        List<Chunk> second = parents.get(1).getChildren();
        assertEquals(0, first.get(0).getId());
        assertEquals(first.size(), second.get(0).getId());
        assertEquals(800, parents.get(0).getContent().length());
        assertEquals(800, parents.get(1).getContent().length());
    }

    private static TextSplitter configuredSplitter() {
        TextSplitter splitter = new TextSplitter();
        splitter.setChunkSize(200);
        splitter.setOverlap(50);
        splitter.setParentChunkSize(800);
        splitter.setParentOverlap(100);
        return splitter;
    }
}

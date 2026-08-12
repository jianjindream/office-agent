package com.jianjin.assistant.service.graph;

public class ChunkRef {
    private final int id;
    private final String content;

    public ChunkRef(int id, String content) {
        this.id = id;
        this.content = content;
    }

    public int getId() { return id; }
    public String getContent() { return content; }
}

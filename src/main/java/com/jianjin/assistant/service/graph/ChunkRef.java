package com.jianjin.assistant.service.graph;

public class ChunkRef {
    private final long id;
    private final String content;

    public ChunkRef(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() { return id; }
    public String getContent() { return content; }
}

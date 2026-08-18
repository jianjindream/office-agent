package com.jianjin.assistant.service.memory;

/** All memory that belongs to one user, intentionally shared by that user's sessions only. */
public class UserMemorySpace {
    private final PreferenceMemory preferences;
    private final LongTermMemory longTerm;
    private volatile GraphMemory graph;

    UserMemorySpace(PreferenceMemory preferences, LongTermMemory longTerm) {
        this.preferences = preferences; this.longTerm = longTerm;
    }
    public PreferenceMemory preferences() { return preferences; }
    public LongTermMemory longTerm() { return longTerm; }
    public GraphMemory graph() { return graph; }
    void setGraph(GraphMemory graph) { this.graph = graph; }
}

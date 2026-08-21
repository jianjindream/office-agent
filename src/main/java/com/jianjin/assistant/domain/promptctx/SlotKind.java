package com.jianjin.assistant.domain.promptctx;

public enum SlotKind {
    PROFILE("profile"),
    PLANNER("planner"),
    TASK_MEMORY("task_memory"),
    TOOL_STATE("tool_state"),
    CONSTRAINTS("constraints"),
    RECALL("recall_memory");

    private final String value;

    SlotKind(String value) { this.value = value; }

    public String value() { return value; }
}

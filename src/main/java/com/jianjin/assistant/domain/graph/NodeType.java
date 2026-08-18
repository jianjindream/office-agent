package com.jianjin.assistant.domain.graph;

public enum NodeType {
    TOOL("tool"),
    SUB_AGENT("sub_agent"),
    THINK("think"),
    AGGREGATE("aggregate");

    private final String value;
    NodeType(String value) { this.value = value; }
    public String value() { return value; }

    public static NodeType fromValue(String s) {
        if (s == null) return TOOL;
        for (NodeType nt : values()) if (nt.value.equals(s)) return nt;
        return TOOL;
    }

    @Override public String toString() { return value; }
}

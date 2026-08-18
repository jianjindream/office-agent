package com.jianjin.assistant.domain.graph;

public enum NodeStatus {
    PENDING("pending"),
    RUNNING("running"),
    DONE("done"),
    FAILED("failed"),
    SKIPPED("skipped"),
    CANCELLED("cancelled");

    private final String value;
    NodeStatus(String value) { this.value = value; }
    public String value() { return value; }

    @Override public String toString() { return value; }
}

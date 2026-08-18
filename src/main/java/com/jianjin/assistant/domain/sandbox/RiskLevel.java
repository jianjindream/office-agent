package com.jianjin.assistant.domain.sandbox;

public enum RiskLevel {
    SAFE("safe"),
    WARN("warn"),
    BLOCK("block");

    private final String value;

    RiskLevel(String value) { this.value = value; }

    public String value() { return value; }
}

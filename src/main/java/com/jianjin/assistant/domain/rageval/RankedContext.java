package com.jianjin.assistant.domain.rageval;

public class RankedContext {
    public long contextId;
    public int rank;
    public double score;
    public String source;
    public int sourceSupportCount;
    public boolean fallback;
}

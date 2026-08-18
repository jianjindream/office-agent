package com.jianjin.assistant.service.memory;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicBoolean;

/** A session-local raw window plus its durable rolling summary. */
public class SessionMemoryState {
    private final ShortTermMemory messages;
    private final ReentrantLock lock = new ReentrantLock();
    private SessionSummary summary;
    private final AtomicBoolean asyncSummaryRunning = new AtomicBoolean(false);

    SessionMemoryState(ShortTermMemory messages, SessionSummary summary) {
        this.messages = messages; this.summary = summary == null ? new SessionSummary() : summary;
    }
    public ShortTermMemory messages() { return messages; }
    public ReentrantLock lock() { return lock; }
    public SessionSummary summary() { return summary; }
    public void setSummary(SessionSummary summary) { this.summary = summary == null ? new SessionSummary() : summary; }
    public boolean beginAsyncSummary() { return asyncSummaryRunning.compareAndSet(false, true); }
    public void endAsyncSummary() { asyncSummaryRunning.set(false); }
}

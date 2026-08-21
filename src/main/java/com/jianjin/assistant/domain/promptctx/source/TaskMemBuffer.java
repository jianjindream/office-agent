package com.jianjin.assistant.domain.promptctx.source;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class TaskMemBuffer {

    private final Deque<StepObservation> buf = new LinkedList<>();
    private final int max;

    public TaskMemBuffer() { this(20); }
    public TaskMemBuffer(int max) {
        this.max = max <= 0 ? 20 : max;
    }

    public synchronized void push(StepObservation obs) {
        if (obs == null) return;
        if (obs.getCreatedAt() == null) obs.setCreatedAt(LocalDateTime.now());
        buf.addLast(obs);
        while (buf.size() > max) buf.removeFirst();
    }

    public synchronized void reset() { buf.clear(); }

    public synchronized List<StepObservation> snapshot() {
        return new ArrayList<>(buf);
    }
}

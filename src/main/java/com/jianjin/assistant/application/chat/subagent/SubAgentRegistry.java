package com.jianjin.assistant.application.chat.subagent;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SubAgentRegistry {

    private final Map<String, SubAgent> agents = new ConcurrentHashMap<>();

    public void register(SubAgent a) {
        if (a == null || a.name() == null || a.name().isEmpty()) return;
        agents.put(a.name(), a);
    }

    public SubAgent get(String name) {
        if (name == null) return null;
        return agents.get(name);
    }

    public boolean has(String name) {
        return name != null && agents.containsKey(name);
    }

    public Map<String, SubAgent> snapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(agents));
    }
}

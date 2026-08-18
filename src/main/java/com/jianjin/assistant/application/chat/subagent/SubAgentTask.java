package com.jianjin.assistant.application.chat.subagent;

import java.util.LinkedHashMap;
import java.util.Map;

public class SubAgentTask {

    public final String id;
    public final String goal;
    public final String query;
    public final Map<String, String> upstream;

    public SubAgentTask(String id, String goal, String query, Map<String, String> upstream) {
        this.id = id == null ? "" : id;
        this.goal = goal == null ? "" : goal;
        this.query = query == null ? "" : query;
        this.upstream = upstream == null ? new LinkedHashMap<>() : upstream;
    }
}

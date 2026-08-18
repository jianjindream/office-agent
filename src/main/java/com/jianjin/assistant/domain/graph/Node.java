package com.jianjin.assistant.domain.graph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Node {

    private String id;
    private NodeType type = NodeType.TOOL;
    /** Planner 给出的 reason，便于在 SSE / 快照里展示 */
    private String name;
    /** 工具节点必填 */
    private String toolName;
    private String agentName = "";
    private String goal = "";
    private Map<String, String> params = new LinkedHashMap<>();
    /** 入边：依赖哪些节点的输出 */
    private List<String> dependsOn = new ArrayList<>();
    /** 空字符串=独立；同 group 表示竞速组 */
    private String raceGroup = "";
    private NodeStatus status = NodeStatus.PENDING;
    private String result = "";
    private String error = "";
    private int retryCount;

    public Node() {}

    public Node(String id, NodeType type, String name, String toolName,
                Map<String, String> params, List<String> dependsOn, String raceGroup) {
        this.id = id;
        this.type = type == null ? NodeType.TOOL : type;
        this.name = name;
        this.toolName = toolName;
        if (params != null) this.params = params;
        if (dependsOn != null) this.dependsOn = dependsOn;
        if (raceGroup != null) this.raceGroup = raceGroup;
    }

    public static Node subAgent(String id, String name, String agentName, String goal,
                                List<String> dependsOn, String raceGroup) {
        Node n = new Node();
        n.id = id;
        n.type = NodeType.SUB_AGENT;
        n.name = name;
        n.agentName = agentName == null ? "" : agentName;
        n.goal = goal == null ? "" : goal;
        if (dependsOn != null) n.dependsOn = dependsOn;
        if (raceGroup != null) n.raceGroup = raceGroup;
        return n;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public NodeType getType() { return type; }
    public void setType(NodeType type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName == null ? "" : agentName; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal == null ? "" : goal; }
    public Map<String, String> getParams() { return params; }
    public void setParams(Map<String, String> params) { this.params = params == null ? new LinkedHashMap<>() : params; }
    public List<String> getDependsOn() { return dependsOn; }
    public void setDependsOn(List<String> dependsOn) { this.dependsOn = dependsOn == null ? new ArrayList<>() : dependsOn; }
    public String getRaceGroup() { return raceGroup; }
    public void setRaceGroup(String raceGroup) { this.raceGroup = raceGroup == null ? "" : raceGroup; }
    public NodeStatus getStatus() { return status; }
    public void setStatus(NodeStatus status) { this.status = status; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result == null ? "" : result; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error == null ? "" : error; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    /** 节点的"执行体名称"：sub-agent 节点用 agentName，否则用 toolName。 */
    public String executorName() {
        if (type == NodeType.SUB_AGENT) return agentName == null ? "" : agentName;
        return toolName == null ? "" : toolName;
    }
}

package com.jianjin.assistant.application.chat;

import com.jianjin.assistant.config.AppConfig;
import com.jianjin.assistant.domain.graph.Node;
import com.jianjin.assistant.domain.graph.NodeType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlannerSubAgentRuleTest {

    @Test
    void researchQueryPlansSubAgents() {
        Planner planner = new Planner(new AppConfig(), null, null);
        List<Node> nodes = planner.planGraph("调研 PPO 的优势，两句话即可", new HashMap<>(), "");
        String[] want = {"research_agent", "writer_agent", "review_agent"};
        assertEquals(want.length, nodes.size(), "expected " + want.length + " sub-agent nodes");
        for (int i = 0; i < want.length; i++) {
            assertEquals(NodeType.SUB_AGENT, nodes.get(i).getType(), "node " + i + " type");
            assertEquals(want[i], nodes.get(i).getAgentName(), "node " + i + " agent");
        }
    }

    @Test
    void reportSaveQueryPlansDocAgent() {
        Planner planner = new Planner(new AppConfig(), null, null);
        List<Node> nodes = planner.planGraph("生成一份 PPO 报告并保存到本地文档库", new HashMap<>(), "");
        String[] want = {"research_agent", "writer_agent", "review_agent", "doc_agent"};
        assertEquals(want.length, nodes.size());
        for (int i = 0; i < want.length; i++) {
            assertEquals(NodeType.SUB_AGENT, nodes.get(i).getType());
            assertEquals(want[i], nodes.get(i).getAgentName());
        }
    }
}

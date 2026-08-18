package com.jianjin.assistant.application.chat;

import com.jianjin.assistant.dto.ChatResponse;
import com.jianjin.assistant.model.Tool;
import com.jianjin.assistant.model.ToolCallResult;
import com.jianjin.assistant.service.llm.LlmService;
import com.jianjin.assistant.service.memory.PreferenceMemory;
import com.jianjin.assistant.service.tools.ToolService;

import java.util.List;
import java.util.Map;

public class ToolModeHandler {

    private final LlmService llm;
    private final ToolService toolService;
    private final PreferenceMemory pref;

    public ToolModeHandler(LlmService llm, ToolService toolService, PreferenceMemory pref) {
        this.llm = llm;
        this.toolService = toolService;
        this.pref = pref;
    }

    public void run(ChatResponse resp, String query, Map<String, Tool> ts,
                    String memPrefix, List<Map<String, String>> histMsgs) {
        ToolCallResult tc = toolService.decide(query, ts);
        if (tc == null) {
            resp.setAnswer("我无法处理这个请求。");
            return;
        }

        Tool tool = ts.get(tc.getToolName());
        if (tool == null) {
            resp.setAnswer("工具 " + tc.getToolName() + " 不存在");
            resp.setToolCall(tc);
            return;
        }

        PreferenceFiller.fill(tc, pref);

        try {
            String result = tool.getExecute().apply(tc.getParams());
            tc.setToolResult(result);
        } catch (Exception e) {
            resp.setAnswer("工具执行失败: " + e.getMessage());
            resp.setToolCall(tc);
            return;
        }

        String sp = ChatHistoryAdapter.buildSystemPrompt(memPrefix,
                "你是一个善于综合信息的AI助手。结合你掌握的用户信息，使回答更个性化。");
        String userMsg = String.format(
                "用户问：%s\n工具 %s 返回结果：%s\n请根据结果自然地回答用户。",
                query, tc.getToolName(), tc.getToolResult());
        resp.setAnswer(llm.chat(sp, List.of(Map.of("role", "user", "content", userMsg))));
        resp.setToolCall(tc);
    }
}

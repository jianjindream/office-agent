package com.jianjin.assistant.application.chat;

import com.jianjin.assistant.model.ConversationMessage;
import com.jianjin.assistant.service.memory.ShortTermMemory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ChatHistoryAdapter {

    private ChatHistoryAdapter() {}

    /** STM → LLM messages，并保证最后一条是当前 user 消息。 */
    public static List<Map<String, String>> buildHistory(ShortTermMemory stm, String query) {
        List<Map<String, String>> msgs = new ArrayList<>();
        for (ConversationMessage m : stm.getMessages()) {
            if ("user".equals(m.getRole()) || "assistant".equals(m.getRole())) {
                msgs.add(Map.of("role", m.getRole(), "content", m.getContent()));
            }
        }
        if (msgs.isEmpty() || !msgs.get(msgs.size() - 1).get("content").equals(query)) {
            msgs.add(Map.of("role", "user", "content", query));
        }
        return msgs;
    }

    /** memPrefix + base 拼成 system prompt。 */
    public static String buildSystemPrompt(String memPrefix, String basePrompt) {
        if (memPrefix == null || memPrefix.isEmpty()) return basePrompt;
        return memPrefix + "\n\n" + basePrompt;
    }
}

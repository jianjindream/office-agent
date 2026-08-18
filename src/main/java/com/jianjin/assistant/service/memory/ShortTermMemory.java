package com.jianjin.assistant.service.memory;

import com.jianjin.assistant.model.ConversationMessage;
import org.springframework.stereotype.Component;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ShortTermMemory {

    private final List<ConversationMessage> messages = Collections.synchronizedList(new ArrayList<>());
    private int maxTurns = 5;

    public void setMaxTurns(int maxTurns) { this.maxTurns = maxTurns; }

    public ConversationMessage add(String role, String content) {
        ConversationMessage message = new ConversationMessage(role, content,
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        messages.add(message);
        int max = maxTurns * 2;
        while (messages.size() > max) {
            messages.remove(0);
        }
        return message;
    }

    public ConversationMessage add(long id, String role, String content, String timestamp) {
        ConversationMessage message = new ConversationMessage(id, role, content,
                timestamp == null ? LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) : timestamp);
        messages.add(message);
        return message;
    }

    public List<ConversationMessage> getMessages() {
        return new ArrayList<>(messages);
    }

    public int size() { return messages.size(); }

    public List<ConversationMessage> removeOldest(int count) {
        List<ConversationMessage> removed = new ArrayList<>();
        while (count-- > 0 && !messages.isEmpty()) removed.add(messages.remove(0));
        return removed;
    }
}

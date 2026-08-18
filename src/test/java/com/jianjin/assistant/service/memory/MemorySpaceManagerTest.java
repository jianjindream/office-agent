package com.jianjin.assistant.service.memory;

import com.jianjin.assistant.config.AppConfig;
import com.jianjin.assistant.infrastructure.InfrastructureService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemorySpaceManagerTest {
    @Test void separatesUsersAndSessionsWhileKeepingUserProfileAcrossSessions() {
        InfrastructureService infra = mock(InfrastructureService.class);
        when(infra.loadPreferences(anyString())).thenReturn(Map.of());
        when(infra.loadLongTermItems(anyString())).thenReturn(List.of());
        when(infra.loadSessionSummary(anyString(), anyString())).thenReturn(new SessionSummary());
        when(infra.loadChatHistory(anyString(), anyString(), anyLong(), anyInt())).thenReturn(List.of());
        MemorySpaceManager manager = new MemorySpaceManager(new AppConfig(), infra);

        manager.user("u1").preferences().save("语言", "中文");
        assertEquals("中文", manager.user("u1").preferences().getData().get("语言"));
        assertFalse(manager.user("u2").preferences().getData().containsKey("语言"));

        manager.session(MemoryScope.from("u1", "s1")).messages().add("user", "会话一");
        assertEquals(1, manager.session(MemoryScope.from("u1", "s1")).messages().size());
        assertEquals(0, manager.session(MemoryScope.from("u1", "s2")).messages().size());
        assertEquals(0, manager.session(MemoryScope.from("u2", "s1")).messages().size());
    }
}

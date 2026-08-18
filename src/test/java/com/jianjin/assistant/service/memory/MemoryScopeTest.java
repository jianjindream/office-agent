package com.jianjin.assistant.service.memory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MemoryScopeTest {
    @Test void missingIdentifiersUseBackwardCompatibleDefaultSpace() {
        MemoryScope scope = MemoryScope.from(null, " ");
        assertEquals("default", scope.userId());
        assertEquals("default", scope.sessionId());
    }

    @Test void invalidIdentifierIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> MemoryScope.from("user id", "s1"));
    }
}

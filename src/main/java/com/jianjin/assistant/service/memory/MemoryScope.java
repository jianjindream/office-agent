package com.jianjin.assistant.service.memory;

/** User-level memory is shared across sessions; transient conversation state is session-level. */
public record MemoryScope(String userId, String sessionId) {
    public static final String DEFAULT = "default";
    private static final int MAX_ID_LENGTH = 128;

    public static MemoryScope from(String userId, String sessionId) {
        return new MemoryScope(normalize(userId, "user_id"), normalize(sessionId, "session_id"));
    }

    private static String normalize(String value, String field) {
        if (value == null || value.isBlank()) return DEFAULT;
        String result = value.trim();
        if (result.length() > MAX_ID_LENGTH || !result.matches("[A-Za-z0-9._:-]+")) {
            throw new IllegalArgumentException(field + " 格式不合法");
        }
        return result;
    }
}

package com.jianjin.assistant.service.memory;

/** Conservative local estimate that stays model-agnostic for the configured LLM providers. */
public final class TokenEstimator {
    private TokenEstimator() {}

    public static int estimate(String text) {
        if (text == null || text.isEmpty()) return 0;
        int tokens = 0, asciiRun = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (isCjk(c)) {
                tokens += flushAscii(asciiRun); asciiRun = 0; tokens++;
            } else if (Character.isLetterOrDigit(c)) {
                asciiRun++;
            } else {
                tokens += flushAscii(asciiRun); asciiRun = 0;
                if (!Character.isWhitespace(c)) tokens++;
            }
        }
        tokens += flushAscii(asciiRun);
        return (int) Math.ceil(tokens * 1.2d);
    }

    public static int estimateMessage(String role, String content) {
        return estimate(content) + estimate(role) + 4;
    }

    private static int flushAscii(int length) { return length == 0 ? 0 : (length + 3) / 4; }
    private static boolean isCjk(char c) { return (c >= 0x2E80 && c <= 0x9FFF) || (c >= 0xAC00 && c <= 0xD7AF); }
}

package com.jianjin.assistant.service.memory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenEstimatorTest {
    @Test void estimatesChineseEnglishAndMessageOverheadConservatively() {
        int chinese = TokenEstimator.estimate("你好，世界");
        int english = TokenEstimator.estimate("hello world");
        int mixed = TokenEstimator.estimate("使用 Spring Boot 开发");
        assertTrue(chinese >= 4);
        assertTrue(english >= 3);
        assertTrue(mixed > english);
        assertTrue(TokenEstimator.estimateMessage("user", "hello") > TokenEstimator.estimate("hello"));
    }
}

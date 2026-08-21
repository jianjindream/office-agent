package com.jianjin.assistant.infrastructure.eventbus;

public interface EventBus {
    /** 发布一个事件。eventType 用作 Kafka key，payload 通常是 JSON。 */
    void publish(String eventType, String payload);
}

package com.jianjin.assistant.application.chat.subagent;

import java.util.concurrent.atomic.AtomicBoolean;

public interface SubAgent {

    /** 注册名（也是 Planner JSON 里写的 agent 字段）。 */
    String name();

    /** 给规划器看的一句话能力描述。 */
    String description();

    /** 执行子 Agent；抛异常表示失败，由 GraphRuntime 决定是否重试。 */
    String run(SubAgentTask task, AtomicBoolean cancelled) throws Exception;
}

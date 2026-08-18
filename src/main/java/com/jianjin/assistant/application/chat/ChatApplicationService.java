package com.jianjin.assistant.application.chat;

import com.jianjin.assistant.dto.ChatRequest;
import com.jianjin.assistant.dto.ChatResponse;
import com.jianjin.assistant.service.agent.UnifiedAgentService;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class ChatApplicationService {

    private final UnifiedAgentService agent;

    public ChatApplicationService(UnifiedAgentService agent) {
        this.agent = agent;
    }

    /** 同步对话入口 */
    public ChatResponse process(ChatRequest req) {
        return agent.processWithOptions(req.getMessage(), req);
    }

    /**
     * 流式对话入口。每完成一个语义事件（start / mode / step / tool_call /
     * observation / rag_result / done）就回调一次 onEvent。
     *
     * <p>调用方（典型为 controller 的 SseEmitter）应在回调中把事件序列化推送给前端。</p>
     */
    public ChatResponse processStream(ChatRequest req, Consumer<StreamEvent> onEvent) {
        return agent.processStream(req.getMessage(), req, onEvent);
    }

    /** 取消所有 in-flight 请求 */
    public void cancel() {
        agent.cancel();
    }

    /** 暴露内部引擎，仅供需要直接访问 RAG/工具/记忆的接口层用例（status / upload）。 */
    public UnifiedAgentService engine() {
        return agent;
    }
}

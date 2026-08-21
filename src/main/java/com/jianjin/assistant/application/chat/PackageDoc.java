package com.jianjin.assistant.application.chat;

import com.jianjin.assistant.dto.ChatRequest;
import com.jianjin.assistant.dto.ChatResponse;
import com.jianjin.assistant.model.Tool;
import com.jianjin.assistant.service.agent.UnifiedAgentService;

import java.util.Locale;
import java.util.Map;

public final class PackageDoc {
    private PackageDoc() {}

    /** 简化签名说明：模式选择 + 工具过滤是 Router 的职责。 */
    @SuppressWarnings("unused")
    static String demoRoute(ChatRequest req, Map<String, Tool> available, boolean ragLoaded) {
        return ChatRouter.decideMode(
                req.getMessage(),
                req.isExplicit(),
                req.isUseRag(),
                req.getSelectedTools(),
                ragLoaded
        ).toLowerCase(Locale.ROOT);
    }

    /** 简化签名说明：调用引擎执行整轮处理。 */
    @SuppressWarnings("unused")
    static ChatResponse demoExecute(UnifiedAgentService engine, ChatRequest req) {
        return engine.processWithOptions(req.getMessage(), req);
    }
}

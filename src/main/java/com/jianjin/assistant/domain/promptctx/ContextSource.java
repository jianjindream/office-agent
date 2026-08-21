package com.jianjin.assistant.domain.promptctx;

import java.util.List;

public interface ContextSource {
    String id();

    boolean supports(SlotKind kind);

    /**
     * 在不超过 slot.filter.tokenBudget 的前提下，返回适合该槽位的 ContextItem。
     * 实现需自己做 TopK 截断与 budget 裁剪。
     */
    List<ContextItem> fetch(Slot slot, Query q);
}

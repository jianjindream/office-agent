package com.jianjin.assistant.domain.promptctx.source;

import com.jianjin.assistant.domain.promptctx.ContextItem;
import com.jianjin.assistant.domain.promptctx.ContextSource;
import com.jianjin.assistant.domain.promptctx.Query;
import com.jianjin.assistant.domain.promptctx.Slot;
import com.jianjin.assistant.domain.promptctx.SlotKind;
import com.jianjin.assistant.model.Tool;
import com.jianjin.assistant.model.ToolParam;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

public class ToolStateSource implements ContextSource {

    private final Supplier<Map<String, Tool>> registry;
    private final ToolStateTracker tracker;

    public ToolStateSource(Supplier<Map<String, Tool>> registry, ToolStateTracker tracker) {
        this.registry = registry;
        this.tracker = tracker;
    }

    @Override
    public String id() { return "tool_state"; }

    @Override
    public boolean supports(SlotKind kind) { return kind == SlotKind.TOOL_STATE; }

    @Override
    public List<ContextItem> fetch(Slot slot, Query q) {
        List<ContextItem> items = new ArrayList<>();
        if (registry != null) {
            Map<String, Tool> tools = registry.get();
            if (tools != null && !tools.isEmpty()) {
                Map<String, Tool> sorted = new TreeMap<>(tools);
                for (Map.Entry<String, Tool> e : sorted.entrySet()) {
                    String name = e.getKey();
                    Tool t = e.getValue();
                    StringBuilder paramHint = new StringBuilder();
                    if (t.getParameters() != null) {
                        for (ToolParam p : t.getParameters()) {
                            if (p.isRequired()) {
                                if (paramHint.length() > 0) paramHint.append(", ");
                                paramHint.append(p.getName());
                            }
                        }
                    }
                    String suffix = paramHint.length() == 0 ? "" : "（必填 " + paramHint + "）";
                    Map<String, String> meta = new LinkedHashMap<>();
                    meta.put("tool", name);
                    items.add(new ContextItem(
                            name + " — " + (t.getDescription() == null ? "" : t.getDescription()) + suffix,
                            0, id(), meta));
                }
            }
        }

        if (tracker != null) {
            List<ToolCallTrace> traces = tracker.snapshot();
            int topK = slot.getFilter().getTopK();
            if (topK > 0 && traces.size() > topK) {
                traces = traces.subList(traces.size() - topK, traces.size());
            }
            for (ToolCallTrace tr : traces) {
                String status = tr.isSuccess() ? "成功" : "失败";
                Map<String, String> meta = new LinkedHashMap<>();
                meta.put("tool", tr.getToolName());
                meta.put("status", status);
                items.add(new ContextItem(
                        "近期调用 " + tr.getToolName() + " [" + status + "]: "
                                + (tr.getSummary() == null ? "" : tr.getSummary()),
                        0, id(), meta));
            }
        }
        return items;
    }
}

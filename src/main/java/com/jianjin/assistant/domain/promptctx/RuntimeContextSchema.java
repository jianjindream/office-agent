package com.jianjin.assistant.domain.promptctx;

import java.util.List;

public class RuntimeContextSchema {
    private final String mode;
    private final List<Slot> slots;

    public RuntimeContextSchema(String mode, List<Slot> slots) {
        this.mode = mode; this.slots = slots;
    }

    public String getMode() { return mode; }
    public List<Slot> getSlots() { return slots; }
}

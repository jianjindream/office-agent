package com.jianjin.assistant.application.chat;

import com.jianjin.assistant.model.ToolCallResult;
import com.jianjin.assistant.service.memory.PreferenceMemory;

import java.util.List;
import java.util.Map;

public final class PreferenceFiller {

    private static final Map<String, List<String>> PREF_TO_PARAM = Map.of(
            "城市", List.of("city", "location", "location_name"),
            "时区", List.of("timezone", "tz", "time_zone"),
            "姓名", List.of("name", "username", "user_name"),
            "语言", List.of("language", "lang"),
            "国家", List.of("country", "nation")
    );

    private PreferenceFiller() {}

    public static void fill(ToolCallResult tc, PreferenceMemory pref) {
        if (tc == null || pref.getData().isEmpty()) return;
        for (Map.Entry<String, List<String>> e : PREF_TO_PARAM.entrySet()) {
            String prefVal = pref.getData().get(e.getKey());
            if (prefVal == null || prefVal.isEmpty()) continue;
            for (String paramName : e.getValue()) {
                Object v = tc.getParams().get(paramName);
                if (v == null || v.toString().isEmpty()) {
                    tc.getParams().put(paramName, prefVal);
                }
            }
        }
    }
}

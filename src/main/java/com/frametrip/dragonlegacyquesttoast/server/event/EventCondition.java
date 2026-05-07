package com.frametrip.dragonlegacyquesttoast.server.event;

import java.util.LinkedHashMap;
import java.util.Map;

public class EventCondition {
    public EventConditionType type = EventConditionType.ITEM_IN_INVENTORY;
    /** Flexible key→value storage for type-specific params. */
    public Map<String, String> params = new LinkedHashMap<>();

    public EventCondition() {}

    public EventCondition(EventConditionType type) {
        this.type = type;
    }

    public String param(String key) {
        return params.getOrDefault(key, "");
    }

    public void param(String key, String value) {
        params.put(key, value);
    }

    public EventCondition copy() {
        EventCondition c = new EventCondition(type);
        c.params = new LinkedHashMap<>(params);
        return c;
    }
}

package com.frametrip.dragonlegacyquesttoast.server.event;

import java.util.HashMap;
import java.util.Map;

public class EventCondition {

    public EventConditionType type = EventConditionType.ITEM_IN_INVENTORY;
    public Map<String, String> params = new HashMap<>();

    public String param(String key) {
        return params.getOrDefault(key, "");
    }

    public void param(String key, String value) {
        params.put(key, value);
    }

    public EventCondition copy() {
        EventCondition c = new EventCondition();
        c.type   = this.type;
        c.params = new HashMap<>(this.params);
        return c;
    }
}

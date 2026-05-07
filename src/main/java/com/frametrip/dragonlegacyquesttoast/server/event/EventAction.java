package com.frametrip.dragonlegacyquesttoast.server.event;

import java.util.LinkedHashMap;
import java.util.Map;

public class EventAction {
    public EventActionType type = EventActionType.SAY_PHRASE;
    public Map<String, String> params = new LinkedHashMap<>();

    public EventAction() {}

    public EventAction(EventActionType type) {
        this.type = type;
    }

    public String param(String key) {
        return params.getOrDefault(key, "");
    }

    public void param(String key, String value) {
        params.put(key, value);
    }

    public EventAction copy() {
        EventAction c = new EventAction(type);
        c.params = new LinkedHashMap<>(params);
        return c;
    }
}

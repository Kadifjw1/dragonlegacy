package com.frametrip.dragonlegacyquesttoast.server.event;

import java.util.HashMap;
import java.util.Map;

public class EventAction {

    public EventActionType type = EventActionType.SAY_PHRASE;
    public Map<String, String> params = new HashMap<>();

    public String param(String key) {
        return params.getOrDefault(key, "");
    }

    public void param(String key, String value) {
        params.put(key, value);
    }

    public EventAction copy() {
        EventAction c = new EventAction();
        c.type   = this.type;
        c.params = new HashMap<>(this.params);
        return c;
    }
}

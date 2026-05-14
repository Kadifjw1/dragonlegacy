package com.frametrip.dragonlegacyquesttoast.server.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EventChain {
    public String              name          = "";
    public boolean             enabled       = true;
    public EventTriggerType    trigger       = EventTriggerType.NPC_CLICK;
    public Map<String, String> triggerParams = new LinkedHashMap<>();
    public List<EventCondition> conditions   = new ArrayList<>();
    public String              conditionMode = "AND";
    public List<EventAction>   actions       = new ArrayList<>();
    public boolean             executeAll    = true;

    public String triggerParam(String key) {
        return triggerParams.getOrDefault(key, "");
    }

    public void triggerParam(String key, String value) {
        triggerParams.put(key, value);
    }

    public EventChain copy() {
        EventChain c = new EventChain();
        c.name          = this.name;
        c.enabled       = this.enabled;
        c.trigger       = this.trigger;
        c.conditionMode = this.conditionMode;
        c.executeAll    = this.executeAll;
        c.triggerParams = new HashMap<>(this.triggerParams);
        for (EventCondition cond : this.conditions) c.conditions.add(cond.copy());
        for (EventAction    act  : this.actions)    c.actions.add(act.copy());
        return c;
    }
}

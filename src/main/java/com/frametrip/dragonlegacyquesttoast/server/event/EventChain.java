package com.frametrip.dragonlegacyquesttoast.server.event;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EventChain {
    public String id = UUID.randomUUID().toString().substring(0, 8);
    public String name = "Событие";
    public boolean enabled = true;

    public EventTriggerType trigger = EventTriggerType.NPC_CLICK;
    /** Type-specific trigger params (e.g. "phrase", "radius", "interval"). */
    public Map<String, String> triggerParams = new LinkedHashMap<>();

    public List<EventCondition> conditions = new ArrayList<>();
    /** "AND" — all conditions must pass; "OR" — any one is enough. */
    public String conditionMode = "AND";

    public List<EventAction> actions = new ArrayList<>();
    /** If true, all matching chains execute; if false, only the first. */
    public boolean executeAll = true;

    public String triggerParam(String key) {
        return triggerParams.getOrDefault(key, "");
    }

    public void triggerParam(String key, String value) {
        triggerParams.put(key, value);
    }

    public EventChain copy() {
        EventChain c = new EventChain();
        c.id            = UUID.randomUUID().toString().substring(0, 8);
        c.name          = this.name + " (копия)";
        c.enabled       = this.enabled;
        c.trigger       = this.trigger;
        c.triggerParams = new LinkedHashMap<>(this.triggerParams);
        c.conditionMode = this.conditionMode;
        c.executeAll    = this.executeAll;
        for (EventCondition cond : conditions) c.conditions.add(cond.copy());
        for (EventAction act  : actions)     c.actions.add(act.copy());
        return c;
    }
}

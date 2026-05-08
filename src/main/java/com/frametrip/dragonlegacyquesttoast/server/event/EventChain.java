package com.frametrip.dragonlegacyquesttoast.server.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EventChain {
    public String             name          = "";
    public boolean            enabled       = true;
    public EventTriggerType   trigger       = EventTriggerType.PLAYER_INTERACT;
    public String             conditionMode = "AND";
    public boolean            executeAll    = false;
    public List<EventCondition> conditions  = new ArrayList<>();
    public List<EventAction>    actions     = new ArrayList<>();
    public Map<String, String>  triggerParams = new HashMap<>();

    public String triggerParam(String key) {
        return triggerParams.getOrDefault(key, "");
    }

    public void triggerParam(String key, String value) {
        triggerParams.put(key, value);
    }

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

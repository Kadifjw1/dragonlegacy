package com.frametrip.dragonlegacyquesttoast.server.script;

import com.frametrip.dragonlegacyquesttoast.server.event.*;

import java.util.*;

// [SCR-3]: Compiles a visual ScriptGraph into a flat EventChain via BFS from the EVENT node.
public class ScriptGraphCompiler {

    public static EventChain compile(ScriptGraph graph) {
        EventChain chain = new EventChain();
        chain.name    = graph.name;
        chain.enabled = true;

        ScriptNode start = findEventNode(graph);
        if (start == null) return chain;

        // Map trigger from EVENT node subType
        try {
            chain.trigger = EventTriggerType.valueOf(start.subType);
        } catch (IllegalArgumentException ignored) {
            chain.trigger = EventTriggerType.NPC_CLICK;
        }
        if (!start.paramKey.isEmpty()) {
            chain.triggerParam(start.paramKey, start.paramValue);
        }

        // BFS walk — collect conditions and actions in graph order
        Set<String> visited = new LinkedHashSet<>();
        Deque<String> queue = new ArrayDeque<>(start.outputTo);

        while (!queue.isEmpty()) {
            String nodeId = queue.pollFirst();
            if (visited.contains(nodeId)) continue;
            visited.add(nodeId);

            ScriptNode node = graph.findById(nodeId);
            if (node == null) continue;

            switch (node.type) {
                case CONDITION -> {
                    EventCondition cond = new EventCondition();
                    try {
                        cond.type = EventConditionType.valueOf(node.subType);
                    } catch (IllegalArgumentException ignored) {
                        cond.type = EventConditionType.TIME_OF_DAY;
                    }
                    if (!node.paramKey.isEmpty()) cond.param(node.paramKey, node.paramValue);
                    chain.conditions.add(cond);
                    queue.addAll(node.outputTo);
                }
                case ACTION -> {
                    EventAction action = new EventAction();
                    try {
                        action.type = EventActionType.valueOf(node.subType);
                    } catch (IllegalArgumentException ignored) {
                        action.type = EventActionType.SAY_PHRASE;
                    }
                    if (!node.paramKey.isEmpty()) action.param(node.paramKey, node.paramValue);
                    chain.actions.add(action);
                    queue.addAll(node.outputTo);
                }
                case DELAY -> {
                    EventAction delay = new EventAction();
                    delay.type = EventActionType.DELAY;
                    if (!node.paramKey.isEmpty()) delay.param(node.paramKey, node.paramValue);
                    else delay.param("ticks", "20");
                    chain.actions.add(delay);
                    queue.addAll(node.outputTo);
                }
                case OUTPUT -> { /* terminal — do not traverse further */ }
                default -> queue.addAll(node.outputTo);
            }
        }

        return chain;
    }

    private static ScriptNode findEventNode(ScriptGraph graph) {
        for (ScriptNode n : graph.nodes) {
            if (n.type == ScriptNodeType.EVENT) return n;
        }
        return null;
    }
}

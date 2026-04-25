package com.frametrip.dragonlegacyquesttoast.server.dialogue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NpcScene {
    
    // Scene types
    public static final String TYPE_GREETING        = "GREETING";
    public static final String TYPE_IDLE            = "IDLE";
    public static final String TYPE_QUEST_OFFER     = "QUEST_OFFER";
    public static final String TYPE_QUEST_PROGRESS  = "QUEST_PROGRESS";
    public static final String TYPE_QUEST_COMPLETE  = "QUEST_COMPLETE";
    public static final String TYPE_REFUSAL         = "REFUSAL";
    public static final String TYPE_HOSTILE_WARNING = "HOSTILE_WARNING";
    public static final String TYPE_FACTION_LOCKED  = "FACTION_LOCKED";
    public static final String TYPE_PATH_LOCKED     = "PATH_LOCKED";
    public static final String TYPE_CUSTOM          = "CUSTOM";

    public static final String[] TYPE_IDS = {
            TYPE_CUSTOM, TYPE_GREETING, TYPE_IDLE,
            TYPE_QUEST_OFFER, TYPE_QUEST_PROGRESS, TYPE_QUEST_COMPLETE,
            TYPE_REFUSAL, TYPE_HOSTILE_WARNING,
            TYPE_FACTION_LOCKED, TYPE_PATH_LOCKED
    };
    public static final String[] TYPE_LABELS = {
            "Кастом", "Приветствие", "Обычный разговор",
            "Предложение квеста", "Прогресс квеста", "Завершение квеста",
            "Отказ", "Предупреждение",
            "Фракция закрыта", "Путь закрыт"
    };

    public String id;
    public String name = "Новая сцена";
    public String type = TYPE_CUSTOM;
    public String description = "";
    public boolean repeatable = true;
    public boolean enabled = true;
    public String startNodeId = "";
    public List<NpcSceneNode> nodes = new ArrayList<>();

    public NpcScene() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
    }

    public NpcSceneNode getNode(String nodeId) {
        if (nodeId == null || nodeId.isEmpty()) return null;
        return nodes.stream().filter(n -> nodeId.equals(n.id)).findFirst().orElse(null);
    }

    public NpcSceneNode getStartNode() {
        return getNode(startNodeId);
    }

    public NpcScene copy() {
        NpcScene c = new NpcScene();
        c.id          = this.id;
        c.name        = this.name;
        c.type        = this.type;
        c.description = this.description;
        c.repeatable  = this.repeatable;
        c.enabled     = this.enabled;
        c.startNodeId = this.startNodeId;
        c.nodes       = new ArrayList<>();
        for (NpcSceneNode n : this.nodes) c.nodes.add(n.copy());
        return c;
    }

    /** Adds a node and returns it. */
    public NpcSceneNode addNode(String type) {
        NpcSceneNode node = new NpcSceneNode();
        node.type = type;
        nodes.add(node);
        if (startNodeId.isEmpty()) startNodeId = node.id;
        return node;
    }

    public void removeNode(String nodeId) {
        nodes.removeIf(n -> n.id.equals(nodeId));
        if (startNodeId.equals(nodeId)) {
            startNodeId = nodes.isEmpty() ? "" : nodes.get(0).id;
        }
        // clear dangling references
        for (NpcSceneNode n : nodes) {
            if (nodeId.equals(n.nextNodeId)) n.nextNodeId = "";
            if (nodeId.equals(n.actionNextNodeId)) n.actionNextNodeId = "";
            if (nodeId.equals(n.trueNextNodeId)) n.trueNextNodeId = "";
            if (nodeId.equals(n.falseNextNodeId)) n.falseNextNodeId = "";
            if (n.choices != null) {
                for (NpcChoiceOption opt : n.choices) {
                    if (nodeId.equals(opt.nextNodeId)) opt.nextNodeId = "";
                }
            }
        }
    }
    
    /** Returns incoming node IDs pointing at nodeId. */
    public List<String> incomingOf(String nodeId) {
        List<String> out = new ArrayList<>();
        if (nodeId == null || nodeId.isEmpty()) return out;
        for (NpcSceneNode n : nodes) {
            if (nodeId.equals(n.nextNodeId))       out.add(n.id);
            else if (nodeId.equals(n.actionNextNodeId)) out.add(n.id);
            else if (nodeId.equals(n.trueNextNodeId))   out.add(n.id);
            else if (nodeId.equals(n.falseNextNodeId))  out.add(n.id);
            else if (n.choices != null) {
                for (NpcChoiceOption opt : n.choices) {
                    if (nodeId.equals(opt.nextNodeId)) { out.add(n.id); break; }
                }
            }
        }
        return out;
    }

    public static String typeLabel(String id) {
        for (int i = 0; i < TYPE_IDS.length; i++) if (TYPE_IDS[i].equals(id)) return TYPE_LABELS[i];
        return id == null ? "" : id;
    }
}

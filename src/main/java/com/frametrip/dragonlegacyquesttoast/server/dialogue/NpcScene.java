package com.frametrip.dragonlegacyquesttoast.server.dialogue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NpcScene {

    public String id;
    public String name = "Новая сцена";
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
            for (NpcChoiceOption opt : n.choices) {
                if (nodeId.equals(opt.nextNodeId)) opt.nextNodeId = "";
            }
        }
    }
}

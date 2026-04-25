package com.frametrip.dragonlegacyquesttoast.server.dialogue;

import com.frametrip.dragonlegacyquesttoast.server.DialogueDefinition;

import java.util.List;
import java.util.UUID;

/** Converts the old line-list dialogue to a node-based scene. */
public final class LegacyDialogueMigration {

    private LegacyDialogueMigration() {}

    /** Build a brand-new scene whose nodes chain the dialogue lines. */
    public static NpcScene fromLegacy(DialogueDefinition dlg) {
        NpcScene scene = new NpcScene();
        scene.id   = UUID.randomUUID().toString().substring(0, 8);
        scene.name = (dlg != null && dlg.npcName != null && !dlg.npcName.isBlank())
                ? dlg.npcName + " (импорт)" : "Импорт диалога";
        scene.type = NpcScene.TYPE_IDLE;
        scene.description = "Создано из устаревшего диалога";

        List<String> lines = (dlg != null && dlg.lines != null) ? dlg.lines : List.of();
        NpcSceneNode prev = null;
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            NpcSceneNode node = scene.addNode(NpcSceneNode.TYPE_SPEECH);
            node.text = line;
            if (dlg != null && dlg.npcName != null) node.speakerName = dlg.npcName;
            if (prev != null) prev.nextNodeId = node.id;
            prev = node;
        }

        // If dialogue was empty just create a placeholder speech + end.
        if (scene.nodes.isEmpty()) {
            NpcSceneNode placeholder = scene.addNode(NpcSceneNode.TYPE_SPEECH);
            placeholder.text = "…";
            prev = placeholder;
        }

        NpcSceneNode end = scene.addNode(NpcSceneNode.TYPE_END);
        prev.nextNodeId = end.id;
        return scene;
    }
}

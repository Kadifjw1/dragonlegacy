package com.frametrip.dragonlegacyquesttoast.client.dialogue;

import com.frametrip.dragonlegacyquesttoast.client.ClientNpcDialogueManager;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcScene;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcSceneNode;
import net.minecraft.client.Minecraft;

/**
 * Client-side runtime controller for NPC scenes.
 * Processes nodes in order: speech → overlay, question → NpcChoiceScreen, action → auto-execute.
 */
public class NpcSceneController {

    private static String currentNpcName = "";
    private static NpcScene currentScene = null;
    private static String pendingNodeId  = null;

    private NpcSceneController() {}

    public static void startScene(String npcName, String sceneId) {
        NpcScene scene = ClientNpcSceneState.get(sceneId);
        if (scene == null) return;

        currentNpcName = npcName;
        currentScene   = scene;
        processNode(scene.startNodeId);
    }

    public static void processNode(String nodeId) {
        if (currentScene == null) return;
        if (nodeId == null || nodeId.isEmpty()) {
            finish();
            return;
        }

        NpcSceneNode node = currentScene.getNode(nodeId);
        if (node == null) {
            finish();
            return;
        }

        switch (node.type) {
            case NpcSceneNode.TYPE_SPEECH   -> processSpeech(node);
            case NpcSceneNode.TYPE_QUESTION -> processQuestion(node);
            case NpcSceneNode.TYPE_ACTION   -> processAction(node);
            default -> finish();
        }
    }

    private static void processSpeech(NpcSceneNode node) {
        // Use existing dialogue overlay; after it ends, call next node
        String next = node.nextNodeId;
        ClientNpcDialogueManager.show(currentNpcName, node.text);
        // We chain: after the overlay text, process next node.
        // Because the overlay is tick-based, we schedule via pending.
        pendingNodeId = next;
        schedulePending();
    }

    private static void processQuestion(NpcSceneNode node) {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new NpcChoiceScreen(currentNpcName, node, NpcSceneController::processNode));
    }

    private static void processAction(NpcSceneNode node) {
        executeAction(node);
        processNode(node.actionNextNodeId);
    }

    private static void executeAction(NpcSceneNode node) {
        // Actions are client-side effects only; server-side effects require additional packets.
        switch (node.actionType) {
            case NpcSceneNode.ACTION_GIVE_QUEST, NpcSceneNode.ACTION_COMPLETE_QUEST -> {
                // Notify server via chat command or dedicated packet (future extension).
                // For now show a brief overlay message.
                String label = node.actionType.equals(NpcSceneNode.ACTION_GIVE_QUEST)
                        ? "Выдан квест: " : "Квест выполнен: ";
                ClientNpcDialogueManager.show("", label + node.actionParam);
            }
            case NpcSceneNode.ACTION_SET_RELATION -> {
                ClientNpcDialogueManager.show("", "Отношение изменено: " + node.actionParam);
            }
        }
    }

    /** Called after overlay finishes ticking to proceed to the next pending node. */
    private static void schedulePending() {
        if (pendingNodeId == null) return;
        // We hook into the existing overlay tick; the simplest client-side approach is
        // to process immediately after the text display is queued (it queues, not blocks).
        String next = pendingNodeId;
        pendingNodeId = null;
        // If next is empty, done; otherwise continue after overlay finishes.
        // For simplicity: if it's another speech node, let it queue in the overlay.
        // If it's a question/action, we need to wait. Store for deferred call.
        if (next.isEmpty()) {
            finish();
        } else {
            NpcScene scene = currentScene;
            NpcSceneNode nextNode = scene.getNode(next);
            if (nextNode == null) { finish(); return; }
            if (nextNode.type.equals(NpcSceneNode.TYPE_SPEECH)) {
                // queue directly — overlay handles multiple pages
                processNode(next);
            } else {
                // defer until overlay is no longer active
                NpcSceneTickHandler.scheduleDeferredNode(next);
            }
        }
    }

    private static void finish() {
        currentScene   = null;
        currentNpcName = "";
        pendingNodeId  = null;
    }

    public static boolean isActive() {
        return currentScene != null;
    }
}

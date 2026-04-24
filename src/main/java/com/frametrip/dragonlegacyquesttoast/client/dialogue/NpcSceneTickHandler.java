package com.frametrip.dragonlegacyquesttoast.client.dialogue;

import com.frametrip.dragonlegacyquesttoast.client.ClientNpcDialogueManager;

/**
 * Handles deferred node transitions for the scene controller.
 * Registered as a client tick event in the mod event bus.
 */
public class NpcSceneTickHandler {

    private static String deferredNodeId = null;

    public static void scheduleDeferredNode(String nodeId) {
        deferredNodeId = nodeId;
    }

    /** Called every client tick. */
    public static void tick() {
        if (deferredNodeId == null) return;
        if (!ClientNpcDialogueManager.isActive()) {
            String node = deferredNodeId;
            deferredNodeId = null;
            NpcSceneController.processNode(node);
        }
    }
}

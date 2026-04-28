package com.frametrip.dragonlegacyquesttoast.client.dialogue;

import com.frametrip.dragonlegacyquesttoast.client.ClientNpcDialogueManager;

/**
 * Handles deferred node transitions for the scene controller.
 * Registered as a client tick event in the mod event bus.
 */
public class NpcSceneTickHandler {

    private static String deferredNodeId = null;
    private static int deferredDelay = 0;

    public static void scheduleDeferredNode(String nodeId) {
        scheduleDeferredNode(nodeId, 0);
    }

    public static void scheduleDeferredNode(String nodeId, int delayTicks) {
        deferredNodeId = nodeId;
        deferredDelay = Math.max(0, delayTicks);
    }

    public static void clearScheduled() {
        deferredNodeId = null;
        deferredDelay = 0;
    }

    /** Called every client tick. */
    public static void tick() {
        if (deferredNodeId == null) return;
        if (deferredDelay > 0) {
            deferredDelay--;
            return;
        }
        if (!ClientNpcDialogueManager.isActive()) {
            String node = deferredNodeId;
            deferredNodeId = null;
            deferredDelay = 0;
            NpcSceneController.processNode(node);
        }
    }
}

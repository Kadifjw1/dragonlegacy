package com.frametrip.dragonlegacyquesttoast.client.building;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Клиентский кэш состояний строительства NPC. */
public class ClientBuildingState {

    public record BuildingProgress(String templateId, String status,
                                   int totalBlocks, int placedBlocks) {
        public float fraction() {
            return totalBlocks == 0 ? 1f : placedBlocks / (float) totalBlocks;
        }
        public String label() { return placedBlocks + "/" + totalBlocks; }
        public boolean isActive() { return "BUILDING".equals(status) || "PAUSED".equals(status); }
    }

    private static final Map<UUID, BuildingProgress> STATES = new ConcurrentHashMap<>();

    public static void updateProgress(UUID npcId, String templateId, String status,
                                       int totalBlocks, int placedBlocks) {
        if ("DONE".equals(status) || "CANCELLED".equals(status)) {
            STATES.remove(npcId);
        } else {
            STATES.put(npcId, new BuildingProgress(templateId, status, totalBlocks, placedBlocks));
        }
    }

    public static BuildingProgress getState(UUID npcId) {
        return npcId == null ? null : STATES.get(npcId);
    }

    public static void clear() {
        STATES.clear();
    }
}

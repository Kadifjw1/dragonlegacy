package com.frametrip.dragonlegacyquesttoast.server.stealth;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Tracks active stealth mission state per player. */
public class StealthMissionManager {

    private static final Map<UUID, StealthMissionState> ACTIVE = new HashMap<>();

    public static void startMission(UUID playerId) {
        ACTIVE.put(playerId, new StealthMissionState(playerId));
    }

    public static void endMission(UUID playerId) {
        ACTIVE.remove(playerId);
    }

    public static StealthMissionState getState(UUID playerId) {
        return ACTIVE.get(playerId);
    }

    public static boolean isInMission(UUID playerId) {
        return ACTIVE.containsKey(playerId);
    }

    public static AlertLevel getAlertLevel(UUID playerId) {
        StealthMissionState s = ACTIVE.get(playerId);
        return s == null ? AlertLevel.NONE : s.alertLevel;
    }

    public static void notifyDetection(UUID playerId, UUID npcId) {
        StealthMissionState s = ACTIVE.get(playerId);
        if (s == null) return;
        s.detectionCount++;
        s.detected = true;
        if (s.detectionCount == 1) s.alertLevel = AlertLevel.MEDIUM;
        if (s.detectionCount >= 3) s.alertLevel = AlertLevel.HIGH;
    }

    public static void raiseAlarm(UUID playerId) {
        StealthMissionState s = ACTIVE.get(playerId);
        if (s != null) {
            s.alertLevel = AlertLevel.LOCKDOWN;
            s.detected   = true;
        }
    }

    public static void lowerAlarm(UUID playerId) {
        StealthMissionState s = ACTIVE.get(playerId);
        if (s != null) {
            s.alertLevel    = AlertLevel.NONE;
            s.detectionCount = 0;
        }
    }

    public static void missionFailed(UUID playerId) {
        StealthMissionState s = ACTIVE.get(playerId);
        if (s != null) s.failed = true;
    }

    public static void missionSucceeded(UUID playerId) {
        StealthMissionState s = ACTIVE.get(playerId);
        if (s != null) s.succeeded = true;
    }
}

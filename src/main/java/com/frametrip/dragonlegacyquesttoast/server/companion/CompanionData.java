package com.frametrip.dragonlegacyquesttoast.server.companion;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class CompanionData {

    public CompanionMode mode          = CompanionMode.WAIT;
    public CompanionMode previousMode  = CompanionMode.WAIT;

    /** UUID of the player this companion belongs to. */
    public String ownerUUID = "";

    // — Movement —
    public float followDistance  = 4.0f;   // blocks
    public float followSpeed     = 0.6f;   // 0–1, multiplier on base speed
    public float guardRadius     = 8.0f;   // blocks

    // — Guard point (world coords) —
    public double guardX = 0, guardY = 0, guardZ = 0;
    public boolean guardPointSet = false;

    // — Combat —
    /** 0 = passive, 0.5 = neutral, 1 = aggressive */
    public float aggressiveness  = 0.5f;

    // — Chat commands (mode name → trigger word, overrides defaults) —
    public Map<String, String> chatCommands = new LinkedHashMap<>();

    public CompanionData() {
        for (CompanionMode m : CompanionMode.values()) {
            chatCommands.put(m.name(), m.defaultCommand());
        }
    }

    public String commandFor(CompanionMode m) {
        return chatCommands.getOrDefault(m.name(), m.defaultCommand());
    }

    /** Switch to a new mode, remembering the previous one. */
    public void setMode(CompanionMode newMode) {
        if (newMode != mode) {
            previousMode = mode;
            mode = newMode;
        }
    }

    public void restorePreviousMode() {
        CompanionMode tmp = mode;
        mode = previousMode;
        previousMode = tmp;
    }

    public CompanionData copy() {
        CompanionData c = new CompanionData();
        c.mode          = this.mode;
        c.previousMode  = this.previousMode;
        c.ownerUUID     = this.ownerUUID;
        c.followDistance = this.followDistance;
        c.followSpeed    = this.followSpeed;
        c.guardRadius    = this.guardRadius;
        c.guardX         = this.guardX;
        c.guardY         = this.guardY;
        c.guardZ         = this.guardZ;
        c.guardPointSet  = this.guardPointSet;
        c.aggressiveness = this.aggressiveness;
        c.chatCommands   = new LinkedHashMap<>(this.chatCommands);
        return c;
    }
}


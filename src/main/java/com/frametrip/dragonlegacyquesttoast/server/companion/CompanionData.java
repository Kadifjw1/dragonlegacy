package com.frametrip.dragonlegacyquesttoast.server.companion;

import java.util.HashMap;
import java.util.Map;

public class CompanionData {

    public CompanionMode mode     = CompanionMode.FOLLOW;
    public float followDistance   = 3f;
    public float aggressiveness   = 0.5f;
    public float guardRadius      = 8f;

    /** UUID of the player who owns this companion (empty = any player). */
    public String ownerUUID = "";

    /** Guard post coordinates (used when mode == GUARD). */
    public double guardX, guardY, guardZ;
    public boolean guardPointSet = false;

    /** Per-mode voice/chat command words. Key = CompanionMode.name(). */
    public Map<String, String> modeCommands = new HashMap<>();

    public void setMode(CompanionMode m) {
        this.mode = m;
    }

    /** Returns the chat phrase that triggers this mode, or "" if not set. */
    public String commandFor(CompanionMode mode) {
        return modeCommands.getOrDefault(mode.name(), "");
    }

    public void setCommand(CompanionMode mode, String cmd) {
        modeCommands.put(mode.name(), cmd);
    }

    public CompanionData copy() {
        CompanionData c = new CompanionData();
        c.mode           = this.mode;
        c.followDistance = this.followDistance;
        c.aggressiveness = this.aggressiveness;
        c.guardRadius    = this.guardRadius;
        c.ownerUUID      = this.ownerUUID;
        c.guardX         = this.guardX;
        c.guardY         = this.guardY;
        c.guardZ         = this.guardZ;
        c.guardPointSet  = this.guardPointSet;
        c.modeCommands   = new HashMap<>(this.modeCommands);
        return c;
    }
}

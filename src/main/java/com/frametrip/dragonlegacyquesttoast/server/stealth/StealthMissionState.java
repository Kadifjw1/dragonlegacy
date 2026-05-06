package com.frametrip.dragonlegacyquesttoast.server.stealth;

import java.util.UUID;

/** Runtime state for a stealth mission for one player. */
public class StealthMissionState {
    public final UUID   playerId;
    public AlertLevel   alertLevel   = AlertLevel.NONE;
    public boolean      detected     = false;
    public boolean      failed       = false;
    public boolean      succeeded    = false;
    public int          alertTick    = 0;    // when alert level was raised
    public int          detectionCount = 0;  // how many NPCs have detected the player

    public StealthMissionState(UUID playerId) {
        this.playerId = playerId;
    }
}

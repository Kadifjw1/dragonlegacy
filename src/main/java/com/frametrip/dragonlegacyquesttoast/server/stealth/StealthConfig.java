package com.frametrip.dragonlegacyquesttoast.server.stealth;

import java.util.ArrayList;
import java.util.List;

/** Per-NPC stealth/guard configuration. */
public class StealthConfig {
    public boolean guardEnabled  = false;

    // Detection parameters
    public float visionRadius    = 12f;
    public float visionAngle     = 120f;   // degrees (total cone, not half-angle)
    public float hearingRadius   = 8f;
    public int   detectionTicks  = 60;     // ticks from SUSPICIOUS to DETECTED
    public float sensitivity     = 1f;     // multiplier

    // Guard type
    public String guardType     = "guard"; // guard, archer, captain, animal

    // Patrol
    public List<PatrolPoint> patrolRoute = new ArrayList<>();
    public boolean loopPatrol            = true;
    public int patrolWaitTicks           = 20; // wait at each point

    // Alert reactions
    public String alarmSceneId           = "";  // scene to start on alarm
    public String detectSceneId          = "";  // scene on first detection
    public String resetSceneId           = "";  // scene after alarm resets
    public int alarmResetTicks           = 1200; // 60 seconds

    // Zones managed by this NPC (optional - for the mission config)
    public List<StealthZone> zones       = new ArrayList<>();

    public StealthConfig copy() {
        StealthConfig c = new StealthConfig();
        c.guardEnabled    = this.guardEnabled;
        c.visionRadius    = this.visionRadius;
        c.visionAngle     = this.visionAngle;
        c.hearingRadius   = this.hearingRadius;
        c.detectionTicks  = this.detectionTicks;
        c.sensitivity     = this.sensitivity;
        c.guardType       = this.guardType;
        for (PatrolPoint p : patrolRoute) c.patrolRoute.add(p.copy());
        c.loopPatrol      = this.loopPatrol;
        c.patrolWaitTicks = this.patrolWaitTicks;
        c.alarmSceneId    = this.alarmSceneId;
        c.detectSceneId   = this.detectSceneId;
        c.resetSceneId    = this.resetSceneId;
        c.alarmResetTicks = this.alarmResetTicks;
        for (StealthZone z : zones) c.zones.add(z.copy());
        return c;
    }

    public static final String[] GUARD_TYPES       = { "guard", "archer", "captain", "animal" };
    public static final String[] GUARD_TYPE_LABELS = { "Страж", "Лучник", "Капитан", "Животное" };
}

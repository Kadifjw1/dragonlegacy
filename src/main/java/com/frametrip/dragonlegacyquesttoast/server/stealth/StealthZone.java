package com.frametrip.dragonlegacyquesttoast.server.stealth;

import java.util.UUID;

public class StealthZone {
    public static final String SAFE       = "safe";
    public static final String FORBIDDEN  = "forbidden";
    public static final String GUARDED    = "guarded";

    public String id   = UUID.randomUUID().toString().substring(0, 8);
    public String type = GUARDED;
    public String name = "Зона";
    public double x1, y1, z1;
    public double x2, y2, z2;
    public float lightModifier    = 1f; // multiplier for detection in this zone
    public String onEnterSceneId  = "";
    public String onDetectSceneId = "";

    public boolean contains(double x, double y, double z) {
        return x >= Math.min(x1, x2) && x <= Math.max(x1, x2)
            && y >= Math.min(y1, y2) && y <= Math.max(y1, y2)
            && z >= Math.min(z1, z2) && z <= Math.max(z1, z2);
    }

    public StealthZone copy() {
        StealthZone c = new StealthZone();
        c.id              = this.id;
        c.type            = this.type;
        c.name            = this.name;
        c.x1 = this.x1; c.y1 = this.y1; c.z1 = this.z1;
        c.x2 = this.x2; c.y2 = this.y2; c.z2 = this.z2;
        c.lightModifier   = this.lightModifier;
        c.onEnterSceneId  = this.onEnterSceneId;
        c.onDetectSceneId = this.onDetectSceneId;
        return c;
    }
}

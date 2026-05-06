package com.frametrip.dragonlegacyquesttoast.server.stealth;

public class PatrolPoint {
    public double x, y, z;
    public int pauseTicks     = 40;
    public float lookYaw      = 0f;
    public boolean forceLook  = false;

    public PatrolPoint() {}
    public PatrolPoint(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
    }

    public PatrolPoint copy() {
        PatrolPoint c = new PatrolPoint(x, y, z);
        c.pauseTicks = pauseTicks;
        c.lookYaw    = lookYaw;
        c.forceLook  = forceLook;
        return c;
    }
}

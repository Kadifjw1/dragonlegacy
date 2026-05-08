package com.frametrip.dragonlegacyquesttoast.server.companion;

public class CompanionData {

    public CompanionMode mode         = CompanionMode.FOLLOW;
    public float followDistance       = 3f;
    public float aggressiveness       = 0.5f;
    public float guardRadius          = 8f;

    public void setMode(CompanionMode m) {
        this.mode = m;
    }

    public CompanionData copy() {
        CompanionData c = new CompanionData();
        c.mode            = this.mode;
        c.followDistance  = this.followDistance;
        c.aggressiveness  = this.aggressiveness;
        c.guardRadius     = this.guardRadius;
        return c;
    }
}

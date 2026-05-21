package com.frametrip.dragonlegacyquesttoast.server.combat;

// [CMB-2]: One phase configuration for a boss NPC.
public class BossPhase {

    public int    phaseIndex       = 0;
    public float  hpThreshold      = 50f; // percent (0-100); phase activates when HP drops below
    public float  speedMultiplier  = 1.0f;
    public float  damageMultiplier = 1.0f;
    public String animationId      = "";   // NpcAnimationData.id to force on transition
    public String summonType       = "";   // entity resource location (e.g. "minecraft:skeleton")
    public int    summonCount      = 0;
    public String phaseDialog      = "";   // spoken to nearby players on transition

    public BossPhase copy() {
        BossPhase c = new BossPhase();
        c.phaseIndex       = this.phaseIndex;
        c.hpThreshold      = this.hpThreshold;
        c.speedMultiplier  = this.speedMultiplier;
        c.damageMultiplier = this.damageMultiplier;
        c.animationId      = this.animationId;
        c.summonType       = this.summonType;
        c.summonCount      = this.summonCount;
        c.phaseDialog      = this.phaseDialog;
        return c;
    }
}

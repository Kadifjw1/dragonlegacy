package com.frametrip.dragonlegacyquesttoast.server.combat;

// [CMB-4]: One combat ability attached to an NPC.
public class NpcAbility {

    public String abilityType     = "HEAL_SELF"; // NpcAbilityType name
    public float  param           = 5f;          // type-specific magnitude
    public int    cooldownSec     = 15;
    public String triggerCondition = "HP_BELOW_50"; // see constants below
    public String summonType      = "";          // used when abilityType = SUMMON
    public int    summonCount     = 1;

    // Trigger condition constants:
    public static final String ON_HIT             = "ON_HIT";            // when NPC hits target
    public static final String ON_HIT_RECEIVED    = "ON_HIT_RECEIVED";   // when NPC takes damage
    public static final String HP_BELOW_50        = "HP_BELOW_50";       // once HP < 50%
    public static final String EVERY_N_SECONDS    = "EVERY_N_SECONDS";   // periodic (period = cooldownSec)
    public static final String ON_TARGET_NEARBY   = "ON_TARGET_NEARBY";  // target within param blocks

    public static final String[] TRIGGERS = {
        ON_HIT, ON_HIT_RECEIVED, HP_BELOW_50, EVERY_N_SECONDS, ON_TARGET_NEARBY
    };
    public static final String[] TRIGGER_LABELS = {
        "При ударе", "При получении урона", "HP < 50%", "Каждые N сек.", "Цель рядом"
    };

    public NpcAbility copy() {
        NpcAbility c = new NpcAbility();
        c.abilityType      = this.abilityType;
        c.param            = this.param;
        c.cooldownSec      = this.cooldownSec;
        c.triggerCondition = this.triggerCondition;
        c.summonType       = this.summonType;
        c.summonCount      = this.summonCount;
        return c;
    }
}

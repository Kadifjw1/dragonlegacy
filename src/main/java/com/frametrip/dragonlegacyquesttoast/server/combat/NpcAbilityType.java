package com.frametrip.dragonlegacyquesttoast.server.combat;

// [CMB-4]: Built-in NPC combat ability types.
public enum NpcAbilityType {
    POISON    ("Яд",          "param = duration ticks"),
    STUN      ("Оглушение",   "param = duration ticks"),
    DASH      ("Рывок",       "param = speed multiplier"),
    SHIELD    ("Щит",         "param = absorption HP"),
    PULL      ("Притяжение",  "param = pull strength"),
    KNOCKBACK ("Отброс",      "param = knock strength"),
    HEAL_SELF ("Лечение",     "param = HP restored"),
    SUMMON    ("Призыв",      "param = count (summonType must be set)");

    public final String label;
    public final String hint;

    NpcAbilityType(String label, String hint) { this.label = label; this.hint = hint; }

    public static NpcAbilityType fromName(String name) {
        for (NpcAbilityType t : values()) if (t.name().equalsIgnoreCase(name)) return t;
        return HEAL_SELF;
    }
}

package com.frametrip.dragonlegacyquesttoast.server.model;

public enum NpcModelProfile {
    PLAYER        ("player",       "Игрок",       1.0f, 1.62f, 2.0f, 1.8f),
    WOLF          ("wolf",         "Волк",         1.0f, 0.85f, 1.2f, 1.0f),
    FOX           ("fox",          "Лисица",       1.0f, 0.65f, 1.0f, 0.8f),
    COW           ("cow",          "Корова",       1.0f, 1.30f, 2.0f, 1.6f),
    PIG           ("pig",          "Свинья",       1.0f, 0.90f, 1.5f, 1.2f),
    SHEEP         ("sheep",        "Овца",         1.0f, 1.30f, 1.8f, 1.5f),
    CHICKEN       ("chicken",      "Курица",       1.0f, 0.70f, 1.0f, 0.8f),
    HORSE         ("horse",        "Лошадь",       1.0f, 1.60f, 2.4f, 2.0f),
    DONKEY        ("donkey",       "Осёл",         1.0f, 1.60f, 2.4f, 2.0f),
    RABBIT        ("rabbit",       "Кролик",       1.0f, 0.45f, 0.8f, 0.6f),
    IRON_GOLEM    ("iron_golem",   "Железный голем", 1.0f, 2.70f, 3.6f, 3.0f),
    SNOW_GOLEM    ("snow_golem",   "Снежный голем", 1.0f, 1.80f, 2.5f, 2.2f),
    VILLAGER      ("villager",     "Житель",       1.0f, 1.62f, 2.0f, 1.8f),
    ZOMBIE        ("zombie",       "Зомби",        1.0f, 1.62f, 2.0f, 1.8f),
    SKELETON      ("skeleton",     "Скелет",       1.0f, 1.62f, 2.0f, 1.8f),
    CREEPER       ("creeper",      "Крипер",       1.0f, 1.62f, 2.0f, 1.7f),
    ENDERMAN      ("enderman",     "Эндермен",     1.0f, 2.90f, 3.6f, 3.2f),
    CAT           ("cat",          "Кошка",        1.0f, 0.70f, 1.0f, 0.8f),
    PANDA         ("panda",        "Панда",        1.0f, 1.25f, 1.8f, 1.5f),
    POLAR_BEAR    ("polar_bear",   "Белый медведь", 1.0f, 1.40f, 2.2f, 1.8f);

    public final String id;
    public final String label;
    /** Default scale multiplier. */
    public final float baseScale;
    /** Eye height in blocks. */
    public final float eyeHeight;
    /** Nameplate Y offset above feet. */
    public final float nameplateOffset;
    /** Dialogue bubble Y offset above feet. */
    public final float dialogueOffset;

    NpcModelProfile(String id, String label, float baseScale,
                    float eyeHeight, float nameplateOffset, float dialogueOffset) {
        this.id              = id;
        this.label           = label;
        this.baseScale       = baseScale;
        this.eyeHeight       = eyeHeight;
        this.nameplateOffset = nameplateOffset;
        this.dialogueOffset  = dialogueOffset;
    }

    public static final NpcModelProfile[] VALUES = values();

    public static NpcModelProfile byId(String id) {
        for (NpcModelProfile p : VALUES) if (p.id.equals(id)) return p;
        return PLAYER;
    }

    /** Vanilla entity type registry name, or null for PLAYER. */
    public String entityTypeId() {
        if (this == PLAYER) return null;
        return "minecraft:" + id;
    }

    public String category() {
        return switch (this) {
            case PLAYER, VILLAGER, ZOMBIE, SKELETON, CREEPER, ENDERMAN -> "humanoid";
            case WOLF, FOX, COW, PIG, SHEEP, CHICKEN, RABBIT, HORSE, DONKEY, CAT, PANDA, POLAR_BEAR -> "animal";
            case IRON_GOLEM, SNOW_GOLEM -> "golem";
        };
    }
}

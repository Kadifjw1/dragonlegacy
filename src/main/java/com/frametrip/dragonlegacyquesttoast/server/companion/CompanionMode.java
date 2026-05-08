package com.frametrip.dragonlegacyquesttoast.server.companion;

public enum CompanionMode {
    FOLLOW  ("Следовать",  "Следует за игроком на заданном расстоянии."),
    GUARD   ("Охранять",   "Стоит на месте и атакует врагов в радиусе охраны."),
    WANDER  ("Бродить",    "Свободно бродит рядом с игроком."),
    STAY    ("Стоять",     "Остаётся на месте, не двигается.");

    private final String label;
    private final String description;

    CompanionMode(String label, String description) {
        this.label       = label;
        this.description = description;
    }

    public String label()       { return label; }
    public String description() { return description; }
}

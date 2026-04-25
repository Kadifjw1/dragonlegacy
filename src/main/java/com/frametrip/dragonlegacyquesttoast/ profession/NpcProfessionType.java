package com.frametrip.dragonlegacyquesttoast.profession;

public enum NpcProfessionType {
    NONE,
    TRADER;

    public String label() {
        return switch (this) {
            case NONE   -> "Нет профессии";
            case TRADER -> "Купля-продажа";
        };
    }
}

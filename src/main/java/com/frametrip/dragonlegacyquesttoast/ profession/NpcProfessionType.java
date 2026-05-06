package com.frametrip.dragonlegacyquesttoast.profession;

public enum NpcProfessionType {
    NONE,
    TRADER,
    BUILDER;

    public String label() {
        return switch (this) {
            case NONE    -> "Нет профессии";
            case TRADER  -> "Купля-продажа";
            case BUILDER -> "Строитель";
        };
    }
}

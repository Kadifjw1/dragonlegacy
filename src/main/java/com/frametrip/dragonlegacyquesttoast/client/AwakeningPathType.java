package com.frametrip.dragonlegacyquesttoast.client;

public enum AwakeningPathType {
    FIRE("Путь Пламени"),
    ICE("Путь Льда"),
    STORM("Путь Грозы"),
    VOID("Путь Пустоты");

    private final String title;

    AwakeningPathType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}

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

    public AwakeningPathType next() {
        AwakeningPathType[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

    public AwakeningPathType prev() {
        AwakeningPathType[] values = values();
        return values[(this.ordinal() - 1 + values.length) % values.length];
    }
}

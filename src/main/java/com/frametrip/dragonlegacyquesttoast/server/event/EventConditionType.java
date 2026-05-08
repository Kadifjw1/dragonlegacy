package com.frametrip.dragonlegacyquesttoast.server.event;

public enum EventConditionType {
    ITEM_IN_INVENTORY ("Предмет в инвентаре"),
    QUEST_STATUS      ("Статус квеста"),
    TIME_OF_DAY       ("Время суток"),
    IN_ZONE           ("В зоне"),
    NPC_PROFESSION    ("Профессия NPC"),
    NPC_STATE         ("Состояние NPC"),
    REPUTATION        ("Репутация");

    private final String label;

    EventConditionType(String label) { this.label = label; }

    public String label() { return label; }
}

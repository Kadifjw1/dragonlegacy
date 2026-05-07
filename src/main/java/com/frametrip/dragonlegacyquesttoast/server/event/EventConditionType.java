package com.frametrip.dragonlegacyquesttoast.server.event;

public enum EventConditionType {
    ITEM_IN_INVENTORY,
    QUEST_STATUS,
    TIME_OF_DAY,
    IN_ZONE,
    NPC_PROFESSION,
    NPC_STATE,
    REPUTATION;

    public String label() {
        return switch (this) {
            case ITEM_IN_INVENTORY -> "Предмет в инвентаре";
            case QUEST_STATUS      -> "Статус квеста";
            case TIME_OF_DAY       -> "Время суток";
            case IN_ZONE           -> "В зоне";
            case NPC_PROFESSION    -> "Профессия NPC";
            case NPC_STATE         -> "Состояние NPC";
            case REPUTATION        -> "Репутация";
        };
    }
}

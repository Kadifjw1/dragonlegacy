package com.frametrip.dragonlegacyquesttoast.server.event;

public enum EventTriggerType {
    NPC_CLICK,
    CHAT_MESSAGE,
    ZONE_ENTER,
    ITEM_RECEIVED,
    TIMER,
    QUEST_COMPLETE,
    SCENE_START,
    SCENE_END,
    NPC_ATTACKED,
    TIME_CHANGE;

    public String label() {
        return switch (this) {
            case NPC_CLICK     -> "Клик по NPC";
            case CHAT_MESSAGE  -> "Сообщение в чат";
            case ZONE_ENTER    -> "Вход в зону";
            case ITEM_RECEIVED -> "Получение предмета";
            case TIMER         -> "Таймер";
            case QUEST_COMPLETE -> "Завершение квеста";
            case SCENE_START   -> "Старт сцены";
            case SCENE_END     -> "Конец сцены";
            case NPC_ATTACKED  -> "Атака NPC";
            case TIME_CHANGE   -> "Смена дня/ночи";
        };
    }

    /** Human-readable description of configurable params for this trigger. */
    public String paramHint() {
        return switch (this) {
            case CHAT_MESSAGE  -> "Фраза: текст";
            case ZONE_ENTER    -> "Радиус: число";
            case ITEM_RECEIVED -> "Предмет ID";
            case TIMER         -> "Интервал (тики)";
            case QUEST_COMPLETE, SCENE_START, SCENE_END -> "ID (из редактора)";
            case TIME_CHANGE   -> "День / Ночь";
            default            -> "";
        };
    }
}

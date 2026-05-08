package com.frametrip.dragonlegacyquesttoast.server.event;

public enum EventTriggerType {
    PLAYER_INTERACT ("Взаимодействие", ""),
    CHAT_MESSAGE    ("Сообщение в чат", "Фраза"),
    ZONE_ENTER      ("Вход в зону",     "Радиус"),
    ZONE_EXIT       ("Выход из зоны",   "Радиус"),
    ITEM_RECEIVED   ("Получен предмет", "ID предмета"),
    TIMER           ("Таймер",          "Интервал (тики)"),
    QUEST_COMPLETE  ("Квест завершён",  "ID квеста"),
    SCENE_START     ("Начало сцены",    "ID сцены"),
    SCENE_END       ("Конец сцены",     "ID сцены"),
    TIME_CHANGE     ("Смена времени",   "day/night/dawn/dusk"),
    DEATH           ("Смерть игрока",   ""),
    RESPAWN         ("Возрождение",     ""),
    DAMAGE_TAKEN    ("Получен урон",    "");

    private final String label;
    private final String paramHint;

    EventTriggerType(String label, String paramHint) {
        this.label     = label;
        this.paramHint = paramHint;
    }

    public String label()     { return label; }
    public String paramHint() { return paramHint; }
}

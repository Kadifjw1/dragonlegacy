package com.frametrip.dragonlegacyquesttoast.server.event;

public enum EventActionType {
    SAY_PHRASE      ("Сказать фразу"),
    OPEN_DIALOGUE   ("Открыть диалог"),
    START_SCENE     ("Начать сцену"),
    GIVE_ITEM       ("Дать предмет"),
    GIVE_QUEST      ("Дать квест"),
    COMPLETE_QUEST  ("Завершить квест"),
    SET_NPC_STATE   ("Установить состояние"),
    PLAY_ANIMATION  ("Воспроизвести анимацию"),
    OPEN_GUI        ("Открыть GUI"),
    TELEPORT        ("Телепортировать"),
    START_PATROL    ("Начать патруль"),
    START_BUILD_SCENE ("Начать сцену строительства"),
    DELAY             ("Задержка"); // [SCR-1]: reserved — no-op at runtime, compiled from DELAY graph node

    private final String label;

    EventActionType(String label) { this.label = label; }

    public String label() { return label; }
}

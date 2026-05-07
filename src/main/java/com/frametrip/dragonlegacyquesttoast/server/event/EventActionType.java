package com.frametrip.dragonlegacyquesttoast.server.event;

public enum EventActionType {
    SAY_PHRASE,
    OPEN_DIALOGUE,
    START_SCENE,
    OPEN_GUI,
    GIVE_ITEM,
    GIVE_QUEST,
    COMPLETE_QUEST,
    SET_NPC_STATE,
    PLAY_ANIMATION,
    TELEPORT,
    START_PATROL,
    START_BUILD_SCENE;

    public String label() {
        return switch (this) {
            case SAY_PHRASE       -> "Сказать фразу";
            case OPEN_DIALOGUE    -> "Открыть диалог";
            case START_SCENE      -> "Запустить сцену";
            case OPEN_GUI         -> "Открыть интерфейс";
            case GIVE_ITEM        -> "Выдать предмет";
            case GIVE_QUEST       -> "Выдать квест";
            case COMPLETE_QUEST   -> "Завершить квест";
            case SET_NPC_STATE    -> "Сменить состояние NPC";
            case PLAY_ANIMATION   -> "Проиграть анимацию";
            case TELEPORT         -> "Телепорт";
            case START_PATROL     -> "Начать патруль";
            case START_BUILD_SCENE -> "Строительная сцена";
        };
    }
}

package com.frametrip.dragonlegacyquesttoast.server.chat;

public enum ChatReactionType {
    SPEECH,
    START_SCENE,
    START_NODE,
    OPEN_SHOP,
    GIVE_QUEST,
    UPDATE_QUEST,
    PLAY_ANIMATION,
    PLAY_SOUND,
    SET_STATE;

    public String label() {
        return switch (this) {
            case SPEECH          -> "Фраза NPC";
            case START_SCENE     -> "Запустить сцену";
            case START_NODE      -> "Запустить узел сцены";
            case OPEN_SHOP       -> "Открыть магазин";
            case GIVE_QUEST      -> "Выдать квест";
            case UPDATE_QUEST    -> "Обновить квест";
            case PLAY_ANIMATION  -> "Анимация";
            case PLAY_SOUND      -> "Звук";
            case SET_STATE       -> "Изменить состояние";
        };
    }
}

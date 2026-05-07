package com.frametrip.dragonlegacyquesttoast.server.animation;

public enum AnimationState {
    IDLE, WALK, RUN, SIT, SLEEP, TALK, WORK, FOLLOW, ATTACK, GUARD, CUSTOM;

    public String label() {
        return switch (this) {
            case IDLE   -> "Стоит";
            case WALK   -> "Идёт";
            case RUN    -> "Бежит";
            case SIT    -> "Сидит";
            case SLEEP  -> "Спит";
            case TALK   -> "Говорит";
            case WORK   -> "Работает";
            case FOLLOW -> "Следует";
            case ATTACK -> "Атакует";
            case GUARD  -> "Охраняет";
            case CUSTOM -> "Произвольная";
        };
    }
}

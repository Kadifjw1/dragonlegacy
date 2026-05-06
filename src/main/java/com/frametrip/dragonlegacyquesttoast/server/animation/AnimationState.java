package com.frametrip.dragonlegacyquesttoast.server.animation;

public enum AnimationState {
    IDLE, WALK, WORK, TALK, GUARD, SLEEP, CUSTOM;

    public String label() {
        return switch (this) {
            case IDLE   -> "Стоит";
            case WALK   -> "Идёт";
            case WORK   -> "Работает";
            case TALK   -> "Говорит";
            case GUARD  -> "Охраняет";
            case SLEEP  -> "Спит";
            case CUSTOM -> "Произвольная";
        };
    }
}

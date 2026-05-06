package com.frametrip.dragonlegacyquesttoast.server.stealth;

public enum AlertLevel {
    NONE, LOW, MEDIUM, HIGH, LOCKDOWN;

    public String label() {
        return switch (this) {
            case NONE     -> "Без тревоги";
            case LOW      -> "Слабая тревога";
            case MEDIUM   -> "Средняя тревога";
            case HIGH     -> "Высокая тревога";
            case LOCKDOWN -> "Режим блокировки";
        };
    }
}

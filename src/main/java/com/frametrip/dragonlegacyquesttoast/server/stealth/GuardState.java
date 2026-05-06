package com.frametrip.dragonlegacyquesttoast.server.stealth;

public enum GuardState {
    CALM, SUSPICIOUS, INVESTIGATING, DETECTED, ALARM, SEARCHING, RESETTING;

    public String label() {
        return switch (this) {
            case CALM          -> "Спокоен";
            case SUSPICIOUS    -> "Подозрение";
            case INVESTIGATING -> "Проверка";
            case DETECTED      -> "Обнаружение";
            case ALARM         -> "Тревога";
            case SEARCHING     -> "Поиск";
            case RESETTING     -> "Сброс тревоги";
        };
    }

    public int color() {
        return switch (this) {
            case CALM          -> 0xFF44AA44;
            case SUSPICIOUS    -> 0xFFFFCC44;
            case INVESTIGATING -> 0xFFFF8844;
            case DETECTED      -> 0xFFFF4444;
            case ALARM         -> 0xFFFF0000;
            case SEARCHING     -> 0xFFFF6644;
            case RESETTING     -> 0xFF8888AA;
        };
    }
}

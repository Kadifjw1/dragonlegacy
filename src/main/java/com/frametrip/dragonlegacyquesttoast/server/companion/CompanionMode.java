package com.frametrip.dragonlegacyquesttoast.server.companion;

public enum CompanionMode {
    FOLLOW,
    WAIT,
    GUARD,
    PROTECT,
    PASSIVE,
    COMBAT,
    STEALTH;

    public String label() {
        return switch (this) {
            case FOLLOW  -> "Следовать";
            case WAIT    -> "Ждать";
            case GUARD   -> "Охранять";
            case PROTECT -> "Защищать";
            case PASSIVE -> "Пассивный";
            case COMBAT  -> "Боевой";
            case STEALTH -> "Скрытный";
        };
    }

    public String description() {
        return switch (this) {
            case FOLLOW  -> "Следует за игроком";
            case WAIT    -> "Стоит на месте";
            case GUARD   -> "Охраняет точку";
            case PROTECT -> "Атакует врагов игрока";
            case PASSIVE -> "Не атакует, уклоняется";
            case COMBAT  -> "Участвует в бою";
            case STEALTH -> "Следует скрытно";
        };
    }

    /** Chat command words that switch to this mode. Checked case-insensitively. */
    public String defaultCommand() {
        return switch (this) {
            case FOLLOW  -> "следуй";
            case WAIT    -> "стой";
            case GUARD   -> "охраняй";
            case PROTECT -> "защищай";
            case PASSIVE -> "пассивный";
            case COMBAT  -> "атакуй";
            case STEALTH -> "прячься";
        };
    }
}

package com.frametrip.dragonlegacyquesttoast.profession.trader;

public enum RestockMode {
    DISABLED,
    EVERY_DAY_AT_TIME,
    EVERY_N_DAYS_AT_TIME;

    public String label() {
        return switch (this) {
            case DISABLED            -> "Отключено";
            case EVERY_DAY_AT_TIME   -> "Каждый игровой день";
            case EVERY_N_DAYS_AT_TIME -> "Каждые N дней";
        };
    }
}

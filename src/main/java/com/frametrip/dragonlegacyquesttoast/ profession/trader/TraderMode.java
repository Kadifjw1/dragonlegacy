package com.frametrip.dragonlegacyquesttoast.profession.trader;

public enum TraderMode {
    SELLER,
    BUYER,
    BOTH;

    public String label() {
        return switch (this) {
            case SELLER -> "Продавец";
            case BUYER  -> "Покупатель";
            case BOTH   -> "Продавец и покупатель";
        };
    }
}

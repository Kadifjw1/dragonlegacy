package com.frametrip.dragonlegacyquesttoast.currency;

public class ClientCurrencyState {

    private static long balance = 0L;

    public static long getBalance() {
        return balance;
    }

    public static void setBalance(long value) {
        balance = value;
    }

    public static String formatted() {
        return String.format("%,d", balance).replace(',', ' ');
    }
}

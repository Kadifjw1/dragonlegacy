package com.frametrip.dragonlegacyquesttoast.currency;

public class ClientCurrencyState {

    private static long         balance = 0L;
    // [ECO-1]: client-side copy of global currency config
    private static CurrencyConfig currencyConfig = new CurrencyConfig();

    public static long getBalance() { return balance; }
    public static void setBalance(long value) { balance = value; }
    public static String formatted() {
        return String.format("%,d", balance).replace(',', ' ');
    }

    public static CurrencyConfig getCurrencyConfig()             { return currencyConfig; }
    public static void           syncCurrencyConfig(CurrencyConfig c) { currencyConfig = c; }
    public static String         symbol()                        { return currencyConfig.symbol; }
    public static String         currencyName()                  { return currencyConfig.name; }
}

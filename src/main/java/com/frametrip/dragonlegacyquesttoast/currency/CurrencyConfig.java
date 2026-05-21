package com.frametrip.dragonlegacyquesttoast.currency;

// [ECO-1]: Global server-wide currency configuration.
public class CurrencyConfig {
    public String name   = "Монеты";
    public String symbol = "G";
    public String itemId = "dragonlegacyquesttoast:legacy_coin";

    public CurrencyConfig copy() {
        CurrencyConfig c = new CurrencyConfig();
        c.name   = this.name;
        c.symbol = this.symbol;
        c.itemId = this.itemId;
        return c;
    }
}

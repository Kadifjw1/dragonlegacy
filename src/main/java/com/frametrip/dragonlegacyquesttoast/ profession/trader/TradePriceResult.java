package com.frametrip.dragonlegacyquesttoast.profession.trader;

public class TradePriceResult {
    public long unitPrice;
    public int  amount;
    public long subtotal;
    public int  discountPercent;
    public long discountValue;
    public long finalPrice;

    public static TradePriceResult calculate(long unitPrice, int amount, int discountPercent) {
        TradePriceResult r = new TradePriceResult();
        r.unitPrice       = unitPrice;
        r.amount          = amount;
        r.subtotal        = unitPrice * amount;
        r.discountPercent = Math.min(90, Math.max(0, discountPercent));
        r.discountValue   = r.subtotal * r.discountPercent / 100;
        r.finalPrice      = Math.max(0, r.subtotal - r.discountValue);
        return r;
    }
}


package com.frametrip.dragonlegacyquesttoast.profession.trader;

public class TraderDiscountData {

    public boolean enabled                 = false;
    public int     globalBuyDiscountPercent = 0;   // 0–90
    public int     globalSellBonusPercent   = 0;   // 0–300
    public boolean allowPerOfferDiscounts   = false;

    public static TraderDiscountData createDefault() { return new TraderDiscountData(); }

    public TraderDiscountData copy() {
        TraderDiscountData c = new TraderDiscountData();
        c.enabled                  = this.enabled;
        c.globalBuyDiscountPercent = this.globalBuyDiscountPercent;
        c.globalSellBonusPercent   = this.globalSellBonusPercent;
        c.allowPerOfferDiscounts   = this.allowPerOfferDiscounts;
        return c;
    }

    /** Final buy discount capped at 90. Returns 0 if disabled. */
    public int effectiveBuyDiscount(int offerDiscount) {
        if (!enabled) return 0;
        int total = globalBuyDiscountPercent + (allowPerOfferDiscounts ? offerDiscount : 0);
        return Math.min(90, Math.max(0, total));
    }

    /** Final sell bonus capped at 300. Returns 0 if disabled. */
    public int effectiveSellBonus(int offerBonus) {
        if (!enabled) return 0;
        int total = globalSellBonusPercent + (allowPerOfferDiscounts ? offerBonus : 0);
        return Math.min(300, Math.max(0, total));
    }
}


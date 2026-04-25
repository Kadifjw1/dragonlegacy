package com.frametrip.dragonlegacyquesttoast.profession.trader;

public class TradeRewardResult {
    public long unitReward;
    public int  amount;
    public long subtotal;
    public int  bonusPercent;
    public long bonusValue;
    public long finalReward;

    public static TradeRewardResult calculate(long unitReward, int amount, int bonusPercent) {
        TradeRewardResult r = new TradeRewardResult();
        r.unitReward   = unitReward;
        r.amount       = amount;
        r.subtotal     = unitReward * amount;
        r.bonusPercent = Math.min(300, Math.max(0, bonusPercent));
        r.bonusValue   = r.subtotal * r.bonusPercent / 100;
        r.finalReward  = r.subtotal + r.bonusValue;
        return r;
    }
}


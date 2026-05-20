package com.frametrip.dragonlegacyquesttoast.currency;

// [ECO-2]: Per-NPC economy settings — wallet and reputation-based pricing.
public class NpcEconomyData {

    public long    npcWallet       = 0;
    public boolean repAffectsPrice = false;
    /** Discount % applied when player rep > 500 with the NPC's faction. */
    public int highRepBonus     = 20;
    /** Markup % applied when player rep < -500 with the NPC's faction. */
    public int lowRepPenalty    = 30;
    /** Player must have at least this rep to trade (-1000 = no restriction). */
    public int minRepToTrade    = -1000;

    /**
     * Returns a price multiplier based on the player's faction reputation.
     * Call server-side only; pass the rep value fetched from PlayerFactionReputationManager.
     */
    public float getPriceMultiplier(int reputation) {
        if (!repAffectsPrice) return 1.0f;
        if (reputation > 500)  return 1.0f - (highRepBonus  / 100f);
        if (reputation < -500) return 1.0f + (lowRepPenalty / 100f);
        return 1.0f;
    }

    public boolean canTrade(int reputation) {
        return reputation >= minRepToTrade;
    }

    public NpcEconomyData copy() {
        NpcEconomyData c = new NpcEconomyData();
        c.npcWallet       = this.npcWallet;
        c.repAffectsPrice = this.repAffectsPrice;
        c.highRepBonus    = this.highRepBonus;
        c.lowRepPenalty   = this.lowRepPenalty;
        c.minRepToTrade   = this.minRepToTrade;
        return c;
    }
}

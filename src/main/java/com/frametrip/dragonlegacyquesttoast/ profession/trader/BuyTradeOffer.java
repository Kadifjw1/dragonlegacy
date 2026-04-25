package com.frametrip.dragonlegacyquesttoast.profession.trader;

import java.util.UUID;

public class BuyTradeOffer {

    public String  id              = UUID.randomUUID().toString();
    public String  itemId          = "";
    public int     itemMeta        = 0;
    public int     reward          = 10;
    public int     requiredAmount  = 1;
    public String  customName      = "";
    public String  description     = "";
    public boolean infiniteDemand  = true;
    public int     demandLeft      = 64;
    public int     maxDemand       = 64;
    public boolean restockEnabled  = false;
    public int     restockAmount   = 16;
    public int     bonusPercent    = 0;   // 0–300, per-offer sell bonus

    public BuyTradeOffer copy() {
        BuyTradeOffer c = new BuyTradeOffer();
        c.id             = this.id;
        c.itemId         = this.itemId;
        c.itemMeta       = this.itemMeta;
        c.reward         = this.reward;
        c.requiredAmount = this.requiredAmount;
        c.customName     = this.customName;
        c.description    = this.description;
        c.infiniteDemand = this.infiniteDemand;
        c.demandLeft     = this.demandLeft;
        c.maxDemand      = this.maxDemand;
        c.restockEnabled = this.restockEnabled;
        c.restockAmount  = this.restockAmount;
        c.bonusPercent   = this.bonusPercent;
        return c;
    }

    public String displayName() {
        return customName.isBlank() ? itemId : customName;
    }
}


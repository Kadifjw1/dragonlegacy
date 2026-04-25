package com.frametrip.dragonlegacyquesttoast.profession.trader;

import java.util.UUID;

public class SellTradeOffer {

    public String  id             = UUID.randomUUID().toString();
    public String  itemId         = "";
    public int     itemMeta       = 0;
    public int     price          = 10;
    public int     amount         = 1;
    public String  customName     = "";
    public String  description    = "";
    public boolean infiniteStock  = true;
    public int     stock          = 64;
    public int     maxStock       = 64;
    public boolean restockEnabled  = false;
    public int     restockAmount   = 16;
    public int     discountPercent = 0;   // 0–90, per-offer discount

    public SellTradeOffer copy() {
        SellTradeOffer c = new SellTradeOffer();
        c.id             = this.id;
        c.itemId         = this.itemId;
        c.itemMeta       = this.itemMeta;
        c.price          = this.price;
        c.amount         = this.amount;
        c.customName     = this.customName;
        c.description    = this.description;
        c.infiniteStock  = this.infiniteStock;
        c.stock          = this.stock;
        c.maxStock       = this.maxStock;
        c.restockEnabled  = this.restockEnabled;
        c.restockAmount   = this.restockAmount;
        c.discountPercent = this.discountPercent;
        return c;
    }

    public String displayName() {
        return customName.isBlank() ? itemId : customName;
    }
}


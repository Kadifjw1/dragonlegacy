package com.frametrip.dragonlegacyquesttoast.profession.trader;

import java.util.ArrayList;
import java.util.List;

public class TraderProfessionData {

    public TraderMode             mode            = TraderMode.SELLER;
    public List<SellTradeOffer>   sellOffers      = new ArrayList<>();
    public List<BuyTradeOffer>    buyOffers       = new ArrayList<>();
    public TradeRestockSettings   restockSettings = new TradeRestockSettings();
    public TraderShopLayoutData   layoutData      = TraderShopLayoutData.createDefault();
    public TraderDiscountData     discountData    = TraderDiscountData.createDefault();

    public TraderProfessionData copy() {
        TraderProfessionData c = new TraderProfessionData();
        c.mode = this.mode;
        for (SellTradeOffer o : this.sellOffers) c.sellOffers.add(o.copy());
        for (BuyTradeOffer  o : this.buyOffers)  c.buyOffers.add(o.copy());
        c.restockSettings = this.restockSettings.copy();
        c.layoutData   = this.layoutData   != null ? this.layoutData.copy()   : TraderShopLayoutData.createDefault();
        c.discountData = this.discountData != null ? this.discountData.copy() : TraderDiscountData.createDefault();
        return c;
    }

    /** Returns layoutData, creating default if null (handles old saved data). */
    public TraderShopLayoutData getOrCreateLayout() {
        if (layoutData == null) layoutData = TraderShopLayoutData.createDefault();
        return layoutData;
    }

    /** Returns discountData, creating default if null (handles old saved data). */
    public TraderDiscountData getOrCreateDiscounts() {
        if (discountData == null) discountData = TraderDiscountData.createDefault();
        return discountData;
    }
}


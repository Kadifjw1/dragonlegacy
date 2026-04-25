package com.frametrip.dragonlegacyquesttoast.profession.trader;

public class TraderShopLayoutPreset {
    public final String              id;
    public final String              displayName;
    public final TraderShopLayoutData layoutData;

    public TraderShopLayoutPreset(String id, String displayName, TraderShopLayoutData layoutData) {
        this.id          = id;
        this.displayName = displayName;
        this.layoutData  = layoutData;
    }
}

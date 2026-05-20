package com.frametrip.dragonlegacyquesttoast.server.vfx;

public class DynamicSkin {
    public String condition = ""; // e.g. "time:night", "weather:rain", "mood:<-50"
    public String skinName  = ""; // texture resource name

    public DynamicSkin() {}

    public DynamicSkin(String condition, String skinName) {
        this.condition = condition;
        this.skinName  = skinName;
    }

    public DynamicSkin copy() {
        return new DynamicSkin(this.condition, this.skinName);
    }
}

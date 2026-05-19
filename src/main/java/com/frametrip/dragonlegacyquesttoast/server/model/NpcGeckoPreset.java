package com.frametrip.dragonlegacyquesttoast.server.model;

// [MOD-3]: Saved GeckoLib resource preset (model + animation + texture triplet).
public class NpcGeckoPreset {
    public String name          = "Пресет";
    public String geckoModel    = "";
    public String geckoAnimation = "";
    public String geckoTexture  = "";

    public NpcGeckoPreset() {}

    public NpcGeckoPreset(String name, String model, String anim, String tex) {
        this.name           = name;
        this.geckoModel     = model;
        this.geckoAnimation = anim;
        this.geckoTexture   = tex;
    }
}

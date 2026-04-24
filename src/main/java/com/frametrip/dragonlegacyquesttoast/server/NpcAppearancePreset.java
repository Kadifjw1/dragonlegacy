package com.frametrip.dragonlegacyquesttoast.server;

import java.util.*;

public class NpcAppearancePreset {

    public String id;
    public String name = "Пресет";
    public String skinId = "default";
    public Map<String, String> textureLayers = new LinkedHashMap<>();
    public Map<String, Integer> bodyParts = new LinkedHashMap<>();

    public NpcAppearancePreset() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
    }

    public NpcAppearancePreset copy() {
        NpcAppearancePreset c = new NpcAppearancePreset();
        c.id            = this.id;
        c.name          = this.name;
        c.skinId        = this.skinId;
        c.textureLayers = new LinkedHashMap<>(this.textureLayers);
        c.bodyParts     = new LinkedHashMap<>(this.bodyParts);
        return c;
    }
}

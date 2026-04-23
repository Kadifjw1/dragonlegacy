package com.frametrip.dragonlegacyquesttoast.entity;
 
import java.util.*;
 
public class FactionData {
 
    public String id;
    public String name;
    public int    color;       // packed ARGB
    public String description;
    public Map<String, String> relations; // factionId -> FRIENDLY / NEUTRAL / HOSTILE
 
    public static final int[] PRESET_COLORS = {
        0xFF4488FF, 0xFFFF4444, 0xFF44CC44,
        0xFFFFAA00, 0xFFAA44FF, 0xFF44CCCC,
        0xFFFF88CC, 0xFF888888
    };
 
    public FactionData() {
        this.id          = UUID.randomUUID().toString().substring(0, 8);
        this.name        = "Новая фракция";
        this.color       = PRESET_COLORS[0];
        this.description = "";
        this.relations   = new LinkedHashMap<>();
    }
 
    public FactionData copy() {
        FactionData c = new FactionData();
        c.id          = this.id;
        c.name        = this.name;
        c.color       = this.color;
        c.description = this.description;
        c.relations   = new LinkedHashMap<>(this.relations);
        return c;
    }
 
    public String getRelationTo(String factionId) {
        return relations.getOrDefault(factionId, "NEUTRAL");
    }
 
    public void setRelationTo(String factionId, String rel) {
        relations.put(factionId, rel);
    }
}

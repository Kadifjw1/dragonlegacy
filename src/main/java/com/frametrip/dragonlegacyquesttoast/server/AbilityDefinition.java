package com.frametrip.dragonlegacyquesttoast.server;
 
import com.frametrip.dragonlegacyquesttoast.client.AwakeningPathType;
 
import java.util.List;
 
public class AbilityDefinition {
    public final String id;
    public final String name;
    public final String description;
    public final AwakeningPathType path;
    public final int tier;
    public final int cost;
    public final List<String> requires;
    public final int treeIndex; // 1-10
 
    public AbilityDefinition(String id, String name, String description,
                              AwakeningPathType path, int tier, int cost,
                              List<String> requires, int treeIndex) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.path = path;
        this.tier = tier;
        this.cost = cost;
        this.requires = requires;
        this.treeIndex = treeIndex;
    }
}

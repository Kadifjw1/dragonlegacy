package com.frametrip.dragonlegacyquesttoast.server.world;

// [WLD-2]: Configuration for the NPC farmer role.
public class FarmerData {

    public boolean farmerEnabled = false;
    // wheat | carrot | potato | beetroot
    public String cropType    = "wheat";
    public int    plotRadius  = 8;
    // "x,y,z" — storage chest position; empty = drop in world
    public String storagePos  = "";

    public FarmerData copy() {
        FarmerData c = new FarmerData();
        c.farmerEnabled = this.farmerEnabled;
        c.cropType      = this.cropType;
        c.plotRadius    = this.plotRadius;
        c.storagePos    = this.storagePos;
        return c;
    }
}

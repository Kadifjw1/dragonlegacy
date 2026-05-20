package com.frametrip.dragonlegacyquesttoast.server.stats;

// [STA-1]: Per-NPC aggregate statistics. Stored in NpcEntityData (Gson-serialized).
public class NpcStatisticsData {

    public int    timesKilled      = 0;
    public int    questsGiven      = 0;
    public int    itemsSold        = 0;
    public int    dialogsStarted   = 0;
    public int    interactionCount = 0;
    public long   firstSpawnTime   = 0L;
    public String createdBy        = "";

    /** Resets counters but preserves creation metadata. */
    public void reset() {
        timesKilled      = 0;
        questsGiven      = 0;
        itemsSold        = 0;
        dialogsStarted   = 0;
        interactionCount = 0;
    }

    public NpcStatisticsData copy() {
        NpcStatisticsData c = new NpcStatisticsData();
        c.timesKilled      = this.timesKilled;
        c.questsGiven      = this.questsGiven;
        c.itemsSold        = this.itemsSold;
        c.dialogsStarted   = this.dialogsStarted;
        c.interactionCount = this.interactionCount;
        c.firstSpawnTime   = this.firstSpawnTime;
        c.createdBy        = this.createdBy;
        return c;
    }
}

package com.frametrip.dragonlegacyquesttoast.server.immersion;

// [IMM-5]: Defines how an NPC reacts when a nearby player holds a specific item.
public class NpcItemReaction {

    public String itemId       = "";
    /** FEAR, INTEREST, AGGRO, DIALOG */
    public String reactionType = "DIALOG";
    public String dialog       = "";
    public int    moodChange   = 0;

    public NpcItemReaction copy() {
        NpcItemReaction c = new NpcItemReaction();
        c.itemId       = this.itemId;
        c.reactionType = this.reactionType;
        c.dialog       = this.dialog;
        c.moodChange   = this.moodChange;
        return c;
    }
}

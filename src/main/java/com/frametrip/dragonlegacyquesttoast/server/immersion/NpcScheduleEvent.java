package com.frametrip.dragonlegacyquesttoast.server.immersion;

// [IMM-4]: One entry in an NPC's daily 24-hour schedule.
public class NpcScheduleEvent {

    /** In-game hour 0-23 when this event triggers. */
    public int    hour   = 8;
    /** Action type: GOTO, SLEEP, WORK, IDLE, SAY, EMOTE */
    public String action = "IDLE";
    /** Target waypoint name or coordinates "x,y,z" for GOTO; animation name for EMOTE. */
    public String target = "";
    /** Phrase to broadcast for SAY action. */
    public String dialog = "";

    public NpcScheduleEvent copy() {
        NpcScheduleEvent c = new NpcScheduleEvent();
        c.hour   = this.hour;
        c.action = this.action;
        c.target = this.target;
        c.dialog = this.dialog;
        return c;
    }
}

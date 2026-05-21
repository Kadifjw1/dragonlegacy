package com.frametrip.dragonlegacyquesttoast.server.immersion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// [IMM-1..6]: All immersion & living-world data for one NPC.
public class NpcImmersionData {

    // [IMM-1]: Player memory — remembers visit counts
    public boolean            rememberPlayers    = false;
    public String             firstVisitDialog   = "";
    public String             returningDialog    = "";
    public String             regularDialog      = "";
    /** UUID.toString() → visit count (server-side only). */
    public Map<String, Integer> playerVisits     = new LinkedHashMap<>();

    // [IMM-2]: Mood system
    public boolean moodEnabled   = false;
    public boolean showMoodIcon  = false;
    public int     mood          = 0;   // clamped -100..100
    public List<String> moodGiftItems = new ArrayList<>();

    // [IMM-3]: NPC-to-NPC conversations
    public boolean      selfConvEnabled = false;
    public List<String> selfConvPhrases = new ArrayList<>();

    // [IMM-4]: Daily schedule
    public List<NpcScheduleEvent> dailySchedule = new ArrayList<>();

    // [IMM-5]: Item-in-hand reactions
    public List<NpcItemReaction> itemReactions = new ArrayList<>();

    // [IMM-6]: Death memory
    public boolean rememberDeath        = false;
    public String  deathReactionDialog  = "";
    public String  killerPlayerUuid     = "";

    /** Clamp and return the current mood in -100..100. */
    public int clampedMood() {
        return Math.min(100, Math.max(-100, mood));
    }

    /** Price multiplier based on mood (used in trade). */
    public float moodPriceMultiplier() {
        if (!moodEnabled) return 1.0f;
        return 1.0f - (clampedMood() / 500.0f);
    }

    public NpcImmersionData copy() {
        NpcImmersionData c = new NpcImmersionData();
        c.rememberPlayers   = this.rememberPlayers;
        c.firstVisitDialog  = this.firstVisitDialog;
        c.returningDialog   = this.returningDialog;
        c.regularDialog     = this.regularDialog;
        c.playerVisits      = new LinkedHashMap<>(this.playerVisits);
        c.moodEnabled       = this.moodEnabled;
        c.showMoodIcon      = this.showMoodIcon;
        c.mood              = this.mood;
        c.moodGiftItems     = new ArrayList<>(this.moodGiftItems);
        c.selfConvEnabled   = this.selfConvEnabled;
        c.selfConvPhrases   = new ArrayList<>(this.selfConvPhrases);
        c.dailySchedule     = new ArrayList<>();
        for (NpcScheduleEvent e : this.dailySchedule) c.dailySchedule.add(e.copy());
        c.itemReactions     = new ArrayList<>();
        for (NpcItemReaction r : this.itemReactions) c.itemReactions.add(r.copy());
        c.rememberDeath       = this.rememberDeath;
        c.deathReactionDialog = this.deathReactionDialog;
        c.killerPlayerUuid    = this.killerPlayerUuid;
        return c;
    }
}

package com.frametrip.dragonlegacyquesttoast.profession;

// [JOB-3]: Conditions that gate whether an NPC performs their profession behaviour.
public class JobConditions {

    public boolean requireWorkSchedule = false;
    public boolean requireFairWeather  = false; // block during rain/thunder
    public boolean requireDaytime      = false;  // block during night
    public float   minHealthPercent    = 0f;     // 0..1, stop working if HP below this

    public JobConditions copy() {
        JobConditions c = new JobConditions();
        c.requireWorkSchedule = this.requireWorkSchedule;
        c.requireFairWeather  = this.requireFairWeather;
        c.requireDaytime      = this.requireDaytime;
        c.minHealthPercent    = this.minHealthPercent;
        return c;
    }
}

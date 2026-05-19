package com.frametrip.dragonlegacyquesttoast.profession;

import net.minecraft.world.level.Level;

// [JOB-1]: Work schedule — gates profession activity to specific in-game hours.
public class WorkSchedule {

    public boolean enabled   = false;
    public int     startHour = 8;
    public int     endHour   = 18;

    public boolean isWorkTime(Level level) {
        if (!enabled) return true;
        int hour = (int) ((level.getDayTime() % 24000) / 1000);
        return hour >= startHour && hour < endHour;
    }

    public WorkSchedule copy() {
        WorkSchedule c = new WorkSchedule();
        c.enabled   = this.enabled;
        c.startHour = this.startHour;
        c.endHour   = this.endHour;
        return c;
    }
}

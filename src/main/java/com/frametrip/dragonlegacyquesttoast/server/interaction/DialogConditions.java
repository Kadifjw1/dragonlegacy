package com.frametrip.dragonlegacyquesttoast.server.interaction;

import net.minecraft.world.level.Level;

// [INT-2]: Dialog access conditions — checked before starting any dialogue/scene
public class DialogConditions {
    public boolean onlyDay   = false;
    public boolean onlyNight = false;
    public boolean onlyRain  = false;
    public boolean onlyClear = false;

    public boolean check(Level level) {
        long time = level.getDayTime() % 24000;
        if (onlyDay   && time >= 12000) return false;
        if (onlyNight && time <  12000) return false;
        if (onlyRain  && !level.isRaining()) return false;
        if (onlyClear &&  level.isRaining()) return false;
        return true;
    }

    public DialogConditions copy() {
        DialogConditions c = new DialogConditions();
        c.onlyDay   = this.onlyDay;
        c.onlyNight = this.onlyNight;
        c.onlyRain  = this.onlyRain;
        c.onlyClear = this.onlyClear;
        return c;
    }
}

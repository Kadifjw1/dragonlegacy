package com.frametrip.dragonlegacyquesttoast.server.interaction;

import com.frametrip.dragonlegacyquesttoast.server.compat.LuckPermsHook;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

// [INT-2]: Dialog access conditions — checked before starting any dialogue/scene
public class DialogConditions {
    public boolean onlyDay   = false;
    public boolean onlyNight = false;
    public boolean onlyRain  = false;
    public boolean onlyClear = false;

    // [INT-API-2]: LuckPerms group required (empty = no restriction)
    public String requiresLuckPermsGroup = "";

    public boolean check(Level level) {
        long time = level.getDayTime() % 24000;
        if (onlyDay   && time >= 12000) return false;
        if (onlyNight && time <  12000) return false;
        if (onlyRain  && !level.isRaining()) return false;
        if (onlyClear &&  level.isRaining()) return false;
        return true;
    }

    /** Extended check including player-specific conditions (LuckPerms, etc.). */
    public boolean checkPlayer(ServerPlayer player) {
        if (requiresLuckPermsGroup != null && !requiresLuckPermsGroup.isBlank()) {
            if (!LuckPermsHook.playerInGroup(player, requiresLuckPermsGroup)) return false;
        }
        return true;
    }

    public DialogConditions copy() {
        DialogConditions c = new DialogConditions();
        c.onlyDay   = this.onlyDay;
        c.onlyNight = this.onlyNight;
        c.onlyRain  = this.onlyRain;
        c.onlyClear = this.onlyClear;
        c.requiresLuckPermsGroup = this.requiresLuckPermsGroup;
        return c;
    }
}

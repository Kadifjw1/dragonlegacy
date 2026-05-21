package com.frametrip.dragonlegacyquesttoast.client.vfx;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

// [VFX-4]: Evaluates dynamic skin conditions client-side.
// Supported: "time:night", "time:day", "weather:rain", "weather:thunder", "mood:<N", "mood:>N"
public class DynamicSkinConditionParser {

    public static boolean check(String condition, NpcEntity npc) {
        if (condition == null || condition.isBlank()) return false;
        Level level = npc.level();

        if (condition.startsWith("time:")) {
            String sub = condition.substring(5);
            long time = level.getDayTime() % 24000L;
            return switch (sub) {
                case "night" -> time >= 13000L && time < 23000L;
                case "day"   -> time < 13000L || time >= 23000L;
                default      -> false;
            };
        }

        if (condition.startsWith("weather:")) {
            String sub = condition.substring(8);
            return switch (sub) {
                case "rain"    -> level.isRaining();
                case "thunder" -> level.isThundering();
                case "clear"   -> !level.isRaining() && !level.isThundering();
                default        -> false;
            };
        }

        if (condition.startsWith("mood:")) {
            String sub = condition.substring(5);
            NpcEntityData data = npc.getNpcData();
            if (data == null || data.immersionData == null) return false;
            int mood = data.immersionData.mood;
            try {
                if (sub.startsWith("<"))  return mood <  Integer.parseInt(sub.substring(1));
                if (sub.startsWith(">"))  return mood >  Integer.parseInt(sub.substring(1));
                if (sub.startsWith("<=")) return mood <= Integer.parseInt(sub.substring(2));
                if (sub.startsWith(">=")) return mood >= Integer.parseInt(sub.substring(2));
                return mood == Integer.parseInt(sub);
            } catch (NumberFormatException ignored) {
                return false;
            }
        }

        return false;
    }
}

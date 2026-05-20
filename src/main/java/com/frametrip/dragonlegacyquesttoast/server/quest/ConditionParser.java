package com.frametrip.dragonlegacyquesttoast.server.quest;

import com.frametrip.dragonlegacyquesttoast.server.PlayerFactionReputationManager;
import com.frametrip.dragonlegacyquesttoast.server.QuestProgressManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

// [QST-2] / [QST-5]: Evaluates string conditions for branching dialogs and hidden quest gates.
// Supported syntax:
//   hasItem:<itemId>:<count>          e.g. hasItem:minecraft:diamond:5
//   questDone:<questId>               e.g. questDone:abc123
//   questActive:<questId>
//   reputation:<factionId>:>value     e.g. reputation:elves:>50
//   reputation:<factionId>:<value     e.g. reputation:elves:<-20
//   time:day | time:night
//   always                            always true (convenience)
//   never                             always false
public class ConditionParser {

    public static boolean check(String condition, ServerPlayer player) {
        if (condition == null || condition.isBlank() || condition.equalsIgnoreCase("always")) return true;
        if (condition.equalsIgnoreCase("never")) return false;

        String[] parts = condition.split(":", -1);
        String type = parts[0].toLowerCase();

        try {
            switch (type) {
                case "hasitem": {
                    if (parts.length < 3) return false;
                    String itemId = parts[1];
                    int required = Integer.parseInt(parts[2]);
                    var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
                    if (item == null) return false;
                    int held = 0;
                    for (ItemStack stack : player.getInventory().items) {
                        if (stack.getItem() == item) held += stack.getCount();
                    }
                    return held >= required;
                }
                case "questdone": {
                    if (parts.length < 2) return false;
                    return QuestProgressManager.isCompleted(player.getUUID(), parts[1]);
                }
                case "questactive": {
                    if (parts.length < 2) return false;
                    return QuestProgressManager.isActive(player.getUUID(), parts[1]);
                }
                case "reputation": {
                    if (parts.length < 3) return false;
                    String factionId = parts[1];
                    String cmp = parts[2]; // e.g. ">50" or "<-20"
                    int rep = PlayerFactionReputationManager.get(player.getUUID(), factionId);
                    if (cmp.startsWith(">")) return rep > Integer.parseInt(cmp.substring(1));
                    if (cmp.startsWith("<")) return rep < Integer.parseInt(cmp.substring(1));
                    if (cmp.startsWith(">=")) return rep >= Integer.parseInt(cmp.substring(2));
                    if (cmp.startsWith("<=")) return rep <= Integer.parseInt(cmp.substring(2));
                    return rep == Integer.parseInt(cmp);
                }
                case "time": {
                    if (parts.length < 2) return false;
                    long time = player.level().getDayTime() % 24000;
                    if (parts[1].equalsIgnoreCase("day"))   return time >= 0 && time < 12000;
                    if (parts[1].equalsIgnoreCase("night"))  return time >= 12000;
                    return false;
                }
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}

package com.frametrip.dragonlegacyquesttoast.client.dialogue;

import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcChoiceOption;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcSceneNode;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Decides whether a choice option is visible to the player (client-side approximation). */
public final class NpcSceneChoiceFilter {

    private NpcSceneChoiceFilter() {}

    public static boolean isAvailable(NpcChoiceOption opt) {
        if (opt == null) return false;
        String type = opt.conditionType;
        if (type == null || type.isBlank()) return true;

        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        Level lvl = mc.level;
        String param = opt.conditionParam;

        return switch (type) {
            case NpcSceneNode.COND_TIME_DAY     -> lvl != null && lvl.isDay();
            case NpcSceneNode.COND_TIME_NIGHT   -> lvl != null && lvl.isNight();
            case NpcSceneNode.COND_HAS_ITEM     -> p != null && hasItem(p, param);
            case NpcSceneNode.COND_NOT_HAS_ITEM -> p == null || !hasItem(p, param);
            // Quest / relation / faction / path conditions are server-authoritative,
            // so we show the choice by default to avoid hiding content.
            default -> true;
        };
    }

    private static boolean hasItem(Player p, String itemId) {
        if (itemId == null || itemId.isBlank()) return false;
        try {
            ResourceLocation rl = new ResourceLocation(itemId);
            var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(rl);
            if (item == null) return false;
            return p.getInventory().contains(new ItemStack(item));
        } catch (Exception e) {
            return false;
        }
    }
}

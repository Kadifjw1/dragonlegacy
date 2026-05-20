package com.frametrip.dragonlegacyquesttoast.server.compat;

import com.frametrip.dragonlegacyquesttoast.entity.FactionData;
import com.frametrip.dragonlegacyquesttoast.server.FactionManager;
import com.frametrip.dragonlegacyquesttoast.server.PlayerFactionReputationManager;
import com.frametrip.dragonlegacyquesttoast.server.QuestProgressManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;

/**
 * [INT-API-1]: Soft PlaceholderAPI integration.
 * On Forge, PlaceholderAPI is not available, so we resolve our own
 * %dragonlegacy_*% placeholders directly from mod data.
 */
public class PlaceholderApiHook {

    private static final String MOD_ID = "placeholderapi";

    public static boolean isAvailable() {
        return ModList.get().isLoaded(MOD_ID);
    }

    /**
     * Resolves %dragonlegacy_*% placeholders in text for the given player.
     * Works regardless of whether PlaceholderAPI mod is present.
     */
    public static String resolve(ServerPlayer player, String text) {
        if (text == null || !text.contains("%")) return text;

        String uuid = player.getStringUUID();

        // %dragonlegacy_quests_active% — number of active quests
        text = text.replace("%dragonlegacy_quests_active%",
                String.valueOf(QuestProgressManager.getActive(player.getUUID()).size()));

        // %dragonlegacy_quests_done% — number of completed quests
        text = text.replace("%dragonlegacy_quests_done%",
                String.valueOf(QuestProgressManager.getCompleted(player.getUUID()).size()));

        // %dragonlegacy_rep_<factionId>% — reputation for each known faction
        for (FactionData faction : FactionManager.getAll()) {
            if (faction.id == null || faction.id.isEmpty()) continue;
            int rep = PlayerFactionReputationManager.get(player.getUUID(), faction.id);
            text = text.replace("%dragonlegacy_rep_" + faction.id + "%", String.valueOf(rep));
        }

        // %dragonlegacy_player% — player name
        text = text.replace("%dragonlegacy_player%", player.getGameProfile().getName());

        return text;
    }
}

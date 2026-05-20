package com.frametrip.dragonlegacyquesttoast.server;

import com.frametrip.dragonlegacyquesttoast.entity.FactionData;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SyncReputationPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

// [REL-3]: Reduces (or increases) player reputation when killing an NPC from a faction.
public class NpcFactionDeathHandler {

    @SubscribeEvent
    public void onNpcDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof NpcEntity npc)) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        // [STA-1]: Increment death counter
        NpcEntityData npcData = npc.getNpcData();
        if (npcData.stats != null) {
            npcData.stats.timesKilled++;
            npc.setNpcData(npcData);
        }

        String factionId = npcData.factionId;
        if (factionId == null || factionId.isEmpty()) return;

        FactionData faction = FactionManager.get(factionId);
        if (faction == null || faction.killReputationPenalty == 0) return;

        PlayerFactionReputationManager.add(player.getUUID(), factionId, faction.killReputationPenalty);

        if (player instanceof ServerPlayer sp) {
            String key = faction.killReputationPenalty < 0 ? "npc.reputation.lost" : "npc.reputation.gain";
            sp.sendSystemMessage(Component.translatable(key,
                    faction.name, Math.abs(faction.killReputationPenalty)));
            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> sp),
                    new SyncReputationPacket(
                            PlayerFactionReputationManager.getAllForPlayer(player.getUUID())));
        }
    }
}

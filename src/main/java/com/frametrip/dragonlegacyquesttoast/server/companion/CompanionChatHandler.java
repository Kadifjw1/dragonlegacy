package com.frametrip.dragonlegacyquesttoast.server.companion;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.profession.NpcProfessionType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Listens to player chat to switch companion mode via configured command words. */
@Mod.EventBusSubscriber
public class CompanionChatHandler {

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String msg = event.getRawText().toLowerCase().trim();

        ServerLevel level = (ServerLevel) player.level();
        level.getAllEntities().forEach(entity -> {
            if (!(entity instanceof NpcEntity npc)) return;
            NpcEntityData data = npc.getNpcData();
            if (data.professionData == null
                    || data.professionData.type != NpcProfessionType.COMPANION) return;
            if (npc.distanceTo(player) > 16) return;

            CompanionData cd = data.companionData;
            if (!player.getUUID().toString().equals(cd.ownerUUID)
                    && !cd.ownerUUID.isEmpty()) return;

            for (CompanionMode mode : CompanionMode.values()) {
                String cmd = cd.commandFor(mode).toLowerCase();
                if (!cmd.isEmpty() && msg.contains(cmd)) {
                    cd.setMode(mode);
                    if (mode == CompanionMode.GUARD && !cd.guardPointSet) {
                        cd.guardX = npc.getX();
                        cd.guardY = npc.getY();
                        cd.guardZ = npc.getZ();
                        cd.guardPointSet = true;
                    }
                    npc.setNpcData(data);
                    player.sendSystemMessage(
                            net.minecraft.network.chat.Component.literal(
                                    "§7" + data.displayName + " → §e" + mode.label())
                    );
                    break;
                }
            }
        });
    }
}


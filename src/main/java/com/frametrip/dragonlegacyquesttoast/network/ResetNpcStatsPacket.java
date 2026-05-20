package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Client → Server: reset all statistics counters for the given NPC. */
public class ResetNpcStatsPacket {

    private final UUID npcUuid;

    public ResetNpcStatsPacket(UUID npcUuid) {
        this.npcUuid = npcUuid;
    }

    public static void encode(ResetNpcStatsPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.npcUuid);
    }

    public static ResetNpcStatsPacket decode(FriendlyByteBuf buf) {
        return new ResetNpcStatsPacket(buf.readUUID());
    }

    public static void handle(ResetNpcStatsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.getAbilities().instabuild) return;

            var server = player.getServer();
            if (server == null) return;

            for (ServerLevel level : server.getAllLevels()) {
                Entity e = level.getEntity(msg.npcUuid);
                if (e instanceof NpcEntity npc) {
                    NpcEntityData data = npc.getNpcData();
                    if (data.stats != null) data.stats.reset();
                    npc.setNpcData(data);
                    break;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

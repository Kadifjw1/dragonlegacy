package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

// [EDT-4]: Client → Server: request the full list of NPC entities in the world.
public class RequestRemoteNpcListPacket {

    public static void encode(RequestRemoteNpcListPacket pkt, FriendlyByteBuf buf) {}

    public static RequestRemoteNpcListPacket decode(FriendlyByteBuf buf) {
        return new RequestRemoteNpcListPacket();
    }

    public static void handle(RequestRemoteNpcListPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.getAbilities().instabuild) return;

            var server = player.getServer();
            if (server == null) return;

            List<SyncRemoteNpcListPacket.NpcEntry> entries = new ArrayList<>();
            for (ServerLevel level : server.getAllLevels()) {
                for (net.minecraft.world.entity.Entity e : level.getAllEntities()) {
                    if (!(e instanceof NpcEntity npc) || npc.isDeadOrDying()) continue;
                    entries.add(new SyncRemoteNpcListPacket.NpcEntry(
                            npc.getUUID(),
                            npc.getNpcData().displayName,
                            (float) npc.getX(), (float) npc.getY(), (float) npc.getZ(),
                            level.dimension().location().toString()
                    ));
                }
            }
            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncRemoteNpcListPacket(entries));
        });
        ctx.get().setPacketHandled(true);
    }
}

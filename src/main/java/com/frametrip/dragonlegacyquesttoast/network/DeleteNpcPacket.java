package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Sent by client to request removal of an NPC entity from the world. */
public class DeleteNpcPacket {

    private final UUID entityUuid;

    public DeleteNpcPacket(UUID entityUuid) {
        this.entityUuid = entityUuid;
    }

    public static void encode(DeleteNpcPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.entityUuid);
    }

    public static DeleteNpcPacket decode(FriendlyByteBuf buf) {
        return new DeleteNpcPacket(buf.readUUID());
    }

    public static void handle(DeleteNpcPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.getAbilities().instabuild) return;

            net.minecraft.server.MinecraftServer server = player.getServer();
            if (server == null) return;

            for (ServerLevel level : server.getAllLevels()) {
                Entity e = level.getEntity(msg.entityUuid);
                if (e instanceof NpcEntity) {
                    e.discard();
                    break;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

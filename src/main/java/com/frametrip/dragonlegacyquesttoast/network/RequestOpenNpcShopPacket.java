package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.profession.NpcProfessionType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

/** Client -> Server request to open trader shop for an NPC entity. */
public class RequestOpenNpcShopPacket {

    private final UUID npcUuid;

    public RequestOpenNpcShopPacket(UUID npcUuid) {
        this.npcUuid = npcUuid;
    }

    public static void encode(RequestOpenNpcShopPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.npcUuid);
    }

    public static RequestOpenNpcShopPacket decode(FriendlyByteBuf buf) {
        return new RequestOpenNpcShopPacket(buf.readUUID());
    }

    public static void handle(RequestOpenNpcShopPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer sender = c.getSender();
            if (sender == null) return;
            var server = sender.getServer();
            if (server == null) return;

            for (ServerLevel level : server.getAllLevels()) {
                Entity e = level.getEntity(msg.npcUuid);
                if (!(e instanceof NpcEntity npc)) continue;
                NpcEntityData d = npc.getNpcData();
                if (d.professionData == null || d.professionData.type != NpcProfessionType.TRADER
                        || d.professionData.traderData == null) return;
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender),
                        new OpenTraderShopPacket(npc.getUUID(), d));
                return;
            }
        });
        c.setPacketHandled(true);
    }
}

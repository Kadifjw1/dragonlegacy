package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.profession.NpcProfessionData;
import com.frametrip.dragonlegacyquesttoast.profession.trader.TraderProfessionData;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Client → Server: save trader profession data for an NPC.
 * Only accepted from creative-mode players.
 */
public class SaveTraderDataPacket {

    private static final Gson GSON = new Gson();

    private final UUID   entityUuid;
    private final String traderJson;

    public SaveTraderDataPacket(UUID entityUuid, TraderProfessionData data) {
        this.entityUuid = entityUuid;
        this.traderJson = GSON.toJson(data);
    }

    private SaveTraderDataPacket(UUID entityUuid, String json) {
        this.entityUuid = entityUuid;
        this.traderJson = json;
    }

    public static void encode(SaveTraderDataPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.entityUuid);
        buf.writeUtf(msg.traderJson, 131072);
    }

    public static SaveTraderDataPacket decode(FriendlyByteBuf buf) {
        return new SaveTraderDataPacket(buf.readUUID(), buf.readUtf(131072));
    }

    public static void handle(SaveTraderDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.getAbilities().instabuild) return;

            TraderProfessionData traderData = GSON.fromJson(msg.traderJson, TraderProfessionData.class);
            if (traderData == null) return;

            var server = player.getServer();
            if (server == null) return;

            for (ServerLevel level : server.getAllLevels()) {
                Entity e = level.getEntity(msg.entityUuid);
                if (e instanceof NpcEntity npc) {
                    NpcEntityData data = npc.getNpcData();
                    if (data.professionData == null) data.professionData = new NpcProfessionData();
                    data.professionData.traderData = traderData;
                    npc.setNpcData(data);
                    break;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

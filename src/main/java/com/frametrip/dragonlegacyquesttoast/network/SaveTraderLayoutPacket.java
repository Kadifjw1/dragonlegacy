package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.profession.trader.TraderShopLayoutData;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Client → Server: save trader shop layout data. Creative-only. */
public class SaveTraderLayoutPacket {

    private static final Gson GSON = new Gson();

    private final UUID   entityUuid;
    private final String layoutJson;

    public SaveTraderLayoutPacket(UUID entityUuid, TraderShopLayoutData layout) {
        this.entityUuid = entityUuid;
        this.layoutJson = GSON.toJson(layout);
    }

    private SaveTraderLayoutPacket(UUID uuid, String json) {
        this.entityUuid = uuid;
        this.layoutJson = json;
    }

    public static void encode(SaveTraderLayoutPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.entityUuid);
        buf.writeUtf(msg.layoutJson, 65536);
    }

    public static SaveTraderLayoutPacket decode(FriendlyByteBuf buf) {
        return new SaveTraderLayoutPacket(buf.readUUID(), buf.readUtf(65536));
    }

    public static void handle(SaveTraderLayoutPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.getAbilities().instabuild) return;
            TraderShopLayoutData layout = GSON.fromJson(msg.layoutJson, TraderShopLayoutData.class);
            if (layout == null) return;
            for (ServerLevel level : player.getServer().getAllLevels()) {
                Entity e = level.getEntity(msg.entityUuid);
                if (e instanceof NpcEntity npc) {
                    NpcEntityData data = npc.getNpcData();
                    if (data.professionData != null && data.professionData.traderData != null)
                        data.professionData.traderData.layoutData = layout;
                    npc.setNpcData(data);
                    break;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}


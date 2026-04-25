package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.profession.trader.TraderDiscountData;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Client → Server: save trader discount data. Creative-only. */
public class SaveTraderDiscountsPacket {

    private static final Gson GSON = new Gson();

    private final UUID   entityUuid;
    private final String discountJson;

    public SaveTraderDiscountsPacket(UUID entityUuid, TraderDiscountData discounts) {
        this.entityUuid   = entityUuid;
        this.discountJson = GSON.toJson(discounts);
    }

    private SaveTraderDiscountsPacket(UUID uuid, String json) {
        this.entityUuid   = uuid;
        this.discountJson = json;
    }

    public static void encode(SaveTraderDiscountsPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.entityUuid);
        buf.writeUtf(msg.discountJson, 4096);
    }

    public static SaveTraderDiscountsPacket decode(FriendlyByteBuf buf) {
        return new SaveTraderDiscountsPacket(buf.readUUID(), buf.readUtf(4096));
    }

    public static void handle(SaveTraderDiscountsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.getAbilities().instabuild) return;
            TraderDiscountData discounts = GSON.fromJson(msg.discountJson, TraderDiscountData.class);
            if (discounts == null) return;
            for (ServerLevel level : player.getServer().getAllLevels()) {
                Entity e = level.getEntity(msg.entityUuid);
                if (e instanceof NpcEntity npc) {
                    NpcEntityData data = npc.getNpcData();
                    if (data.professionData != null && data.professionData.traderData != null)
                        data.professionData.traderData.discountData = discounts;
                    npc.setNpcData(data);
                    break;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}


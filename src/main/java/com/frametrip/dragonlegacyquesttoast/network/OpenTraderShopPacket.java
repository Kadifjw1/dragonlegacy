package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.profession.NpcProfessionType;
import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Server → Client: open the trader shop screen for a given NPC.
 */
public class OpenTraderShopPacket {

    private static final Gson GSON = new Gson();

    private final UUID   npcUuid;
    private final String npcDataJson;

    public OpenTraderShopPacket(UUID npcUuid, NpcEntityData data) {
        this.npcUuid     = npcUuid;
        this.npcDataJson = GSON.toJson(data);
    }

    private OpenTraderShopPacket(UUID npcUuid, String json) {
        this.npcUuid     = npcUuid;
        this.npcDataJson = json;
    }

    public static void encode(OpenTraderShopPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.npcUuid);
        buf.writeUtf(msg.npcDataJson, 131072);
    }

    public static OpenTraderShopPacket decode(FriendlyByteBuf buf) {
        return new OpenTraderShopPacket(buf.readUUID(), buf.readUtf(131072));
    }

    public static void handle(OpenTraderShopPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            NpcEntityData data = GSON.fromJson(msg.npcDataJson, NpcEntityData.class);
            if (data == null || data.professionData == null) return;
            if (data.professionData.type != NpcProfessionType.TRADER) return;
            mc.setScreen(new com.frametrip.dragonlegacyquesttoast.client.TraderShopScreen(
                    msg.npcUuid, data));
        });
        ctx.get().setPacketHandled(true);
    }
}

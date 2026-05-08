package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/** Server → Client: open companion control screen for the given NPC. */
public class OpenCompanionScreenPacket {

    private static final Gson GSON = new Gson();

    private final UUID   npcUuid;
    private final String npcDataJson;

    public OpenCompanionScreenPacket(UUID npcUuid, NpcEntityData data) {
        this.npcUuid     = npcUuid;
        this.npcDataJson = GSON.toJson(data);
    }

    private OpenCompanionScreenPacket(UUID uuid, String json) {
        this.npcUuid     = uuid;
        this.npcDataJson = json;
    }

    public static void encode(OpenCompanionScreenPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.npcUuid);
        buf.writeUtf(msg.npcDataJson, 131072);
    }

    public static OpenCompanionScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenCompanionScreenPacket(buf.readUUID(), buf.readUtf(131072));
    }

    public static void handle(OpenCompanionScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;
            AABB searchBox = mc.player.getBoundingBox().inflate(128);
            List<NpcEntity> found = mc.level.getEntitiesOfClass(
                    NpcEntity.class, searchBox, e -> e.getUUID().equals(msg.npcUuid));
            if (found.isEmpty()) return;
            mc.setScreen(new com.frametrip.dragonlegacyquesttoast.client.CompanionControlScreen(
                    found.get(0), msg.npcUuid));
        });
        ctx.get().setPacketHandled(true);
    }
}

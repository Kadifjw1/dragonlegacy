package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Server → client: open the companion control screen. */
public class OpenCompanionScreenPacket {

    private static final Gson GSON = new Gson();

    private final UUID npcUuid;

    public OpenCompanionScreenPacket(UUID npcUuid, NpcEntityData ignored) {
        this.npcUuid = npcUuid;
    }

    public static void encode(OpenCompanionScreenPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.npcUuid);
    }

    public static OpenCompanionScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenCompanionScreenPacket(buf.readUUID(), null);
    }

    public static void handle(OpenCompanionScreenPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> openScreen(msg.npcUuid))
        );
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void openScreen(UUID npcUuid) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof NpcEntity npc && npc.getUUID().equals(npcUuid)) {
                mc.setScreen(new com.frametrip.dragonlegacyquesttoast.client.CompanionControlScreen(
                        npc, npcUuid));
                return;
            }
        }
    }
}


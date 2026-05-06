package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.building.ClientBuildingState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Сервер → Клиент: краткое состояние строительства NPC.
 * Отправляется каждые 20 тиков для активных строек.
 */
public class SyncNpcBuildingStatePacket {

    private final UUID   npcId;
    private final String templateId;
    private final String status;       // BUILDING / PAUSED / DONE / CANCELLED
    private final int    totalBlocks;
    private final int    placedBlocks;

    public SyncNpcBuildingStatePacket(UUID npcId, String templateId, String status,
                                       int totalBlocks, int placedBlocks) {
        this.npcId        = npcId;
        this.templateId   = templateId;
        this.status       = status;
        this.totalBlocks  = totalBlocks;
        this.placedBlocks = placedBlocks;
    }

    public static void encode(SyncNpcBuildingStatePacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.npcId);
        buf.writeUtf(msg.templateId);
        buf.writeUtf(msg.status);
        buf.writeInt(msg.totalBlocks);
        buf.writeInt(msg.placedBlocks);
    }

    public static SyncNpcBuildingStatePacket decode(FriendlyByteBuf buf) {
        return new SyncNpcBuildingStatePacket(
                buf.readUUID(), buf.readUtf(), buf.readUtf(),
                buf.readInt(), buf.readInt());
    }

    public static void handle(SyncNpcBuildingStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                        ClientBuildingState.updateProgress(msg.npcId, msg.templateId,
                                msg.status, msg.totalBlocks, msg.placedBlocks)));
        ctx.get().setPacketHandled(true);
    }
}

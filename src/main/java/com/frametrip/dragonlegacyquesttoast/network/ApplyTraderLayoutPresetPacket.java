package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.profession.trader.TraderShopLayoutData;
import com.frametrip.dragonlegacyquesttoast.profession.trader.TraderShopLayoutPresetManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Client → Server: apply a named layout preset to an NPC trader. Creative-only. */
public class ApplyTraderLayoutPresetPacket {

    private final UUID   entityUuid;
    private final String presetId;

    public ApplyTraderLayoutPresetPacket(UUID entityUuid, String presetId) {
        this.entityUuid = entityUuid;
        this.presetId   = presetId;
    }

    public static void encode(ApplyTraderLayoutPresetPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.entityUuid);
        buf.writeUtf(msg.presetId, 64);
    }

    public static ApplyTraderLayoutPresetPacket decode(FriendlyByteBuf buf) {
        return new ApplyTraderLayoutPresetPacket(buf.readUUID(), buf.readUtf(64));
    }

    public static void handle(ApplyTraderLayoutPresetPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.getAbilities().instabuild) return;
            TraderShopLayoutData layout = TraderShopLayoutPresetManager.createLayoutFromPreset(msg.presetId);
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


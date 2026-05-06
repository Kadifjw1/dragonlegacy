package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.server.building.NpcBuildingManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Клиент → Сервер: управление строительством NPC.
 * action: "start" | "pause" | "resume" | "cancel"
 * templateId, ox, oy, oz используются только для "start".
 */
public class NpcBuildingActionPacket {

    private final UUID   npcId;
    private final String action;
    private final String templateId;
    private final int    ox, oy, oz;

    public NpcBuildingActionPacket(UUID npcId, String action, String templateId,
                                    int ox, int oy, int oz) {
        this.npcId      = npcId;
        this.action     = action;
        this.templateId = templateId;
        this.ox = ox; this.oy = oy; this.oz = oz;
    }

    public static void encode(NpcBuildingActionPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.npcId);
        buf.writeUtf(msg.action);
        buf.writeUtf(msg.templateId);
        buf.writeInt(msg.ox);
        buf.writeInt(msg.oy);
        buf.writeInt(msg.oz);
    }

    public static NpcBuildingActionPacket decode(FriendlyByteBuf buf) {
        return new NpcBuildingActionPacket(
                buf.readUUID(), buf.readUtf(), buf.readUtf(),
                buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void handle(NpcBuildingActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.getAbilities().instabuild) return;

            switch (msg.action) {
                case "start"  -> {
                    int ox = msg.ox, oy = msg.oy, oz = msg.oz;
                    // If origin is (0,0,0) from a scene action, resolve from the NPC entity position.
                    if (ox == 0 && oy == 0 && oz == 0) {
                        MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
                        if (srv != null) {
                            for (ServerLevel level : srv.getAllLevels()) {
                                Entity e = level.getEntity(msg.npcId);
                                if (e != null) {
                                    ox = e.getBlockX();
                                    oy = e.getBlockY();
                                    oz = e.getBlockZ();
                                    break;
                                }
                            }
                        }
                    }
                    NpcBuildingManager.startBuilding(msg.npcId, msg.templateId, ox, oy, oz);
                }
                case "pause"  -> NpcBuildingManager.pause(msg.npcId);
                case "resume" -> NpcBuildingManager.resume(msg.npcId);
                case "cancel" -> NpcBuildingManager.cancel(msg.npcId);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

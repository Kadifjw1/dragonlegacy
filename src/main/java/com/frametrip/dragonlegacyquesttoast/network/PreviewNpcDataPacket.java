package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.TickTask;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

// [EDT-5]: Client → Server: apply NPC data temporarily (auto-reverts after 10 s).
public class PreviewNpcDataPacket {

    private static final Gson GSON = new Gson();
    private static final Map<UUID, NpcEntityData> ORIGINALS = new HashMap<>();

    private final UUID   npcUuid;
    private final String dataJson;

    public PreviewNpcDataPacket(UUID npcUuid, NpcEntityData data) {
        this.npcUuid  = npcUuid;
        this.dataJson = GSON.toJson(data);
    }

    private PreviewNpcDataPacket(UUID uuid, String json) {
        this.npcUuid  = uuid;
        this.dataJson = json;
    }

    public static void encode(PreviewNpcDataPacket pkt, FriendlyByteBuf buf) {
        buf.writeUUID(pkt.npcUuid);
        buf.writeUtf(pkt.dataJson, 262144);
    }

    public static PreviewNpcDataPacket decode(FriendlyByteBuf buf) {
        return new PreviewNpcDataPacket(buf.readUUID(), buf.readUtf(262144));
    }

    public static void handle(PreviewNpcDataPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.getAbilities().instabuild) return;

            NpcEntityData previewData;
            try {
                previewData = GSON.fromJson(pkt.dataJson, NpcEntityData.class);
            } catch (Exception e) { return; }
            if (previewData == null) return;

            var server = player.getServer();
            if (server == null) return;

            for (ServerLevel level : server.getAllLevels()) {
                Entity e = level.getEntity(pkt.npcUuid);
                if (e instanceof NpcEntity npc) {
                    // Store original if not already previewing.
                    ORIGINALS.computeIfAbsent(pkt.npcUuid, k -> npc.getNpcData().copy());
                    npc.setNpcData(previewData);

                    // Schedule auto-revert after 200 ticks (10 seconds).
                    server.tell(new TickTask(server.getTickCount() + 200, () -> {
                        NpcEntityData original = ORIGINALS.remove(pkt.npcUuid);
                        if (original != null) {
                            // Find the NPC again (it may have moved to a different level reference).
                            for (ServerLevel sl : server.getAllLevels()) {
                                Entity npcE = sl.getEntity(pkt.npcUuid);
                                if (npcE instanceof NpcEntity n) {
                                    n.setNpcData(original);
                                    break;
                                }
                            }
                        }
                    }));

                    player.sendSystemMessage(
                            net.minecraft.network.chat.Component.literal(
                                    "§e[EDT-5] §fПревью применено. §7Авто-откат через 10 с."));
                    return;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    // Called when the player explicitly saves — cancels the pending revert.
    public static void cancelRevert(UUID npcUuid) {
        ORIGINALS.remove(npcUuid);
    }
}

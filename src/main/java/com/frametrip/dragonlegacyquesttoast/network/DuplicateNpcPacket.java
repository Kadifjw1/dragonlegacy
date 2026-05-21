package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

// [EDT-1]: Client → Server: duplicate the NPC with the given UUID.
public class DuplicateNpcPacket {

    private static final Gson GSON = new Gson();
    private final UUID npcUuid;

    public DuplicateNpcPacket(UUID npcUuid) {
        this.npcUuid = npcUuid;
    }

    private DuplicateNpcPacket(UUID uuid, boolean dummy) {
        this.npcUuid = uuid;
    }

    public static void encode(DuplicateNpcPacket pkt, FriendlyByteBuf buf) {
        buf.writeUUID(pkt.npcUuid);
    }

    public static DuplicateNpcPacket decode(FriendlyByteBuf buf) {
        return new DuplicateNpcPacket(buf.readUUID(), false);
    }

    public static void handle(DuplicateNpcPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.getAbilities().instabuild) return;

            var server = player.getServer();
            if (server == null) return;

            for (ServerLevel level : server.getAllLevels()) {
                Entity e = level.getEntity(pkt.npcUuid);
                if (e instanceof NpcEntity original) {
                    NpcEntityData dataCopy = original.getNpcData().copy();
                    // Clear identity-specific fields on the copy.
                    dataCopy.stats = new com.frametrip.dragonlegacyquesttoast.server.stats.NpcStatisticsData();
                    dataCopy.creatorUUID = player.getStringUUID();

                    NpcEntity copy = com.frametrip.dragonlegacyquesttoast.registry.ModEntities.NPC.get().create(level);
                    if (copy == null) return;
                    copy.moveTo(original.getX() + 2, original.getY(), original.getZ(),
                            original.getYRot(), 0f);
                    copy.setNpcData(dataCopy);
                    level.addFreshEntity(copy);
                    copy.finalizeSpawn(level, level.getCurrentDifficultyAt(copy.blockPosition()),
                            MobSpawnType.MOB_SUMMONED, null, null);

                    player.sendSystemMessage(Component.literal(
                            "§a[EDT-1] §fNPC «" + dataCopy.displayName + "» §aскопирован."));
                    return;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

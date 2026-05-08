package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.server.companion.CompanionData;
import com.frametrip.dragonlegacyquesttoast.server.companion.CompanionMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/** Client → Server: update companion mode and parameters for an NPC. */
public class SetCompanionModePacket {

    private final UUID          npcUuid;
    private final CompanionMode mode;
    private final float         followDistance;
    private final float         aggressiveness;
    private final float         guardRadius;

    public SetCompanionModePacket(UUID npcUuid, CompanionMode mode,
                                  float followDistance, float aggressiveness, float guardRadius) {
        this.npcUuid        = npcUuid;
        this.mode           = mode;
        this.followDistance = followDistance;
        this.aggressiveness = aggressiveness;
        this.guardRadius    = guardRadius;
    }

    public static void encode(SetCompanionModePacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.npcUuid);
        buf.writeUtf(msg.mode.name());
        buf.writeFloat(msg.followDistance);
        buf.writeFloat(msg.aggressiveness);
        buf.writeFloat(msg.guardRadius);
    }

    public static SetCompanionModePacket decode(FriendlyByteBuf buf) {
        UUID   uuid = buf.readUUID();
        CompanionMode mode = CompanionMode.valueOf(buf.readUtf());
        float fd = buf.readFloat();
        float ag = buf.readFloat();
        float gr = buf.readFloat();
        return new SetCompanionModePacket(uuid, mode, fd, ag, gr);
    }

    public static void handle(SetCompanionModePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            for (ServerLevel level : player.getServer().getAllLevels()) {
                AABB big = new AABB(-30000000, -320, -30000000, 30000000, 320, 30000000);
                List<NpcEntity> found = level.getEntitiesOfClass(NpcEntity.class, big,
                        e -> e.getUUID().equals(msg.npcUuid));
                if (found.isEmpty()) continue;
                NpcEntity npc = found.get(0);
                NpcEntityData data = npc.getNpcData();
                if (data.companionData == null) data.companionData = new CompanionData();
                data.companionData.mode           = msg.mode;
                data.companionData.followDistance = msg.followDistance;
                data.companionData.aggressiveness = msg.aggressiveness;
                data.companionData.guardRadius    = msg.guardRadius;
                npc.setNpcData(data);
                break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

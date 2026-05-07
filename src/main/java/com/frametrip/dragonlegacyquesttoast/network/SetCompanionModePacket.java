package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.server.companion.CompanionMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Sent by client when player changes companion mode via CompanionControlScreen. */
public class SetCompanionModePacket {

    private final UUID npcUuid;
    private final CompanionMode mode;
    private final float followDistance;
    private final float aggressiveness;
    private final float guardRadius;

    public SetCompanionModePacket(UUID npcUuid, CompanionMode mode,
                                   float followDistance, float aggressiveness, float guardRadius) {
        this.npcUuid         = npcUuid;
        this.mode            = mode;
        this.followDistance  = followDistance;
        this.aggressiveness  = aggressiveness;
        this.guardRadius     = guardRadius;
    }

    public static void encode(SetCompanionModePacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.npcUuid);
        buf.writeEnum(msg.mode);
        buf.writeFloat(msg.followDistance);
        buf.writeFloat(msg.aggressiveness);
        buf.writeFloat(msg.guardRadius);
    }

    public static SetCompanionModePacket decode(FriendlyByteBuf buf) {
        return new SetCompanionModePacket(
                buf.readUUID(),
                buf.readEnum(CompanionMode.class),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat()
        );
    }

    public static void handle(SetCompanionModePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            for (ServerLevel level : player.getServer().getAllLevels()) {
                Entity e = level.getEntity(msg.npcUuid);
                if (!(e instanceof NpcEntity npc)) continue;

                var data = npc.getNpcData();
                var cd   = data.companionData;
                cd.setMode(msg.mode);
                cd.followDistance  = msg.followDistance;
                cd.aggressiveness  = msg.aggressiveness;
                cd.guardRadius     = msg.guardRadius;
                cd.ownerUUID       = player.getUUID().toString();

                // Set guard point to current NPC position when switching to GUARD
                if (msg.mode == CompanionMode.GUARD && !cd.guardPointSet) {
                    cd.guardX = npc.getX();
                    cd.guardY = npc.getY();
                    cd.guardZ = npc.getZ();
                    cd.guardPointSet = true;
                }

                npc.setNpcData(data);
                break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.server.animation.AnimationState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Client → Server: set or clear a forced animation state on an NPC.
 *
 * Sent by NpcSceneController when executing ACTION_PLAY_ANIM_STATE or
 * ACTION_STOP_ANIMATION scene nodes.
 *
 * animStateName == "" means clear (return to AUTO).
 */
public class NpcAnimStatePacket {

    private final UUID   npcUuid;
    private final String animStateName; // AnimationState.name() or "" to clear

    public NpcAnimStatePacket(UUID npcUuid, String animStateName) {
        this.npcUuid       = npcUuid;
        this.animStateName = animStateName == null ? "" : animStateName;
    }

    public static void encode(NpcAnimStatePacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.npcUuid);
        buf.writeUtf(msg.animStateName, 64);
    }

    public static NpcAnimStatePacket decode(FriendlyByteBuf buf) {
        return new NpcAnimStatePacket(buf.readUUID(), buf.readUtf(64));
    }

    public static void handle(NpcAnimStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            net.minecraft.server.MinecraftServer server = player.getServer();
            if (server == null) return;

            for (ServerLevel level : server.getAllLevels()) {
                Entity e = level.getEntity(msg.npcUuid);
                if (e instanceof NpcEntity npc) {
                    if (msg.animStateName.isEmpty()) {
                        npc.clearAnimState();
                    } else {
                        try {
                            AnimationState state = AnimationState.valueOf(msg.animStateName.toUpperCase());
                            npc.setAnimState(state);
                        } catch (IllegalArgumentException ignored) {
                            // unknown state name — ignore
                        }
                    }
                    break;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

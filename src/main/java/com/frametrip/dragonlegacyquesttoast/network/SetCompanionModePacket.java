package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.server.companion.CompanionData;
import com.frametrip.dragonlegacyquesttoast.server.companion.CompanionMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/** Client → Server: update companion mode and all parameters for an NPC. */
public class SetCompanionModePacket {

    private final UUID          npcUuid;
    private final CompanionMode mode;
    private final float         followDistance;
    private final float         aggressiveness;
    private final float         guardRadius;
    private final boolean       bindOwner;       // true = assign sender as owner
    private final boolean       setGuardHere;    // true = use NPC current pos as guard point
    private final Map<String, String> commands;  // mode-name → chat phrase

    public SetCompanionModePacket(UUID npcUuid, CompanionMode mode,
                                  float followDistance, float aggressiveness, float guardRadius,
                                  boolean bindOwner, boolean setGuardHere,
                                  Map<String, String> commands) {
        this.npcUuid        = npcUuid;
        this.mode           = mode;
        this.followDistance = followDistance;
        this.aggressiveness = aggressiveness;
        this.guardRadius    = guardRadius;
        this.bindOwner      = bindOwner;
        this.setGuardHere   = setGuardHere;
        this.commands       = commands;
    }

    public static void encode(SetCompanionModePacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.npcUuid);
        buf.writeUtf(msg.mode.name());
        buf.writeFloat(msg.followDistance);
        buf.writeFloat(msg.aggressiveness);
        buf.writeFloat(msg.guardRadius);
        buf.writeBoolean(msg.bindOwner);
        buf.writeBoolean(msg.setGuardHere);
        buf.writeVarInt(msg.commands.size());
        msg.commands.forEach((k, v) -> { buf.writeUtf(k); buf.writeUtf(v); });
    }

    public static SetCompanionModePacket decode(FriendlyByteBuf buf) {
        UUID uuid          = buf.readUUID();
        CompanionMode mode = CompanionMode.valueOf(buf.readUtf());
        float fd           = buf.readFloat();
        float ag           = buf.readFloat();
        float gr           = buf.readFloat();
        boolean bind       = buf.readBoolean();
        boolean guard      = buf.readBoolean();
        int cmdCount       = buf.readVarInt();
        Map<String, String> cmds = new HashMap<>();
        for (int i = 0; i < cmdCount; i++) cmds.put(buf.readUtf(), buf.readUtf());
        return new SetCompanionModePacket(uuid, mode, fd, ag, gr, bind, guard, cmds);
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
                CompanionData cd = data.companionData;

                cd.mode           = msg.mode;
                cd.followDistance = msg.followDistance;
                cd.aggressiveness = msg.aggressiveness;
                cd.guardRadius    = msg.guardRadius;
                cd.modeCommands   = msg.commands;

                if (msg.bindOwner) {
                    cd.ownerUUID = player.getUUID().toString();
                }
                if (msg.setGuardHere) {
                    cd.guardX         = npc.getX();
                    cd.guardY         = npc.getY();
                    cd.guardZ         = npc.getZ();
                    cd.guardPointSet  = true;
                }

                npc.setNpcData(data);
                break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

package com.frametrip.dragonlegacyquesttoast.network;
 
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
 
import java.util.UUID;
import java.util.function.Supplier;
 
public class SaveNpcEntityDataPacket {
 
    private static final Gson GSON = new Gson();
 
    private final UUID entityUuid;
    private final String dataJson;
 
    public SaveNpcEntityDataPacket(UUID entityUuid, NpcEntityData data) {
        this.entityUuid = entityUuid;
        this.dataJson   = GSON.toJson(data);
    }
 
    private SaveNpcEntityDataPacket(UUID entityUuid, String dataJson) {
        this.entityUuid = entityUuid;
        this.dataJson   = dataJson;
    }
 
    public static void encode(SaveNpcEntityDataPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.entityUuid);
        buf.writeUtf(msg.dataJson, 65535);
    }
 
    public static SaveNpcEntityDataPacket decode(FriendlyByteBuf buf) {
        return new SaveNpcEntityDataPacket(buf.readUUID(), buf.readUtf(65535));
    }
 
    public static void handle(SaveNpcEntityDataPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.getAbilities().instabuild) return;
 
            NpcEntityData data = GSON.fromJson(msg.dataJson, NpcEntityData.class);
            if (data == null) return;
 
            net.minecraft.server.MinecraftServer server = player.getServer();
            if (server == null) return;
 
            for (ServerLevel level : server.getAllLevels()) {
                Entity e = level.getEntity(msg.entityUuid);
                if (e instanceof NpcEntity npc) {
                    npc.setNpcData(data);
                    break;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

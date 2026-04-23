package com.frametrip.dragonlegacyquesttoast.network;
 
import com.frametrip.dragonlegacyquesttoast.entity.FactionData;
import com.frametrip.dragonlegacyquesttoast.server.FactionManager;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
 
import java.util.function.Supplier;
 
public class SaveFactionPacket {
 
    private static final Gson GSON = new Gson();
    private final String json;
    private final boolean delete;
 
    public SaveFactionPacket(FactionData faction, boolean delete) {
        this.json   = GSON.toJson(faction);
        this.delete = delete;
    }
 
    private SaveFactionPacket(String json, boolean delete) {
        this.json   = json;
        this.delete = delete;
    }
 
    public static void encode(SaveFactionPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.json, 32767);
        buf.writeBoolean(msg.delete);
    }
 
    public static SaveFactionPacket decode(FriendlyByteBuf buf) {
        return new SaveFactionPacket(buf.readUtf(32767), buf.readBoolean());
    }
 
    public static void handle(SaveFactionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.getAbilities().instabuild) return;
 
            FactionData faction = GSON.fromJson(msg.json, FactionData.class);
            if (faction == null || faction.id == null) return;
 
            if (msg.delete) {
                FactionManager.delete(faction.id);
            } else {
                FactionManager.save(faction);
            }
 
            ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncFactionsPacket(FactionManager.getAll())
            );
        });
        ctx.get().setPacketHandled(true);
    }
}

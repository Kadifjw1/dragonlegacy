package com.frametrip.dragonlegacyquesttoast.network;
 
import com.frametrip.dragonlegacyquesttoast.server.NpcProfile;
import com.frametrip.dragonlegacyquesttoast.server.NpcProfileManager;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
 
import java.util.function.Supplier;
 
public class SaveNpcProfilePacket {
 
    private static final Gson GSON = new Gson();
    private final String profileJson;
    private final boolean delete;
 
    public SaveNpcProfilePacket(NpcProfile profile, boolean delete) {
        this.profileJson = GSON.toJson(profile);
        this.delete = delete;
    }
 
    private SaveNpcProfilePacket(String json, boolean delete) {
        this.profileJson = json;
        this.delete = delete;
    }
 
    public static void encode(SaveNpcProfilePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.profileJson, 32767);
        buf.writeBoolean(msg.delete);
    }
 
    public static SaveNpcProfilePacket decode(FriendlyByteBuf buf) {
        return new SaveNpcProfilePacket(buf.readUtf(32767), buf.readBoolean());
    }
 
    public static void handle(SaveNpcProfilePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.getAbilities().instabuild) return;
 
            NpcProfile profile = GSON.fromJson(msg.profileJson, NpcProfile.class);
            if (profile == null || profile.id == null) return;
 
            if (msg.delete) {
                NpcProfileManager.delete(profile.id);
            } else {
                NpcProfileManager.save(profile);
            }
 
            ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncNpcProfilesPacket(NpcProfileManager.getAll())
            );
        });
        ctx.get().setPacketHandled(true);
    }
}

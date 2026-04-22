package com.frametrip.dragonlegacyquesttoast.network;
 
import com.frametrip.dragonlegacyquesttoast.client.ClientNpcProfileState;
import com.frametrip.dragonlegacyquesttoast.server.NpcProfile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
 
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Supplier;
 
public class SyncNpcProfilesPacket {
 
    private static final Gson GSON = new Gson();
    private final String json;
 
    public SyncNpcProfilesPacket(List<NpcProfile> profiles) {
        this.json = GSON.toJson(profiles);
    }
 
    private SyncNpcProfilesPacket(String json) {
        this.json = json;
    }
 
    public static void encode(SyncNpcProfilesPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.json, 65535);
    }
 
    public static SyncNpcProfilesPacket decode(FriendlyByteBuf buf) {
        return new SyncNpcProfilesPacket(buf.readUtf(65535));
    }
 
    public static void handle(SyncNpcProfilesPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Type t = new TypeToken<List<NpcProfile>>() {}.getType();
            List<NpcProfile> list = GSON.fromJson(msg.json, t);
            ClientNpcProfileState.sync(list);
        });
        ctx.get().setPacketHandled(true);
    }
}

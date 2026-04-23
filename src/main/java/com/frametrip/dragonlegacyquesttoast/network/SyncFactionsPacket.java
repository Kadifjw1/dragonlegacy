package com.frametrip.dragonlegacyquesttoast.network;
 
import com.frametrip.dragonlegacyquesttoast.client.ClientFactionState;
import com.frametrip.dragonlegacyquesttoast.entity.FactionData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
 
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Supplier;
 
public class SyncFactionsPacket {
 
    private static final Gson GSON = new Gson();
    private final String json;
 
    public SyncFactionsPacket(List<FactionData> factions) {
        this.json = GSON.toJson(factions);
    }
 
    private SyncFactionsPacket(String json) {
        this.json = json;
    }
 
    public static void encode(SyncFactionsPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.json, 65535);
    }
 
    public static SyncFactionsPacket decode(FriendlyByteBuf buf) {
        return new SyncFactionsPacket(buf.readUtf(65535));
    }
 
    public static void handle(SyncFactionsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Type t = new TypeToken<List<FactionData>>() {}.getType();
            ClientFactionState.sync(GSON.fromJson(msg.json, t));
        });
        ctx.get().setPacketHandled(true);
    }
}

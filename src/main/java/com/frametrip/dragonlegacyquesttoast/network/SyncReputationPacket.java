package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.ClientReputationState;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Supplier;

// [REL-2]: Syncs player faction reputations from server to client.
public class SyncReputationPacket {

    private static final Gson GSON = new Gson();
    private final String json;

    public SyncReputationPacket(Map<String, Integer> reputations) {
        this.json = GSON.toJson(reputations);
    }

    private SyncReputationPacket(String json) {
        this.json = json;
    }

    public static void encode(SyncReputationPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.json, 65535);
    }

    public static SyncReputationPacket decode(FriendlyByteBuf buf) {
        return new SyncReputationPacket(buf.readUtf(65535));
    }

    public static void handle(SyncReputationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Type t = new TypeToken<Map<String, Integer>>() {}.getType();
            ClientReputationState.sync(GSON.fromJson(msg.json, t));
        });
        ctx.get().setPacketHandled(true);
    }
}

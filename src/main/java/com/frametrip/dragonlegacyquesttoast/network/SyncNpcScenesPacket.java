package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.dialogue.ClientNpcSceneState;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcScene;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Supplier;

public class SyncNpcScenesPacket {

    private static final Gson GSON = new Gson();
    private final String json;

    public SyncNpcScenesPacket(List<NpcScene> scenes) {
        this.json = GSON.toJson(scenes);
    }

    private SyncNpcScenesPacket(String json) {
        this.json = json;
    }

    public static void encode(SyncNpcScenesPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.json, 65535);
    }

    public static SyncNpcScenesPacket decode(FriendlyByteBuf buf) {
        return new SyncNpcScenesPacket(buf.readUtf(65535));
    }

    public static void handle(SyncNpcScenesPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(msg));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(SyncNpcScenesPacket msg) {
        Type t = new TypeToken<List<NpcScene>>() {}.getType();
        List<NpcScene> scenes = GSON.fromJson(msg.json, t);
        ClientNpcSceneState.sync(scenes);
    }
}

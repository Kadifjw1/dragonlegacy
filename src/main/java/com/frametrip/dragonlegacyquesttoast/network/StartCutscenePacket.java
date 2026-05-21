package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.cutscene.CutscenePlayer;
import com.frametrip.dragonlegacyquesttoast.server.cutscene.CutsceneDefinition;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// S→C: triggers a cutscene on the client
public class StartCutscenePacket {
    private static final Gson GSON = new Gson();
    private final String cutsceneJson;

    public StartCutscenePacket(CutsceneDefinition def) {
        this.cutsceneJson = GSON.toJson(def);
    }

    private StartCutscenePacket(String json) {
        this.cutsceneJson = json;
    }

    public static void encode(StartCutscenePacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.cutsceneJson);
    }

    public static StartCutscenePacket decode(FriendlyByteBuf buf) {
        return new StartCutscenePacket(buf.readUtf(65536));
    }

    public static void handle(StartCutscenePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            CutsceneDefinition def = GSON.fromJson(pkt.cutsceneJson, CutsceneDefinition.class);
            if (def != null) {
                CutscenePlayer.start(def);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

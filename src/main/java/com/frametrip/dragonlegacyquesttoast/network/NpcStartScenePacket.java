package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.dialogue.NpcSceneController;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server → Client: start a scene for the player. */
public class NpcStartScenePacket {

    private static final Gson GSON = new Gson();

    private final String npcName;
    private final String sceneId;

    public NpcStartScenePacket(String npcName, String sceneId) {
        this.npcName = npcName;
        this.sceneId = sceneId;
    }

    public static void encode(NpcStartScenePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.npcName);
        buf.writeUtf(msg.sceneId);
    }

    public static NpcStartScenePacket decode(FriendlyByteBuf buf) {
        return new NpcStartScenePacket(buf.readUtf(), buf.readUtf());
    }

    public static void handle(NpcStartScenePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(msg));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(NpcStartScenePacket msg) {
        NpcSceneController.startScene(msg.npcName, msg.sceneId);
    }
}

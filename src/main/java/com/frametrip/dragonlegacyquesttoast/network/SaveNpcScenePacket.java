package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcScene;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcSceneManager;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SaveNpcScenePacket {

    private static final Gson GSON = new Gson();

    private final String sceneJson;
    private final boolean delete;

    public SaveNpcScenePacket(NpcScene scene, boolean delete) {
        this.sceneJson = GSON.toJson(scene);
        this.delete = delete;
    }

    private SaveNpcScenePacket(String json, boolean delete) {
        this.sceneJson = json;
        this.delete = delete;
    }

    public static void encode(SaveNpcScenePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.sceneJson, 65535);
        buf.writeBoolean(msg.delete);
    }

    public static SaveNpcScenePacket decode(FriendlyByteBuf buf) {
        return new SaveNpcScenePacket(buf.readUtf(65535), buf.readBoolean());
    }

    public static void handle(SaveNpcScenePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.getAbilities().instabuild) return;

            NpcScene scene = GSON.fromJson(msg.sceneJson, NpcScene.class);
            if (scene == null) return;

            if (msg.delete) {
                NpcSceneManager.deleteScene(scene.id);
            } else {
                NpcSceneManager.saveScene(scene);
            }

            // broadcast updated list to all players
            java.util.List<NpcScene> all = NpcSceneManager.getAll();
            for (net.minecraft.server.level.ServerLevel level : player.getServer().getAllLevels()) {
                for (ServerPlayer sp : level.players()) {
                    ModNetwork.CHANNEL.send(
                            net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> sp),
                            new SyncNpcScenesPacket(all)
                    );
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

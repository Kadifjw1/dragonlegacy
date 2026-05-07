package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.ClientPresetCache;
import com.frametrip.dragonlegacyquesttoast.server.animation.NpcAnimationData;
import com.frametrip.dragonlegacyquesttoast.server.gui.GuiTemplate;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Supplier;

/** Server → Client: syncs data-pack animation presets and GUI template presets. */
public class SyncDataPresetsPacket {

    private static final Gson GSON = new Gson();
    private final String animJson;
    private final String guiJson;

    public SyncDataPresetsPacket(List<NpcAnimationData> anims, List<GuiTemplate> guis) {
        this.animJson = GSON.toJson(anims);
        this.guiJson  = GSON.toJson(guis);
    }

    private SyncDataPresetsPacket(String animJson, String guiJson) {
        this.animJson = animJson;
        this.guiJson  = guiJson;
    }

    public static void encode(SyncDataPresetsPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.animJson, 131072);
        buf.writeUtf(msg.guiJson,  65535);
    }

    public static SyncDataPresetsPacket decode(FriendlyByteBuf buf) {
        return new SyncDataPresetsPacket(buf.readUtf(131072), buf.readUtf(65535));
    }

    public static void handle(SyncDataPresetsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(msg));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(SyncDataPresetsPacket msg) {
        Type animType = new TypeToken<List<NpcAnimationData>>() {}.getType();
        Type guiType  = new TypeToken<List<GuiTemplate>>()       {}.getType();
        List<NpcAnimationData> anims = GSON.fromJson(msg.animJson, animType);
        List<GuiTemplate>       guis = GSON.fromJson(msg.guiJson,  guiType);
        ClientPresetCache.sync(anims != null ? anims : List.of(),
                               guis  != null ? guis  : List.of());
    }
}

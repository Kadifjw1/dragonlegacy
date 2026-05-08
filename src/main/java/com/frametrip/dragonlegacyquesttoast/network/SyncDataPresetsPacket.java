package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.server.data.DataPackManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Server → Client: sync data-pack preset lists. */
public class SyncDataPresetsPacket {

    private final List<String> animationPresets;
    private final List<String> guiPresets;

    public SyncDataPresetsPacket(List<String> animationPresets, List<String> guiPresets) {
        this.animationPresets = animationPresets;
        this.guiPresets       = guiPresets;
    }

    public static void encode(SyncDataPresetsPacket msg, FriendlyByteBuf buf) {
        buf.writeCollection(msg.animationPresets, FriendlyByteBuf::writeUtf);
        buf.writeCollection(msg.guiPresets,       FriendlyByteBuf::writeUtf);
    }

    public static SyncDataPresetsPacket decode(FriendlyByteBuf buf) {
        List<String> anim = buf.readList(FriendlyByteBuf::readUtf);
        List<String> gui  = buf.readList(FriendlyByteBuf::readUtf);
        return new SyncDataPresetsPacket(anim, gui);
    }

    public static void handle(SyncDataPresetsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DataPackManager.animationPresets = new ArrayList<>(msg.animationPresets);
            DataPackManager.guiPresets       = new ArrayList<>(msg.guiPresets);
        });
        ctx.get().setPacketHandled(true);
    }
}

package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.NpcDialogueEditorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenNpcDialogueEditorPacket {
    public OpenNpcDialogueEditorPacket() {
    }

    public static void encode(OpenNpcDialogueEditorPacket msg, FriendlyByteBuf buf) {
    }

    public static OpenNpcDialogueEditorPacket decode(FriendlyByteBuf buf) {
        return new OpenNpcDialogueEditorPacket();
    }

    public static void handle(OpenNpcDialogueEditorPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> Minecraft.getInstance().setScreen(new NpcDialogueEditorScreen(null)));
        ctx.setPacketHandled(true);
    }
}

package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.UiEditorMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenUiEditorMenuPacket {
    public OpenUiEditorMenuPacket() {
    }

    public static void encode(OpenUiEditorMenuPacket msg, FriendlyByteBuf buf) {
    }

    public static OpenUiEditorMenuPacket decode(FriendlyByteBuf buf) {
        return new OpenUiEditorMenuPacket();
    }

    public static void handle(OpenUiEditorMenuPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> Minecraft.getInstance().setScreen(new UiEditorMenuScreen(null)));
        ctx.setPacketHandled(true);
    }
}

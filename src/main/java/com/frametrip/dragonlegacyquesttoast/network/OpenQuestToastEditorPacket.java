package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.QuestToastEditorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenQuestToastEditorPacket {
    public OpenQuestToastEditorPacket() {
    }

    public static void encode(OpenQuestToastEditorPacket msg, FriendlyByteBuf buf) {
    }

    public static OpenQuestToastEditorPacket decode(FriendlyByteBuf buf) {
        return new OpenQuestToastEditorPacket();
    }

    public static void handle(OpenQuestToastEditorPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> Minecraft.getInstance().setScreen(new QuestToastEditorScreen(null)));
        ctx.setPacketHandled(true);
    }
}

package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.AwakeningMainScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenAwakeningScreenPacket {
    public OpenAwakeningScreenPacket() {
    }

    public static void encode(OpenAwakeningScreenPacket msg, FriendlyByteBuf buf) {
    }

    public static OpenAwakeningScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenAwakeningScreenPacket();
    }

    public static void handle(OpenAwakeningScreenPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> Minecraft.getInstance().setScreen(new AwakeningMainScreen()));
        ctx.setPacketHandled(true);
    }
}

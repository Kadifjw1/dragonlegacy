package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.AbilityToggleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenAbilityToggleScreenPacket {
    public OpenAbilityToggleScreenPacket() {
    }

    public static void encode(OpenAbilityToggleScreenPacket msg, FriendlyByteBuf buf) {
    }

    public static OpenAbilityToggleScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenAbilityToggleScreenPacket();
    }

    public static void handle(OpenAbilityToggleScreenPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> Minecraft.getInstance().setScreen(new AbilityToggleScreen()));
        ctx.setPacketHandled(true);
    }
}

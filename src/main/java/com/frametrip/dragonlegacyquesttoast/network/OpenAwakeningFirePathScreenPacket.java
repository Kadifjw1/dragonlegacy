package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.AwakeningPathDetailScreen;
import com.frametrip.dragonlegacyquesttoast.client.AwakeningPathType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenAwakeningFirePathScreenPacket {
    public OpenAwakeningFirePathScreenPacket() {
    }

    public static void encode(OpenAwakeningFirePathScreenPacket msg, FriendlyByteBuf buf) {
    }

    public static OpenAwakeningFirePathScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenAwakeningFirePathScreenPacket();
    }

    public static void handle(OpenAwakeningFirePathScreenPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> Minecraft.getInstance().setScreen(new AwakeningPathDetailScreen(null, AwakeningPathType.FIRE)));
        ctx.setPacketHandled(true);
    }
}

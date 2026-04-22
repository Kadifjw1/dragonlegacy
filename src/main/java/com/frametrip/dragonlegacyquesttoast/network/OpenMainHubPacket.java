package com.frametrip.dragonlegacyquesttoast.network;
 
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
 
import java.util.function.Supplier;
 
public class OpenMainHubPacket {
 
    public static void encode(OpenMainHubPacket msg, FriendlyByteBuf buf) {}
 
    public static OpenMainHubPacket decode(FriendlyByteBuf buf) {
        return new OpenMainHubPacket();
    }
 
    public static void handle(OpenMainHubPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.setScreen(new com.frametrip.dragonlegacyquesttoast.client.MainHubMenuScreen());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

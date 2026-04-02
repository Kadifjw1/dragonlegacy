package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.ClientAwakeningScreenState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AwakeningBackgroundConfigPacket {
    private final boolean reset;
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public AwakeningBackgroundConfigPacket(boolean reset, int x, int y, int width, int height) {
        this.reset = reset;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static void encode(AwakeningBackgroundConfigPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.reset);
        buf.writeInt(msg.x);
        buf.writeInt(msg.y);
        buf.writeInt(msg.width);
        buf.writeInt(msg.height);
    }

    public static AwakeningBackgroundConfigPacket decode(FriendlyByteBuf buf) {
        return new AwakeningBackgroundConfigPacket(
                buf.readBoolean(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt()
        );
    }

    public static void handle(AwakeningBackgroundConfigPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (msg.reset) {
                ClientAwakeningScreenState.resetBackgroundConfig();
            } else {
                ClientAwakeningScreenState.applyBackgroundConfig(msg.x, msg.y, msg.width, msg.height);
            }
        });
        ctx.setPacketHandled(true);
    }
}

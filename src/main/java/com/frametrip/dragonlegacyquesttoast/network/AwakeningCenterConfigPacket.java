package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.ClientAwakeningScreenState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AwakeningCenterConfigPacket {
    private final boolean reset;
    private final int frameX;
    private final int frameY;
    private final int frameWidth;
    private final int frameHeight;
    private final int playerOffsetX;
    private final int playerOffsetY;
    private final float playerScale;

    public AwakeningCenterConfigPacket(boolean reset, int frameX, int frameY, int frameWidth, int frameHeight,
                                       int playerOffsetX, int playerOffsetY, float playerScale) {
        this.reset = reset;
        this.frameX = frameX;
        this.frameY = frameY;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.playerOffsetX = playerOffsetX;
        this.playerOffsetY = playerOffsetY;
        this.playerScale = playerScale;
    }

    public static void encode(AwakeningCenterConfigPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.reset);
        buf.writeInt(msg.frameX);
        buf.writeInt(msg.frameY);
        buf.writeInt(msg.frameWidth);
        buf.writeInt(msg.frameHeight);
        buf.writeInt(msg.playerOffsetX);
        buf.writeInt(msg.playerOffsetY);
        buf.writeFloat(msg.playerScale);
    }

    public static AwakeningCenterConfigPacket decode(FriendlyByteBuf buf) {
        return new AwakeningCenterConfigPacket(
                buf.readBoolean(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readFloat()
        );
    }

    public static void handle(AwakeningCenterConfigPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (msg.reset) {
                ClientAwakeningScreenState.resetCenterConfig();
            } else {
                ClientAwakeningScreenState.applyCenterConfig(
                        msg.frameX,
                        msg.frameY,
                        msg.frameWidth,
                        msg.frameHeight,
                        msg.playerOffsetX,
                        msg.playerOffsetY,
                        msg.playerScale
                );
            }
        });
        ctx.setPacketHandled(true);
    }
}

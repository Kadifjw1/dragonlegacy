package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.ClientAwakeningScreenState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AwakeningPathsConfigPacket {
    private final boolean reset;
    private final int pathFrameSize;
    private final int pathIconSize;
    private final int fireX;
    private final int fireY;
    private final int iceX;
    private final int iceY;
    private final int stormX;
    private final int stormY;
    private final int voidX;
    private final int voidY;

    public AwakeningPathsConfigPacket(boolean reset,
                                      int pathFrameSize, int pathIconSize,
                                      int fireX, int fireY,
                                      int iceX, int iceY,
                                      int stormX, int stormY,
                                      int voidX, int voidY) {
        this.reset = reset;
        this.pathFrameSize = pathFrameSize;
        this.pathIconSize = pathIconSize;
        this.fireX = fireX;
        this.fireY = fireY;
        this.iceX = iceX;
        this.iceY = iceY;
        this.stormX = stormX;
        this.stormY = stormY;
        this.voidX = voidX;
        this.voidY = voidY;
    }

    public static void encode(AwakeningPathsConfigPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.reset);
        buf.writeInt(msg.pathFrameSize);
        buf.writeInt(msg.pathIconSize);
        buf.writeInt(msg.fireX);
        buf.writeInt(msg.fireY);
        buf.writeInt(msg.iceX);
        buf.writeInt(msg.iceY);
        buf.writeInt(msg.stormX);
        buf.writeInt(msg.stormY);
        buf.writeInt(msg.voidX);
        buf.writeInt(msg.voidY);
    }

    public static AwakeningPathsConfigPacket decode(FriendlyByteBuf buf) {
        return new AwakeningPathsConfigPacket(
                buf.readBoolean(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt()
        );
    }

    public static void handle(AwakeningPathsConfigPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (msg.reset) {
                ClientAwakeningScreenState.resetPathsConfig();
            } else {
                ClientAwakeningScreenState.applyPathsConfig(
                        msg.pathFrameSize,
                        msg.pathIconSize,
                        msg.fireX, msg.fireY,
                        msg.iceX, msg.iceY,
                        msg.stormX, msg.stormY,
                        msg.voidX, msg.voidY
                );
            }
        });
        ctx.setPacketHandled(true);
    }
}

package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.ClientNpcDialogueManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class NpcDialogueConfigPacket {
    private final boolean reset;
    private final int x;
    private final int yOffsetFromBottom;
    private final int width;
    private final int height;
    private final int fadeInTicks;
    private final int stayTicks;
    private final int fadeOutTicks;
    private final int textMaxCharsPerLine;
    private final int textMaxLines;
    private final int nameXOffset;
    private final int nameYOffset;
    private final int textXOffset;
    private final int textYOffset;
    private final int textLineHeight;

    public NpcDialogueConfigPacket(
            boolean reset,
            int x,
            int yOffsetFromBottom,
            int width,
            int height,
            int fadeInTicks,
            int stayTicks,
            int fadeOutTicks,
            int textMaxCharsPerLine,
            int textMaxLines,
            int nameXOffset,
            int nameYOffset,
            int textXOffset,
            int textYOffset,
            int textLineHeight
    ) {
        this.reset = reset;
        this.x = x;
        this.yOffsetFromBottom = yOffsetFromBottom;
        this.width = width;
        this.height = height;
        this.fadeInTicks = fadeInTicks;
        this.stayTicks = stayTicks;
        this.fadeOutTicks = fadeOutTicks;
        this.textMaxCharsPerLine = textMaxCharsPerLine;
        this.textMaxLines = textMaxLines;
        this.nameXOffset = nameXOffset;
        this.nameYOffset = nameYOffset;
        this.textXOffset = textXOffset;
        this.textYOffset = textYOffset;
        this.textLineHeight = textLineHeight;
    }

    public static void encode(NpcDialogueConfigPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.reset);
        buf.writeInt(msg.x);
        buf.writeInt(msg.yOffsetFromBottom);
        buf.writeInt(msg.width);
        buf.writeInt(msg.height);
        buf.writeInt(msg.fadeInTicks);
        buf.writeInt(msg.stayTicks);
        buf.writeInt(msg.fadeOutTicks);
        buf.writeInt(msg.textMaxCharsPerLine);
        buf.writeInt(msg.textMaxLines);
        buf.writeInt(msg.nameXOffset);
        buf.writeInt(msg.nameYOffset);
        buf.writeInt(msg.textXOffset);
        buf.writeInt(msg.textYOffset);
        buf.writeInt(msg.textLineHeight);
    }

    public static NpcDialogueConfigPacket decode(FriendlyByteBuf buf) {
        return new NpcDialogueConfigPacket(
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
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt()
        );
    }

    public static void handle(NpcDialogueConfigPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (msg.reset) {
                ClientNpcDialogueManager.resetConfig();
            } else {
                ClientNpcDialogueManager.applyConfig(
                        msg.x,
                        msg.yOffsetFromBottom,
                        msg.width,
                        msg.height,
                        msg.fadeInTicks,
                        msg.stayTicks,
                        msg.fadeOutTicks
                );

                ClientNpcDialogueManager.applyTextLayoutConfig(
                        msg.textMaxCharsPerLine,
                        msg.textMaxLines,
                        msg.nameXOffset,
                        msg.nameYOffset,
                        msg.textXOffset,
                        msg.textYOffset,
                        msg.textLineHeight
                );
            }
        });
        ctx.setPacketHandled(true);
    }
}

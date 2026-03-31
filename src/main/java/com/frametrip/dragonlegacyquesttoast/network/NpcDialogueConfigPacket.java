package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.ClientNpcDialogueManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class NpcDialogueConfigPacket {
    private final boolean reset;
    private final int x;
    private final int yOffsetFromBottom;
    private final int minWidth;
    private final int maxWidth;
    private final int minHeight;
    private final int fadeInTicks;
    private final int stayTicks;
    private final int fadeOutTicks;
    private final int textMaxLines;
    private final int leftPadding;
    private final int rightPadding;
    private final int topPadding;
    private final int bottomPadding;
    private final int nameYOffset;
    private final int textYOffset;
    private final int textLineHeight;

    public NpcDialogueConfigPacket(
            boolean reset,
            int x,
            int yOffsetFromBottom,
            int minWidth,
            int maxWidth,
            int minHeight,
            int fadeInTicks,
            int stayTicks,
            int fadeOutTicks,
            int textMaxLines,
            int leftPadding,
            int rightPadding,
            int topPadding,
            int bottomPadding,
            int nameYOffset,
            int textYOffset,
            int textLineHeight
    ) {
        this.reset = reset;
        this.x = x;
        this.yOffsetFromBottom = yOffsetFromBottom;
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
        this.minHeight = minHeight;
        this.fadeInTicks = fadeInTicks;
        this.stayTicks = stayTicks;
        this.fadeOutTicks = fadeOutTicks;
        this.textMaxLines = textMaxLines;
        this.leftPadding = leftPadding;
        this.rightPadding = rightPadding;
        this.topPadding = topPadding;
        this.bottomPadding = bottomPadding;
        this.nameYOffset = nameYOffset;
        this.textYOffset = textYOffset;
        this.textLineHeight = textLineHeight;
    }

    public static void encode(NpcDialogueConfigPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.reset);
        buf.writeInt(msg.x);
        buf.writeInt(msg.yOffsetFromBottom);
        buf.writeInt(msg.minWidth);
        buf.writeInt(msg.maxWidth);
        buf.writeInt(msg.minHeight);
        buf.writeInt(msg.fadeInTicks);
        buf.writeInt(msg.stayTicks);
        buf.writeInt(msg.fadeOutTicks);
        buf.writeInt(msg.textMaxLines);
        buf.writeInt(msg.leftPadding);
        buf.writeInt(msg.rightPadding);
        buf.writeInt(msg.topPadding);
        buf.writeInt(msg.bottomPadding);
        buf.writeInt(msg.nameYOffset);
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
                        msg.minWidth,
                        msg.maxWidth,
                        msg.minHeight,
                        msg.fadeInTicks,
                        msg.stayTicks,
                        msg.fadeOutTicks
                );

                ClientNpcDialogueManager.applyTextLayoutConfig(
                        msg.textMaxLines,
                        msg.leftPadding,
                        msg.rightPadding,
                        msg.topPadding,
                        msg.bottomPadding,
                        msg.nameYOffset,
                        msg.textYOffset,
                        msg.textLineHeight
                );
            }
        });
        ctx.setPacketHandled(true);
    }
}

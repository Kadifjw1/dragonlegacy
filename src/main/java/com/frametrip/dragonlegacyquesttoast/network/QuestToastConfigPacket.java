package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.ClientQuestToastManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class QuestToastConfigPacket {
    private final boolean reset;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int fadeInTicks;
    private final int stayTicks;
    private final int fadeOutTicks;
    private final int startOffsetX;

    public QuestToastConfigPacket(
            boolean reset,
            int x,
            int y,
            int width,
            int height,
            int fadeInTicks,
            int stayTicks,
            int fadeOutTicks,
            int startOffsetX
    ) {
        this.reset = reset;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.fadeInTicks = fadeInTicks;
        this.stayTicks = stayTicks;
        this.fadeOutTicks = fadeOutTicks;
        this.startOffsetX = startOffsetX;
    }

    public static void encode(QuestToastConfigPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.reset);
        buf.writeInt(msg.x);
        buf.writeInt(msg.y);
        buf.writeInt(msg.width);
        buf.writeInt(msg.height);
        buf.writeInt(msg.fadeInTicks);
        buf.writeInt(msg.stayTicks);
        buf.writeInt(msg.fadeOutTicks);
        buf.writeInt(msg.startOffsetX);
    }

    public static QuestToastConfigPacket decode(FriendlyByteBuf buf) {
        return new QuestToastConfigPacket(
                buf.readBoolean(),
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

    public static void handle(QuestToastConfigPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (msg.reset) {
                ClientQuestToastManager.resetConfig();
            } else {
                ClientQuestToastManager.applyConfig(
                        msg.x,
                        msg.y,
                        msg.width,
                        msg.height,
                        msg.fadeInTicks,
                        msg.stayTicks,
                        msg.fadeOutTicks,
                        msg.startOffsetX
                );
            }
        });
        ctx.setPacketHandled(true);
    }
}

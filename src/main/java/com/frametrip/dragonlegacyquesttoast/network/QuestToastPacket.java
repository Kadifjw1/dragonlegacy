package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.ClientQuestToastManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class QuestToastPacket {
    private final String type;
    private final String questTitle;

    public QuestToastPacket(String type, String questTitle) {
        this.type = type;
        this.questTitle = questTitle;
    }

    public static void encode(QuestToastPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.type);
        buf.writeUtf(msg.questTitle);
    }

    public static QuestToastPacket decode(FriendlyByteBuf buf) {
        return new QuestToastPacket(buf.readUtf(), buf.readUtf());
    }

    public static void handle(QuestToastPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> ClientQuestToastManager.show(msg.type, msg.questTitle));
        ctx.setPacketHandled(true);
    }
}
package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.ClientNpcDialogueManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class NpcDialoguePacket {
    private final String npcName;
    private final String text;

    public NpcDialoguePacket(String npcName, String text) {
        this.npcName = npcName;
        this.text = text;
    }

    public static void encode(NpcDialoguePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.npcName);
        buf.writeUtf(msg.text);
    }

    public static NpcDialoguePacket decode(FriendlyByteBuf buf) {
        return new NpcDialoguePacket(buf.readUtf(), buf.readUtf());
    }

    public static void handle(NpcDialoguePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> ClientNpcDialogueManager.show(msg.npcName, msg.text));
        ctx.setPacketHandled(true);
    }
}

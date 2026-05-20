package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.ClientQuestDeadlineState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

// [QST-3]: Server→Client: syncs timed-quest deadline timestamps.
public class SyncQuestDeadlinesPacket {

    private final Map<String, Long> deadlines;

    public SyncQuestDeadlinesPacket(Map<String, Long> deadlines) {
        this.deadlines = new HashMap<>(deadlines);
    }

    public static void encode(SyncQuestDeadlinesPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.deadlines.size());
        msg.deadlines.forEach((k, v) -> { buf.writeUtf(k, 64); buf.writeLong(v); });
    }

    public static SyncQuestDeadlinesPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<String, Long> map = new HashMap<>();
        for (int i = 0; i < size; i++) map.put(buf.readUtf(64), buf.readLong());
        return new SyncQuestDeadlinesPacket(map);
    }

    public static void handle(SyncQuestDeadlinesPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientQuestDeadlineState.sync(msg.deadlines));
        ctx.get().setPacketHandled(true);
    }
}

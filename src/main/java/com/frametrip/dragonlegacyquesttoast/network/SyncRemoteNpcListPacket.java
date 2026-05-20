package com.frametrip.dragonlegacyquesttoast.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

// [EDT-4]: Server → Client: syncs the list of all NPC entities.
public class SyncRemoteNpcListPacket {

    public record NpcEntry(UUID uuid, String name, float x, float y, float z, String dimension) {}

    private final List<NpcEntry> entries;

    public SyncRemoteNpcListPacket(List<NpcEntry> entries) {
        this.entries = entries;
    }

    public static void encode(SyncRemoteNpcListPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.entries.size());
        for (NpcEntry e : pkt.entries) {
            buf.writeUUID(e.uuid());
            buf.writeUtf(e.name(), 64);
            buf.writeFloat(e.x());
            buf.writeFloat(e.y());
            buf.writeFloat(e.z());
            buf.writeUtf(e.dimension(), 128);
        }
    }

    public static SyncRemoteNpcListPacket decode(FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<NpcEntry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(new NpcEntry(
                    buf.readUUID(),
                    buf.readUtf(64),
                    buf.readFloat(), buf.readFloat(), buf.readFloat(),
                    buf.readUtf(128)
            ));
        }
        return new SyncRemoteNpcListPacket(entries);
    }

    public static void handle(SyncRemoteNpcListPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            net.minecraft.client.Minecraft.getInstance()
                    .setScreen(new com.frametrip.dragonlegacyquesttoast.client.RemoteNpcListScreen(pkt.entries));
        });
        ctx.get().setPacketHandled(true);
    }
}

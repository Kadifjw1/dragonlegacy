package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.ClientQuestChainState;
import com.frametrip.dragonlegacyquesttoast.server.quest.QuestChain;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Supplier;

// [QST-1]: Server→Client: sync full quest chain list.
public class SyncQuestChainsPacket {

    private static final Gson GSON = new Gson();
    private final String json;

    public SyncQuestChainsPacket(List<QuestChain> chains) {
        this.json = GSON.toJson(chains);
    }

    private SyncQuestChainsPacket(String json) {
        this.json = json;
    }

    public static void encode(SyncQuestChainsPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.json, 32767);
    }

    public static SyncQuestChainsPacket decode(FriendlyByteBuf buf) {
        return new SyncQuestChainsPacket(buf.readUtf(32767));
    }

    public static void handle(SyncQuestChainsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Type t = new TypeToken<List<QuestChain>>() {}.getType();
            List<QuestChain> chains = GSON.fromJson(msg.json, t);
            ClientQuestChainState.sync(chains);
        });
        ctx.get().setPacketHandled(true);
    }
}

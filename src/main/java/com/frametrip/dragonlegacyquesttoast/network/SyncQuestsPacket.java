package com.frametrip.dragonlegacyquesttoast.network;
 
import com.frametrip.dragonlegacyquesttoast.client.ClientQuestState;
import com.frametrip.dragonlegacyquesttoast.server.QuestDefinition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
 
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Supplier;
 
public class SyncQuestsPacket {
 
    private static final Gson GSON = new Gson();
    private final String json;
 
    public SyncQuestsPacket(List<QuestDefinition> quests) {
        this.json = GSON.toJson(quests);
    }
 
    private SyncQuestsPacket(String json) {
        this.json = json;
    }
 
    public static void encode(SyncQuestsPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.json, 32767);
    }
 
    public static SyncQuestsPacket decode(FriendlyByteBuf buf) {
        return new SyncQuestsPacket(buf.readUtf(32767));
    }
 
    public static void handle(SyncQuestsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Type t = new TypeToken<List<QuestDefinition>>() {}.getType();
            List<QuestDefinition> list = GSON.fromJson(msg.json, t);
            ClientQuestState.sync(list);
        });
        ctx.get().setPacketHandled(true);
    }
}

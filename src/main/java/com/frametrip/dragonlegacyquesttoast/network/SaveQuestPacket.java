package com.frametrip.dragonlegacyquesttoast.network;
 
import com.frametrip.dragonlegacyquesttoast.server.QuestDefinition;
import com.frametrip.dragonlegacyquesttoast.server.QuestManager;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
 
import java.util.function.Supplier;
 
public class SaveQuestPacket {
 
    private static final Gson GSON = new Gson();
    private final String questJson;
    private final boolean delete;
 
    public SaveQuestPacket(QuestDefinition quest, boolean delete) {
        this.questJson = GSON.toJson(quest);
        this.delete = delete;
    }
 
    private SaveQuestPacket(String json, boolean delete) {
        this.questJson = json;
        this.delete = delete;
    }
 
    public static void encode(SaveQuestPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.questJson, 32767);
        buf.writeBoolean(msg.delete);
    }
 
    public static SaveQuestPacket decode(FriendlyByteBuf buf) {
        return new SaveQuestPacket(buf.readUtf(32767), buf.readBoolean());
    }
 
    public static void handle(SaveQuestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.getAbilities().instabuild) return;
 
            QuestDefinition quest = GSON.fromJson(msg.questJson, QuestDefinition.class);
            if (quest == null || quest.id == null) return;
 
            if (msg.delete) {
                QuestManager.delete(quest.id);
            } else {
                QuestManager.save(quest);
            }
 
            ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncQuestsPacket(QuestManager.getAll())
            );
        });
        ctx.get().setPacketHandled(true);
    }
}

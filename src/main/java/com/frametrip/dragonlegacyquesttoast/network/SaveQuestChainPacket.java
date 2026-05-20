package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.server.quest.QuestChain;
import com.frametrip.dragonlegacyquesttoast.server.quest.QuestChainController;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// [QST-1]: Client→Server: save or create a quest chain.
public class SaveQuestChainPacket {

    private static final Gson GSON = new Gson();
    private final String chainJson;

    public SaveQuestChainPacket(QuestChain chain) {
        this.chainJson = GSON.toJson(chain);
    }

    private SaveQuestChainPacket(String json) {
        this.chainJson = json;
    }

    public static void encode(SaveQuestChainPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.chainJson, 8192);
    }

    public static SaveQuestChainPacket decode(FriendlyByteBuf buf) {
        return new SaveQuestChainPacket(buf.readUtf(8192));
    }

    public static void handle(SaveQuestChainPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.getAbilities().instabuild) return;
            QuestChain chain = GSON.fromJson(msg.chainJson, QuestChain.class);
            if (chain != null && chain.chainId != null) {
                QuestChainController.save(chain);
                ModNetwork.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.ALL.noArg(),
                    new SyncQuestChainsPacket(QuestChainController.getAll())
                );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

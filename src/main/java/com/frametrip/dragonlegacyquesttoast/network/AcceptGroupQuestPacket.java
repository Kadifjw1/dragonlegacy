package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.server.QuestDefinition;
import com.frametrip.dragonlegacyquesttoast.server.QuestManager;
import com.frametrip.dragonlegacyquesttoast.server.QuestProgressManager;
import com.frametrip.dragonlegacyquesttoast.server.quest.QuestChainController;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

// [QST-4]: Client→Server: accept a group quest. Server finds nearby players and gives quest to all.
public class AcceptGroupQuestPacket {

    private final String questId;

    public AcceptGroupQuestPacket(String questId) {
        this.questId = questId;
    }

    public static void encode(AcceptGroupQuestPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.questId, 64);
    }

    public static AcceptGroupQuestPacket decode(FriendlyByteBuf buf) {
        return new AcceptGroupQuestPacket(buf.readUtf(64));
    }

    public static void handle(AcceptGroupQuestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer requester = ctx.get().getSender();
            if (requester == null) return;

            QuestDefinition def = QuestManager.get(msg.questId);
            if (def == null || !def.groupQuestEnabled) {
                requester.sendSystemMessage(Component.literal("§cЭтот квест не является групповым."));
                return;
            }

            // Chain prerequisite check for requester.
            if (!QuestChainController.canStart(requester.getUUID(), msg.questId)) {
                requester.sendSystemMessage(Component.literal("§cНеобходимо завершить предыдущие квесты цепочки."));
                return;
            }

            // Find nearby eligible players (within 20 blocks).
            List<ServerPlayer> group = new ArrayList<>();
            group.add(requester);
            Vec3 pos = requester.position();
            if (requester.level() instanceof ServerLevel sl) {
                for (ServerPlayer other : sl.players()) {
                    if (other == requester) continue;
                    if (other.position().distanceTo(pos) <= 20.0) group.add(other);
                }
            }

            if (group.size() < def.groupQuestMinPlayers) {
                requester.sendSystemMessage(Component.literal(
                    "§cНедостаточно игроков рядом. Нужно: §f" + def.groupQuestMinPlayers));
                return;
            }

            for (ServerPlayer member : group) {
                if (!QuestProgressManager.isActive(member.getUUID(), msg.questId)
                        && !QuestProgressManager.isCompleted(member.getUUID(), msg.questId)) {
                    QuestProgressManager.accept(member.getUUID(), msg.questId);
                    ModNetwork.CHANNEL.send(
                        net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> member),
                        new QuestToastPacket("accepted", def.title != null ? def.title : msg.questId)
                    );
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.server.QuestProgressManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/** Client -> Server quest state mutation from scene actions. */
public class QuestStateActionPacket {

    public static final String ACTION_ACCEPT = "accept";
    public static final String ACTION_COMPLETE = "complete";
    public static final String ACTION_FAIL = "fail";

    private final String action;
    private final String questId;

    public QuestStateActionPacket(String action, String questId) {
        this.action = action == null ? "" : action;
        this.questId = questId == null ? "" : questId;
    }

    public static void encode(QuestStateActionPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.action, 24);
        buf.writeUtf(msg.questId, 64);
    }

    public static QuestStateActionPacket decode(FriendlyByteBuf buf) {
        return new QuestStateActionPacket(buf.readUtf(24), buf.readUtf(64));
    }

    public static void handle(QuestStateActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer sender = c.getSender();
            if (sender == null || msg.questId.isBlank()) return;

            boolean changed;
            switch (msg.action) {
                case ACTION_ACCEPT -> changed = QuestProgressManager.accept(sender.getUUID(), msg.questId);
                case ACTION_COMPLETE -> changed = QuestProgressManager.complete(sender.getUUID(), msg.questId);
                case ACTION_FAIL -> changed = QuestProgressManager.fail(sender.getUUID(), msg.questId);
                default -> {
                    return;
                }
            }

            if (!changed) return;
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender),
                    new SyncQuestProgressPacket(
                            QuestProgressManager.getAllProgress(sender.getUUID()),
                            QuestProgressManager.getActive(sender.getUUID()),
                            QuestProgressManager.getCompleted(sender.getUUID()),
                            QuestProgressManager.getFailed(sender.getUUID())
                    ));
        });
        c.setPacketHandled(true);
    }

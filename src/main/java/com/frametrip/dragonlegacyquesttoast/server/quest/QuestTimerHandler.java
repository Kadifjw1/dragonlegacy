package com.frametrip.dragonlegacyquesttoast.server.quest;

import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.QuestToastPacket;
import com.frametrip.dragonlegacyquesttoast.network.SyncQuestDeadlinesPacket;
import com.frametrip.dragonlegacyquesttoast.network.SyncQuestProgressPacket;
import com.frametrip.dragonlegacyquesttoast.server.QuestDefinition;
import com.frametrip.dragonlegacyquesttoast.server.QuestManager;
import com.frametrip.dragonlegacyquesttoast.server.QuestProgressManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// [QST-3]: Checks timed quest deadlines every second; fails expired quests.
public class QuestTimerHandler {

    private static int tickCounter = 0;
    private static final int CHECK_INTERVAL = 20; // every second

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new QuestTimerHandler());
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (++tickCounter < CHECK_INTERVAL) return;
        tickCounter = 0;

        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        for (var level : server.getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                UUID uid = player.getUUID();
                List<String> expired = QuestProgressManager.getExpiredQuests(uid);
                if (expired.isEmpty()) continue;

                for (String questId : expired) {
                    QuestProgressManager.fail(uid, questId);
                    QuestDefinition def = QuestManager.get(questId);
                    String title = def != null && def.title != null ? def.title : questId;
                    ModNetwork.CHANNEL.send(
                        net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                        new QuestToastPacket("failed", title)
                    );
                }
                // Sync updated progress and deadlines to client.
                ModNetwork.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                    new SyncQuestProgressPacket(
                        QuestProgressManager.getAllProgress(uid),
                        QuestProgressManager.getActive(uid),
                        QuestProgressManager.getCompleted(uid),
                        QuestProgressManager.getFailed(uid)
                    )
                );
                syncDeadlines(player);
            }
        }
    }

    /** Sends the current deadline map for all active timed quests to the given player. */
    public static void syncDeadlines(ServerPlayer player) {
        UUID uid = player.getUUID();
        Map<String, Long> deadlines = new HashMap<>();
        for (String questId : QuestProgressManager.getActive(uid)) {
            long dl = QuestProgressManager.getDeadlineMillis(uid, questId);
            if (dl > 0) deadlines.put(questId, dl);
        }
        ModNetwork.CHANNEL.send(
            net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
            new SyncQuestDeadlinesPacket(deadlines)
        );
    }
}

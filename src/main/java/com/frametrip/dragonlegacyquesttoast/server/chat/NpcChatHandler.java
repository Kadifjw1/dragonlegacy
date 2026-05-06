package com.frametrip.dragonlegacyquesttoast.server.chat;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.NpcStartScenePacket;
import com.frametrip.dragonlegacyquesttoast.server.QuestProgressManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Listens to player chat messages and dispatches NPC reactions. */
public class NpcChatHandler {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NpcChatHandler.class);

    /** Last-fired timestamps per (npcId, triggerId, playerId). */
    private static final Map<String, Long> cooldowns = new HashMap<>();
    /** Global per-NPC cooldown: npcId → tick. */
    private static final Map<UUID, Long> globalCooldowns = new HashMap<>();

    @SubscribeEvent
    public void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String message = event.getMessage().getString();
        long now = player.level().getGameTime();

        for (ServerLevel level : player.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (!(entity instanceof NpcEntity npc)) continue;

                NpcChatConfig cfg = npc.getNpcData().chatConfig;
                if (!cfg.enabled) continue;

                double dist = player.distanceTo(npc);
                if (dist > cfg.radius) continue;

                UUID npcId = npc.getUUID();
                Long globalLast = globalCooldowns.get(npcId);
                if (globalLast != null && now - globalLast < cfg.globalCooldownTicks) continue;

                ChatTrigger best = findBestTrigger(cfg.triggers, message, npcId, player.getUUID(), now);
                if (best == null) continue;

                globalCooldowns.put(npcId, now);
                String cooldownKey = npcId + ":" + best.id + ":" + player.getUUID();
                cooldowns.put(cooldownKey, now);

                dispatch(best.reaction, npc, player);
            }
        }
    }

    private ChatTrigger findBestTrigger(List<ChatTrigger> triggers, String message,
                                         UUID npcId, UUID playerId, long now) {
        return triggers.stream()
                .filter(t -> t.matches(message))
                .filter(t -> {
                    String key = npcId + ":" + t.id + ":" + playerId;
                    Long last = cooldowns.get(key);
                    return last == null || (now - last) >= t.cooldownTicks;
                })
                .max(Comparator.comparingInt(t -> t.priority))
                .orElse(null);
    }

    private void dispatch(ChatReaction reaction, NpcEntity npc, ServerPlayer player) {
        switch (reaction.type) {
            case SPEECH -> {
                if (!reaction.param.isBlank()) {
                    player.sendSystemMessage(
                            Component.literal("§e[" + npc.getNpcData().displayName + "]§r " + reaction.param));
                }
            }
            case START_SCENE, START_NODE -> {
                if (!reaction.param.isBlank()) {
                    ModNetwork.CHANNEL.send(
                            PacketDistributor.PLAYER.with(() -> player),
                            new NpcStartScenePacket(npc.getNpcData().displayName,
                                    reaction.param, "NEUTRAL", npc.getUUID()));
                }
            }
            case GIVE_QUEST, UPDATE_QUEST -> {
                if (!reaction.param.isBlank()) {
                    QuestProgressManager.accept(player.getUUID(), reaction.param);
                }
            }
            case PLAY_SOUND -> {
                // sounds dispatched client-side via packet
            }
            default -> LOG.debug("[NpcChatHandler] Unhandled reaction type: {}", reaction.type);
        }
    }
}


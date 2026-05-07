package com.frametrip.dragonlegacyquesttoast.server.event;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.NpcDialoguePacket;
import com.frametrip.dragonlegacyquesttoast.network.NpcStartScenePacket;
import com.frametrip.dragonlegacyquesttoast.server.QuestManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@Mod.EventBusSubscriber
public class EventChainHandler {

    /** NPC_CLICK trigger — fired when player right-clicks an NPC without shift. */
    @SubscribeEvent
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof NpcEntity npc)) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        NpcEntityData data = npc.getNpcData();
        fireChains(npc, data, player, EventTriggerType.NPC_CLICK, null);
    }

    /** CHAT_MESSAGE trigger — fired when player sends a chat message. */
    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String message = event.getRawText().toLowerCase();
        ServerLevel level = player.serverLevel();

        level.getAllEntities().forEach(entity -> {
            if (!(entity instanceof NpcEntity npc)) return;
            if (npc.distanceTo(player) > 16) return;

            NpcEntityData data = npc.getNpcData();
            for (EventChain chain : data.eventChains) {
                if (!chain.enabled || chain.trigger != EventTriggerType.CHAT_MESSAGE) continue;

                String phrase = chain.triggerParam("phrase").toLowerCase();
                if (!phrase.isEmpty() && message.contains(phrase)) {
                    fireChain(chain, npc, data, player);
                }
            }
        });
    }

    /** NPC_ATTACKED trigger. */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof NpcEntity npc)) return;

        Player attacker = null;
        if (event.getSource().getEntity() instanceof Player p) attacker = p;
        if (!(attacker instanceof ServerPlayer sp)) return;

        NpcEntityData data = npc.getNpcData();
        fireChains(npc, data, sp, EventTriggerType.NPC_ATTACKED, null);
    }

    /** ZONE_ENTER + TIMER + TIME_CHANGE — checked on level tick. */
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level)) return;

        long dayTime = level.getDayTime() % 24000;

        level.getAllEntities().forEach(entity -> {
            if (!(entity instanceof NpcEntity npc)) return;
            NpcEntityData data = npc.getNpcData();

            for (EventChain chain : data.eventChains) {
                if (!chain.enabled) continue;

                switch (chain.trigger) {
                    case ZONE_ENTER -> {
                        float radius = parseFloat(chain.triggerParam("radius"), 8f);
                        for (ServerPlayer sp : level.players()) {
                            if (npc.distanceTo(sp) <= radius) {
                                fireChain(chain, npc, data, sp);
                            }
                        }
                    }
                    case TIMER -> {
                        int interval = parseInt(chain.triggerParam("interval"), 200);
                        if (interval > 0 && level.getGameTime() % interval == 0) {
                            for (ServerPlayer sp : level.players()) {
                                fireChain(chain, npc, data, sp);
                            }
                        }
                    }
                    case TIME_CHANGE -> {
                        String when = chain.triggerParam("time");
                        boolean triggerDay = "День".equals(when) && dayTime == 0;
                        boolean triggerNight = "Ночь".equals(when) && dayTime == 13000;
                        if (triggerDay || triggerNight) {
                            for (ServerPlayer sp : level.players()) {
                                fireChain(chain, npc, data, sp);
                            }
                        }
                    }
                    default -> {
                    }
                }
            }
        });
    }

    public static void fireChains(NpcEntity npc, NpcEntityData data,
                                  ServerPlayer player, EventTriggerType trigger,
                                  String extraParam) {
        boolean first = false;
        for (EventChain chain : data.eventChains) {
            if (!chain.enabled || chain.trigger != trigger) continue;
            if (!first || chain.executeAll) {
                if (fireChain(chain, npc, data, player)) {
                    first = true;
                }
            }
        }
    }

    /** Evaluates conditions and executes actions. Returns true if chain fired. */
    public static boolean fireChain(EventChain chain, NpcEntity npc,
                                    NpcEntityData data, ServerPlayer player) {
        if (!checkConditions(chain, npc, data, player)) return false;
        for (EventAction action : chain.actions) {
            executeAction(action, npc, data, player);
        }
        return true;
    }

    private static boolean checkConditions(EventChain chain, NpcEntity npc,
                                           NpcEntityData data, ServerPlayer player) {
        if (chain.conditions.isEmpty()) return true;
        boolean and = "AND".equals(chain.conditionMode);

        for (EventCondition cond : chain.conditions) {
            boolean result = evalCondition(cond, npc, data, player);
            if (and && !result) return false;
            if (!and && result) return true;
        }
        return and;
    }

    private static boolean evalCondition(EventCondition cond, NpcEntity npc,
                                         NpcEntityData data, ServerPlayer player) {
        return switch (cond.type) {
            case ITEM_IN_INVENTORY -> {
                String itemId = cond.param("itemId");
                int qty = parseInt(cond.param("qty"), 1);
                if (itemId.isEmpty()) yield false;

                var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
                if (item == null) yield false;

                int count = 0;
                for (ItemStack s : player.getInventory().items) {
                    if (s.getItem() == item) count += s.getCount();
                }
                yield count >= qty;
            }
            case QUEST_STATUS -> {
                String questId = cond.param("questId");
                if (questId.isEmpty()) yield false;
                var quest = QuestManager.get(questId);
                yield quest != null; // TODO: link to QuestProgressManager
            }
            case TIME_OF_DAY -> {
                long t = npc.level().getDayTime() % 24000;
                yield switch (cond.param("time")) {
                    case "День" -> t < 12000;
                    case "Ночь" -> t >= 13000 && t < 23000;
                    case "Рассвет" -> t >= 23000;
                    case "Закат" -> t >= 12000 && t < 13000;
                    default -> false;
                };
            }
            case IN_ZONE -> {
                float radius = parseFloat(cond.param("radius"), 8f);
                yield npc.distanceTo(player) <= radius;
            }
            case NPC_PROFESSION -> {
                String prof = cond.param("profession");
                yield data.professionData != null
                        && data.professionData.type != null
                        && data.professionData.type.name().equalsIgnoreCase(prof);
            }
            case NPC_STATE -> true; // TODO: wire to companion/AI state
            case REPUTATION -> true; // TODO: wire to faction reputation
        };
    }

    private static void executeAction(EventAction action, NpcEntity npc,
                                      NpcEntityData data, ServerPlayer player) {
        switch (action.type) {
            case SAY_PHRASE -> {
                String phrase = action.param("phrase");
                if (!phrase.isEmpty()) {
                    ModNetwork.CHANNEL.send(
                            PacketDistributor.PLAYER.with(() -> player),
                            new NpcDialoguePacket(data.displayName, phrase)
                    );
                }
            }
            case OPEN_DIALOGUE -> {
                String dlgId = action.param("dialogueId");
                if (!dlgId.isEmpty()) {
                    var dlg = com.frametrip.dragonlegacyquesttoast.server.DialogueManager.get(dlgId);
                    if (dlg != null) {
                        String text = String.join("\n", dlg.lines);
                        ModNetwork.CHANNEL.send(
                                PacketDistributor.PLAYER.with(() -> player),
                                new NpcDialoguePacket(data.displayName, text)
                        );
                    }
                }
            }
            case START_SCENE -> {
                String sceneId = action.param("sceneId");
                if (!sceneId.isEmpty()) {
                    ModNetwork.CHANNEL.send(
                            PacketDistributor.PLAYER.with(() -> player),
                            new NpcStartScenePacket(data.displayName, sceneId, data.playerRelation, npc.getUUID())
                    );
                }
            }
            case GIVE_ITEM -> {
                String itemId = action.param("itemId");
                int qty = Math.max(1, parseInt(action.param("qty"), 1));
                if (!itemId.isEmpty()) {
                    var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
                    if (item != null) {
                        player.getInventory().add(new ItemStack(item, qty));
                    }
                }
            }
            case GIVE_QUEST -> {
                String questId = action.param("questId");
                if (!questId.isEmpty()) {
                    // TODO: QuestProgressManager.start(player, questId)
                }
            }
            case COMPLETE_QUEST -> {
                String questId = action.param("questId");
                if (!questId.isEmpty()) {
                    // TODO: QuestProgressManager.complete(player, questId)
                }
            }
            case SET_NPC_STATE -> {
                String stateName = action.param("state");
                // TODO: wire to companion mode when System 3 is implemented
            }
            case PLAY_ANIMATION -> {
                String animName = action.param("animName");
                // TODO: send ForcePlayAnimationPacket when client-side trigger packet exists
            }
            case TELEPORT -> {
                try {
                    double x = Double.parseDouble(action.param("x"));
                    double y = Double.parseDouble(action.param("y"));
                    double z = Double.parseDouble(action.param("z"));
                    player.teleportTo(x, y, z);
                } catch (NumberFormatException ignored) {
                }
            }
            case START_PATROL, START_BUILD_SCENE, OPEN_GUI -> {
                // TODO: implement when respective systems are ready
            }
        }
    }

    private static float parseFloat(String s, float def) {
        try {
            return Float.parseFloat(s);
        } catch (Exception e) {
            return def;
        }
    }

    private static int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }
}

import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.QuestToastPacket;
import com.frametrip.dragonlegacyquesttoast.network.SyncQuestProgressPacket;
import com.frametrip.dragonlegacyquesttoast.server.QuestDefinition;
import com.frametrip.dragonlegacyquesttoast.server.QuestManager;
import com.frametrip.dragonlegacyquesttoast.server.QuestProgressManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraftforge.common.MinecraftForge;
 
import java.util.List;
import java.util.UUID;
 
/**
 * Listens to game events and advances quest progress for all matching quests.
 *
 * Logic per type:
 *  COLLECT  – player picks up items matching logicData["item"]
 *  DIALOGUE – triggered externally via completeDialogueQuest()
 *  HUNT     – player kills a mob matching logicData["mobType"]
 *  EXPLORE  – player ticks within radius of logicData["x/y/z/radius"]
 *  SEARCH   – player right-clicks a block matching logicData["blockType"]
 *  ESCORT   – player reaches destination near logicData["endX/Y/Z"] (tick-based)
 *  CRAFT    – player crafts an item matching logicData["item"]
 *  BUILD    – player places a block matching logicData["block"]
 */
public class QuestLogicHandler {
 
    private static final int EXPLORE_CHECK_INTERVAL = 20; // ticks between explore checks
    private int tickCounter = 0;
 
    // Called from DragonLegacyQuestToastMod to register this handler
    public static QuestLogicHandler register() {
        QuestLogicHandler h = new QuestLogicHandler();
        MinecraftForge.EVENT_BUS.register(h);
        // Register server tick for EXPLORE / ESCORT
        MinecraftForge.EVENT_BUS.addListener(h::onServerTick);
        return h;
    }
 
    // ── COLLECT ───────────────────────────────────────────────────────────────
    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getItem().getItem();
        String pickedId = itemId(stack);
        if (pickedId == null) return;
 
        forEachActiveQuest(player.getUUID(), "COLLECT", quest -> {
            String target = quest.logicData.getOrDefault("item", "");
            if (target.isEmpty() || !target.equalsIgnoreCase(pickedId)) return;
            int count = stack.getCount();
            if (tryComplete(player, quest, count)) return;
        });
    }
 
    // ── HUNT ──────────────────────────────────────────────────────────────────
    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        String killedType = entityTypeId(event.getEntity().getType());
        if (killedType == null) return;
 
        forEachActiveQuest(player.getUUID(), "HUNT", quest -> {
            String target = quest.logicData.getOrDefault("mobType", "");
            if (target.isEmpty() || !target.equalsIgnoreCase(killedType)) return;
            tryComplete(player, quest, 1);
        });
    }
 
    // ── CRAFT ─────────────────────────────────────────────────────────────────
    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        String craftedId = itemId(event.getCrafting());
        if (craftedId == null) return;
 
        forEachActiveQuest(player.getUUID(), "CRAFT", quest -> {
            String target = quest.logicData.getOrDefault("item", "");
            if (target.isEmpty() || !target.equalsIgnoreCase(craftedId)) return;
            tryComplete(player, quest, event.getCrafting().getCount());
        });
    }
 
    // ── SEARCH (right-click block) ─────────────────────────────────────────────
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Block clicked = player.level().getBlockState(event.getPos()).getBlock();
        String blockId = blockId(clicked);
        if (blockId == null) return;
 
        forEachActiveQuest(player.getUUID(), "SEARCH", quest -> {
            String target = quest.logicData.getOrDefault("blockType", "");
            if (target.isEmpty() || !target.equalsIgnoreCase(blockId)) return;
            tryComplete(player, quest, 1);
        });
    }
 
    // ── BUILD (block placement) ────────────────────────────────────────────────
    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        String placed = blockId(event.getPlacedBlock().getBlock());
        if (placed == null) return;
 
        forEachActiveQuest(player.getUUID(), "BUILD", quest -> {
            String target = quest.logicData.getOrDefault("block", "");
            if (target.isEmpty() || !target.equalsIgnoreCase(placed)) return;
 
            // Optional: check specific coordinates
            String xStr = quest.logicData.getOrDefault("x", "");
            String yStr = quest.logicData.getOrDefault("y", "");
            String zStr = quest.logicData.getOrDefault("z", "");
            if (!xStr.isEmpty() && !yStr.isEmpty() && !zStr.isEmpty()) {
                try {
                    BlockPos pos = event.getPos();
                    int qx = Integer.parseInt(xStr);
                    int qy = Integer.parseInt(yStr);
                    int qz = Integer.parseInt(zStr);
                    if (Math.abs(pos.getX() - qx) > 2 ||
                        Math.abs(pos.getY() - qy) > 2 ||
                        Math.abs(pos.getZ() - qz) > 2) return;
                } catch (NumberFormatException ignored) { }
            }
            tryComplete(player, quest, 1);
        });
    }
 
    // ── EXPLORE / ESCORT (server tick) ────────────────────────────────────────
    public void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        if (++tickCounter < EXPLORE_CHECK_INTERVAL) return;
        tickCounter = 0;
 
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
 
        for (var level : server.getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                UUID uid = player.getUUID();
 
                // EXPLORE
                forEachActiveQuest(uid, "EXPLORE", quest -> {
                    try {
                        double qx = Double.parseDouble(quest.logicData.getOrDefault("x", "0"));
                        double qy = Double.parseDouble(quest.logicData.getOrDefault("y", "0"));
                        double qz = Double.parseDouble(quest.logicData.getOrDefault("z", "0"));
                        double r  = Double.parseDouble(quest.logicData.getOrDefault("radius", "5"));
                        double dist = player.position().distanceTo(
                            new net.minecraft.world.phys.Vec3(qx, qy, qz));
                        if (dist <= r) {
                            if (QuestProgressManager.complete(uid, quest.id)) {
                                sendCompletion(player, quest);
                            }
                        }
                    } catch (NumberFormatException ignored) { }
                });
 
                // ESCORT – check if player reached the end destination
                forEachActiveQuest(uid, "ESCORT", quest -> {
                    try {
                        double ex = Double.parseDouble(quest.logicData.getOrDefault("endX", "0"));
                        double ey = Double.parseDouble(quest.logicData.getOrDefault("endY", "0"));
                        double ez = Double.parseDouble(quest.logicData.getOrDefault("endZ", "0"));
                        double r  = 5.0;
                        double dist = player.position().distanceTo(
                            new net.minecraft.world.phys.Vec3(ex, ey, ez));
                        if (dist <= r) {
                            if (QuestProgressManager.complete(uid, quest.id)) {
                                sendCompletion(player, quest);
                            }
                        }
                    } catch (NumberFormatException ignored) { }
                });
            }
        }
    }
 
    // ── Public API for DIALOGUE completion ────────────────────────────────────
 
    /** Call this from the dialogue system when a dialogue interaction is finished. */
    public static void completeDialogueQuest(ServerPlayer player, String dialogueId) {
        forEachActiveQuest(player.getUUID(), "DIALOGUE", quest -> {
            String target = quest.logicData.getOrDefault("dialogueId", "");
            if (target.equalsIgnoreCase(dialogueId)) {
                if (QuestProgressManager.complete(player.getUUID(), quest.id)) {
                    sendCompletion(player, quest);
                    syncProgress(player);
                }
            }
        });
    }
 
    // ── Internals ─────────────────────────────────────────────────────────────
 
    private interface QuestConsumer {
        void accept(QuestDefinition quest);
    }
 
    private static void forEachActiveQuest(UUID playerId, String logicType, QuestConsumer consumer) {
        List<QuestDefinition> all = QuestManager.getAll();
        for (QuestDefinition q : all) {
            if (!logicType.equals(q.questLogicType)) continue;
            if (QuestProgressManager.isCompleted(playerId, q.id)) continue;
            consumer.accept(q);
        }
    }
 
    /** Increments progress; returns true if quest just completed. */
    private static boolean tryComplete(ServerPlayer player, QuestDefinition quest, int amount) {
        boolean justCompleted = QuestProgressManager.increment(player.getUUID(), quest.id, amount);
        syncProgress(player);
        if (justCompleted) sendCompletion(player, quest);
        return justCompleted;
    }
 
    private static void sendCompletion(ServerPlayer player, QuestDefinition quest) {
        ModNetwork.CHANNEL.send(
            net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
            new QuestToastPacket("completed", quest.title != null ? quest.title : "Квест")
        );
    }
 
    private static void syncProgress(ServerPlayer player) {
        UUID uid = player.getUUID();
        ModNetwork.CHANNEL.send(
            net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
            new SyncQuestProgressPacket(
                QuestProgressManager.getAllProgress(uid),
                QuestProgressManager.getCompleted(uid)
            )
        );
    }
 
    // ── Registry helpers ──────────────────────────────────────────────────────
 
    private static String itemId(ItemStack stack) {
        if (stack.isEmpty()) return null;
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return rl != null ? rl.toString() : null;
    }
 
    private static String blockId(Block block) {
        ResourceLocation rl = ForgeRegistries.BLOCKS.getKey(block);
        return rl != null ? rl.toString() : null;
    }
 
    private static String entityTypeId(EntityType<?> type) {
        ResourceLocation rl = ForgeRegistries.ENTITY_TYPES.getKey(type);
        return rl != null ? rl.toString() : null;
    }
}

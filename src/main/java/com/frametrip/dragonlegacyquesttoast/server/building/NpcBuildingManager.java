package com.frametrip.dragonlegacyquesttoast.server.building;

import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SyncNpcBuildingStatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Управляет активными стройками NPC на сервере.
 * Вешается как Forge event listener в DragonLegacyQuestToastMod.
 */
public class NpcBuildingManager {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NpcBuildingManager.class);

    /** Активные стройки: npcId → состояние. */
    private static final Map<UUID, NpcBuildingState> ACTIVE = new ConcurrentHashMap<>();

    private int syncTick = 0;

    // ── Public API ────────────────────────────────────────────────────────────

    public static NpcBuildingState startBuilding(UUID npcId, String templateId,
                                                  int ox, int oy, int oz) {
        BuildingTemplate tmpl = BuildingTemplateManager.get(templateId);
        if (tmpl == null) {
            LOG.warn("[NpcBuildingManager] Шаблон '{}' не найден.", templateId);
            return null;
        }
        NpcBuildingState state = new NpcBuildingState(npcId, templateId, ox, oy, oz, tmpl);
        ACTIVE.put(npcId, state);
        LOG.info("[NpcBuildingManager] NPC {} начал строить '{}' ({} блоков).",
                npcId, tmpl.name, state.totalBlocks);
        return state;
    }

    public static void pause(UUID npcId) {
        NpcBuildingState s = ACTIVE.get(npcId);
        if (s != null) s.pause();
    }

    public static void resume(UUID npcId) {
        NpcBuildingState s = ACTIVE.get(npcId);
        if (s != null) s.resume();
    }

    public static void cancel(UUID npcId) {
        NpcBuildingState s = ACTIVE.get(npcId);
        if (s != null) { s.cancel(); ACTIVE.remove(npcId); }
    }

    public static NpcBuildingState getState(UUID npcId) {
        return ACTIVE.get(npcId);
    }

    public static boolean isBuilding(UUID npcId) {
        NpcBuildingState s = ACTIVE.get(npcId);
        return s != null && s.status == NpcBuildingState.Status.BUILDING;
    }

// ── Tick handler ──────────────────────────────────────────────────────────

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (ACTIVE.isEmpty()) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        ServerLevel level = server.overworld();

        for (var entry : ACTIVE.entrySet()) {
            NpcBuildingState state = entry.getValue();
            if (state.status != NpcBuildingState.Status.BUILDING) continue;

            // place blocksPerTick blocks this tick (simplified: always 1)
            BuildingTemplate.BlockEntry next = state.queue.poll();
            if (next == null) {
                state.status = NpcBuildingState.Status.DONE;
                LOG.info("[NpcBuildingManager] NPC {} завершил строительство '{}'.",
                        entry.getKey(), state.templateId);
                // notify clients immediately so they see the DONE state
                ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(),
                        new SyncNpcBuildingStatePacket(entry.getKey(), state.templateId,
                                state.status.name(), state.totalBlocks, state.placedBlocks));
                continue;
            }

            BlockPos pos = new BlockPos(
                    state.originX + next.x,
                    state.originY + next.y,
                    state.originZ + next.z);

            BlockState bs = resolveBlock(next.block);
            if (bs != null) {
                level.setBlock(pos, bs, 3);
            }
            state.placedBlocks++;
        }

        // cleanup done/cancelled
        ACTIVE.entrySet().removeIf(e ->
                e.getValue().status == NpcBuildingState.Status.DONE ||
                e.getValue().status == NpcBuildingState.Status.CANCELLED);

        // broadcast progress every 20 ticks
        if (++syncTick >= 20) {
            syncTick = 0;
            for (var entry : ACTIVE.entrySet()) {
                NpcBuildingState s = entry.getValue();
                ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(),
                        new SyncNpcBuildingStatePacket(entry.getKey(), s.templateId,
                                s.status.name(), s.totalBlocks, s.placedBlocks));
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static BlockState resolveBlock(String id) {
        if (id == null || id.isBlank() || "minecraft:air".equals(id)) return Blocks.AIR.defaultBlockState();
        try {
            ResourceLocation rl = new ResourceLocation(id);
            Block block = ForgeRegistries.BLOCKS.getValue(rl);
            return block != null ? block.defaultBlockState() : null;
        } catch (Exception e) {
            return null;
        }
    }
}

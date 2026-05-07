package com.frametrip.dragonlegacyquesttoast.server.data;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SyncDataPresetsPacket;
import com.frametrip.dragonlegacyquesttoast.server.animation.NpcAnimationData;
import com.frametrip.dragonlegacyquesttoast.server.gui.GuiTemplate;
import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Loads NPC animation presets and GUI template presets from data packs.
 * Registered via AddReloadListenerEvent so that /reload refreshes all presets
 * and automatically re-syncs them to online players.
 *
 * Data pack layout:
 *   data/{namespace}/npc_animation_presets/*.json  — GeckoLib animation format
 *   data/{namespace}/gui_template_presets/*.json   — GuiTemplate Gson format
 */
public class DataPackManager implements PreparableReloadListener {

    public static final DataPackManager INSTANCE = new DataPackManager();
    private static final Gson GSON = new Gson();

    private static final String ANIM_PATH = "npc_animation_presets";
    private static final String GUI_PATH  = "gui_template_presets";

    public static final List<NpcAnimationData> animationPresets = new ArrayList<>();
    public static final List<GuiTemplate>       guiPresets       = new ArrayList<>();

    private record PreparedData(List<NpcAnimationData> anims, List<GuiTemplate> guis) {}

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier,
                                          ResourceManager manager,
                                          ProfilerFiller prepareProfiler,
                                          ProfilerFiller applyProfiler,
                                          Executor prepareExecutor,
                                          Executor applyExecutor) {
        return CompletableFuture.supplyAsync(() -> prepare(manager), prepareExecutor)
                .thenCompose(barrier::wait)
                .thenAcceptAsync(this::apply, applyExecutor);
    }

    private PreparedData prepare(ResourceManager manager) {
        List<NpcAnimationData> anims = new ArrayList<>();
        List<GuiTemplate> guis = new ArrayList<>();

        for (Map.Entry<ResourceLocation, Resource> entry :
                manager.listResources(ANIM_PATH, loc -> loc.getPath().endsWith(".json")).entrySet()) {
            try (InputStreamReader reader = new InputStreamReader(
                    entry.getValue().open(), StandardCharsets.UTF_8)) {
                StringBuilder sb = new StringBuilder();
                char[] buf = new char[4096];
                int n;
                while ((n = reader.read(buf)) != -1) sb.append(buf, 0, n);
                NpcAnimationData anim = NpcAnimationData.fromGeckoLibJson(sb.toString());
                if (anim != null) {
                    // Use the file name (without .json) as the preset name if JSON doesn't specify
                    String path = entry.getKey().getPath();
                    String fileName = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));
                    if ("Анимация".equals(anim.name)) anim.name = fileName;
                    anims.add(anim);
                }
            } catch (IOException ignored) {}
        }

        for (Map.Entry<ResourceLocation, Resource> entry :
                manager.listResources(GUI_PATH, loc -> loc.getPath().endsWith(".json")).entrySet()) {
            try (InputStreamReader reader = new InputStreamReader(
                    entry.getValue().open(), StandardCharsets.UTF_8)) {
                GuiTemplate t = GSON.fromJson(reader, GuiTemplate.class);
                if (t != null && t.name != null) guis.add(t);
            } catch (Exception ignored) {}
        }

        return new PreparedData(anims, guis);
    }

    private void apply(PreparedData data) {
        animationPresets.clear();
        animationPresets.addAll(data.anims());
        guiPresets.clear();
        guiPresets.addAll(data.guis());

        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            SyncDataPresetsPacket packet = new SyncDataPresetsPacket(animationPresets, guiPresets);
            server.execute(() -> {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
                }
            });
        }
    }

    @Override
    public String getName() {
        return DragonLegacyQuestToastMod.MODID + ":data_packs";
    }
}


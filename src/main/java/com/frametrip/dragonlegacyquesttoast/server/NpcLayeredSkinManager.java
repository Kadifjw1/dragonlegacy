package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;

/**
 * Manages per-category NPC texture layers.
 *
 * Directory layout under config/dragonlegacyquesttoast/textures/:
 *   base/, hair/, eyes/, top/, bottom/, shoes/, accessory/, overlay/
 *
 * Textures can be blended into a single DynamicTexture for rendering.
 */
public class NpcLayeredSkinManager {

    private static final Path LAYERS_DIR = FMLPaths.CONFIGDIR.get()
            .resolve("dragonlegacyquesttoast/textures");

    /** category → list of texture IDs (file names without .png) */
    private static final Map<String, List<String>> availableByCategory = new LinkedHashMap<>();

    /** cacheKey(category+id) → ResourceLocation */
    private static final Map<String, ResourceLocation> textureCache = new LinkedHashMap<>();

    /** blended cache: sorted layer string → ResourceLocation */
    private static final Map<String, ResourceLocation> blendCache = new LinkedHashMap<>();

    public static void init() {
        for (String layer : NpcEntityData.TEXTURE_LAYERS) {
            try {
                Files.createDirectories(LAYERS_DIR.resolve(layer));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        refresh();
    }

    public static void refresh() {
        availableByCategory.clear();
        textureCache.clear();
        blendCache.clear();

        for (String layer : NpcEntityData.TEXTURE_LAYERS) {
            List<String> ids = new ArrayList<>();
            ids.add("none");
            Path dir = LAYERS_DIR.resolve(layer);
            if (Files.isDirectory(dir)) {
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.png")) {
                    for (Path p : ds) {
                        String name = p.getFileName().toString();
                        ids.add(name.substring(0, name.length() - 4));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            availableByCategory.put(layer, ids);
        }
    }

    public static List<String> getAvailable(String category) {
        return Collections.unmodifiableList(
                availableByCategory.getOrDefault(category, List.of("none")));
    }

    /** Returns the ResourceLocation for a single layer texture. */
    public static ResourceLocation getLayerTexture(String category, String textureId) {
        if (textureId == null || textureId.isEmpty() || "none".equals(textureId)) return null;
        String key = category + ":" + textureId;
        if (textureCache.containsKey(key)) return textureCache.get(key);

        Path file = LAYERS_DIR.resolve(category).resolve(textureId + ".png");
        if (!Files.exists(file)) return null;

        try (InputStream is = Files.newInputStream(file)) {
            NativeImage img = NativeImage.read(is);
            DynamicTexture tex = new DynamicTexture(img);
            ResourceLocation rl = new ResourceLocation(DragonLegacyQuestToastMod.MODID,
                    "dynamic/layer/" + category + "/" + textureId);
            Minecraft.getInstance().getTextureManager().register(rl, tex);
            textureCache.put(key, rl);
            return rl;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Blends all selected layer textures from the map into a single 64×64 DynamicTexture.
     * Returns the blended ResourceLocation, or null if all layers are none/empty.
     * Falls back to NpcSkinManager.getTexture(skinId) if no layers are set.
     */
    public static ResourceLocation getBlendedTexture(String skinId, Map<String, String> layers) {
        // Build a stable cache key
        StringBuilder keyBuilder = new StringBuilder(skinId);
        for (String cat : NpcEntityData.TEXTURE_LAYERS) {
            String id = layers.getOrDefault(cat, "");
            if (!id.isEmpty() && !"none".equals(id)) {
                keyBuilder.append("|").append(cat).append(":").append(id);
            }
        }
        String cacheKey = keyBuilder.toString();

        if (blendCache.containsKey(cacheKey)) return blendCache.get(cacheKey);

        // Check if any real layer is set
        boolean anyLayer = layers.values().stream()
                .anyMatch(v -> v != null && !v.isEmpty() && !"none".equals(v));

        if (!anyLayer) {
            // No layers → use base skin
            return NpcSkinManager.getTexture(skinId);
        }

        // Load base image
        NativeImage base = loadNativeImage(skinId, null, null);
        if (base == null) {
            try {
                base = new NativeImage(NativeImage.Format.RGBA, 64, 64, true);
            } catch (Exception e) {
                return NpcSkinManager.getTexture(skinId);
            }
        }

        // Overlay each layer in order
        for (String cat : NpcEntityData.TEXTURE_LAYERS) {
            String id = layers.getOrDefault(cat, "");
            if (id.isEmpty() || "none".equals(id)) continue;

            NativeImage overlay = loadNativeImage(null, cat, id);
            if (overlay == null) continue;

            blendImages(base, overlay);
            overlay.close();
        }

        // Register blended texture
        DynamicTexture tex = new DynamicTexture(base);
        String rlPath = "dynamic/blended/" + cacheKey.hashCode();
        ResourceLocation rl = new ResourceLocation(DragonLegacyQuestToastMod.MODID, rlPath);
        Minecraft.getInstance().getTextureManager().register(rl, tex);
        blendCache.put(cacheKey, rl);
        return rl;
    }

    private static NativeImage loadNativeImage(String skinId, String category, String textureId) {
        Path file;
        if (skinId != null && !skinId.equals("default")) {
            file = FMLPaths.CONFIGDIR.get()
                    .resolve("dragonlegacyquesttoast/skins/" + skinId + ".png");
        } else if (category != null && textureId != null) {
            file = LAYERS_DIR.resolve(category).resolve(textureId + ".png");
        } else {
            return null;
        }

        if (!Files.exists(file)) return null;
        try (InputStream is = Files.newInputStream(file)) {
            return NativeImage.read(is);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Alpha-composites overlay on top of base in-place. Both must be RGBA 64×64. */
    private static void blendImages(NativeImage base, NativeImage overlay) {
        int w = Math.min(base.getWidth(),  overlay.getWidth());
        int h = Math.min(base.getHeight(), overlay.getHeight());
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int src = overlay.getPixelRGBA(x, y);
                int srcA = (src >> 24) & 0xFF;
                if (srcA == 0) continue;
                if (srcA == 255) {
                    base.setPixelRGBA(x, y, src);
                    continue;
                }
                int dst  = base.getPixelRGBA(x, y);
                int dstA = (dst >> 24) & 0xFF;
                float sa = srcA / 255f, da = dstA / 255f;
                float outA = sa + da * (1 - sa);
                if (outA < 0.001f) {
                    base.setPixelRGBA(x, y, 0);
                    continue;
                }
                int r = blend((src) & 0xFF, (dst) & 0xFF, sa, da, outA);
                int g = blend((src >> 8) & 0xFF, (dst >> 8) & 0xFF, sa, da, outA);
                int b = blend((src >> 16) & 0xFF, (dst >> 16) & 0xFF, sa, da, outA);
                int a = Math.min(255, (int)(outA * 255));
                base.setPixelRGBA(x, y, r | (g << 8) | (b << 16) | (a << 24));
            }
        }
    }

    private static int blend(int sc, int dc, float sa, float da, float outA) {
        return Math.min(255, (int)((sc * sa + dc * da * (1 - sa)) / outA));
    }

    public static void openLayersFolder(String category) {
        try {
            java.awt.Desktop.getDesktop().open(LAYERS_DIR.resolve(category).toFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openLayersRootFolder() {
        try {
            java.awt.Desktop.getDesktop().open(LAYERS_DIR.toFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

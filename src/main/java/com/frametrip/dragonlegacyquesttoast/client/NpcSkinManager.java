package com.frametrip.dragonlegacyquesttoast.client;
 
import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
 
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
 
public class NpcSkinManager {
 
    public static final ResourceLocation DEFAULT_SKIN =
        new ResourceLocation("textures/entity/player/wide/steve.png");
 
    private static final Path SKINS_DIR = FMLPaths.CONFIGDIR.get()
        .resolve("dragonlegacyquesttoast/skins");
 
    private static final Map<String, ResourceLocation> loaded = new LinkedHashMap<>();
    private static final List<String> skinIds = new ArrayList<>();
 
    public static void init() {
        try {
            Files.createDirectories(SKINS_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
        refresh();
    }
 
    public static void refresh() {
        loaded.clear();
        skinIds.clear();
        skinIds.add("default");
 
        if (!Files.isDirectory(SKINS_DIR)) return;
 
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(SKINS_DIR, "*.png")) {
            for (Path path : stream) {
                String name = path.getFileName().toString();
                String id   = name.substring(0, name.length() - 4); // strip ".png"
                skinIds.add(id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    public static ResourceLocation getTexture(String skinId) {
        if (skinId == null || skinId.equals("default")) return DEFAULT_SKIN;
        if (loaded.containsKey(skinId)) return loaded.get(skinId);
 
        Path file = SKINS_DIR.resolve(skinId + ".png");
        if (!Files.exists(file)) return DEFAULT_SKIN;
 
        try (InputStream is = Files.newInputStream(file)) {
            NativeImage img = NativeImage.read(is);
            DynamicTexture tex = new DynamicTexture(img);
            ResourceLocation rl = new ResourceLocation(DragonLegacyQuestToastMod.MODID,
                "dynamic/skin/" + skinId);
            Minecraft.getInstance().getTextureManager().register(rl, tex);
            loaded.put(skinId, rl);
            return rl;
        } catch (IOException e) {
            e.printStackTrace();
            return DEFAULT_SKIN;
        }
    }
 
    public static List<String> getAvailableSkins() {
        return Collections.unmodifiableList(skinIds);
    }
 
    public static void openSkinsFolder() {
        try {
            java.awt.Desktop.getDesktop().open(SKINS_DIR.toFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

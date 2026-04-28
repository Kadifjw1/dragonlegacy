package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.server.NpcAppearancePreset;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class NpcAppearancePresetManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get()
            .resolve("dragonlegacyquesttoast-appearance-presets.json");

    private static final List<NpcAppearancePreset> PRESETS = new ArrayList<>();

    private NpcAppearancePresetManager() {}

    public static void load() {
        try {
            if (!Files.exists(FILE)) {
                save();
                return;
            }
            try (Reader r = Files.newBufferedReader(FILE)) {
                Type t = new TypeToken<List<NpcAppearancePreset>>(){}.getType();
                List<NpcAppearancePreset> list = GSON.fromJson(r, t);
                PRESETS.clear();
                if (list != null) PRESETS.addAll(list);
            }
        } catch (Exception e) {
            System.err.println("[DL] Failed to load appearance presets: " + e.getMessage());
        }
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(PRESETS, w);
        } catch (Exception e) {
            System.err.println("[DL] Failed to save appearance presets: " + e.getMessage());
        }
    }

    public static List<NpcAppearancePreset> getAll() {
        return PRESETS;
    }

    public static void savePreset(String name, NpcEntityData data) {
        if (data == null) return;
        NpcAppearancePreset p = new NpcAppearancePreset();
        p.name = (name == null || name.isBlank()) ? "Пресет" : name;
        p.skinId = data.skinId;
        p.textureLayers = new LinkedHashMap<>(data.textureLayers);
        p.bodyParts = new LinkedHashMap<>(data.bodyParts);
        PRESETS.add(p);
        save();
    }

    public static void applyPreset(NpcAppearancePreset p, NpcEntityData data) {
        if (p == null || data == null) return;
        data.skinId = p.skinId == null || p.skinId.isBlank() ? "default" : p.skinId;
        data.textureLayers.clear();
        data.textureLayers.putAll(p.textureLayers);
        data.bodyParts.clear();
        data.bodyParts.putAll(p.bodyParts);
    }
}

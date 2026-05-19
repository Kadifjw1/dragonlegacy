package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.server.model.NpcGeckoPreset;
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
import java.util.List;

// [MOD-3]: Client-side manager for saved GeckoLib resource presets.
public final class NpcGeckoPresetManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get()
            .resolve("dragonlegacyquesttoast-gecko-presets.json");

    private static final List<NpcGeckoPreset> PRESETS = new ArrayList<>();

    private NpcGeckoPresetManager() {}

    public static void load() {
        PRESETS.clear();
        try {
            if (!Files.exists(FILE)) { save(); return; }
            try (Reader r = Files.newBufferedReader(FILE)) {
                Type t = new TypeToken<List<NpcGeckoPreset>>(){}.getType();
                List<NpcGeckoPreset> list = GSON.fromJson(r, t);
                if (list != null) PRESETS.addAll(list);
            }
        } catch (Exception e) {
            System.err.println("[DL] Failed to load gecko presets: " + e.getMessage());
        }
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(PRESETS, w);
        } catch (Exception e) {
            System.err.println("[DL] Failed to save gecko presets: " + e.getMessage());
        }
    }

    public static List<NpcGeckoPreset> getAll() { return PRESETS; }

    public static void add(NpcGeckoPreset preset) {
        PRESETS.add(preset);
        save();
    }

    public static void remove(int index) {
        if (index >= 0 && index < PRESETS.size()) {
            PRESETS.remove(index);
            save();
        }
    }
}

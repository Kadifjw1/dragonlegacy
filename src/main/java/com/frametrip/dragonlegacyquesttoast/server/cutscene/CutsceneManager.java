package com.frametrip.dragonlegacyquesttoast.server.cutscene;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class CutsceneManager {
    private static final Logger LOGGER = LogManager.getLogger(DragonLegacyQuestToastMod.MODID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, CutsceneDefinition> CUTSCENES = new LinkedHashMap<>();

    private static File cutsceneDir() {
        File dir = FMLPaths.CONFIGDIR.get()
                .resolve(DragonLegacyQuestToastMod.MODID)
                .resolve("cutscenes")
                .toFile();
        dir.mkdirs();
        return dir;
    }

    public static void load() {
        CUTSCENES.clear();
        File dir = cutsceneDir();
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null) return;
        for (File f : files) {
            try (FileReader r = new FileReader(f)) {
                CutsceneDefinition def = GSON.fromJson(r, CutsceneDefinition.class);
                if (def != null && def.id != null && !def.id.isEmpty()) {
                    CUTSCENES.put(def.id, def);
                }
            } catch (IOException e) {
                LOGGER.warn("Failed to load cutscene {}: {}", f.getName(), e.getMessage());
            }
        }
    }

    public static void save(CutsceneDefinition def) {
        CUTSCENES.put(def.id, def);
        File f = new File(cutsceneDir(), def.id + ".json");
        try (FileWriter w = new FileWriter(f)) {
            GSON.toJson(def, w);
        } catch (IOException e) {
            LOGGER.warn("Failed to save cutscene {}: {}", def.id, e.getMessage());
        }
    }

    public static CutsceneDefinition get(String id) {
        return CUTSCENES.get(id);
    }

    public static Collection<CutsceneDefinition> getAll() {
        return CUTSCENES.values();
    }
}

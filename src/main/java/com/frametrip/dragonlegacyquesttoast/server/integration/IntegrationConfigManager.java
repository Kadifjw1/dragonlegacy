package com.frametrip.dragonlegacyquesttoast.server.integration;

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

public class IntegrationConfigManager {

    private static final Logger LOGGER = LogManager.getLogger(DragonLegacyQuestToastMod.MODID);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static IntegrationConfig config = new IntegrationConfig();

    private static File configFile() {
        File dir = FMLPaths.CONFIGDIR.get()
                .resolve(DragonLegacyQuestToastMod.MODID)
                .toFile();
        dir.mkdirs();
        return new File(dir, "integrations.json");
    }

    public static void load() {
        File f = configFile();
        if (!f.exists()) {
            config = new IntegrationConfig();
            save();
            return;
        }
        try (FileReader r = new FileReader(f)) {
            IntegrationConfig loaded = GSON.fromJson(r, IntegrationConfig.class);
            config = loaded != null ? loaded : new IntegrationConfig();
        } catch (IOException e) {
            LOGGER.warn("Failed to load integrations config: {}", e.getMessage());
            config = new IntegrationConfig();
        }
    }

    public static void save() {
        try (FileWriter w = new FileWriter(configFile())) {
            GSON.toJson(config, w);
        } catch (IOException e) {
            LOGGER.warn("Failed to save integrations config: {}", e.getMessage());
        }
    }

    public static IntegrationConfig get() { return config; }
}

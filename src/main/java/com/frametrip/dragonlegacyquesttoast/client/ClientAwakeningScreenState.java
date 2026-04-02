package com.frametrip.dragonlegacyquesttoast.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientAwakeningScreenState {
    public static class AwakeningScreenConfigData {
        public int bgX = 0;
        public int bgY = 0;
        public int bgWidth = 320;
        public int bgHeight = 220;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FMLPaths.CONFIGDIR.get().resolve("dragonlegacyquesttoast-awakening-screen.json");

    private static int bgX = 0;
    private static int bgY = 0;
    private static int bgWidth = 320;
    private static int bgHeight = 220;

    static {
        loadConfig();
    }

    public static void applyBackgroundConfig(int x, int y, int width, int height) {
        bgX = x;
        bgY = y;
        bgWidth = Math.max(1, width);
        bgHeight = Math.max(1, height);
        saveConfig();
    }

    public static void resetBackgroundConfig() {
        bgX = 0;
        bgY = 0;
        bgWidth = 320;
        bgHeight = 220;
        saveConfig();
    }

    public static int getBgX() {
        return bgX;
    }

    public static int getBgY() {
        return bgY;
    }

    public static int getBgWidth() {
        return bgWidth;
    }

    public static int getBgHeight() {
        return bgHeight;
    }

    private static void loadConfig() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                saveConfig();
                return;
            }

            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                AwakeningScreenConfigData data = GSON.fromJson(reader, AwakeningScreenConfigData.class);
                if (data == null) {
                    saveConfig();
                    return;
                }

                bgX = data.bgX;
                bgY = data.bgY;
                bgWidth = Math.max(1, data.bgWidth);
                bgHeight = Math.max(1, data.bgHeight);
            }
        } catch (Exception e) {
            System.out.println("[DragonLegacyQuestToast] Failed to load awakening screen config: " + e.getMessage());
        }
    }

    private static void saveConfig() {
        try {
            AwakeningScreenConfigData data = new AwakeningScreenConfigData();
            data.bgX = bgX;
            data.bgY = bgY;
            data.bgWidth = bgWidth;
            data.bgHeight = bgHeight;

            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            System.out.println("[DragonLegacyQuestToast] Failed to save awakening screen config: " + e.getMessage());
        }
    }
}

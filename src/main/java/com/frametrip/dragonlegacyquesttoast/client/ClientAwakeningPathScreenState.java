package com.frametrip.dragonlegacyquesttoast.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientAwakeningPathScreenState {
    public static class PathScreenConfigData {
        public int fireBgX = 0;
        public int fireBgY = 0;
        public int fireBgWidth = 320;
        public int fireBgHeight = 220;

        public int iceBgX = 0;
        public int iceBgY = 0;
        public int iceBgWidth = 320;
        public int iceBgHeight = 220;

        public int stormBgX = 0;
        public int stormBgY = 0;
        public int stormBgWidth = 320;
        public int stormBgHeight = 220;

        public int voidBgX = 0;
        public int voidBgY = 0;
        public int voidBgWidth = 320;
        public int voidBgHeight = 220;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FMLPaths.CONFIGDIR.get().resolve("dragonlegacyquesttoast-awakening-path-screens.json");

    private static int fireBgX = 0;
    private static int fireBgY = 0;
    private static int fireBgWidth = 320;
    private static int fireBgHeight = 220;

    private static int iceBgX = 0;
    private static int iceBgY = 0;
    private static int iceBgWidth = 320;
    private static int iceBgHeight = 220;

    private static int stormBgX = 0;
    private static int stormBgY = 0;
    private static int stormBgWidth = 320;
    private static int stormBgHeight = 220;

    private static int voidBgX = 0;
    private static int voidBgY = 0;
    private static int voidBgWidth = 320;
    private static int voidBgHeight = 220;

    static {
        loadConfig();
    }

    public static void applyPathBackgroundConfig(AwakeningPathType pathType, int x, int y, int width, int height) {
        width = Math.max(1, width);
        height = Math.max(1, height);

        switch (pathType) {
            case FIRE -> {
                fireBgX = x;
                fireBgY = y;
                fireBgWidth = width;
                fireBgHeight = height;
            }
            case ICE -> {
                iceBgX = x;
                iceBgY = y;
                iceBgWidth = width;
                iceBgHeight = height;
            }
            case STORM -> {
                stormBgX = x;
                stormBgY = y;
                stormBgWidth = width;
                stormBgHeight = height;
            }
            case VOID -> {
                voidBgX = x;
                voidBgY = y;
                voidBgWidth = width;
                voidBgHeight = height;
            }
        }

        saveConfig();
    }

    public static void resetPathBackgroundConfig(AwakeningPathType pathType) {
        applyPathBackgroundConfig(pathType, 0, 0, 320, 220);
    }

    public static int getBgX(AwakeningPathType pathType) {
        return switch (pathType) {
            case FIRE -> fireBgX;
            case ICE -> iceBgX;
            case STORM -> stormBgX;
            case VOID -> voidBgX;
        };
    }

    public static int getBgY(AwakeningPathType pathType) {
        return switch (pathType) {
            case FIRE -> fireBgY;
            case ICE -> iceBgY;
            case STORM -> stormBgY;
            case VOID -> voidBgY;
        };
    }

    public static int getBgWidth(AwakeningPathType pathType) {
        return switch (pathType) {
            case FIRE -> fireBgWidth;
            case ICE -> iceBgWidth;
            case STORM -> stormBgWidth;
            case VOID -> voidBgWidth;
        };
    }

    public static int getBgHeight(AwakeningPathType pathType) {
        return switch (pathType) {
            case FIRE -> fireBgHeight;
            case ICE -> iceBgHeight;
            case STORM -> stormBgHeight;
            case VOID -> voidBgHeight;
        };
    }

    private static void loadConfig() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                saveConfig();
                return;
            }

            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                PathScreenConfigData data = GSON.fromJson(reader, PathScreenConfigData.class);
                if (data == null) {
                    saveConfig();
                    return;
                }

                fireBgX = data.fireBgX;
                fireBgY = data.fireBgY;
                fireBgWidth = Math.max(1, data.fireBgWidth);
                fireBgHeight = Math.max(1, data.fireBgHeight);

                iceBgX = data.iceBgX;
                iceBgY = data.iceBgY;
                iceBgWidth = Math.max(1, data.iceBgWidth);
                iceBgHeight = Math.max(1, data.iceBgHeight);

                stormBgX = data.stormBgX;
                stormBgY = data.stormBgY;
                stormBgWidth = Math.max(1, data.stormBgWidth);
                stormBgHeight = Math.max(1, data.stormBgHeight);

                voidBgX = data.voidBgX;
                voidBgY = data.voidBgY;
                voidBgWidth = Math.max(1, data.voidBgWidth);
                voidBgHeight = Math.max(1, data.voidBgHeight);
            }
        } catch (Exception e) {
            System.out.println("[DragonLegacyQuestToast] Failed to load path screen config: " + e.getMessage());
        }
    }

    private static void saveConfig() {
        try {
            PathScreenConfigData data = new PathScreenConfigData();

            data.fireBgX = fireBgX;
            data.fireBgY = fireBgY;
            data.fireBgWidth = fireBgWidth;
            data.fireBgHeight = fireBgHeight;

            data.iceBgX = iceBgX;
            data.iceBgY = iceBgY;
            data.iceBgWidth = iceBgWidth;
            data.iceBgHeight = iceBgHeight;

            data.stormBgX = stormBgX;
            data.stormBgY = stormBgY;
            data.stormBgWidth = stormBgWidth;
            data.stormBgHeight = stormBgHeight;

            data.voidBgX = voidBgX;
            data.voidBgY = voidBgY;
            data.voidBgWidth = voidBgWidth;
            data.voidBgHeight = voidBgHeight;

            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            System.out.println("[DragonLegacyQuestToast] Failed to save path screen config: " + e.getMessage());
        }
    }
}

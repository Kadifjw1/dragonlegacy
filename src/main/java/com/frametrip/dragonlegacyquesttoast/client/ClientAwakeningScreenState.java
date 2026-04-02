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

        public int centerFrameX = 112;
        public int centerFrameY = 44;
        public int centerFrameWidth = 96;
        public int centerFrameHeight = 96;

        public int playerOffsetX = 0;
        public int playerOffsetY = 8;
        public float playerScale = 38.0F;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FMLPaths.CONFIGDIR.get().resolve("dragonlegacyquesttoast-awakening-screen.json");

    private static int bgX = 0;
    private static int bgY = 0;
    private static int bgWidth = 320;
    private static int bgHeight = 220;

    private static int centerFrameX = 112;
    private static int centerFrameY = 44;
    private static int centerFrameWidth = 96;
    private static int centerFrameHeight = 96;

    private static int playerOffsetX = 0;
    private static int playerOffsetY = 8;
    private static float playerScale = 38.0F;

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

    public static void applyCenterConfig(int frameX, int frameY, int frameWidth, int frameHeight,
                                         int modelOffsetX, int modelOffsetY, float modelScale) {
        centerFrameX = frameX;
        centerFrameY = frameY;
        centerFrameWidth = Math.max(1, frameWidth);
        centerFrameHeight = Math.max(1, frameHeight);
        playerOffsetX = modelOffsetX;
        playerOffsetY = modelOffsetY;
        playerScale = Math.max(1.0F, modelScale);
        saveConfig();
    }

    public static void resetCenterConfig() {
        centerFrameX = 112;
        centerFrameY = 44;
        centerFrameWidth = 96;
        centerFrameHeight = 96;
        playerOffsetX = 0;
        playerOffsetY = 8;
        playerScale = 38.0F;
        saveConfig();
    }

    public static int getBgX() { return bgX; }
    public static int getBgY() { return bgY; }
    public static int getBgWidth() { return bgWidth; }
    public static int getBgHeight() { return bgHeight; }

    public static int getCenterFrameX() { return centerFrameX; }
    public static int getCenterFrameY() { return centerFrameY; }
    public static int getCenterFrameWidth() { return centerFrameWidth; }
    public static int getCenterFrameHeight() { return centerFrameHeight; }

    public static int getPlayerOffsetX() { return playerOffsetX; }
    public static int getPlayerOffsetY() { return playerOffsetY; }
    public static float getPlayerScale() { return playerScale; }

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

                centerFrameX = data.centerFrameX;
                centerFrameY = data.centerFrameY;
                centerFrameWidth = Math.max(1, data.centerFrameWidth);
                centerFrameHeight = Math.max(1, data.centerFrameHeight);

                playerOffsetX = data.playerOffsetX;
                playerOffsetY = data.playerOffsetY;
                playerScale = Math.max(1.0F, data.playerScale);
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

            data.centerFrameX = centerFrameX;
            data.centerFrameY = centerFrameY;
            data.centerFrameWidth = centerFrameWidth;
            data.centerFrameHeight = centerFrameHeight;

            data.playerOffsetX = playerOffsetX;
            data.playerOffsetY = playerOffsetY;
            data.playerScale = playerScale;

            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            System.out.println("[DragonLegacyQuestToast] Failed to save awakening screen config: " + e.getMessage());
        }
    }
}

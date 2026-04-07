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

        public int pathFrameSize = 48;
        public int pathIconSize = 32;

        public int fireX = 136;
        public int fireY = 12;

        public int iceX = 56;
        public int iceY = 68;

        public int stormX = 216;
        public int stormY = 68;

        public int voidX = 136;
        public int voidY = 148;

        public int attributesPanelX = 8;
        public int attributesPanelY = 132;
        public int attributesPanelWidth = 120;
        public int attributesPanelHeight = 80;

        public int attributesContentOffsetX = 8;
        public int attributesContentOffsetY = 14;
        public int attributesRowSpacing = 14;

        public int attributesIconSize = 16;
        public int attributesTextOffsetX = 20;
        public int attributesValueOffsetX = 92;

        public int attributesHoverWidth = 168;
        public int attributesHoverHeight = 34;
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

    private static int pathFrameSize = 48;
    private static int pathIconSize = 32;

    private static int fireX = 136;
    private static int fireY = 12;

    private static int iceX = 56;
    private static int iceY = 68;

    private static int stormX = 216;
    private static int stormY = 68;

    private static int voidX = 136;
    private static int voidY = 148;

    private static int attributesPanelX = 8;
    private static int attributesPanelY = 132;
    private static int attributesPanelWidth = 120;
    private static int attributesPanelHeight = 80;

    private static int attributesContentOffsetX = 8;
    private static int attributesContentOffsetY = 14;
    private static int attributesRowSpacing = 14;

    private static int attributesIconSize = 16;
    private static int attributesTextOffsetX = 20;
    private static int attributesValueOffsetX = 92;

    private static int attributesHoverWidth = 168;
    private static int attributesHoverHeight = 34;

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

    public static void applyPathsConfig(int newPathFrameSize, int newPathIconSize,
                                        int newFireX, int newFireY,
                                        int newIceX, int newIceY,
                                        int newStormX, int newStormY,
                                        int newVoidX, int newVoidY) {
        pathFrameSize = Math.max(1, newPathFrameSize);
        pathIconSize = Math.max(1, newPathIconSize);

        fireX = newFireX;
        fireY = newFireY;
        iceX = newIceX;
        iceY = newIceY;
        stormX = newStormX;
        stormY = newStormY;
        voidX = newVoidX;
        voidY = newVoidY;

        saveConfig();
    }

    public static void resetPathsConfig() {
        pathFrameSize = 48;
        pathIconSize = 32;

        fireX = 136;
        fireY = 12;

        iceX = 56;
        iceY = 68;

        stormX = 216;
        stormY = 68;

        voidX = 136;
        voidY = 148;

        saveConfig();
    }

    public static void applyAttributesConfig(
            int panelX,
            int panelY,
            int panelWidth,
            int panelHeight,
            int contentOffsetX,
            int contentOffsetY,
            int rowSpacing,
            int iconSize,
            int textOffsetX,
            int valueOffsetX,
            int hoverWidth,
            int hoverHeight
    ) {
        attributesPanelX = panelX;
        attributesPanelY = panelY;
        attributesPanelWidth = Math.max(1, panelWidth);
        attributesPanelHeight = Math.max(1, panelHeight);

        attributesContentOffsetX = contentOffsetX;
        attributesContentOffsetY = contentOffsetY;
        attributesRowSpacing = Math.max(1, rowSpacing);

        attributesIconSize = Math.max(1, iconSize);
        attributesTextOffsetX = textOffsetX;
        attributesValueOffsetX = valueOffsetX;

        attributesHoverWidth = Math.max(1, hoverWidth);
        attributesHoverHeight = Math.max(1, hoverHeight);

        saveConfig();
    }

    public static void resetAttributesConfig() {
        attributesPanelX = 8;
        attributesPanelY = 132;
        attributesPanelWidth = 120;
        attributesPanelHeight = 80;

        attributesContentOffsetX = 8;
        attributesContentOffsetY = 14;
        attributesRowSpacing = 14;

        attributesIconSize = 16;
        attributesTextOffsetX = 20;
        attributesValueOffsetX = 92;

        attributesHoverWidth = 168;
        attributesHoverHeight = 34;

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

    public static int getPathFrameSize() { return pathFrameSize; }
    public static int getPathIconSize() { return pathIconSize; }

    public static int getFireX() { return fireX; }
    public static int getFireY() { return fireY; }

    public static int getIceX() { return iceX; }
    public static int getIceY() { return iceY; }

    public static int getStormX() { return stormX; }
    public static int getStormY() { return stormY; }

    public static int getVoidX() { return voidX; }
    public static int getVoidY() { return voidY; }

    public static int getAttributesPanelX() { return attributesPanelX; }
    public static int getAttributesPanelY() { return attributesPanelY; }
    public static int getAttributesPanelWidth() { return attributesPanelWidth; }
    public static int getAttributesPanelHeight() { return attributesPanelHeight; }

    public static int getAttributesContentOffsetX() { return attributesContentOffsetX; }
    public static int getAttributesContentOffsetY() { return attributesContentOffsetY; }
    public static int getAttributesRowSpacing() { return attributesRowSpacing; }

    public static int getAttributesIconSize() { return attributesIconSize; }
    public static int getAttributesTextOffsetX() { return attributesTextOffsetX; }
    public static int getAttributesValueOffsetX() { return attributesValueOffsetX; }

    public static int getAttributesHoverWidth() { return attributesHoverWidth; }
    public static int getAttributesHoverHeight() { return attributesHoverHeight; }

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

                pathFrameSize = Math.max(1, data.pathFrameSize);
                pathIconSize = Math.max(1, data.pathIconSize);

                fireX = data.fireX;
                fireY = data.fireY;

                iceX = data.iceX;
                iceY = data.iceY;

                stormX = data.stormX;
                stormY = data.stormY;

                voidX = data.voidX;
                voidY = data.voidY;

                attributesPanelX = data.attributesPanelX;
                attributesPanelY = data.attributesPanelY;
                attributesPanelWidth = Math.max(1, data.attributesPanelWidth);
                attributesPanelHeight = Math.max(1, data.attributesPanelHeight);

                attributesContentOffsetX = data.attributesContentOffsetX;
                attributesContentOffsetY = data.attributesContentOffsetY;
                attributesRowSpacing = Math.max(1, data.attributesRowSpacing);

                attributesIconSize = Math.max(1, data.attributesIconSize);
                attributesTextOffsetX = data.attributesTextOffsetX;
                attributesValueOffsetX = data.attributesValueOffsetX;

                attributesHoverWidth = Math.max(1, data.attributesHoverWidth);
                attributesHoverHeight = Math.max(1, data.attributesHoverHeight);
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

            data.pathFrameSize = pathFrameSize;
            data.pathIconSize = pathIconSize;

            data.fireX = fireX;
            data.fireY = fireY;

            data.iceX = iceX;
            data.iceY = iceY;

            data.stormX = stormX;
            data.stormY = stormY;

            data.voidX = voidX;
            data.voidY = voidY;

            data.attributesPanelX = attributesPanelX;
            data.attributesPanelY = attributesPanelY;
            data.attributesPanelWidth = attributesPanelWidth;
            data.attributesPanelHeight = attributesPanelHeight;

            data.attributesContentOffsetX = attributesContentOffsetX;
            data.attributesContentOffsetY = attributesContentOffsetY;
            data.attributesRowSpacing = attributesRowSpacing;

            data.attributesIconSize = attributesIconSize;
            data.attributesTextOffsetX = attributesTextOffsetX;
            data.attributesValueOffsetX = attributesValueOffsetX;

            data.attributesHoverWidth = attributesHoverWidth;
            data.attributesHoverHeight = attributesHoverHeight;

            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            System.out.println("[DragonLegacyQuestToast] Failed to save awakening screen config: " + e.getMessage());
        }
    }
}

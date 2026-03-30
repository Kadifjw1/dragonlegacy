package com.frametrip.dragonlegacyquesttoast.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ClientNpcDialogueManager {
    public static class DialogueConfigData {
        public int x = 32;
        public int yOffsetFromBottom = 56;
        public int width = 256;
        public int height = 48;
        public int fadeInTicks = 6;
        public int stayTicks = 80;
        public int fadeOutTicks = 6;
        public int textMaxCharsPerLine = 34;
        public int textMaxLines = 2;
        public int nameXOffset = 12;
        public int nameYOffset = 10;
        public int textXOffset = 12;
        public int textYOffset = 22;
        public int textLineHeight = 10;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("dragonlegacyquesttoast-dialogue-client.json");

    private static String npcName = "";
    private static String text = "";
    private static int age = 0;

    private static int x = 32;
    private static int yOffsetFromBottom = 56;
    private static int width = 256;
    private static int height = 48;
    private static int fadeInTicks = 6;
    private static int stayTicks = 80;
    private static int fadeOutTicks = 6;

    private static int textMaxCharsPerLine = 34;
    private static int textMaxLines = 2;

    private static int nameXOffset = 12;
    private static int nameYOffset = 10;
    private static int textXOffset = 12;
    private static int textYOffset = 22;
    private static int textLineHeight = 10;

    static {
        loadConfig();
    }

    public static void show(String newNpcName, String newText) {
        npcName = newNpcName == null ? "" : newNpcName;
        text = newText == null ? "" : newText;
        age = 0;
    }

    public static void applyConfig(
            int newX,
            int newYOffsetFromBottom,
            int newWidth,
            int newHeight,
            int newFadeInTicks,
            int newStayTicks,
            int newFadeOutTicks
    ) {
        x = newX;
        yOffsetFromBottom = newYOffsetFromBottom;
        width = Math.max(1, newWidth);
        height = Math.max(1, newHeight);
        fadeInTicks = Math.max(1, newFadeInTicks);
        stayTicks = Math.max(1, newStayTicks);
        fadeOutTicks = Math.max(1, newFadeOutTicks);

        saveConfig();
    }

    public static void applyTextLayoutConfig(
            int newTextMaxCharsPerLine,
            int newTextMaxLines,
            int newNameXOffset,
            int newNameYOffset,
            int newTextXOffset,
            int newTextYOffset,
            int newTextLineHeight
    ) {
        textMaxCharsPerLine = Math.max(1, newTextMaxCharsPerLine);
        textMaxLines = Math.max(1, newTextMaxLines);
        nameXOffset = newNameXOffset;
        nameYOffset = newNameYOffset;
        textXOffset = newTextXOffset;
        textYOffset = newTextYOffset;
        textLineHeight = Math.max(1, newTextLineHeight);

        saveConfig();
    }

    public static void resetConfig() {
        x = 32;
        yOffsetFromBottom = 56;
        width = 256;
        height = 48;
        fadeInTicks = 6;
        stayTicks = 80;
        fadeOutTicks = 6;

        textMaxCharsPerLine = 34;
        textMaxLines = 2;
        nameXOffset = 12;
        nameYOffset = 10;
        textXOffset = 12;
        textYOffset = 22;
        textLineHeight = 10;

        saveConfig();
    }

    public static void tick() {
        if (!isActive()) return;

        age++;
        if (age >= getTotalTicks()) {
            npcName = "";
            text = "";
            age = 0;
        }
    }

    public static boolean isActive() {
        return text != null && !text.isEmpty() && age < getTotalTicks();
    }

    public static int getTotalTicks() {
        return fadeInTicks + stayTicks + fadeOutTicks;
    }

    public static float getAlpha() {
        if (!isActive()) return 0f;

        if (age <= fadeInTicks) {
            float t = age / (float) fadeInTicks;
            return clamp01(easeOutCubic(t));
        }

        if (age >= fadeInTicks + stayTicks) {
            int localAge = age - (fadeInTicks + stayTicks);
            float t = localAge / (float) fadeOutTicks;
            return clamp01(1f - easeInCubic(t));
        }

        return 1f;
    }

    public static int getX() {
        return x;
    }

    public static int getY(int screenHeight) {
        return screenHeight - yOffsetFromBottom;
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    public static String getNpcName() {
        return npcName;
    }

    public static int getNameXOffset() {
        return nameXOffset;
    }

    public static int getNameYOffset() {
        return nameYOffset;
    }

    public static int getTextXOffset() {
        return textXOffset;
    }

    public static int getTextYOffset() {
        return textYOffset;
    }

    public static int getTextLineHeight() {
        return textLineHeight;
    }

    public static List<String> wrapText(String input, int maxCharsPerLine, int maxLines) {
        List<String> result = new ArrayList<>();
        if (input == null || input.isEmpty()) return result;

        String[] words = input.trim().split("\\s+");
        String current = "";

        for (String word : words) {
            String test = current.isEmpty() ? word : current + " " + word;

            if (test.length() <= maxCharsPerLine) {
                current = test
;
            } else {
                if (!current.isEmpty()) {
                    result.add(current);
                }
                current = word;

                if (result.size() >= maxLines) break;
            }
        }

        if (!current.isEmpty() && result.size() < maxLines) {
            result.add(current);
        }

        if (result.size() == maxLines) {
            int last = result.size() - 1;
            String line = result.get(last);
            if (line.length() > maxCharsPerLine - 1) {
                result.set(last, line.substring(0, maxCharsPerLine - 1) + "…");
            }
        }

        return result;
    }

    public static List<String> getWrappedText() {
        return wrapText(text, textMaxCharsPerLine, textMaxLines);
    }

    private static float easeOutCubic(float t) {
        t = clamp01(t);
        return 1f - (float)Math.pow(1f - t, 3);
    }

    private static float easeInCubic(float t) {
        t = clamp01(t);
        return t * t * t;
    }

    private static float clamp01(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }

    private static void loadConfig() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                saveConfig();
                return;
            }

            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                DialogueConfigData data = GSON.fromJson(reader, DialogueConfigData.class);

                if (data == null) {
                    saveConfig();
                    return;
                }

                x = data.x;
                yOffsetFromBottom = data.yOffsetFromBottom;
                width = Math.max(1, data.width);
                height = Math.max(1, data.height);
                fadeInTicks = Math.max(1, data.fadeInTicks);
                stayTicks = Math.max(1, data.stayTicks);
                fadeOutTicks = Math.max(1, data.fadeOutTicks);

                textMaxCharsPerLine = Math.max(1, data.textMaxCharsPerLine);
                textMaxLines = Math.max(1, data.textMaxLines);
                nameXOffset = data.nameXOffset;
                nameYOffset = data.nameYOffset;
                textXOffset = data.textXOffset;
                textYOffset = data.textYOffset;
                textLineHeight = Math.max(1, data.textLineHeight);
            }
        } catch (Exception e) {
            System.out.println("[DragonLegacyQuestToast] Failed to load dialogue config: " + e.getMessage());
        }
    }

    private static void saveConfig() {
        try {
            DialogueConfigData data = new DialogueConfigData();
            data.x = x;
            data.yOffsetFromBottom = yOffsetFromBottom;
            data.width = width;
            data.height = height;
            data.fadeInTicks = fadeInTicks;
            data.stayTicks = stayTicks;
            data.fadeOutTicks = fadeOutTicks;

            data.textMaxCharsPerLine = textMaxCharsPerLine;
            data.textMaxLines = textMaxLines;
            data.nameXOffset = nameXOffset;
            data.nameYOffset = nameYOffset;
            data.textXOffset = textXOffset;
            data.textYOffset = textYOffset;
            data.textLineHeight = textLineHeight;

            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            System.out.println("[DragonLegacyQuestToast] Failed to save dialogue config: " + e.getMessage());
        }
    }
}

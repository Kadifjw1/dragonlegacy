package com.frametrip.dragonlegacyquesttoast.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ClientNpcDialogueManager {
    public static class DialogueConfigData {
        public int x = -1;
        public int yOffsetFromBottom = 56;

        public int minWidth = 120;
        public int maxWidth = 280;
        public int minHeight = 34;

        public int fadeInTicks = 6;
        public int stayTicks = 80;
        public int fadeOutTicks = 6;

        public int textMaxLines = 2;

        public int leftPadding = 12;
        public int rightPadding = 12;
        public int topPadding = 8;
        public int bottomPadding = 8;

        public int nameYOffset = 0;
        public int textYOffset = 14;
        public int textLineHeight = 10;
    }

    private static class DialogueEntry {
        public final String npcName;
        public final String text;

        public DialogueEntry(String npcName, String text) {
            this.npcName = npcName;
            this.text = text;
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("dragonlegacyquesttoast-dialogue-client.json");

    private static final Deque<DialogueEntry> QUEUE = new ArrayDeque<>();

    private static String npcName = "";
    private static String text = "";
    private static int age = 0;

    private static int x = -1;
    private static int yOffsetFromBottom = 56;

    private static int minWidth = 120;
    private static int maxWidth = 280;
    private static int minHeight = 34;

    private static int fadeInTicks = 6;
    private static int stayTicks = 80;
    private static int fadeOutTicks = 6;

    private static int textMaxLines = 2;

    private static int leftPadding = 12;
    private static int rightPadding = 12;
    private static int topPadding = 8;
    private static int bottomPadding = 8;

    private static int nameYOffset = 0;
    private static int textYOffset = 14;
    private static int textLineHeight = 10;

    static {
        loadConfig();
    }

    public static void show(String newNpcName, String newText) {
        List<String> pages = splitIntoPages(newText);

        if (pages.isEmpty()) {
            QUEUE.addLast(new DialogueEntry(newNpcName == null ? "" : newNpcName, ""));
        } else {
            for (String page : pages) {
                QUEUE.addLast(new DialogueEntry(newNpcName == null ? "" : newNpcName, page));
            }
        }

        if (!isActive()) {
            popNext();
        }
    }

    private static void popNext() {
        DialogueEntry next = QUEUE.pollFirst();
        if (next == null) {
            npcName = "";
            text = "";
            age = 0;
            return;
        }

        npcName = next.npcName;
        text = next.text;
        age = 0;
    }

    public static void applyConfig(
            int newX,
            int newYOffsetFromBottom,
            int newMinWidth,
            int newMaxWidth,
            int newMinHeight,
            int newFadeInTicks,
            int newStayTicks,
            int newFadeOutTicks
    ) {
        x = newX;
        yOffsetFromBottom = newYOffsetFromBottom;
        minWidth = Math.max(1, newMinWidth);
        maxWidth = Math.max(minWidth, newMaxWidth);
        minHeight = Math.max(1, newMinHeight);
        fadeInTicks = Math.max(1, newFadeInTicks);
        stayTicks = Math.max(1, newStayTicks);
        fadeOutTicks = Math.max(1, newFadeOutTicks);

        saveConfig();
    }

    public static void applyTextLayoutConfig(
            int newTextMaxLines,
            int newLeftPadding,
            int newRightPadding,
            int newTopPadding,
            int newBottomPadding,
            int newNameYOffset,
            int newTextYOffset,
            int newTextLineHeight
    ) {
        textMaxLines = Math.max(1, newTextMaxLines);
        leftPadding = Math.max(0, newLeftPadding);
        rightPadding = Math.max(0, newRightPadding);
        topPadding = Math.max(0, newTopPadding);
        bottomPadding = Math.max(0, newBottomPadding);
        nameYOffset = newNameYOffset;
        textYOffset = newTextYOffset;
        textLineHeight = Math.max(1, newTextLineHeight);

        saveConfig();
    }

    public static void resetConfig() {
        x = -1;
        yOffsetFromBottom = 56;

        minWidth = 120;
        maxWidth = 280;
        minHeight = 34;

        fadeInTicks = 6;
        stayTicks = 80;
        fadeOutTicks = 6;

        textMaxLines = 2;

        leftPadding = 12;
        rightPadding = 12;
        topPadding = 8;
        bottomPadding = 8;

        nameYOffset = 0;
        textYOffset = 14;
        textLineHeight = 10;

        saveConfig();
    }

    public static void tick() {
        if (!isActive()) {
            if (!QUEUE.isEmpty()) {
                popNext();
            }
            return;
        }

        age++;

        if (age >= getTotalTicks()) {
            if (!QUEUE.isEmpty()) {
                popNext();
            } else {
                npcName = "";
                text = "";
                age = 0;
            }
        }
    }

    public static boolean isActive() {
        return text != null && !text.isEmpty();
    }

    /** Immediately hide current dialogue bar and clear queued pages. */
    public static void clear() {
        QUEUE.clear();
        npcName = "";
        text = "";
        age = 0;
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

    public static int getX(int screenWidth) {
        if (x >= 0) return x;
        return (screenWidth - getWidth()) / 2;
    }

    public static int getY(int screenHeight) {
        return screenHeight - yOffsetFromBottom;
    }

    public static String getNpcName() {
        return npcName;
    }

    public static int getNameX() {
        return leftPadding;
    }

    public static int getNameY() {
        return topPadding + nameYOffset;
    }

    public static int getTextX() {
        return leftPadding;
    }

    public static int getTextY() {
        return topPadding + textYOffset;
    }

    public static int getTextLineHeight() {
        return textLineHeight;
    }

    public static int getWidth() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) return minWidth;

        int longest = mc.font.width(npcName);

        List<String> lines = getWrappedText();
        for (String line : lines) {
            longest = Math.max(longest, mc.font.width(line));
        }

        int calculated = leftPadding + longest + rightPadding;

        if (calculated < minWidth) calculated = minWidth;
        if (calculated > maxWidth) calculated = maxWidth;

        return calculated;
    }

    public static int getHeight() {
        int textLines = getWrappedText().size();
        if (textLines <= 0) textLines = 1;

        int contentBottom = getTextY() + (textLines * textLineHeight);
        int calculated = contentBottom + bottomPadding;

        if (calculated < minHeight) calculated = minHeight;
        return calculated;
    }

    public static List<String> wrapTextByWidth(String input, int maxPixelWidth) {
        List<String> result = new ArrayList<>();
        if (input == null || input.isEmpty()) return result;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) {
            result.add(input);
            return result;
        }

        String[] words = input.trim().split("\\s+");
        String current = "";

        for (String word : words) {
            String test = current.isEmpty() ? word : current + " " + word;

            if (mc.font.width(test) <= maxPixelWidth) {
                current = test;
            } else {
                if (!current.isEmpty()) {
                    result.add(current);
                }
                current = word;
            }
        }

        if (!current.isEmpty()) {
            result.add(current);
        }

        return result;
    }

    public static List<String> getWrappedText() {
        int innerWidth = maxWidth - leftPadding - rightPadding;
        if (innerWidth < 20) innerWidth = 20;

        List<String> lines = wrapTextByWidth(text, innerWidth);
        if (lines.size() <= textMaxLines) return lines;

        return new ArrayList<>(lines.subList(0, textMaxLines));
    }

    private static List<String> splitIntoPages(String fullText) {
        List<String> pages = new ArrayList<>();

        if (fullText == null || fullText.trim().isEmpty()) {
            return pages;
        }

        int innerWidth = maxWidth - leftPadding - rightPadding;
        if (innerWidth < 20) innerWidth = 20;

        List<String> allLines = wrapTextByWidth(fullText, innerWidth);
        if (allLines.isEmpty()) return pages;

        StringBuilder currentPage = new StringBuilder();
        int lineCount = 0;

        for (String line : allLines) {
            if (lineCount > 0) currentPage.append(" ");
            currentPage.append(line);
            lineCount++;

            if (lineCount >= textMaxLines) {
                pages.add(currentPage.toString());
                currentPage = new StringBuilder();
                lineCount = 0;
            }
        }

        if (currentPage.length() > 0) {
            pages.add(currentPage.toString());
        }

        return pages;
    }

    private static float easeOutCubic(float t) {
        t = clamp01(t);
        return 1f - (float) Math.pow(1f - t, 3);
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

                minWidth = Math.max(1, data.minWidth);
                maxWidth = Math.max(minWidth, data.maxWidth);
                minHeight = Math.max(1, data.minHeight);

                fadeInTicks = Math.max(1, data.fadeInTicks);
                stayTicks = Math.max(1, data.stayTicks);
                fadeOutTicks = Math.max(1, data.fadeOutTicks);

                textMaxLines = Math.max(1, data.textMaxLines);

                leftPadding = Math.max(0, data.leftPadding);
                rightPadding = Math.max(0, data.rightPadding);
                topPadding = Math.max(0, data.topPadding);
                bottomPadding = Math.max(0, data.bottomPadding);

                nameYOffset = data.nameYOffset;
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

            data.minWidth = minWidth;
            data.maxWidth = maxWidth;
            data.minHeight = minHeight;

            data.fadeInTicks = fadeInTicks;
            data.stayTicks = stayTicks;
            data.fadeOutTicks = fadeOutTicks;

            data.textMaxLines = textMaxLines;

            data.leftPadding = leftPadding;
            data.rightPadding = rightPadding;
            data.topPadding = topPadding;
            data.bottomPadding = bottomPadding;

            data.nameYOffset = nameYOffset;
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

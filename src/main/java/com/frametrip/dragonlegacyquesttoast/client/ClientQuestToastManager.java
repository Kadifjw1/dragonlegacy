package com.frametrip.dragonlegacyquesttoast.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

public class ClientQuestToastManager {
    public static class ToastEntry {
        public final String type;
        public final String questTitle;

        public ToastEntry(String type, String questTitle) {
            this.type = type;
            this.questTitle = questTitle;
        }
    }

    public static class ToastConfigData {
        public int x = 8;
        public int y = 8;
        public int width = 160;
        public int height = 40;
        public int fadeInTicks = 8;
        public int stayTicks = 124;
        public int fadeOutTicks = 8;
        public int startOffsetX = -180;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("dragonlegacyquesttoast-client.json");

    private static final Deque<ToastEntry> QUEUE = new ArrayDeque<>();

    private static ToastEntry currentToast = null;
    private static int age = 0;

    // ========= НАСТРОЙКИ =========
    private static int x = 8;
    private static int y = 8;
    private static int width = 160;
    private static int height = 40;
    private static int fadeInTicks = 8;
    private static int stayTicks = 124;
    private static int fadeOutTicks = 8;
    private static int startOffsetX = -180;

    static {
        loadConfig();
    }

    public static void show(String type, String questTitle) {
        QUEUE.addLast(new ToastEntry(type, questTitle));
        if (currentToast == null) {
            popNext();
        }
    }

    public static void applyConfig(
            int newX,
            int newY,
            int newWidth,
            int newHeight,
            int newFadeInTicks,
            int newStayTicks,
            int newFadeOutTicks,
            int newStartOffsetX
    ) {
        x = newX;
        y = newY;
        width = newWidth;
        height = newHeight;
        fadeInTicks = Math.max(1, newFadeInTicks);
        stayTicks = Math.max(1, newStayTicks);
        fadeOutTicks = Math.max(1, newFadeOutTicks);
        startOffsetX = newStartOffsetX;

        saveConfig();
    }

    public static void resetConfig() {
        x = 8;
        y = 8;
        width = 160;
        height = 40;
        fadeInTicks = 8;
        stayTicks = 124;
        fadeOutTicks = 8;
        startOffsetX = -180;

        saveConfig();
    }

    private static void popNext() {
        currentToast = QUEUE.pollFirst();
        age = 0;
    }

    public static void tick() {
        if (currentToast == null) {
            if (!QUEUE.isEmpty()) {
                popNext();
            }
            return;
        }

        age++;

        if (age >= getTotalTicks()) {
            currentToast = null;
            age = 0;

            if (!QUEUE.isEmpty()) {
                popNext();
            }
        }
    }

    public static boolean isActive() {
        return currentToast != null;
    }

    public static boolean isCompleted() {
        return currentToast != null && "completed".equals(currentToast.type);
    }

    public static String getQuestTitle() {
        return currentToast == null ? "" : currentToast.questTitle;
    }

    public static int getX() {
        if (currentToast == null) return x;

        int startX = x + startOffsetX;

        if (age <= fadeInTicks) {
            float t = age / (float) fadeInTicks;
            t = easeOutCubic(t);
            return (int) (startX + (x - startX) * t);
        }

        if (age >= fadeInTicks + stayTicks) {
            int localAge = age - (fadeInTicks + stayTicks);
            float t = localAge / (float) fadeOutTicks;
            t = easeInCubic(t);
            return (int) (x + (startX - x) * t);
        }

        return x;
    }

    public static float getAlpha() {
        if (currentToast == null) return 0f;

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

    public static int getY() {
        return y;
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    public static int getTotalTicks() {
        return fadeInTicks + stayTicks + fadeOutTicks;
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
                ToastConfigData data = GSON.fromJson(reader, ToastConfigData.class);

                if (data == null) {
                    saveConfig();
                    return;
                }

                x = data.x;
                y = data.y;
                width = data.width;
                height = data.height;
                fadeInTicks = Math.max(1, data.fadeInTicks);
                stayTicks = Math.max(1, data.stayTicks);
                fadeOutTicks = Math.max(1, data.fadeOutTicks);
                startOffsetX = data.startOffsetX;
            }
        } catch (Exception e) {
            System.out.println("[DragonLegacyQuestToast] Failed to load config: " + e.getMessage());
        }
    }

    private static void saveConfig() {
        try {
            ToastConfigData data = new ToastConfigData();
            data.x = x;
            data.y = y;
            data.width = width;
            data.height = height;
            data.fadeInTicks = fadeInTicks;
            data.stayTicks = stayTicks;
            data.fadeOutTicks = fadeOutTicks;
            data.startOffsetX = startOffsetX;

            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            System.out.println("[DragonLegacyQuestToast] Failed to save config: " + e.getMessage());
        }
    }
}

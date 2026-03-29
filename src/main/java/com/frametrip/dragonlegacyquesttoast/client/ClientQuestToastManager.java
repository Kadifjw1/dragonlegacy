package com.frametrip.dragonlegacyquesttoast.client;

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

    private static final Deque<ToastEntry> QUEUE = new ArrayDeque<>();

    private static ToastEntry currentToast = null;
    private static int age = 0;

    // ========= НАСТРОЙКИ =========
    private static int x = 8;
    private static int y = 8;

    private static int width = 128;
    private static int height = 32;

    private static int fadeInTicks = 8;
    private static int stayTicks = 124;
    private static int fadeOutTicks = 8;

    // Насколько слева стартует плашка при появлении
    private static int startOffsetX = -180;

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
}

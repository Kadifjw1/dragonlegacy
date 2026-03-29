package com.frametrip.dragonlegacyquesttoast.client;

import java.util.ArrayList;
import java.util.List;

public class ClientNpcDialogueManager {
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

    public static void show(String newNpcName, String newText) {
        npcName = newNpcName == null ? "" : newNpcName;
        text = newText == null ? "" : newText;
        age = 0;
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

    public static List<String> wrapText(String input, int maxCharsPerLine, int maxLines) {
        List<String> result = new ArrayList<>();
        if (input == null || input.isEmpty()) return result;

        String[] words = input.trim().split("\\s+");
        String current = "";

        for (String word : words) {
            String test = current.isEmpty() ? word : current + " " + word;

            if (test.length() <= maxCharsPerLine) {
                current = test;
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
        return wrapText(text, 34, 2);
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

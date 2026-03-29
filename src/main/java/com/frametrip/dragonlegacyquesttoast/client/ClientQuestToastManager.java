package com.frametrip.dragonlegacyquesttoast.client;

public class ClientQuestToastManager {
    private static String currentType = null;
    private static String currentQuestTitle = null;

    private static int age = 0;
    private static final int TOTAL_TICKS = 36;
    private static final int MOVE_TICKS = 8;

    private static final int START_X = -180;
    private static final int END_X = 8;
    private static final int Y = 8;

    public static void show(String type, String questTitle) {
        currentType = type;
        currentQuestTitle = questTitle;
        age = 0;
    }

    public static boolean isActive() {
        return currentType != null && currentQuestTitle != null && age < TOTAL_TICKS;
    }

    public static void tick() {
        if (!isActive()) {
            currentType = null;
            currentQuestTitle = null;
            age = 0;
            return;
        }

        age++;

        if (age >= TOTAL_TICKS) {
            currentType = null;
            currentQuestTitle = null;
            age = 0;
        }
    }

    public static int getX() {
        if (age <= MOVE_TICKS) {
            float t = age / (float) MOVE_TICKS;
            return (int) (START_X + (END_X - START_X) * t);
        }
        return END_X;
    }

    public static int getY() {
        return Y;
    }

    public static String getHeaderText() {
        if ("completed".equals(currentType)) {
            return "Завершено задание";
        }
        return "Получено новое задание";
    }

    public static String getQuestTitle() {
        return currentQuestTitle == null ? "" : currentQuestTitle;
    }

    public static boolean isCompleted() {
        return "completed".equals(currentType);
    }
}

package com.frametrip.dragonlegacyquesttoast.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// [QST-3]: Client-side cache of timed-quest deadline timestamps (epoch millis).
public class ClientQuestDeadlineState {

    private static Map<String, Long> deadlines = new HashMap<>();

    public static void sync(Map<String, Long> incoming) {
        deadlines = incoming != null ? new HashMap<>(incoming) : new HashMap<>();
    }

    public static Map<String, Long> getAll() {
        return Collections.unmodifiableMap(deadlines);
    }

    /** Returns remaining seconds for this quest, or -1 if not timed. */
    public static int getRemainingSeconds(String questId) {
        Long dl = deadlines.get(questId);
        if (dl == null || dl == 0) return -1;
        long remaining = dl - System.currentTimeMillis();
        return (int) Math.max(0, remaining / 1000);
    }
}

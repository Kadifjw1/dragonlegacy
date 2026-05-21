package com.frametrip.dragonlegacyquesttoast.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// [REL-2]: Client-side cache of the local player's faction reputation values.
public class ClientReputationState {

    private static Map<String, Integer> reputations = new HashMap<>();

    public static void sync(Map<String, Integer> data) {
        reputations = data != null ? new HashMap<>(data) : new HashMap<>();
    }

    public static int get(String factionId) {
        return reputations.getOrDefault(factionId, 0);
    }

    public static Map<String, Integer> getAll() {
        return Collections.unmodifiableMap(reputations);
    }
}

package com.frametrip.dragonlegacyquesttoast.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks numeric player<->faction reputation.
 * Values: negative = hostile, 0 = neutral, positive = friendly.
 * Storage: config/dragonlegacyquesttoast-reputation.json
 */
public class PlayerFactionReputationManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get()
            .resolve("dragonlegacyquesttoast-reputation.json");

    // playerId -> (factionId -> reputation)
    private static Map<String, Map<String, Integer>> data = new HashMap<>();

    static { load(); }

    public static int get(UUID playerId, String factionId) {
        Map<String, Integer> map = data.get(playerId.toString());
        return map == null ? 0 : map.getOrDefault(factionId, 0);
    }

    public static void add(UUID playerId, String factionId, int amount) {
        data.computeIfAbsent(playerId.toString(), k -> new HashMap<>())
            .merge(factionId, amount, Integer::sum);
        save();
    }

    public static void set(UUID playerId, String factionId, int value) {
        data.computeIfAbsent(playerId.toString(), k -> new HashMap<>())
            .put(factionId, value);
        save();
    }

    /** Returns "FRIENDLY" / "NEUTRAL" / "HOSTILE" based on reputation value. */
    public static String getRelation(UUID playerId, String factionId) {
        int rep = get(playerId, factionId);
        if (rep >= 25)  return "FRIENDLY";
        if (rep <= -25) return "HOSTILE";
        return "NEUTRAL";
    }

    // [REL-2]: Returns all faction→reputation entries for a player.
    public static Map<String, Integer> getAllForPlayer(UUID playerId) {
        Map<String, Integer> map = data.get(playerId.toString());
        return map != null ? new java.util.HashMap<>(map) : new java.util.HashMap<>();
    }

    /** No-op — called only to trigger class initialisation and load data from disk. */
    public static void init() {}

    private static void load() {
        try {
            if (!Files.exists(FILE)) { save(); return; }
            try (Reader r = Files.newBufferedReader(FILE)) {
                Type type = new TypeToken<Map<String, Map<String, Integer>>>() {}.getType();
                Map<String, Map<String, Integer>> loaded = GSON.fromJson(r, type);
                if (loaded != null) data = loaded;
            }
        } catch (Exception e) {
            System.out.println("[DL] Failed to load reputation data: " + e.getMessage());
        }
    }

    private static void save() {
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(data, w);
        } catch (Exception e) {
            System.out.println("[DL] Failed to save reputation data: " + e.getMessage());
        }
    }
}

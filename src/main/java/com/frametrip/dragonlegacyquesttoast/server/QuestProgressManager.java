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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
 
public class QuestProgressManager {
 
    private static class PlayerProgress {
        Map<String, Integer> progress  = new HashMap<>();
        Set<String>          active    = new HashSet<>();
        Set<String>          completed = new HashSet<>();
        Set<String>          failed    = new HashSet<>();
    }
 
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get()
        .resolve("dragonlegacyquesttoast-quest-progress.json");
 
    private static Map<String, PlayerProgress> data = new HashMap<>();
 
    static { load(); }
 
    // ── Progress queries ──────────────────────────────────────────────────────
 
    public static int getProgress(UUID playerId, String questId) {
        PlayerProgress pp = data.get(playerId.toString());
        return pp == null ? 0 : pp.progress.getOrDefault(questId, 0);
    }
 
    public static Map<String, Integer> getAllProgress(UUID playerId) {
        PlayerProgress pp = data.get(playerId.toString());
        return pp == null ? Collections.emptyMap() : new HashMap<>(pp.progress);
    }
 
    public static Set<String> getCompleted(UUID playerId) {
        PlayerProgress pp = data.get(playerId.toString());
        return pp == null ? Collections.emptySet() : new HashSet<>(pp.completed);
    }
 
    public static Set<String> getActive(UUID playerId) {
        PlayerProgress pp = data.get(playerId.toString());
        return pp == null ? Collections.emptySet() : new HashSet<>(pp.active);
    }

    public static Set<String> getFailed(UUID playerId) {
        PlayerProgress pp = data.get(playerId.toString());
        return pp == null ? Collections.emptySet() : new HashSet<>(pp.failed);
    }
 
    public static boolean isCompleted(UUID playerId, String questId) {
        PlayerProgress pp = data.get(playerId.toString());
        return pp != null && pp.completed.contains(questId);
    }
 
   public static boolean isActive(UUID playerId, String questId) {
        PlayerProgress pp = data.get(playerId.toString());
        return pp != null && pp.active.contains(questId);
    }

    public static boolean isFailed(UUID playerId, String questId) {
        PlayerProgress pp = data.get(playerId.toString());
        return pp != null && pp.failed.contains(questId);
    }
 
    // ── Progress updates ──────────────────────────────────────────────────────
 
    /**
     * Increments progress for a quest and checks for completion.
     * Returns true if this increment caused the quest to complete.
     */
    public static boolean increment(UUID playerId, String questId, int amount) {
        if (isCompleted(playerId, questId)) return false;
        if (isFailed(playerId, questId)) return false;
 
        QuestDefinition quest = QuestManager.get(questId);
        if (quest == null) return false;
 
        PlayerProgress pp = data.computeIfAbsent(playerId.toString(), k -> new PlayerProgress());
        pp.active.add(questId);
        int current = pp.progress.getOrDefault(questId, 0) + amount;
        pp.progress.put(questId, current);
 
        int required = quest.getRequiredCount();
        if (current >= required) {
            pp.active.remove(questId);
            pp.completed.add(questId);
            pp.progress.put(questId, required);
            save();
            return true;
        }
        save();
        return false;
    }
 
    /** Marks a quest directly as complete (for logic types like EXPLORE / DIALOGUE). */
    public static boolean complete(UUID playerId, String questId) {
        if (isCompleted(playerId, questId)) return false;
        PlayerProgress pp = data.computeIfAbsent(playerId.toString(), k -> new PlayerProgress());
        pp.active.add(questId);
        pp.failed.remove(questId);
        pp.active.remove(questId);
        pp.completed.add(questId);
        save();
        return true;
    }
 
public static boolean accept(UUID playerId, String questId) {
        if (questId == null || questId.isBlank()) return false;
        if (isCompleted(playerId, questId) || isFailed(playerId, questId)) return false;
        PlayerProgress pp = data.computeIfAbsent(playerId.toString(), k -> new PlayerProgress());
        boolean changed = pp.active.add(questId);
        if (changed) save();
        return changed;
    }

    public static boolean fail(UUID playerId, String questId) {
        if (questId == null || questId.isBlank()) return false;
        PlayerProgress pp = data.computeIfAbsent(playerId.toString(), k -> new PlayerProgress());
        boolean changed = pp.active.remove(questId);
        changed = pp.failed.add(questId) || changed;
        if (changed) save();
        return changed;
    }
 
    public static void reset(UUID playerId, String questId) {
        PlayerProgress pp = data.get(playerId.toString());
        if (pp != null) {
            pp.progress.remove(questId);
            pp.active.remove(questId);
            pp.completed.remove(questId);
            pp.failed.remove(questId);
            save();
        }
    }
 
    // ── Persistence ───────────────────────────────────────────────────────────
 
    public static synchronized void load() {
        try {
            if (!Files.exists(FILE)) { save(); return; }
            try (Reader r = Files.newBufferedReader(FILE)) {
                Type t = new TypeToken<Map<String, PlayerProgress>>() {}.getType();
                Map<String, PlayerProgress> loaded = GSON.fromJson(r, t);
                if (loaded != null) data = loaded;
            }
        } catch (Exception e) {
            System.err.println("[DL] Failed to load quest progress: " + e.getMessage());
        }
    }
 
    private static synchronized void save() {
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(data, w);
        } catch (Exception e) {
            System.err.println("[DL] Failed to save quest progress: " + e.getMessage());
        }
    }
}

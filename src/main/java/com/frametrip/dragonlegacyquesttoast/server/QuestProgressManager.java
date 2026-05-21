package com.frametrip.dragonlegacyquesttoast.server;
 
import com.frametrip.dragonlegacyquesttoast.server.quest.QuestChainController;
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
        // [QST-3]: questId -> System.currentTimeMillis() deadline (0 = no limit)
        Map<String, Long>    deadlines = new HashMap<>();
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
            if (pp.deadlines != null) pp.deadlines.remove(questId); // [QST-3]
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
        if (pp.deadlines != null) pp.deadlines.remove(questId);
        save();
        return true;
    }
 
public static boolean accept(UUID playerId, String questId) {
        if (questId == null || questId.isBlank()) return false;
        if (isCompleted(playerId, questId) || isFailed(playerId, questId)) return false;
        // [QST-1]: Chain prerequisite check.
        if (!QuestChainController.canStart(playerId, questId)) return false;
        PlayerProgress pp = data.computeIfAbsent(playerId.toString(), k -> new PlayerProgress());
        boolean changed = pp.active.add(questId);
        // [QST-3]: Record deadline if quest has a time limit.
        if (changed) {
            QuestDefinition def = QuestManager.get(questId);
            if (def != null && def.timeLimitSec > 0) {
                if (pp.deadlines == null) pp.deadlines = new HashMap<>();
                pp.deadlines.put(questId, System.currentTimeMillis() + def.timeLimitSec * 1000L);
            }
            save();
        }
        return changed;
    }

    // [QST-3]: Returns millis until deadline (positive), or 0 if no deadline.
    public static long getDeadlineMillis(UUID playerId, String questId) {
        PlayerProgress pp = data.get(playerId.toString());
        if (pp == null || pp.deadlines == null) return 0;
        Long dl = pp.deadlines.get(questId);
        return dl != null ? dl : 0;
    }

    // [QST-3]: Returns remaining seconds, or -1 if no deadline, or 0 if expired.
    public static int getRemainingSeconds(UUID playerId, String questId) {
        long dl = getDeadlineMillis(playerId, questId);
        if (dl == 0) return -1;
        long remaining = dl - System.currentTimeMillis();
        return (int) Math.max(0, remaining / 1000);
    }

    // [QST-3]: Returns all quests for this player that have passed their deadline.
    public static java.util.List<String> getExpiredQuests(UUID playerId) {
        PlayerProgress pp = data.get(playerId.toString());
        if (pp == null || pp.deadlines == null) return java.util.Collections.emptyList();
        long now = System.currentTimeMillis();
        java.util.List<String> expired = new java.util.ArrayList<>();
        for (Map.Entry<String, Long> e : pp.deadlines.entrySet()) {
            if (pp.active.contains(e.getKey()) && e.getValue() > 0 && now > e.getValue()) {
                expired.add(e.getKey());
            }
        }
        return expired;
    }

    public static boolean fail(UUID playerId, String questId) {
        if (questId == null || questId.isBlank()) return false;
        PlayerProgress pp = data.computeIfAbsent(playerId.toString(), k -> new PlayerProgress());
        boolean changed = pp.active.remove(questId);
        changed = pp.failed.add(questId) || changed;
        if (pp.deadlines != null) pp.deadlines.remove(questId);
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
            if (pp.deadlines != null) pp.deadlines.remove(questId);
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

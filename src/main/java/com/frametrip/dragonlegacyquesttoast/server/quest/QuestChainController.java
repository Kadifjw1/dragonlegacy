package com.frametrip.dragonlegacyquesttoast.server.quest;

import com.frametrip.dragonlegacyquesttoast.server.QuestProgressManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

// [QST-1]: Loads/saves quest chains and enforces chain prerequisites.
public class QuestChainController {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get()
            .resolve("dragonlegacyquesttoast-quest-chains.json");

    private static List<QuestChain> chains = new ArrayList<>();

    static { load(); }

    // ── Query ─────────────────────────────────────────────────────────────────

    public static List<QuestChain> getAll() {
        return Collections.unmodifiableList(chains);
    }

    public static QuestChain get(String chainId) {
        for (QuestChain c : chains) if (c.chainId.equals(chainId)) return c;
        return null;
    }

    /**
     * Returns true if the player may start the given quest.
     * A quest is allowed when either:
     *   - it doesn't appear in any chain at all, OR
     *   - it is the first link of its chain, OR
     *   - the previous link's quest is completed (or it appears in someone's unlocksOnComplete list).
     */
    public static boolean canStart(UUID playerId, String questId) {
        for (QuestChain chain : chains) {
            for (int i = 0; i < chain.links.size(); i++) {
                QuestChain.QuestChainLink link = chain.links.get(i);
                if (!link.questId.equals(questId)) continue;
                // First link in a chain — always unlocked.
                if (i == 0) return true;
                // Otherwise the previous link must be completed.
                QuestChain.QuestChainLink prev = chain.links.get(i - 1);
                return QuestProgressManager.isCompleted(playerId, prev.questId);
            }
            // Check explicit unlocksOnComplete lists
            for (QuestChain.QuestChainLink link : chain.links) {
                if (link.unlocksOnComplete.contains(questId)) {
                    return QuestProgressManager.isCompleted(playerId, link.questId);
                }
            }
        }
        // Not part of any chain — always allowed.
        return true;
    }

    // ── Mutation ──────────────────────────────────────────────────────────────

    public static void save(QuestChain chain) {
        chains.removeIf(c -> c.chainId.equals(chain.chainId));
        chains.add(chain);
        persist();
    }

    public static void delete(String chainId) {
        if (chains.removeIf(c -> c.chainId.equals(chainId))) persist();
    }

    public static void setAll(List<QuestChain> list) {
        chains = new ArrayList<>(list);
        persist();
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    public static synchronized void load() {
        try {
            if (!Files.exists(FILE)) { persist(); return; }
            try (Reader r = Files.newBufferedReader(FILE)) {
                Type t = new TypeToken<List<QuestChain>>() {}.getType();
                List<QuestChain> loaded = GSON.fromJson(r, t);
                if (loaded != null) chains = loaded;
            }
        } catch (Exception e) {
            System.err.println("[DL] Failed to load quest chains: " + e.getMessage());
        }
    }

    private static synchronized void persist() {
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(chains, w);
        } catch (Exception e) {
            System.err.println("[DL] Failed to save quest chains: " + e.getMessage());
        }
    }
}

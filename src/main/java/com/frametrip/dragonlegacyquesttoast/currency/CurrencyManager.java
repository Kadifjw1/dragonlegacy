package com.frametrip.dragonlegacyquesttoast.currency;

import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SyncPlayerCurrencyPacket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CurrencyManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE =
            FMLPaths.CONFIGDIR.get().resolve("dragonlegacyquesttoast-currency.json");

    private static Map<String, Long> data = new HashMap<>();

    static { load(); }

    // ── Queries ───────────────────────────────────────────────────────────────

    public static synchronized long getBalance(UUID playerId) {
        return data.getOrDefault(playerId.toString(), 0L);
    }

    public static synchronized boolean hasBalance(UUID playerId, long amount) {
        return getBalance(playerId) >= amount;
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    public static synchronized void setBalance(ServerPlayer player, long amount) {
        data.put(player.getUUID().toString(), Math.max(0, amount));
        save();
        sync(player);
    }

    public static synchronized void addBalance(ServerPlayer player, long amount) {
        long cur = getBalance(player.getUUID());
        data.put(player.getUUID().toString(), cur + amount);
        save();
        sync(player);
    }

    /**
     * Deducts amount from balance. Returns true if successful, false if insufficient funds.
     */
    public static synchronized boolean removeBalance(ServerPlayer player, long amount) {
        long cur = getBalance(player.getUUID());
        if (cur < amount) return false;
        data.put(player.getUUID().toString(), cur - amount);
        save();
        sync(player);
        return true;
    }

    // ── Sync ──────────────────────────────────────────────────────────────────

    private static void sync(ServerPlayer player) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncPlayerCurrencyPacket(getBalance(player.getUUID()))
        );
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    public static synchronized void load() {
        try {
            if (!Files.exists(FILE)) { save(); return; }
            try (Reader r = Files.newBufferedReader(FILE)) {
                Type type = new TypeToken<Map<String, Long>>() {}.getType();
                Map<String, Long> loaded = GSON.fromJson(r, type);
                if (loaded != null) data = loaded;
            }
        } catch (Exception e) {
            System.out.println("[DL] Failed to load currency data: " + e.getMessage());
        }
    }

    private static void save() {
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(data, w);
        } catch (Exception e) {
            System.out.println("[DL] Failed to save currency data: " + e.getMessage());
        }
    }
}

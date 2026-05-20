package com.frametrip.dragonlegacyquesttoast.currency;

import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SyncCurrencyConfigPacket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

// [ECO-1]: Server-side persistence for the global currency configuration.
public final class CurrencyConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get()
            .resolve("dragonlegacyquesttoast-currency-config.json");

    private static CurrencyConfig config = new CurrencyConfig();

    private CurrencyConfigManager() {}

    public static CurrencyConfig get() { return config; }

    public static void load() {
        try {
            if (!Files.exists(FILE)) { save(); return; }
            try (Reader r = Files.newBufferedReader(FILE)) {
                CurrencyConfig loaded = GSON.fromJson(r, CurrencyConfig.class);
                if (loaded != null) config = loaded;
            }
        } catch (Exception e) {
            System.err.println("[DL] Failed to load currency config: " + e.getMessage());
        }
    }

    public static void set(CurrencyConfig newConfig) {
        config = newConfig;
        save();
    }

    public static void syncToPlayer(net.minecraft.server.level.ServerPlayer player) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncCurrencyConfigPacket(config));
    }

    public static void syncToAll(MinecraftServer server) {
        SyncCurrencyConfigPacket packet = new SyncCurrencyConfigPacket(config);
        for (var player : server.getPlayerList().getPlayers()) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
    }

    private static void save() {
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(config, w);
        } catch (Exception e) {
            System.err.println("[DL] Failed to save currency config: " + e.getMessage());
        }
    }
}

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
import java.util.*;
 
public class PlayerAbilityManager {
 
    private static class PlayerData {
        Set<String> abilities = new HashSet<>();
        Set<String> disabledAbilities = new HashSet<>();
        int points = 0;
    }
 
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FMLPaths.CONFIGDIR.get().resolve("dragonlegacyquesttoast-abilities.json");
 
    private static Map<String, PlayerData> data = new HashMap<>();
 
    static { load(); }
 
    // ── ability queries ───────────────────────────────────────────────────────
 
    public static boolean hasAbility(UUID id, String abilityId) {
        PlayerData pd = data.get(id.toString());
        return pd != null && pd.abilities.contains(abilityId);
    }
 
    public static Set<String> getAbilities(UUID id) {
        PlayerData pd = data.get(id.toString());
        return pd == null ? Collections.emptySet() : new HashSet<>(pd.abilities);
    }

    public static Set<String> getDisabledAbilities(UUID id) {
        PlayerData pd = data.get(id.toString());
        return pd == null ? Collections.emptySet() : new HashSet<>(pd.disabledAbilities);
    }

    public static boolean isAbilityEnabled(UUID id, String abilityId) {
        PlayerData pd = data.get(id.toString());
        return pd != null && pd.abilities.contains(abilityId) && !pd.disabledAbilities.contains(abilityId);
    }

    public static void setAbilityEnabled(UUID id, String abilityId, boolean enabled) {
        PlayerData pd = data.get(id.toString());
        if (pd == null || !pd.abilities.contains(abilityId)) return;
        if (enabled) {
            pd.disabledAbilities.remove(abilityId);
        } else {
            pd.disabledAbilities.add(abilityId);
        }
        save();
    }
 
    public static void grantAbility(UUID id, String abilityId) {
        PlayerData pd = data.computeIfAbsent(id.toString(), k -> new PlayerData());
        pd.abilities.add(abilityId);
        pd.disabledAbilities.remove(abilityId);
        save();
    }
 
    public static void revokeAbility(UUID id, String abilityId) {
        PlayerData pd = data.get(id.toString());
        if (pd != null) {
            pd.abilities.remove(abilityId);
            pd.disabledAbilities.remove(abilityId);
            save();
        }
    }
 
    // ── points ────────────────────────────────────────────────────────────────
 
    public static int getPoints(UUID id) {
        PlayerData pd = data.get(id.toString());
        return pd == null ? 0 : pd.points;
    }
 
    public static void addPoints(UUID id, int amount) {
        data.computeIfAbsent(id.toString(), k -> new PlayerData()).points += amount;
        save();
    }
 
    public static void setPoints(UUID id, int amount) {
        data.computeIfAbsent(id.toString(), k -> new PlayerData()).points = Math.max(0, amount);
        save();
    }
 
    public static boolean spendPoints(UUID id, int amount) {
        PlayerData pd = data.get(id.toString());
        if (pd == null || pd.points < amount) return false;
        pd.points -= amount;
        save();
        return true;
    }
 
    // ── persistence ───────────────────────────────────────────────────────────
 
    private static void load() {
        try {
            if (!Files.exists(CONFIG_PATH)) { save(); return; }
            try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
                Type type = new TypeToken<Map<String, PlayerData>>() {}.getType();
                Map<String, PlayerData> loaded = GSON.fromJson(r, type);
                if (loaded != null) data = loaded;
            }
        } catch (Exception e) {
            System.out.println("[DL] Failed to load ability data: " + e.getMessage());
        }
    }
 
    private static void save() {
        try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(data, w);
        } catch (Exception e) {
            System.out.println("[DL] Failed to save ability data: " + e.getMessage());
        }
    }
}

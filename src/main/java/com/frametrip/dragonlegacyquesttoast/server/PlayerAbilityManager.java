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
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FMLPaths.CONFIGDIR.get().resolve("dragonlegacyquesttoast-abilities.json");
 
    private static Map<String, Set<String>> playerAbilities = new HashMap<>();
 
    static {
        load();
    }
 
    public static boolean hasAbility(UUID playerId, String abilityId) {
        return playerAbilities.getOrDefault(playerId.toString(), Collections.emptySet()).contains(abilityId);
    }
 
    public static Set<String> getAbilities(UUID playerId) {
        return new HashSet<>(playerAbilities.getOrDefault(playerId.toString(), Collections.emptySet()));
    }
 
    public static void grantAbility(UUID playerId, String abilityId) {
        playerAbilities.computeIfAbsent(playerId.toString(), k -> new HashSet<>()).add(abilityId);
        save();
    }
 
    public static void revokeAbility(UUID playerId, String abilityId) {
        Set<String> abilities = playerAbilities.get(playerId.toString());
        if (abilities != null) {
            abilities.remove(abilityId);
            save();
        }
    }
 
    private static void load() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                save();
                return;
            }
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                Type type = new TypeToken<Map<String, Set<String>>>() {}.getType();
                Map<String, Set<String>> loaded = GSON.fromJson(reader, type);
                if (loaded != null) {
                    playerAbilities = loaded;
                }
            }
        } catch (Exception e) {
            System.out.println("[DragonLegacyQuestToast] Failed to load ability data: " + e.getMessage());
        }
    }
 
    private static void save() {
        try {
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(playerAbilities, writer);
            }
        } catch (Exception e) {
            System.out.println("[DragonLegacyQuestToast] Failed to save ability data: " + e.getMessage());
        }
    }
}

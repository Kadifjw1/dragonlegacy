package com.frametrip.dragonlegacyquesttoast.server;
 
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;
 
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
 
public class NpcProfileManager {
 
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("dragonlegacyquesttoast-npcprofiles.json");
    private static final LinkedHashMap<String, NpcProfile> PROFILES = new LinkedHashMap<>();
 
    static { load(); }
 
    public static synchronized void save(NpcProfile profile) {
        PROFILES.put(profile.id, profile);
        persist();
    }
 
    public static synchronized void delete(String id) {
        PROFILES.remove(id);
        persist();
    }
 
    public static synchronized List<NpcProfile> getAll() {
        return new ArrayList<>(PROFILES.values());
    }
 
    public static synchronized NpcProfile get(String id) {
        return PROFILES.get(id);
    }
 
    public static synchronized void load() {
        if (!Files.exists(FILE)) return;
        try (Reader r = new InputStreamReader(new FileInputStream(FILE.toFile()), StandardCharsets.UTF_8)) {
            Type t = new TypeToken<LinkedHashMap<String, NpcProfile>>() {}.getType();
            LinkedHashMap<String, NpcProfile> loaded = GSON.fromJson(r, t);
            if (loaded != null) {
                PROFILES.clear();
                PROFILES.putAll(loaded);
            }
        } catch (Exception e) {
            System.err.println("[DL] Failed to load NPC profiles: " + e.getMessage());
        }
    }
 
    private static void persist() {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(FILE.toFile()), StandardCharsets.UTF_8)) {
            GSON.toJson(PROFILES, w);
        } catch (Exception e) {
            System.err.println("[DL] Failed to save NPC profiles: " + e.getMessage());
        }
    }
}

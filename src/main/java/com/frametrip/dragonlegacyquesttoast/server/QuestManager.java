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
 
public class QuestManager {
 
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("dragonlegacyquesttoast-quests.json");
    private static final LinkedHashMap<String, QuestDefinition> QUESTS = new LinkedHashMap<>();
 
    static { load(); }
 
    public static synchronized void save(QuestDefinition quest) {
        QUESTS.put(quest.id, quest);
        persist();
    }
 
    public static synchronized void delete(String id) {
        QUESTS.remove(id);
        persist();
    }
 
    public static synchronized List<QuestDefinition> getAll() {
        return new ArrayList<>(QUESTS.values());
    }
 
    public static synchronized QuestDefinition get(String id) {
        return QUESTS.get(id);
    }
 
    public static synchronized void load() {
        if (!Files.exists(FILE)) return;
        try (Reader r = new InputStreamReader(new FileInputStream(FILE.toFile()), StandardCharsets.UTF_8)) {
            Type t = new TypeToken<LinkedHashMap<String, QuestDefinition>>() {}.getType();
            LinkedHashMap<String, QuestDefinition> loaded = GSON.fromJson(r, t);
            if (loaded != null) {
                QUESTS.clear();
                QUESTS.putAll(loaded);
            }
        } catch (Exception e) {
            System.err.println("[DL] Failed to load quests: " + e.getMessage());
        }
    }
 
    private static void persist() {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(FILE.toFile()), StandardCharsets.UTF_8)) {
            GSON.toJson(QUESTS, w);
        } catch (Exception e) {
            System.err.println("[DL] Failed to save quests: " + e.getMessage());
        }
    }
}

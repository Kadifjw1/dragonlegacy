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
 
public class DialogueManager {
 
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("dragonlegacyquesttoast-dialogues.json");
    private static final LinkedHashMap<String, DialogueDefinition> DIALOGUES = new LinkedHashMap<>();
 
    static { load(); }
 
    public static synchronized void save(DialogueDefinition dialogue) {
        DIALOGUES.put(dialogue.id, dialogue);
        persist();
    }
 
    public static synchronized void delete(String id) {
        DIALOGUES.remove(id);
        persist();
    }
 
    public static synchronized List<DialogueDefinition> getAll() {
        return new ArrayList<>(DIALOGUES.values());
    }
 
    public static synchronized DialogueDefinition get(String id) {
        return DIALOGUES.get(id);
    }
 
    public static synchronized void load() {
        if (!Files.exists(FILE)) return;
        try (Reader r = new InputStreamReader(new FileInputStream(FILE.toFile()), StandardCharsets.UTF_8)) {
            Type t = new TypeToken<LinkedHashMap<String, DialogueDefinition>>() {}.getType();
            LinkedHashMap<String, DialogueDefinition> loaded = GSON.fromJson(r, t);
            if (loaded != null) {
                DIALOGUES.clear();
                DIALOGUES.putAll(loaded);
            }
        } catch (Exception e) {
            System.err.println("[DL] Failed to load dialogues: " + e.getMessage());
        }
    }
 
    private static void persist() {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(FILE.toFile()), StandardCharsets.UTF_8)) {
            GSON.toJson(DIALOGUES, w);
        } catch (Exception e) {
            System.err.println("[DL] Failed to save dialogues: " + e.getMessage());
        }
    }
}

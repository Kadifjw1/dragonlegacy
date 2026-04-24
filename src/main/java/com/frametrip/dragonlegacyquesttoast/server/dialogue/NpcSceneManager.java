package com.frametrip.dragonlegacyquesttoast.server.dialogue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class NpcSceneManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get()
            .resolve("dragonlegacyquesttoast-npc-scenes.json");

    private static final List<NpcScene> scenes = new ArrayList<>();

    public static void load() {
        scenes.clear();
        if (!Files.exists(FILE)) return;
        try (Reader r = Files.newBufferedReader(FILE)) {
            Type t = new TypeToken<List<NpcScene>>() {}.getType();
            List<NpcScene> loaded = GSON.fromJson(r, t);
            if (loaded != null) scenes.addAll(loaded);
        } catch (Exception e) {
            System.out.println("[DragonLegacy] Failed to load NPC scenes: " + e.getMessage());
        }
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(scenes, w);
        } catch (Exception e) {
            System.out.println("[DragonLegacy] Failed to save NPC scenes: " + e.getMessage());
        }
    }

    public static List<NpcScene> getAll() {
        return new ArrayList<>(scenes);
    }

    public static NpcScene get(String id) {
        return scenes.stream().filter(s -> id.equals(s.id)).findFirst().orElse(null);
    }

    public static void saveScene(NpcScene scene) {
        scenes.removeIf(s -> s.id.equals(scene.id));
        scenes.add(scene);
        save();
    }

    public static void deleteScene(String id) {
        scenes.removeIf(s -> s.id.equals(id));
        save();
    }
}

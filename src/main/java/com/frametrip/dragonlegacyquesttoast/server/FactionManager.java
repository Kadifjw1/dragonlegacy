package com.frametrip.dragonlegacyquesttoast.server;
 
import com.frametrip.dragonlegacyquesttoast.entity.FactionData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;
 
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
 
public class FactionManager {
 
    private static final Gson GSON = new Gson();
    private static final Path FILE = FMLPaths.CONFIGDIR.get()
        .resolve("dragonlegacyquesttoast-factions.json");
 
    private static final List<FactionData> factions = new ArrayList<>();
 
    public static void load() {
        factions.clear();
        if (!Files.exists(FILE)) return;
        try (Reader r = Files.newBufferedReader(FILE)) {
            Type t = new TypeToken<List<FactionData>>() {}.getType();
            List<FactionData> loaded = GSON.fromJson(r, t);
            if (loaded != null) factions.addAll(loaded);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    private static void persist() {
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(factions, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
    public static List<FactionData> getAll() {
        return Collections.unmodifiableList(factions);
    }
 
    public static FactionData get(String id) {
        return factions.stream().filter(f -> f.id.equals(id)).findFirst().orElse(null);
    }
 
    public static void save(FactionData faction) {
        factions.removeIf(f -> f.id.equals(faction.id));
        factions.add(faction);
        persist();
    }
 
    public static void delete(String id) {
        factions.removeIf(f -> f.id.equals(id));
        persist();
    }
}

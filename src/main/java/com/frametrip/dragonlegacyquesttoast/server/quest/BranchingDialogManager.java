package com.frametrip.dragonlegacyquesttoast.server.quest;

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

// [QST-2]: Loads and saves branching dialogue trees from config JSON.
public class BranchingDialogManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get()
            .resolve("dragonlegacyquesttoast-branching-dialogs.json");

    private static List<BranchingDialog> dialogs = new ArrayList<>();

    static { load(); }

    public static List<BranchingDialog> getAll() {
        return Collections.unmodifiableList(dialogs);
    }

    public static BranchingDialog get(String id) {
        for (BranchingDialog d : dialogs) if (d.id.equals(id)) return d;
        return null;
    }

    public static void save(BranchingDialog dialog) {
        dialogs.removeIf(d -> d.id.equals(dialog.id));
        dialogs.add(dialog);
        persist();
    }

    public static void delete(String id) {
        if (dialogs.removeIf(d -> d.id.equals(id))) persist();
    }

    public static void setAll(List<BranchingDialog> list) {
        dialogs = new ArrayList<>(list);
        persist();
    }

    public static synchronized void load() {
        try {
            if (!Files.exists(FILE)) { persist(); return; }
            try (Reader r = Files.newBufferedReader(FILE)) {
                Type t = new TypeToken<List<BranchingDialog>>() {}.getType();
                List<BranchingDialog> loaded = GSON.fromJson(r, t);
                if (loaded != null) dialogs = loaded;
            }
        } catch (Exception e) {
            System.err.println("[DL] Failed to load branching dialogs: " + e.getMessage());
        }
    }

    private static synchronized void persist() {
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(dialogs, w);
        } catch (Exception e) {
            System.err.println("[DL] Failed to save branching dialogs: " + e.getMessage());
        }
    }
}

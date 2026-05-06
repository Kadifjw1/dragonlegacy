package com.frametrip.dragonlegacyquesttoast.server.building;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Хранит и загружает шаблоны зданий.
 * Встроенные шаблоны добавляются в память при старте.
 * Внешние JSON-шаблоны загружаются из папки config/dragonlegacy/buildings/.
 */
public class BuildingTemplateManager {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(BuildingTemplateManager.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<String, BuildingTemplate> TEMPLATES = new LinkedHashMap<>();

    public static void load() {
        TEMPLATES.clear();

        // 1. Встроенные шаблоны
        for (BuildingTemplate t : BuiltInTemplates.all()) {
            TEMPLATES.put(t.id, t);
        }

        // 2. Внешние JSON-файлы
        loadFromDisk();

        LOG.info("[BuildingTemplateManager] Загружено {} шаблонов зданий.", TEMPLATES.size());
    }

    private static void loadFromDisk() {
        try {
            Path dir = Path.of("config", "dragonlegacy", "buildings");
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                return;
            }
            File[] files = dir.toFile().listFiles((d, n) -> n.endsWith(".json"));
            if (files == null) return;
            for (File f : files) {
                try (FileReader r = new FileReader(f)) {
                    BuildingTemplate t = GSON.fromJson(r, BuildingTemplate.class);
                    if (t != null && t.id != null && !t.id.isBlank()) {
                        TEMPLATES.put(t.id, t);
                        LOG.info("[BuildingTemplateManager] Загружен внешний шаблон: {}", t.id);
                    }
                } catch (Exception e) {
                    LOG.warn("[BuildingTemplateManager] Ошибка чтения {}: {}", f.getName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            LOG.warn("[BuildingTemplateManager] Ошибка при загрузке шаблонов с диска: {}", e.getMessage());
        }
    }

    public static void saveTemplate(BuildingTemplate t) {
        TEMPLATES.put(t.id, t);
        try {
            Path dir = Path.of("config", "dragonlegacy", "buildings");
            Files.createDirectories(dir);
            File f = dir.resolve(t.id + ".json").toFile();
            try (FileWriter w = new FileWriter(f)) {
                GSON.toJson(t, w);
            }
        } catch (Exception e) {
            LOG.warn("[BuildingTemplateManager] Не удалось сохранить шаблон {}: {}", t.id, e.getMessage());
        }
    }

    public static List<BuildingTemplate> getAll() {
        return new ArrayList<>(TEMPLATES.values());
    }

    public static BuildingTemplate get(String id) {
        return TEMPLATES.get(id);
    }

    public static boolean exists(String id) {
        return TEMPLATES.containsKey(id);
    }
}

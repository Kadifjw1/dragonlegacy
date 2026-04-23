package com.frametrip.dragonlegacyquesttoast.server;
 
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
 
public class QuestDefinition {
 
 // ── Base fields ───────────────────────────────────────────────────────────
    public String id;
    public String title;
    public String description;
    public List<String> objectives;
    public String giverNpcId;
    public String rewardText;
    public String questType; // "main", "side", "daily"
 
 // ── Title text styling ────────────────────────────────────────────────────
    public boolean titleBold      = false;
    public boolean titleItalic    = false;
    public boolean titleUnderline = false;
    public int     titleFontSize  = 9;
    public int     titleColor     = 0xFFFFFF;
 
    // ── Description text styling ──────────────────────────────────────────────
    public boolean descBold      = false;
    public boolean descItalic    = false;
    public boolean descUnderline = false;
    public int     descFontSize  = 9;
    public int     descColor     = 0xAAAAAA;
 
    // ── Quest logic ───────────────────────────────────────────────────────────
    public String questLogicType = "COLLECT";
    public Map<String, String> logicData = new LinkedHashMap<>();
 
    // ── Logic type constants ──────────────────────────────────────────────────
    public static final String[] LOGIC_IDS = {
        "COLLECT", "DIALOGUE", "HUNT", "EXPLORE",
        "SEARCH",  "ESCORT",   "CRAFT", "BUILD"
    };
    public static final String[] LOGIC_LABELS = {
        "Сбор и доставка",
        "Диалоговые квесты",
        "Охота и убийство",
        "Исследование и посещение точек",
        "Поиск объектов и взаимодействие",
        "Эскорт и сопровождение",
        "Крафт и создание",
        "Строительство и размещение"
    };
 
    // Fields per logic type: logicType -> []{fieldKey, label}
    public static final Map<String, String[][]> LOGIC_FIELDS = new LinkedHashMap<>();
    static {
        LOGIC_FIELDS.put("COLLECT",  new String[][]{
            {"item",       "Предмет (ID, напр. minecraft:diamond)"},
            {"count",      "Количество"},
            {"deliverNpc", "Доставить к NPC (ID профиля)"}
        });
        LOGIC_FIELDS.put("DIALOGUE", new String[][]{
            {"npcId",      "NPC (ID профиля)"},
            {"dialogueId", "Диалог (ID)"}
        });
        LOGIC_FIELDS.put("HUNT", new String[][]{
            {"mobType",      "Тип моба (ID, напр. minecraft:zombie)"},
            {"killCount",    "Количество убийств"},
            {"locationHint", "Подсказка локации (необязательно)"}
        });
        LOGIC_FIELDS.put("EXPLORE", new String[][]{
            {"x",      "Координата X"},
            {"y",      "Координата Y"},
            {"z",      "Координата Z"},
            {"radius", "Радиус (блоков)"}
        });
        LOGIC_FIELDS.put("SEARCH", new String[][]{
            {"blockType", "Тип блока (ID, напр. minecraft:chest)"},
            {"count",     "Количество взаимодействий"},
            {"hint",      "Подсказка (необязательно)"}
        });
        LOGIC_FIELDS.put("ESCORT", new String[][]{
            {"npcId",  "NPC для эскорта (ID профиля)"},
            {"startX", "Старт X"}, {"startY", "Старт Y"}, {"startZ", "Старт Z"},
            {"endX",   "Цель X"},  {"endY",   "Цель Y"},  {"endZ",   "Цель Z"}
        });
        LOGIC_FIELDS.put("CRAFT", new String[][]{
            {"item",  "Крафтить предмет (ID)"},
            {"count", "Количество"}
        });
        LOGIC_FIELDS.put("BUILD", new String[][]{
            {"block", "Блок (ID, напр. minecraft:oak_log)"},
            {"count", "Количество"},
            {"x",     "Позиция X (необязательно)"},
            {"y",     "Позиция Y (необязательно)"},
            {"z",     "Позиция Z (необязательно)"}
        });
    }
 
    // ── Quest type constants ──────────────────────────────────────────────────
    public static final String[] TYPES       = {"main", "side", "daily"};
    public static final String[] TYPE_LABELS = {"Основной", "Доп.", "Ежедневный"};
 
    // ── Constructor ───────────────────────────────────────────────────────────
    public QuestDefinition() {
        this.id          = UUID.randomUUID().toString().substring(0, 8);
        this.title       = "Новый квест";
        this.description = "";
        this.objectives  = new ArrayList<>();
        this.giverNpcId  = "";
        this.rewardText  = "";
        this.questType   = "side";
        this.logicData   = new LinkedHashMap<>();
    }
 
    public QuestDefinition copy() {
        QuestDefinition c = new QuestDefinition();
        c.id          = this.id;
        c.title       = this.title;
        c.description = this.description;
        c.objectives  = new ArrayList<>(this.objectives);
        c.giverNpcId  = this.giverNpcId;
        c.rewardText  = this.rewardText;
        c.questType   = this.questType;
 
        c.titleBold      = this.titleBold;
        c.titleItalic    = this.titleItalic;
        c.titleUnderline = this.titleUnderline;
        c.titleFontSize  = this.titleFontSize;
        c.titleColor     = this.titleColor;
 
        c.descBold      = this.descBold;
        c.descItalic    = this.descItalic;
        c.descUnderline = this.descUnderline;
        c.descFontSize  = this.descFontSize;
        c.descColor     = this.descColor;
 
        c.questLogicType = this.questLogicType;
        c.logicData      = new LinkedHashMap<>(this.logicData);
        return c;
    }
 
    public String typeLabel() {
        for (int i = 0; i < TYPES.length; i++) {
            if (TYPES[i].equals(questType)) return TYPE_LABELS[i];
        }
        return questType;
    }
 
    public String logicLabel() {
        for (int i = 0; i < LOGIC_IDS.length; i++) {
            if (LOGIC_IDS[i].equals(questLogicType)) return LOGIC_LABELS[i];
        }
        return questLogicType != null ? questLogicType : "—";
    }
 
    /** Returns required count for progress-based logic types, or -1 if N/A. */
    public int getRequiredCount() {
        try {
            switch (questLogicType != null ? questLogicType : "") {
                case "COLLECT": return Integer.parseInt(logicData.getOrDefault("count", "1"));
                case "HUNT":    return Integer.parseInt(logicData.getOrDefault("killCount", "1"));
                case "SEARCH":  return Integer.parseInt(logicData.getOrDefault("count", "1"));
                case "CRAFT":   return Integer.parseInt(logicData.getOrDefault("count", "1"));
                case "BUILD":   return Integer.parseInt(logicData.getOrDefault("count", "1"));
                default:        return 1;
            }
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}

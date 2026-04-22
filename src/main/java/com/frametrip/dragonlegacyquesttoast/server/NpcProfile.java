package com.frametrip.dragonlegacyquesttoast.server;
 
import java.util.*;
 
public class NpcProfile {
 
    public String id;
    public String displayName;
    public String dialogueId;
    public List<String> questIds;
    public Map<String, Integer> bodyParts;
 
    // ── Option arrays per body part ───────────────────────────────────────────
 
    public static final String[] EYE_OPTIONS    = {"Обычные", "Сердитые", "Сонные", "Широкие", "Прищур"};
    public static final String[] MOUTH_OPTIONS  = {"Нейтральный", "Улыбка", "Нахмуренный", "Открытый", "Оскал"};
    public static final String[] HAIR_OPTIONS   = {"Нет", "Короткие", "Длинные", "Пучок", "Торчащие", "Кудри"};
    public static final String[] ARM_OPTIONS    = {"Обычная", "Поднята", "Опущена", "Согнута"};
    public static final String[] LEG_OPTIONS    = {"Обычная", "Вперёд", "Назад", "Согнута"};
    public static final String[] TORSO_OPTIONS  = {"Обычное", "Широкое", "Стройное"};
 
    public static final LinkedHashMap<String, String[]> PART_OPTIONS = new LinkedHashMap<>();
    public static final LinkedHashMap<String, String>   PART_LABELS  = new LinkedHashMap<>();
 
    static {
        PART_OPTIONS.put("eyes",     EYE_OPTIONS);
        PART_OPTIONS.put("mouth",    MOUTH_OPTIONS);
        PART_OPTIONS.put("hair",     HAIR_OPTIONS);
        PART_OPTIONS.put("rightArm", ARM_OPTIONS);
        PART_OPTIONS.put("leftArm",  ARM_OPTIONS);
        PART_OPTIONS.put("torso",    TORSO_OPTIONS);
        PART_OPTIONS.put("leftLeg",  LEG_OPTIONS);
        PART_OPTIONS.put("rightLeg", LEG_OPTIONS);
 
        PART_LABELS.put("eyes",     "Глаза");
        PART_LABELS.put("mouth",    "Рот");
        PART_LABELS.put("hair",     "Причёска");
        PART_LABELS.put("rightArm", "Пр. рука");
        PART_LABELS.put("leftArm",  "Лв. рука");
        PART_LABELS.put("torso",    "Туловище");
        PART_LABELS.put("leftLeg",  "Лв. нога");
        PART_LABELS.put("rightLeg", "Пр. нога");
    }
 
    public NpcProfile() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.displayName = "Новый NPC";
        this.dialogueId = "";
        this.questIds = new ArrayList<>();
        this.bodyParts = new LinkedHashMap<>();
        for (String part : PART_OPTIONS.keySet()) {
            bodyParts.put(part, 0);
        }
    }
 
    public NpcProfile copy() {
        NpcProfile c = new NpcProfile();
        c.id = this.id;
        c.displayName = this.displayName;
        c.dialogueId = this.dialogueId;
        c.questIds = new ArrayList<>(this.questIds);
        c.bodyParts = new LinkedHashMap<>(this.bodyParts);
        return c;
    }
 
    public String getPartLabel(String part) {
        String[] opts = PART_OPTIONS.get(part);
        if (opts == null) return "?";
        int idx = bodyParts.getOrDefault(part, 0);
        if (idx < 0 || idx >= opts.length) return opts[0];
        return opts[idx];
    }
 
    public void cyclePart(String part, int delta) {
        String[] opts = PART_OPTIONS.get(part);
        if (opts == null) return;
        int cur = bodyParts.getOrDefault(part, 0);
        bodyParts.put(part, Math.floorMod(cur + delta, opts.length));
    }
}


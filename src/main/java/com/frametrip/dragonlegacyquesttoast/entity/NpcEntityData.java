package com.frametrip.dragonlegacyquesttoast.entity;

import com.frametrip.dragonlegacyquesttoast.profession.NpcProfessionData;
import com.frametrip.dragonlegacyquesttoast.server.NpcProfile;
 
import java.util.*;
 
public class NpcEntityData {
 
    public String displayName  = "NPC";
    public String skinId       = "default";
    public Map<String, Integer> bodyParts = new LinkedHashMap<>();
    public String dialogueId   = "";
    public String sceneId      = "";
    public List<String> questIds = new ArrayList<>();
    public String factionId    = "";
 
    // texture layers: category -> textureId (base, hair, eyes, top, bottom, shoes, accessory, overlay)
    public Map<String, String> textureLayers = new LinkedHashMap<>();

    // — Animation —
    public String  idlePose          = "STANDING"; // STANDING, CROUCHING
    public float   walkSpeed         = 0.5f;       // 0.0–1.0
    public boolean lookAtPlayer      = true;
    public String  rightArmPose      = "EMPTY";    // HumanoidModel.ArmPose name
    public String  leftArmPose       = "EMPTY";
    public boolean lockBodyRotation  = false;
    public float   bodyYaw           = 0f;
 
    // — Relations —
    public String playerRelation = "NEUTRAL"; // FRIENDLY, NEUTRAL, HOSTILE
 
    // — Profession —
    public NpcProfessionData professionData = new NpcProfessionData();
 
    // ── Pose labels (for UI) ──────────────────────────────────────────────────
    public static final String[] IDLE_POSES       = {"STANDING", "CROUCHING"};
    public static final String[] IDLE_POSE_LABELS = {"Стоит", "Крадётся"};
 
    public static final String[] ARM_POSES = {
        "EMPTY", "BLOCK", "ITEM", "THROW_SPEAR", "BOW_AND_ARROW",
        "CROSSBOW_CHARGE", "CROSSBOW_HOLD"
    };
    public static final String[] ARM_POSE_LABELS = {
        "Свободная", "Блок", "Предмет", "Копьё", "Лук",
        "Заряд арбалета", "Арбалет"
    };
 
    public static final String[] RELATIONS       = {"FRIENDLY", "NEUTRAL", "HOSTILE"};
    public static final String[] RELATION_LABELS = {"Дружелюбный", "Нейтральный", "Враждебный"};
    
    public static final String[] TEXTURE_LAYERS = {
        "base", "hair", "eyes", "top", "bottom", "shoes", "accessory", "overlay"
    };
    public static final String[] TEXTURE_LAYER_LABELS = {
        "База", "Волосы", "Глаза", "Верх", "Низ", "Обувь", "Аксессуар", "Оверлей"
    };

    public NpcEntityData() {
        for (String part : NpcProfile.PART_OPTIONS.keySet()) {
            bodyParts.put(part, 0);
        }
    }
 
    public NpcEntityData copy() {
        NpcEntityData c = new NpcEntityData();
        c.displayName       = this.displayName;
        c.skinId            = this.skinId;
        c.bodyParts         = new LinkedHashMap<>(this.bodyParts);
        c.dialogueId        = this.dialogueId;
        c.sceneId           = this.sceneId;
        c.questIds          = new ArrayList<>(this.questIds);
        c.factionId         = this.factionId;
        c.textureLayers     = new LinkedHashMap<>(this.textureLayers);
        c.idlePose          = this.idlePose;
        c.walkSpeed         = this.walkSpeed;
        c.lookAtPlayer      = this.lookAtPlayer;
        c.rightArmPose      = this.rightArmPose;
        c.leftArmPose       = this.leftArmPose;
        c.lockBodyRotation  = this.lockBodyRotation;
        c.bodyYaw           = this.bodyYaw;
        c.playerRelation    = this.playerRelation;
        c.professionData    = this.professionData != null ? this.professionData.copy() : new NpcProfessionData();

        return c;
    }
 
    public static String armPoseLabel(String pose) {
        for (int i = 0; i < ARM_POSES.length; i++)
            if (ARM_POSES[i].equals(pose)) return ARM_POSE_LABELS[i];
        return pose;
    }
 
    public static String idlePoseLabel(String pose) {
        for (int i = 0; i < IDLE_POSES.length; i++)
            if (IDLE_POSES[i].equals(pose)) return IDLE_POSE_LABELS[i];
        return pose;
    }
 
    public static String relationLabel(String rel) {
        for (int i = 0; i < RELATIONS.length; i++)
            if (RELATIONS[i].equals(rel)) return RELATION_LABELS[i];
        return rel;
    }
 
    public static String textureLayerLabel(String layer) {
        for (int i = 0; i < TEXTURE_LAYERS.length; i++)
            if (TEXTURE_LAYERS[i].equals(layer)) return TEXTURE_LAYER_LABELS[i];
        return layer;
    }
}

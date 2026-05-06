package com.frametrip.dragonlegacyquesttoast.server.animation;

import java.util.ArrayList;
import java.util.List;

public class AnimationBone {
    public static final String HEAD      = "head";
    public static final String BODY      = "body";
    public static final String LEFT_ARM  = "left_arm";
    public static final String RIGHT_ARM = "right_arm";
    public static final String LEFT_LEG  = "left_leg";
    public static final String RIGHT_LEG = "right_leg";

    public static final String[] BONE_IDS    = { HEAD, BODY, LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG };
    public static final String[] BONE_LABELS = { "Голова", "Тело", "Лев. рука", "Прав. рука", "Лев. нога", "Прав. нога" };

    public String boneName = HEAD;

    public List<AnimationKeyframe> rotationFrames = new ArrayList<>();
    public List<AnimationKeyframe> positionFrames = new ArrayList<>();
    public List<AnimationKeyframe> scaleFrames    = new ArrayList<>();

    public AnimationBone() {}
    public AnimationBone(String boneName) {
        this.boneName = boneName;
    }

    public AnimationBone copy() {
        AnimationBone c = new AnimationBone(boneName);
        for (AnimationKeyframe f : rotationFrames) c.rotationFrames.add(f.copy());
        for (AnimationKeyframe f : positionFrames) c.positionFrames.add(f.copy());
        for (AnimationKeyframe f : scaleFrames)    c.scaleFrames.add(f.copy());
        return c;
    }

    public static String boneLabel(String id) {
        for (int i = 0; i < BONE_IDS.length; i++) if (BONE_IDS[i].equals(id)) return BONE_LABELS[i];
        return id;
    }
}

package com.frametrip.dragonlegacyquesttoast.server.model;

public class NpcModelConfig {
    public NpcModelProfile profile         = NpcModelProfile.PLAYER;
    public float scale                     = 1f;
    public float offsetX                   = 0f;
    public float offsetY                   = 0f;
    public float offsetZ                   = 0f;
    public float rotation                  = 0f;
    public float eyeHeightOverride         = -1f; // -1 = use profile default
    public float nameplateOffsetOverride   = -1f;
    public float dialogueOffsetOverride    = -1f;
    public boolean useCreatureSounds       = false;
    public boolean useCreatureAnimations   = false;

    public float effectiveEyeHeight() {
        return eyeHeightOverride >= 0 ? eyeHeightOverride : profile.eyeHeight;
    }

    public float effectiveNameplateOffset() {
        return nameplateOffsetOverride >= 0 ? nameplateOffsetOverride : profile.nameplateOffset;
    }

    public float effectiveDialogueOffset() {
        return dialogueOffsetOverride >= 0 ? dialogueOffsetOverride : profile.dialogueOffset;
    }

    public NpcModelConfig copy() {
        NpcModelConfig c = new NpcModelConfig();
        c.profile                = this.profile;
        c.scale                  = this.scale;
        c.offsetX                = this.offsetX;
        c.offsetY                = this.offsetY;
        c.offsetZ                = this.offsetZ;
        c.rotation               = this.rotation;
        c.eyeHeightOverride      = this.eyeHeightOverride;
        c.nameplateOffsetOverride = this.nameplateOffsetOverride;
        c.dialogueOffsetOverride = this.dialogueOffsetOverride;
        c.useCreatureSounds      = this.useCreatureSounds;
        c.useCreatureAnimations  = this.useCreatureAnimations;
        return c;
    }
}

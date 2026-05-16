package com.frametrip.dragonlegacyquesttoast.client.model;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.frametrip.dragonlegacyquesttoast.client.NpcSkinManager;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NpcGeoModel extends GeoModel<NpcEntity> {

    private static final String MODID = DragonLegacyQuestToastMod.MODID;
    public static final ResourceLocation DEFAULT_MODEL =
            new ResourceLocation(MODID, "geo/npc_default.geo.json");
    public static final ResourceLocation DEFAULT_ANIM =
            new ResourceLocation(MODID, "animations/npc_default.animation.json");

    @Override
    public ResourceLocation getModelResource(NpcEntity entity) {
        NpcEntityData data = entity.getNpcData();
        ResourceLocation override = tryLocation(data.geckoModel);
        return override != null ? override : DEFAULT_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(NpcEntity entity) {
        NpcEntityData data = entity.getNpcData();
        ResourceLocation override = tryLocation(data.geckoTexture);
        return override != null ? override : NpcSkinManager.getTexture(data.skinId);
    }

    @Override
    public ResourceLocation getAnimationResource(NpcEntity entity) {
        NpcEntityData data = entity.getNpcData();
        ResourceLocation override = tryLocation(data.geckoAnimation);
        return override != null ? override : DEFAULT_ANIM;
    }

    private static ResourceLocation tryLocation(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return new ResourceLocation(s); } catch (Exception ignored) { return null; }
    }
}

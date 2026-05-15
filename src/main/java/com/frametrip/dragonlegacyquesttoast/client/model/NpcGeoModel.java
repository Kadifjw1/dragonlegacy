package com.frametrip.dragonlegacyquesttoast.client.model;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.frametrip.dragonlegacyquesttoast.client.NpcSkinManager;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.server.model.NpcModelProfile;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NpcGeoModel extends GeoModel<NpcEntity> {

    private static final String MODID = DragonLegacyQuestToastMod.MODID;
    private static final ResourceLocation DEFAULT_MODEL =
            new ResourceLocation(MODID, "geo/npc_default.geo.json");
    private static final ResourceLocation DEFAULT_ANIM =
            new ResourceLocation(MODID, "animations/npc_default.animation.json");

    @Override
    public ResourceLocation getModelResource(NpcEntity entity) {
        NpcEntityData data = entity.getNpcData();
        ResourceLocation override = tryLocation(data.geckoModel);
        if (override != null) return override;
        NpcModelProfile profile = profileOf(data);
        return profile != NpcModelProfile.PLAYER ? profile.geoResource(MODID) : DEFAULT_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(NpcEntity entity) {
        NpcEntityData data = entity.getNpcData();
        ResourceLocation override = tryLocation(data.geckoTexture);
        if (override != null) return override;
        NpcModelProfile profile = profileOf(data);
        // Fall back to skin system (preserves player-skin support for the default profile)
        return profile != NpcModelProfile.PLAYER
                ? profile.textureResource(MODID)
                : NpcSkinManager.getTexture(data.skinId);
    }

    @Override
    public ResourceLocation getAnimationResource(NpcEntity entity) {
        NpcEntityData data = entity.getNpcData();
        ResourceLocation override = tryLocation(data.geckoAnimation);
        if (override != null) return override;
        NpcModelProfile profile = profileOf(data);
        // GeckoLib silently falls back if the profile animation file is absent
        return profile != NpcModelProfile.PLAYER ? profile.animationResource(MODID) : DEFAULT_ANIM;
    }

    private static NpcModelProfile profileOf(NpcEntityData data) {
        return (data.modelConfig != null && data.modelConfig.profile != null)
                ? data.modelConfig.profile : NpcModelProfile.PLAYER;
    }

    private static ResourceLocation tryLocation(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return new ResourceLocation(s); } catch (Exception ignored) { return null; }
    }
}

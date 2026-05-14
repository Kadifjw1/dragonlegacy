package com.frametrip.dragonlegacyquesttoast.client.model;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.frametrip.dragonlegacyquesttoast.client.NpcSkinManager;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.server.model.NpcModelConfig;
import com.frametrip.dragonlegacyquesttoast.server.model.NpcModelProfile;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NpcGeoModel extends GeoModel<NpcEntity> {

    private static final ResourceLocation DEFAULT_MODEL =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "geo/npc_default.geo.json");
    private static final ResourceLocation DEFAULT_ANIM =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "animations/npc_default.animation.json");

    @Override
    public ResourceLocation getModelResource(NpcEntity entity) {
        NpcEntityData data = entity.getNpcData();
        // Explicit geckoModel override takes priority
        if (data.geckoModel != null && !data.geckoModel.isEmpty()) {
            try { return new ResourceLocation(data.geckoModel); } catch (Exception ignored) {}
        }
        // Derive from selected model profile
        NpcModelProfile profile = profileOf(data);
        if (profile != NpcModelProfile.PLAYER) {
            return new ResourceLocation(DragonLegacyQuestToastMod.MODID,
                    "geo/npc_" + profile.id + ".geo.json");
        }
        return DEFAULT_MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(NpcEntity entity) {
        NpcEntityData data = entity.getNpcData();
        // Explicit geckoTexture override takes priority
        if (data.geckoTexture != null && !data.geckoTexture.isEmpty()) {
            try { return new ResourceLocation(data.geckoTexture); } catch (Exception ignored) {}
        }
        // Profile-specific texture when not the default player model
        NpcModelProfile profile = profileOf(data);
        if (profile != NpcModelProfile.PLAYER) {
            return new ResourceLocation(DragonLegacyQuestToastMod.MODID,
                    "textures/entity/npc_" + profile.id + ".png");
        }
        // Fall back to skin system (same as old NpcEntityRenderer)
        return NpcSkinManager.getTexture(data.skinId);
    }

    @Override
    public ResourceLocation getAnimationResource(NpcEntity entity) {
        NpcEntityData data = entity.getNpcData();
        if (data.geckoAnimation != null && !data.geckoAnimation.isEmpty()) {
            try { return new ResourceLocation(data.geckoAnimation); } catch (Exception ignored) {}
        }
        // Profile-specific animation file when present
        NpcModelProfile profile = profileOf(data);
        if (profile != NpcModelProfile.PLAYER) {
            ResourceLocation profileAnim = new ResourceLocation(DragonLegacyQuestToastMod.MODID,
                    "animations/npc_" + profile.id + ".animation.json");
            // Return profile animation; GeckoLib will fall back internally if the file is absent
            return profileAnim;
        }
        return DEFAULT_ANIM;
    }

    private static NpcModelProfile profileOf(NpcEntityData data) {
        if (data.modelConfig != null && data.modelConfig.profile != null) {
            return data.modelConfig.profile;
        }
        return NpcModelProfile.PLAYER;
    }
}

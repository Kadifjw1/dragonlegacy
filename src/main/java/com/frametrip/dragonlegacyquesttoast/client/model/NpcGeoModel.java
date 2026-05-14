package com.frametrip.dragonlegacyquesttoast.client.model;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.frametrip.dragonlegacyquesttoast.client.NpcSkinManager;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NpcGeoModel extends GeoModel<NpcEntity> {

    private static final ResourceLocation DEFAULT_MODEL =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "geo/npc_default.geo.json");
    private static final ResourceLocation DEFAULT_ANIM =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "animations/npc_default.animation.json");

    @Override
    public ResourceLocation getModelResource(NpcEntity entity) {
        String path = entity.getNpcData().geckoModel;
        if (path != null && !path.isEmpty()) {
            try { return new ResourceLocation(path); } catch (Exception ignored) {}
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
        // Fall back to skin system (same as old NpcEntityRenderer)
        return NpcSkinManager.getTexture(data.skinId);
    }

    @Override
    public ResourceLocation getAnimationResource(NpcEntity entity) {
        String path = entity.getNpcData().geckoAnimation;
        if (path != null && !path.isEmpty()) {
            try { return new ResourceLocation(path); } catch (Exception ignored) {}
        }
        return DEFAULT_ANIM;
    }
}

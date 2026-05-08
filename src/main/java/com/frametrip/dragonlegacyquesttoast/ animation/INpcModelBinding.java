package com.frametrip.dragonlegacyquesttoast.animation;

import com.frametrip.dragonlegacyquesttoast.server.NpcProfile;
import net.minecraft.resources.ResourceLocation;

/**
 * Provides model / animation / texture resource locations for an NPC.
 * Implemented by NpcGeoModel for the "humanoid" type.
 * Future types (animal, creature, mechanical) add new implementations without touching existing code.
 */
public interface INpcModelBinding {

    ResourceLocation getGeoModel(NpcProfile profile);

    ResourceLocation getAnimations(NpcProfile profile);

    ResourceLocation getTexture(NpcProfile profile);

    /** One of: "humanoid", "animal", "creature". */
    String getModelType();
}


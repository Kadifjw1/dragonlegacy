package com.frametrip.dragonlegacyquesttoast.client.renderer;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.server.animation.AnimationState;

import java.util.UUID;

/**
 * Legacy keyframe animation applicator — retained for reference.
 * GeckoLib now drives all NPC animations via NpcGeoModel/NpcGeoRenderer.
 * This class is no longer called by the active renderer path.
 */
public class NpcAnimationPlayer {

    public static void applyAnimation(UUID uuid, AnimationState state,
                                      float tick, NpcEntityData data, NpcEntityModel model) {
        // No-op: GeckoLib AnimationController handles animation playback.
    }
}

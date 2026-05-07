package com.frametrip.dragonlegacyquesttoast.client.renderer;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.server.animation.AnimationBone;
import com.frametrip.dragonlegacyquesttoast.server.animation.AnimationKeyframe;
import com.frametrip.dragonlegacyquesttoast.server.animation.AnimationState;
import com.frametrip.dragonlegacyquesttoast.server.animation.NpcAnimationData;
import net.minecraft.client.model.geom.ModelPart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Client-side per-entity animation playback.
 * Reads animations from NpcEntityData, interpolates keyframes, applies to model parts.
 * Called from NpcEntityModel.setupAnim every frame on the render thread.
 */
public class NpcAnimationPlayer {

    private static final Map<UUID, Entry> ENTRIES = new HashMap<>();

    private static class Entry {
        AnimationState currentState = AnimationState.IDLE;
        float stateStartAge = 0f;
    }

    /**
     * Apply the custom animation for the given state on top of the already-computed base pose.
     * Only bones with keyframes are affected; empty bones keep the base pose untouched.
     * Returns true if a custom animation was found and applied.
     */
    public static boolean applyAnimation(UUID entityId, AnimationState targetState,
                                         float ageInTicks, NpcEntityData data,
                                         NpcEntityModel model) {
        Entry entry = ENTRIES.computeIfAbsent(entityId, k -> new Entry());

        if (entry.currentState != targetState) {
            entry.currentState = targetState;
            entry.stateStartAge = ageInTicks;
        }

        if (data.animations == null || data.animations.isEmpty()) return false;

        NpcAnimationData anim = null;
        for (NpcAnimationData a : data.animations) {
            if (a.stateBinding == targetState) {
                anim = a;
                break;
            }
        }
        if (anim == null || anim.bones == null || anim.bones.isEmpty()) return false;

        float tick = ageInTicks - entry.stateStartAge;
        if (anim.loop && anim.durationTicks > 0) {
            tick = tick % anim.durationTicks;
        } else {
            tick = Math.min(tick, anim.durationTicks);
        }

        for (AnimationBone bone : anim.bones) {
            ModelPart part = resolvePart(bone.boneName, model);
            if (part == null) continue;

            if (!bone.rotationFrames.isEmpty()) {
                float[] rot = interpolate(bone.rotationFrames, tick);
                part.xRot = (float) Math.toRadians(rot[0]);
                part.yRot = (float) Math.toRadians(rot[1]);
                part.zRot = (float) Math.toRadians(rot[2]);
            }
            if (!bone.positionFrames.isEmpty()) {
                float[] pos = interpolate(bone.positionFrames, tick);
                part.x += pos[0];
                part.y += pos[1];
                part.z += pos[2];
            }
        }
        return true;
    }

    public static void remove(UUID entityId) {
        ENTRIES.remove(entityId);
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private static float[] interpolate(List<AnimationKeyframe> frames, float tick) {
        if (frames.isEmpty()) return new float[]{0, 0, 0};
        if (frames.size() == 1) {
            AnimationKeyframe f = frames.get(0);
            return new float[]{f.x, f.y, f.z};
        }

        AnimationKeyframe prev = frames.get(0);
        AnimationKeyframe next = frames.get(frames.size() - 1);

        for (int i = 0; i < frames.size() - 1; i++) {
            if (frames.get(i).tick <= tick && frames.get(i + 1).tick >= tick) {
                prev = frames.get(i);
                next = frames.get(i + 1);
                break;
            }
        }

        if (prev == next || next.tick <= prev.tick) {
            return new float[]{prev.x, prev.y, prev.z};
        }

        float t = (tick - prev.tick) / (next.tick - prev.tick);
        t = eased(t, next.easing);
        return new float[]{
            lerp(prev.x, next.x, t),
            lerp(prev.y, next.y, t),
            lerp(prev.z, next.z, t)
        };
    }

    private static float eased(float t, String mode) {
        return switch (mode == null ? "linear" : mode) {
            case "easeIn"    -> t * t;
            case "easeOut"   -> 1 - (1 - t) * (1 - t);
            case "easeInOut" -> t < 0.5f ? 2 * t * t : 1 - 2 * (1 - t) * (1 - t);
            case "step"      -> t < 1f ? 0f : 1f;
            default          -> t;
        };
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static ModelPart resolvePart(String boneName, NpcEntityModel model) {
        return switch (boneName) {
            case AnimationBone.HEAD      -> model.head;
            case AnimationBone.BODY      -> model.body;
            case AnimationBone.LEFT_ARM  -> model.leftArm;
            case AnimationBone.RIGHT_ARM -> model.rightArm;
            case AnimationBone.LEFT_LEG  -> model.leftLeg;
            case AnimationBone.RIGHT_LEG -> model.rightLeg;
            default -> null;
        };
    }
}

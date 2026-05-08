package com.frametrip.dragonlegacyquesttoast.entity;

/**
 * GeckoLib animation states for NPC entities.
 * Stored in synced entity data so the client AnimationController reads the correct state.
 */
public enum NpcAnimationState {
    CALM,
    IDLE,
    WALKING,
    TALKING,
    INTERACTING,
    WORKING,
    FOLLOWING,
    GUARDING,
    ALERT,
    ATTACKING,
    SLEEPING,
    SITTING,
    CUSTOM_SCENE
}

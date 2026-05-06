package com.frametrip.dragonlegacyquesttoast.server.stealth;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.NpcStartScenePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-tick handler for NPC guard detection of players. */
public class NpcDetectionHandler {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NpcDetectionHandler.class);

    /** Per-NPC active guard runtime state. */
    private static final Map<UUID, GuardRuntime> RUNTIMES = new HashMap<>();

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.getServer() == null) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (!(entity instanceof NpcEntity npc)) continue;
                StealthConfig cfg = npc.getNpcData().stealthConfig;
                if (!cfg.guardEnabled) continue;

                GuardRuntime rt = RUNTIMES.computeIfAbsent(npc.getUUID(), id -> new GuardRuntime());
                tickGuard(npc, cfg, rt, level);
            }
        }
    }

    private void tickGuard(NpcEntity npc, StealthConfig cfg, GuardRuntime rt, ServerLevel level) {
        long now = level.getGameTime();
        ServerPlayer nearestDetected = null;
        float highestDetection = 0f;

        for (ServerPlayer player : level.players()) {
            if (player.isCreative() || player.isSpectator()) continue;

            float detection = computeDetection(npc, player, cfg, level);
            if (detection > highestDetection) {
                highestDetection = detection;
                nearestDetected = player;
            }
        }

        if (highestDetection <= 0f) {
            // Nobody detectable
            if (rt.state == GuardState.ALARM || rt.state == GuardState.SEARCHING) {
                if (now - rt.alarmStartTick >= cfg.alarmResetTicks) {
                    transition(npc, rt, GuardState.RESETTING, cfg);
                }
            } else if (rt.state == GuardState.RESETTING) {
                if (now - rt.stateStartTick >= 60) {
                    transition(npc, rt, GuardState.CALM, cfg);
                }
            } else if (rt.state == GuardState.SUSPICIOUS || rt.state == GuardState.INVESTIGATING) {
                if (now - rt.stateStartTick >= 100) {
                    transition(npc, rt, GuardState.CALM, cfg);
                }
            }
        } else if (highestDetection < 0.4f) {
            if (rt.state == GuardState.CALM && nearestDetected != null) {
                transition(npc, rt, GuardState.SUSPICIOUS, cfg);
                rt.suspectPlayer = nearestDetected.getUUID();
            }
        } else if (highestDetection < 0.8f) {
            if ((rt.state == GuardState.CALM || rt.state == GuardState.SUSPICIOUS) && nearestDetected != null) {
                transition(npc, rt, GuardState.INVESTIGATING, cfg);
                rt.suspectPlayer = nearestDetected.getUUID();
            }
        } else {
            // Full detection
            if (rt.state != GuardState.ALARM) {
                transition(npc, rt, GuardState.DETECTED, cfg);
                rt.alarmStartTick = now;

                if (nearestDetected != null) {
                    final ServerPlayer targetPlayer = nearestDetected;

                    StealthMissionManager.notifyDetection(targetPlayer.getUUID(), npc.getUUID());

                    if (!cfg.detectSceneId.isBlank()) {
                        ModNetwork.CHANNEL.send(
                                PacketDistributor.PLAYER.with(() -> targetPlayer),
                                new NpcStartScenePacket(
                                        npc.getNpcData().displayName,
                                        cfg.detectSceneId,
                                        "NEUTRAL",
                                        npc.getUUID()
                                )
                        );
                    }
                }

                transition(npc, rt, GuardState.ALARM, cfg);
            }
        }

        rt.prevDetectionLevel = highestDetection;
    }

    /** Compute a 0–1 detection value for this player by this NPC. */
    private float computeDetection(NpcEntity npc, ServerPlayer player,
                                   StealthConfig cfg, ServerLevel level) {
        double dist = npc.distanceTo(player);
        if (dist > cfg.visionRadius && dist > cfg.hearingRadius) return 0f;

        float score = 0f;

        // Vision check
        if (dist <= cfg.visionRadius) {
            Vec3 npcEye = npc.getEyePosition();
            Vec3 playerPos = player.getEyePosition();
            Vec3 toPlayer = playerPos.subtract(npcEye).normalize();
            Vec3 npcForward = Vec3.directionFromRotation(npc.getXRot(), npc.getYRot());

            double dot = toPlayer.dot(npcForward);
            double halfAngle = Math.toRadians(cfg.visionAngle / 2.0);
            if (dot >= Math.cos(halfAngle)) {
                // In FOV — check line of sight
                boolean los = level.clip(new net.minecraft.world.level.ClipContext(
                        npcEye, playerPos,
                        net.minecraft.world.level.ClipContext.Block.COLLIDER,
                        net.minecraft.world.level.ClipContext.Fluid.NONE, npc))
                        .getType() == net.minecraft.world.phys.HitResult.Type.MISS;

                if (los) {
                    float distFactor = 1f - (float) (dist / cfg.visionRadius);

                    // Light level affects detectability
                    int lightLevel = level.getBrightness(
                            LightLayer.BLOCK,
                            BlockPos.containing(player.position())
                    );
                    float lightFactor = 0.3f + 0.7f * (lightLevel / 15f);

                    // Moving players more visible
                    float moveFactor = player.getDeltaMovement().lengthSqr() > 0.001 ? 1.2f : 1.0f;

                    score = Math.max(score, distFactor * lightFactor * moveFactor * cfg.sensitivity);
                }
            }
        }

        // Hearing check
        if (dist <= cfg.hearingRadius) {
            double speed = player.getDeltaMovement().lengthSqr();
            float noiseFactor = 0f;

            if (speed > 0.1) noiseFactor = 0.3f; // walking
            if (speed > 0.2) noiseFactor = 0.5f; // sprinting
            if (!player.onGround()) noiseFactor += 0.3f; // falling
            if (player.isSprinting()) noiseFactor += 0.2f;

            float distFactor = 1f - (float) (dist / cfg.hearingRadius);
            score = Math.max(score, noiseFactor * distFactor * cfg.sensitivity);
        }

        return Math.min(1f, score);
    }

    private void transition(NpcEntity npc, GuardRuntime rt, GuardState newState, StealthConfig cfg) {
        if (rt.state == newState) return;
        rt.state = newState;
        rt.stateStartTick = npc.level().getGameTime();
        LOG.debug("[NpcDetectionHandler] NPC {} -> {}", npc.getUUID(), newState);
    }

    public static GuardState getGuardState(UUID npcId) {
        GuardRuntime rt = RUNTIMES.get(npcId);
        return rt == null ? GuardState.CALM : rt.state;
    }

    /** Per-NPC mutable runtime (not persisted). */
    private static class GuardRuntime {
        GuardState state = GuardState.CALM;
        long stateStartTick = 0;
        long alarmStartTick = 0;
        float prevDetectionLevel = 0f;
        UUID suspectPlayer = null;
    }
}

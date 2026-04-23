package com.frametrip.dragonlegacyquesttoast.server;
 
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
 
public class VoidAbilityHandler {
 
    private static final Map<UUID, Integer> riftHits      = new HashMap<>();
    private static final Map<UUID, Integer> horizonHits   = new HashMap<>();
    private static final Map<UUID, Long>    shadowCooldown = new HashMap<>();
    private static final Map<UUID, Long>    phaseCooldown  = new HashMap<>();
    private static final Random RNG = new Random();
 
    private static boolean has(ServerPlayer p, String id) {
    return PlayerAbilityManager.isAbilityEnabled(p.getUUID(), id);
    }
 
    // ─── HIT events ──────────────────────────────────────────────────────────
 
    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
 
        if (event.getEntity() instanceof ServerPlayer player) {
            onPlayerHurt(player, event, level);
            return;
        }
 
        if (!(event.getSource().getDirectEntity() instanceof ServerPlayer player)) return;
        LivingEntity target = event.getEntity();
        UUID uid = player.getUUID();
 
        // 1. Void Touch — every hit: Blindness 2s
        if (has(player, "void_touch")) {
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
            voidParticles(level, target.getX(), target.getY() + 1, target.getZ(), 8);
        }
 
        // 4. Void Rift — every 6 hits: pull all in r=6 to target + Weakness
        if (has(player, "void_rift")) {
            int n = riftHits.merge(uid, 1, Integer::sum);
            if (n >= 6) {
                riftHits.put(uid, 0);
                double tx = target.getX(), ty = target.getY(), tz = target.getZ();
                AABB aabb = new AABB(tx-6, ty-3, tz-6, tx+6, ty+3, tz+6);
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aabb)) {
                    if (e == player) continue;
                    Vec3 pull = new Vec3(tx - e.getX(), ty - e.getY(), tz - e.getZ()).normalize().scale(0.6);
                    e.setDeltaMovement(pull.x, pull.y * 0.3, pull.z);
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
                }
                voidRing(level, tx, ty + 1, tz, 5.5f, 30);
                level.playSound(null, tx, ty, tz, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8f, 0.6f);
            }
        }
 
        // 6. Soul Drain — hit on <50% HP enemy: heal 0.5 HP
        if (has(player, "void_soul_drain") && target.getHealth() < target.getMaxHealth() * 0.5f) {
            player.heal(1.0f);
            voidParticles(level, target.getX(), target.getY() + 1, target.getZ(), 5);
        }
 
        // 9. Event Horizon — every 25 hits: gravity pull all in r=8 + Weakness II
        if (has(player, "void_event_horizon")) {
            int n = horizonHits.merge(uid, 1, Integer::sum);
            if (n >= 25) {
                horizonHits.put(uid, 0);
                double tx = target.getX(), ty = target.getY(), tz = target.getZ();
                AABB aabb = new AABB(tx-8, ty-4, tz-8, tx+8, ty+4, tz+8);
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aabb)) {
                    if (e == player) continue;
                    Vec3 pull = new Vec3(tx-e.getX(), ty-e.getY(), tz-e.getZ()).normalize().scale(1.0);
                    e.setDeltaMovement(pull.x, pull.y * 0.2, pull.z);
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
                }
                voidExplosion(level, tx, ty, tz, 60);
                level.playSound(null, tx, ty, tz, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.2f, 0.4f);
            }
        }
    }
 
    private void onPlayerHurt(ServerPlayer player, LivingHurtEvent event, ServerLevel level) {
        float dmg = event.getAmount();
        UUID uid = player.getUUID();
        long now = level.getGameTime();
 
        // 2. Shadow Escape — HP<30% + hit: invisibility + teleport (60s CD)
        if (has(player, "void_shadow")
                && player.getHealth() < player.getMaxHealth() * 0.30f
                && now - shadowCooldown.getOrDefault(uid, 0L) >= 1200) {
            shadowCooldown.put(uid, now);
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 100, 0));
            voidTeleportRandom(player, level, 3.0);
        }
 
        // 5. Phase Shift — lethal hit → negate damage, teleport, Absorption (120s CD)
        if (has(player, "void_phase_shift")
                && player.getHealth() - dmg < 4.0f
                && now - phaseCooldown.getOrDefault(uid, 0L) >= 2400) {
            phaseCooldown.put(uid, now);
            event.setAmount(0);
            voidExplosion(level, player.getX(), player.getY(), player.getZ(), 20);
            voidTeleportFacing(player, level, 5.0);
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.5f);
        }
 
        // 8. Void Mirror — 40% chance: reflect 50% damage to attacker
        if (has(player, "void_mirror") && RNG.nextFloat() < 0.40f) {
            if (event.getSource().getDirectEntity() instanceof LivingEntity att) {
                att.hurt(level.damageSources().magic(), dmg * 0.5f);
                voidParticles(level, att.getX(), att.getY() + 1, att.getZ(), 15);
            }
        }
    }
 
    // ─── KILL events ──────────────────────────────────────────────────────────
 
    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
 
        LivingEntity dead = event.getEntity();
        double dx = dead.getX(), dy = dead.getY(), dz = dead.getZ();
 
        // 10. Annihilation — on kill: AoE 15 dmg in r=8 + heal 5 HP
        if (has(player, "void_annihilation")) {
            AABB aabb = new AABB(dx-8, dy-4, dz-8, dx+8, dy+4, dz+8);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aabb)) {
                if (e == player || e == dead) continue;
                if (e.distanceTo(dead) > 8) continue;
                e.hurt(level.damageSources().playerAttack(player), 15.0f);
            }
            player.heal(10.0f);
            voidExplosion(level, dx, dy, dz, 80);
            level.playSound(null, dx, dy, dz, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.5f, 0.3f);
        }
    }
 
    // ─── TICK events ──────────────────────────────────────────────────────────
 
    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
 
        long t = level.getGameTime();
        UUID uid = player.getUUID();
        double px = player.getX(), py = player.getY(), pz = player.getZ();
 
        // 3. Null Aura — every 80t: drain 1 HP from all in r=5 + heal
        if (has(player, "void_null_aura") && (t + uid.hashCode()) % 80 == 0) {
            AABB aabb = new AABB(px-5, py-3, pz-5, px+5, py+3, pz+5);
            int count = 0;
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aabb)) {
                if (e == player || e.distanceTo(player) > 5) continue;
                e.hurt(level.damageSources().magic(), 2.0f);
                voidParticles(level, e.getX(), e.getY() + 1, e.getZ(), 4);
                count++;
            }
            if (count > 0) player.heal(count * 2.0f);
        }
 
        // 7. Phantom Trail — sneak: every 5t Weakness I in r=2
        if (has(player, "void_phantom_trail") && player.isCrouching() && t % 5 == 0) {
            level.sendParticles(ParticleTypes.ASH, px, py + 0.5, pz, 5, 0.3, 0.3, 0.3, 0.01);
            AABB close = new AABB(px-2, py-1, pz-2, px+2, py+1, pz+2);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, close)) {
                if (e != player) e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
            }
        }
 
        // 10. Annihilation — permanent weakness aura every 60t
        if (has(player, "void_annihilation") && (t + uid.hashCode()) % 60 == 0) {
            AABB aabb = new AABB(px-5, py-3, pz-5, px+5, py+3, pz+5);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aabb)) {
                if (e != player) e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 70, 0));
            }
            level.sendParticles(ParticleTypes.SQUID_INK, px, py + 1, pz, 3, 0.8, 0.8, 0.8, 0.02);
        }
    }
 
    // ─── helpers ──────────────────────────────────────────────────────────────
 
    private static void voidParticles(ServerLevel level, double x, double y, double z, int count) {
        level.sendParticles(ParticleTypes.SQUID_INK, x, y, z, count, 0.4, 0.3, 0.4, 0.04);
        level.sendParticles(ParticleTypes.ASH,       x, y, z, count / 2, 0.5, 0.4, 0.5, 0.02);
    }
 
    private static void voidExplosion(ServerLevel level, double x, double y, double z, int count) {
        level.sendParticles(ParticleTypes.SQUID_INK, x, y + 1, z, count,     1.5, 1.0, 1.5, 0.06);
        level.sendParticles(ParticleTypes.ASH,       x, y + 1, z, count / 2, 2.0, 1.2, 2.0, 0.04);
    }
 
    private static void voidRing(ServerLevel level, double cx, double cy, double cz,
                                 float radius, int count) {
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI / count) * i;
            level.sendParticles(ParticleTypes.SQUID_INK,
                    cx + radius * Math.cos(angle), cy, cz + radius * Math.sin(angle),
                    2, 0.05, 0.2, 0.05, 0.01);
        }
    }
 
    private static void voidTeleportRandom(ServerPlayer player, ServerLevel level, double radius) {
        double ox = player.getX(), oy = player.getY(), oz = player.getZ();
        voidParticles(level, ox, oy + 1, oz, 20);
        double angle = RNG.nextDouble() * 2 * Math.PI;
        player.teleportTo(ox + radius * Math.cos(angle), oy, oz + radius * Math.sin(angle));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8f, 1.0f);
    }
 
    private static void voidTeleportFacing(ServerPlayer player, ServerLevel level, double distance) {
        double ox = player.getX(), oy = player.getY(), oz = player.getZ();
        var look = player.getLookAngle();
        voidParticles(level, ox, oy + 1, oz, 15);
        player.teleportTo(ox + look.x * distance, oy, oz + look.z * distance);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8f, 1.2f);
    }
}

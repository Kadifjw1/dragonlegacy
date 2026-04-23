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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
 
public class IceAbilityHandler {
 
    private static final Map<UUID, Integer> shardHits    = new HashMap<>();
    private static final Map<UUID, Integer> armorCharges = new HashMap<>();
    private static final Random RNG = new Random();
 
    private static boolean has(ServerPlayer p, String id) {
    return PlayerAbilityManager.isAbilityEnabled(p.getUUID(), id);
    }
 
    // ─── HIT events ──────────────────────────────────────────────────────────
 
    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
 
        // Player being attacked
        if (event.getEntity() instanceof ServerPlayer player) {
            onPlayerHurt(player, event, level);
            return;
        }
 
        if (!(event.getSource().getDirectEntity() instanceof ServerPlayer player)) return;
        LivingEntity target = event.getEntity();
        UUID uid = player.getUUID();
 
        // 1. Frost Touch — every hit: Slowness I (3s)
        if (has(player, "ice_frost_touch")) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
            snowParticles(level, target.getX(), target.getY() + 1, target.getZ(), 6);
        }
 
        // 2. Ice Shards — every 5 hits: Slowness II burst in radius 4
        if (has(player, "ice_shards")) {
            int n = shardHits.merge(uid, 1, Integer::sum);
            if (n >= 5) {
                shardHits.put(uid, 0);
                double tx = target.getX(), ty = target.getY(), tz = target.getZ();
                aoeEffect(level, tx, ty, tz, 4.0f, 0, 100);
                snowParticles(level, tx, ty + 1, tz, 40);
                level.playSound(null, tx, ty, tz, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8f, 1.4f);
            }
        }
 
        // 6. Ice Reflect — 25% chance: deal 2 dmg to attacker
        if (has(player, "ice_reflect") && RNG.nextFloat() < 0.25f) {
            target.hurt(level.damageSources().playerAttack(player), 2.0f);
            snowParticles(level, target.getX(), target.getY() + 1, target.getZ(), 12);
        }
    }
 
    private void onPlayerHurt(ServerPlayer player, LivingHurtEvent event, ServerLevel level) {
        float dmg = event.getAmount();
 
        // 3. Freeze Guard — >5 dmg: Slowness III to attacker
        if (has(player, "ice_freeze_guard") && dmg > 5.0f) {
            if (event.getSource().getDirectEntity() instanceof LivingEntity attacker) {
                attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80,  2));
                attacker.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,      80,  0));
                snowParticles(level, attacker.getX(), attacker.getY() + 1, attacker.getZ(), 20);
                level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                        SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.6f, 1.6f);
            }
        }
 
        // 8. Ice Armor — accumulate damage charges, at 5 → AoE freeze
        if (has(player, "ice_armor")) {
            UUID uid = player.getUUID();
            int charges = armorCharges.getOrDefault(uid, 0) + (int) dmg;
            if (charges >= 30) { // 5 * 6
                armorCharges.put(uid, 0);
                double px = player.getX(), py = player.getY(), pz = player.getZ();
                aoeEffect(level, px, py, pz, 5.0f, 0, 200);
                snowParticles(level, px, py + 1, pz, 60);
                level.playSound(null, px, py, pz, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 0.8f);
            } else {
                armorCharges.put(uid, charges);
            }
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
 
        // 4. Blizzard Cloak — every 60t: Slowness I in radius 4
        if (has(player, "ice_blizzard_cloak") && (t + uid.hashCode()) % 60 == 0) {
            aoeEffect(level, px, py, pz, 4.0f, 0, 60);
            snowParticlesRing(level, px, py + 1, pz, 3.5f, 20);
        }
 
        // 5. Cryo Burst — every 60t: Slowness III enemy within 2 + heal
        if (has(player, "ice_cryo_burst") && (t + uid.hashCode() + 30) % 60 == 0) {
            AABB close = new AABB(px-2, py-2, pz-2, px+2, py+2, pz+2);
            List<LivingEntity> near = level.getEntitiesOfClass(LivingEntity.class, close);
            for (LivingEntity e : near) {
                if (e == player) continue;
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));
                e.addEffect(new MobEffectInstance(MobEffects.GLOWING,           80, 0));
                snowParticles(level, e.getX(), e.getY() + 1, e.getZ(), 15);
                player.heal(4.0f);
                break; // one target
            }
        }
 
        // 7. Ice Path — sprint: every 10t Slowness I to enemies near feet
        if (has(player, "ice_path") && player.isSprinting() && t % 10 == 0) {
            snowParticles(level, px, py + 0.1, pz, 5);
            AABB close = new AABB(px-2, py-1, pz-2, px+2, py+1, pz+2);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, close)) {
                if (e != player) e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0));
            }
        }
 
        // 9. Permafrost — every 40t: Slowness II in radius 6
        if (has(player, "ice_permafrost") && (t + uid.hashCode()) % 40 == 0) {
            aoeEffect(level, px, py, pz, 6.0f, 0, 100);
            snowParticlesRing(level, px, py + 1, pz, 5.5f, 35);
            level.playSound(null, px, py, pz, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.4f, 1.8f);
        }
 
        // 10. Absolute Zero — every 100t: Slowness III in radius 8 + heal
        if (has(player, "ice_absolute_zero") && (t + uid.hashCode()) % 100 == 0) {
            AABB aabb = new AABB(px-8, py-4, pz-8, px+8, py+4, pz+8);
            int count = 0;
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aabb)) {
                if (e == player || e.distanceTo(player) > 8) continue;
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                e.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,      100, 1));
                count++;
            }
            if (count > 0) {
                player.heal(count * 6.0f);
                snowParticlesRing(level, px, py + 1, pz, 7.5f, 60);
                level.playSound(null, px, py, pz, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.2f, 0.6f);
            }
        }
    }
 
    // ─── helpers ──────────────────────────────────────────────────────────────
 
    private static void aoeEffect(ServerLevel level, double x, double y, double z,
                                  float radius, float dmg, int slowTicks) {
        AABB aabb = new AABB(x-radius, y-radius, z-radius, x+radius, y+radius, z+radius);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aabb)) {
            if (e.distanceTo(e.level().getNearestPlayer(e, -1)) == 0) continue; // skip players
            if (dmg > 0) e.hurt(e.level().damageSources().generic(), dmg);
            if (slowTicks > 0) e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slowTicks, 1));
        }
    }
 
    private static void snowParticles(ServerLevel level, double x, double y, double z, int count) {
        level.sendParticles(ParticleTypes.SNOWFLAKE, x, y, z, count, 0.6, 0.4, 0.6, 0.05);
    }
 
    private static void snowParticlesRing(ServerLevel level, double cx, double cy, double cz,
                                          float radius, int count) {
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI / count) * i;
            double lx = cx + radius * Math.cos(angle);
            double lz = cz + radius * Math.sin(angle);
            level.sendParticles(ParticleTypes.SNOWFLAKE, lx, cy, lz, 2, 0.1, 0.3, 0.1, 0.02);
        }
    }
}

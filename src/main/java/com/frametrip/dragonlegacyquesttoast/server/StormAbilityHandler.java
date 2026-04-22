package com.frametrip.dragonlegacyquesttoast.server;
 
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.block.Blocks;
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
 
public class StormAbilityHandler {
 
    private static final Map<UUID, Integer> staticHits     = new HashMap<>();
    private static final Map<UUID, Integer> ballHits       = new HashMap<>();
    private static final Map<UUID, Integer> overchargeHits = new HashMap<>();
    private static final Map<UUID, Long>    vengeCooldown  = new HashMap<>();
    private static final Random RNG = new Random();
 
    private static boolean has(ServerPlayer p, String id) {
        return p.isCreative() || PlayerAbilityManager.hasAbility(p.getUUID(), id);
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
 
        // 1. Static Charge — every 5 hits: lightning + 5 dmg
        if (has(player, "storm_static")) {
            int n = staticHits.merge(uid, 1, Integer::sum);
            if (n >= 5) {
                staticHits.put(uid, 0);
                spawnLightning(level, target.getX(), target.getY(), target.getZ());
                target.hurt(level.damageSources().playerAttack(player), 5.0f);
            }
        }
 
        // 4. Chain Lightning — 30% chance: jump to nearest enemy
        if (has(player, "storm_chain") && RNG.nextFloat() < 0.30f) {
            LivingEntity chain = nearestEnemy(level, target, 5.0f, player, target);
            if (chain != null) {
                chain.hurt(level.damageSources().playerAttack(player), 4.0f);
                sparkLine(level, target.getX(), target.getY() + 1, target.getZ(),
                        chain.getX(), chain.getY() + 1, chain.getZ());
            }
        }
 
        // 6. Ball Lightning — every 15 hits: electric burst
        if (has(player, "storm_ball")) {
            int n = ballHits.merge(uid, 1, Integer::sum);
            if (n >= 15) {
                ballHits.put(uid, 0);
                double tx = target.getX(), ty = target.getY(), tz = target.getZ();
                aoeElectric(player, level, tx, ty, tz, 3.0f, 6.0f);
                spawnLightning(level, tx, ty, tz);
            }
        }
 
        // 7. Overcharge — count consecutive hits (reset if player takes dmg)
        if (has(player, "storm_overcharge")) {
            int n = overchargeHits.merge(uid, 1, Integer::sum);
            if (n >= 20) {
                overchargeHits.put(uid, 0);
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 240, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,   240, 0));
                sparkBurst(level, player.getX(), player.getY() + 1, player.getZ(), 50);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.5f, 2.0f);
            }
        }
    }
 
    private void onPlayerHurt(ServerPlayer player, LivingHurtEvent event, ServerLevel level) {
        // 7. Overcharge — reset counter when player is hit
        if (has(player, "storm_overcharge")) {
            overchargeHits.put(player.getUUID(), 0);
        }
 
        // 3. Electric Shield — 30% chance: stun attacker
        if (has(player, "storm_electric_shield") && RNG.nextFloat() < 0.30f) {
            if (event.getSource().getDirectEntity() instanceof LivingEntity att) {
                att.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2));
                att.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,          40, 0));
                att.hurt(level.damageSources().playerAttack(player), 3.0f);
                sparkBurst(level, att.getX(), att.getY() + 1, att.getZ(), 20);
            }
        }
 
        // 9. Vengeful Storm — >8 dmg: lightning on all nearby (60s CD)
        if (has(player, "storm_vengeful") && event.getAmount() > 8.0f) {
            long now = level.getGameTime();
            if (now - vengeCooldown.getOrDefault(player.getUUID(), 0L) >= 1200) {
                vengeCooldown.put(player.getUUID(), now);
                AABB aabb = new AABB(player.getX()-6, player.getY()-3, player.getZ()-6,
                        player.getX()+6, player.getY()+3, player.getZ()+6);
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aabb)) {
                    if (e == player) continue;
                    spawnLightning(level, e.getX(), e.getY(), e.getZ());
                    e.hurt(level.damageSources().playerAttack(player), 6.0f);
                }
            }
        }
    }
 
    // ─── KILL events ──────────────────────────────────────────────────────────
 
    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
 
        // 5. Storm Leap — teleport to nearest enemy + lightning at old pos
        if (has(player, "storm_leap")) {
            double ox = player.getX(), oy = player.getY(), oz = player.getZ();
            LivingEntity nearest = nearestEnemy(level, player, 8.0f, player, event.getEntity());
            if (nearest != null) {
                spawnLightning(level, ox, oy, oz);
                player.teleportTo(nearest.getX(), nearest.getY(), nearest.getZ());
                sparkBurst(level, nearest.getX(), nearest.getY() + 1, nearest.getZ(), 30);
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
 
        // 2. Thunder Step — sprint: every 10t shock enemies in radius 3
        if (has(player, "storm_thunder_step") && player.isSprinting() && t % 10 == 0) {
            sparkBurst(level, px, py + 0.1, pz, 8);
            AABB close = new AABB(px-3, py-2, pz-3, px+3, py+2, pz+3);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, close)) {
                if (e != player) e.hurt(level.damageSources().playerAttack(player), 2.0f);
            }
        }
 
        // 8. Cyclone — every 40t: knock back all in radius 5
        if (has(player, "storm_cyclone") && (t + uid.hashCode()) % 40 == 0) {
            AABB aabb = new AABB(px-5, py-3, pz-5, px+5, py+3, pz+5);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aabb)) {
                if (e == player || e.distanceTo(player) > 5) continue;
                e.knockback(0.9, px - e.getX(), pz - e.getZ());
            }
            sparkRing(level, px, py + 1, pz, 4.5f, 30);
            level.playSound(null, px, py, pz, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.4f, 1.8f);
        }
 
        // 10. Thundergod — every 100t: strike 3 random enemies in r=12
        if (has(player, "storm_thundergod") && (t + uid.hashCode()) % 100 == 0) {
            AABB wide = new AABB(px-12, py-6, pz-12, px+12, py+6, pz+12);
            List<LivingEntity> enemies = level.getEntitiesOfClass(LivingEntity.class, wide)
                    .stream().filter(e -> e != player && e.distanceTo(player) <= 12)
                    .toList();
            int strikes = Math.min(3, enemies.size());
            for (int i = 0; i < strikes; i++) {
                LivingEntity target = enemies.get(RNG.nextInt(enemies.size()));
                spawnLightning(level, target.getX(), target.getY(), target.getZ());
                target.hurt(level.damageSources().playerAttack(player), 8.0f);
            }
            // Permanent electric aura visual
            sparkBurst(level, px, py + 1, pz, 20);
        }
 
        // Thundergod — constant aura particles every 5t
        if (has(player, "storm_thundergod") && t % 5 == 0) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, px, py + 1, pz, 3, 0.5, 0.8, 0.5, 0.05);
        }
    }
 
    // ─── helpers ──────────────────────────────────────────────────────────────
 
    private static void spawnLightning(ServerLevel level, double x, double y, double z) {
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y + 0.5, z, 40, 0.2, 1.5, 0.2, 0.15);
        level.sendParticles(ParticleTypes.CLOUD,          x, y + 0.5, z, 5,  0.1, 0.5, 0.1, 0.02);
        level.playSound(null, x, y, z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.6f, 1.5f);
    }
 
    private static void aoeElectric(ServerPlayer player, ServerLevel level,
                                    double x, double y, double z, float radius, float dmg) {
        AABB aabb = new AABB(x-radius, y-radius, z-radius, x+radius, y+radius, z+radius);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aabb)) {
            if (e == player || e.distanceTo(e.level().getNearestPlayer(e, -1)) == 0) continue;
            if (e.distanceTo(player) > radius) continue;
            e.hurt(level.damageSources().playerAttack(player), dmg);
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1));
        }
        sparkBurst(level, x, y + 1, z, 35);
    }
 
    private static void sparkBurst(ServerLevel level, double x, double y, double z, int count) {
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, count, 0.8, 0.5, 0.8, 0.1);
    }
 
    private static void sparkLine(ServerLevel level, double x1, double y1, double z1,
                                  double x2, double y2, double z2) {
        int steps = (int) Math.max(5, Math.sqrt((x2-x1)*(x2-x1)+(z2-z1)*(z2-z1)) * 3);
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    x1 + (x2-x1)*t, y1 + (y2-y1)*t, z1 + (z2-z1)*t, 1, 0.05, 0.05, 0.05, 0.0);
        }
    }
 
    private static void sparkRing(ServerLevel level, double cx, double cy, double cz,
                                  float radius, int count) {
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI / count) * i;
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    cx + radius * Math.cos(angle), cy, cz + radius * Math.sin(angle),
                    2, 0.1, 0.2, 0.1, 0.05);
        }
    }
 
    private static LivingEntity nearestEnemy(ServerLevel level, LivingEntity from,
                                             float radius, LivingEntity... exclude) {
        AABB aabb = new AABB(from.getX()-radius, from.getY()-radius, from.getZ()-radius,
                from.getX()+radius, from.getY()+radius, from.getZ()+radius);
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        outer:
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aabb)) {
            for (LivingEntity ex : exclude) if (e == ex) continue outer;
            double d = e.distanceTo(from);
            if (d < bestDist) { bestDist = d; best = e; }
        }
        return best;
    }
}

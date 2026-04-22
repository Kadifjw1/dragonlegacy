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
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
 
public class FireAbilityHandler {
 
    private static final Map<UUID, Integer> emberHits      = new HashMap<>();
    private static final Map<UUID, Integer> infernoHits    = new HashMap<>();
    private static final Map<UUID, Long>    vengeCooldown  = new HashMap<>();
 
    private static boolean has(ServerPlayer p, String id) {
        return p.isCreative() || PlayerAbilityManager.hasAbility(p.getUUID(), id);
    }
 
    // ─── HIT events ──────────────────────────────────────────────────────────
 
    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
 
        // ── Abilities that fire when the PLAYER is attacked ───────────────────
        if (event.getEntity() instanceof ServerPlayer player) {
            onPlayerHurt(player, event, level);
            return;
        }
 
        // ── Abilities that fire when the PLAYER attacks a mob ─────────────────
        if (!(event.getSource().getDirectEntity() instanceof ServerPlayer player)) return;
        LivingEntity target = event.getEntity();
 
        UUID uid = player.getUUID();
 
        // 1. Ember Touch — every 5 hits → AoE fire burst
        if (has(player, "fire_ember")) {
            int n = emberHits.merge(uid, 1, Integer::sum);
            if (n >= 5) {
                emberHits.put(uid, 0);
                aoeFireBurst(player, target, level, 3.0f, 8.0f, 3, 50);
            }
        }
 
        // 5. Inferno Wave — every 10 hits → ring of fire
        if (has(player, "fire_inferno_wave")) {
            int n = infernoHits.merge(uid, 1, Integer::sum);
            if (n >= 10) {
                infernoHits.put(uid, 0);
                aoeFireBurst(player, target, level, 4.0f, 4.0f, 2, 70);
            }
        }
    }
 
    private void onPlayerHurt(ServerPlayer player, LivingHurtEvent event, ServerLevel level) {
        float dmg = event.getAmount();
 
        // 4. Magma Shield — >4 dmg: reduce 30%, burn attacker
        if (has(player, "fire_magma_shield") && dmg > 4.0f) {
            event.setAmount(dmg * 0.70f);
            if (event.getSource().getDirectEntity() instanceof LivingEntity attacker) {
                attacker.setSecondsOnFire(4);
                double ax = attacker.getX(), ay = attacker.getY(), az = attacker.getZ();
                level.sendParticles(ParticleTypes.LAVA, ax, ay + 1, az, 15, 0.5, 0.5, 0.5, 0.0);
                level.playSound(null, ax, ay, az, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8f, 1.2f);
            }
        }
 
        // 8. Vengeance Flash — >6 dmg: explosion (30s CD)
        if (has(player, "fire_vengeance_flash") && dmg > 6.0f) {
            long now = level.getGameTime();
            long last = vengeCooldown.getOrDefault(player.getUUID(), 0L);
            if (now - last >= 600) {
                vengeCooldown.put(player.getUUID(), now);
                double px = player.getX(), py = player.getY(), pz = player.getZ();
                aoeAroundPoint(player, level, px, py, pz, 5.0f, 10.0f, 0);
                level.sendParticles(ParticleTypes.FLAME,       px, py + 1, pz, 80, 2.0, 1.0, 2.0, 0.1);
                level.sendParticles(ParticleTypes.LARGE_SMOKE, px, py + 2, pz, 20, 1.5, 0.8, 1.5, 0.03);
                level.playSound(null, px, py, pz, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.2f, 0.7f);
            }
        }
 
        // 9. Ashen Armor — while on fire: reduce incoming dmg by 40%
        if (has(player, "fire_ashen_armor") && player.isOnFire()) {
            event.setAmount(event.getAmount() * 0.60f);
        }
    }
 
    // ─── KILL events ──────────────────────────────────────────────────────────
 
    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
 
        LivingEntity dead = event.getEntity();
        double dx = dead.getX(), dy = dead.getY(), dz = dead.getZ();
 
        // 6. Phoenix Rush — on kill: Speed III + Strength I + heal
        if (has(player, "fire_phoenix_rush")) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,  160, 2));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,    160, 0));
            player.heal(4.0f);
            level.sendParticles(ParticleTypes.FLAME, dx, dy + 1, dz, 40, 1.0, 0.8, 1.0, 0.1);
        }
 
        // 10. Eternal Flame — on kill: fire explosion at target
        if (has(player, "fire_eternal_flame")) {
            aoeAroundPoint(player, level, dx, dy, dz, 4.0f, 8.0f, 3);
            level.sendParticles(ParticleTypes.FLAME,       dx, dy + 1, dz, 60, 1.5, 0.8, 1.5, 0.1);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, dx, dy + 2, dz, 15, 1.0, 0.5, 1.0, 0.02);
            level.playSound(null, dx, dy, dz, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 0.8f);
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
 
        // 2. Flame Aura — every 80t: 3 dmg + fire 2s in radius 3
        if (has(player, "fire_aura") && (t + uid.hashCode()) % 80 == 0) {
            aoeAroundPoint(player, level, px, py, pz, 3.0f, 3.0f, 2);
            level.sendParticles(ParticleTypes.FLAME, px, py + 1, pz, 20, 1.2, 0.5, 1.2, 0.05);
        }
 
        // 3. Fire Trail — sprint: every 10t, fire particles + fire nearby
        if (has(player, "fire_trail") && player.isSprinting() && t % 10 == 0) {
            level.sendParticles(ParticleTypes.FLAME, px, py + 0.1, pz, 8, 0.3, 0.1, 0.3, 0.03);
            aoeAroundPoint(player, level, px, py, pz, 2.0f, 0.0f, 2);
        }
 
        // 7. Blazing Gaze — every 20t: fire entity in facing direction
        if (has(player, "fire_blazing_gaze") && t % 20 == 0) {
            var lookDir = player.getLookAngle();
            for (int i = 1; i <= 4; i++) {
                double lx = px + lookDir.x * i, ly = py + lookDir.y * i + 1.6, lz = pz + lookDir.z * i;
                level.sendParticles(ParticleTypes.FLAME, lx, ly, lz, 3, 0.15, 0.15, 0.15, 0.01);
                AABB aabb = new AABB(lx - 1, ly - 1, lz - 1, lx + 1, ly + 1, lz + 1);
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aabb)) {
                    if (e != player) e.setSecondsOnFire(4);
                }
            }
        }
 
        // 9. Ashen Armor — while on fire: deal 1 dmg to nearby every 10t
        if (has(player, "fire_ashen_armor") && player.isOnFire() && t % 10 == 0) {
            level.sendParticles(ParticleTypes.LAVA, px, py + 1, pz, 5, 0.4, 0.4, 0.4, 0.0);
            aoeAroundPoint(player, level, px, py, pz, 2.0f, 1.0f, 0);
        }
 
        // 10. Eternal Flame — fire immunity + permanent aura (every 40t)
        if (has(player, "fire_eternal_flame")) {
            if (player.isOnFire()) player.clearFire();
            if ((t + uid.hashCode()) % 40 == 0) {
                aoeAroundPoint(player, level, px, py, pz, 5.0f, 6.0f, 3);
                level.sendParticles(ParticleTypes.FLAME, px, py + 1, pz, 30, 2.0, 0.8, 2.0, 0.06);
                level.sendParticles(ParticleTypes.LARGE_SMOKE, px, py + 1.5, pz, 8, 1.5, 0.5, 1.5, 0.02);
            }
        }
    }
 
    // ─── helpers ──────────────────────────────────────────────────────────────
 
    private static void aoeFireBurst(ServerPlayer player, LivingEntity target, ServerLevel level,
                                     float radius, float dmg, int fireSec, int particles) {
        double x = target.getX(), y = target.getY(), z = target.getZ();
        aoeAroundPoint(player, level, x, y, z, radius, dmg, fireSec);
        level.sendParticles(ParticleTypes.FLAME,       x, y + 1.0, z, particles,     radius * 0.8, 0.6, radius * 0.8, 0.08);
        level.sendParticles(ParticleTypes.LAVA,        x, y + 0.5, z, particles / 5, radius * 0.5, 0.3, radius * 0.5, 0.0);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 1.5, z, particles / 8, radius * 0.5, 0.5, radius * 0.5, 0.02);
        level.playSound(null, x, y, z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 0.8f);
    }
 
    private static void aoeAroundPoint(ServerPlayer player, ServerLevel level,
                                       double x, double y, double z,
                                       float radius, float dmg, int fireSec) {
        AABB aabb = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, aabb)) {
            if (e == player) continue;
            double dist = Math.sqrt((e.getX()-x)*(e.getX()-x) + (e.getZ()-z)*(e.getZ()-z));
            if (dist > radius) continue;
            if (dmg > 0) e.hurt(level.damageSources().playerAttack(player), dmg);
            if (fireSec > 0) e.setSecondsOnFire(fireSec);
        }
    }
}

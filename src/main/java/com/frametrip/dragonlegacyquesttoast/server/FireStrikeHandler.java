package com.frametrip.dragonlegacyquesttoast.server;
 
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
 
public class FireStrikeHandler {
    public static final String ABILITY_ID = "fire_strike";
    private static final int HITS_REQUIRED = 5;
    private static final float AOE_RADIUS = 3.0f;
    private static final float AOE_DAMAGE = 8.0f;
    private static final int FIRE_SECONDS = 3;
 
    private static final Map<UUID, Integer> hitCounters = new HashMap<>();
 
    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;
        if (event.getEntity() instanceof ServerPlayer) return;
        if (!(event.getSource().getDirectEntity() instanceof ServerPlayer player)) return;
        if (!PlayerAbilityManager.hasAbility(player.getUUID(), ABILITY_ID)) return;
 
        int count = hitCounters.merge(player.getUUID(), 1, Integer::sum);
        if (count >= HITS_REQUIRED) {
            hitCounters.put(player.getUUID(), 0);
            triggerFireStrike(player, event.getEntity(), serverLevel);
        }
    }
 
    private static void triggerFireStrike(ServerPlayer player, LivingEntity target, ServerLevel level) {
        double x = target.getX();
        double y = target.getY();
        double z = target.getZ();
 
        AABB aabb = new AABB(x - AOE_RADIUS, y - AOE_RADIUS, z - AOE_RADIUS,
                             x + AOE_RADIUS, y + AOE_RADIUS, z + AOE_RADIUS);
        List<LivingEntity> nearby = level.getEntitiesOfClass(LivingEntity.class, aabb);
        for (LivingEntity entity : nearby) {
            if (entity == player) continue;
            if (entity.distanceTo(target) > AOE_RADIUS) continue;
            entity.hurt(level.damageSources().playerAttack(player), AOE_DAMAGE);
            entity.setSecondsOnFire(FIRE_SECONDS);
        }
 
        level.sendParticles(ParticleTypes.FLAME,       x, y + 1.0, z, 50, 1.5, 0.8, 1.5, 0.08);
        level.sendParticles(ParticleTypes.LAVA,        x, y + 0.5, z,  8, 1.0, 0.3, 1.0, 0.0);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 1.5, z,  6, 0.8, 0.5, 0.8, 0.02);
 
        level.playSound(null, x, y, z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 0.8f);
    }
}

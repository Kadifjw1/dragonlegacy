package com.frametrip.dragonlegacyquesttoast.server.combat;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// [CMB-2/3/4]: Combat event handler — boss phases, reinforcement, unique abilities.
public class NpcCombatHandler {

    // [CMB-2]: Tracks current boss phase index per NPC uuid.
    private static final Map<UUID, Integer> bossPhaseIndex = new HashMap<>();
    // [CMB-3]: Tracks whether reinforcements have been called.
    private static final Map<UUID, Long> reinforcementCallTime = new HashMap<>();
    // [CMB-4]: Per-ability last-use timestamps per NPC.
    private static final Map<UUID, Map<Integer, Long>> abilityCooldowns = new HashMap<>();
    // [CMB-4]: EVERY_N_SECONDS last tick per NPC.
    private static final Map<UUID, Long> periodicAbilityTick = new HashMap<>();

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new NpcCombatHandler());
        // Register server tick for periodic ability and arena checks.
        MinecraftForge.EVENT_BUS.addListener(NpcCombatHandler::onServerTick);
    }

    // ── On any living entity hurt ─────────────────────────────────────────────

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof NpcEntity npc)) return;
        if (npc.level().isClientSide) return;

        NpcEntityData data = npc.getNpcData();
        float hpAfter = npc.getHealth() - event.getAmount();
        float hpPct = (hpAfter / Math.max(1, data.maxHealth)) * 100f;

        // [CMB-2]: Boss phase transition check.
        if (data.bossPhases != null && !data.bossPhases.isEmpty()) {
            checkBossPhase(npc, data, hpPct);
        }

        // [CMB-3]: Reinforcement on low HP.
        if (data.reinforcementEnabled && hpPct <= data.reinforcementHpThreshold) {
            long now = System.currentTimeMillis();
            long last = reinforcementCallTime.getOrDefault(npc.getUUID(), 0L);
            if (now - last > data.reinforcementCooldownSec * 1000L) {
                reinforcementCallTime.put(npc.getUUID(), now);
                callReinforcements(npc, data);
            }
        }

        // [CMB-4]: ON_HIT_RECEIVED abilities.
        if (data.combatAbilities != null) {
            for (int i = 0; i < data.combatAbilities.size(); i++) {
                NpcAbility ab = data.combatAbilities.get(i);
                if (NpcAbility.ON_HIT_RECEIVED.equals(ab.triggerCondition) && canUseAbility(npc.getUUID(), i, ab)) {
                    executeAbility(npc, data, ab);
                    recordAbilityUse(npc.getUUID(), i);
                }
            }
        }
    }

    // Also catch NPC hitting a target (ON_HIT trigger).
    @SubscribeEvent
    public void onNpcHitsTarget(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof NpcEntity npc) {
            if (npc.level().isClientSide) return;
            NpcEntityData data = npc.getNpcData();
            if (data.combatAbilities == null) return;
            for (int i = 0; i < data.combatAbilities.size(); i++) {
                NpcAbility ab = data.combatAbilities.get(i);
                if (NpcAbility.ON_HIT.equals(ab.triggerCondition) && canUseAbility(npc.getUUID(), i, ab)) {
                    executeAbility(npc, data, ab);
                    recordAbilityUse(npc.getUUID(), i);
                }
            }
        }
    }

    // ── Server tick — periodic abilities + arena ──────────────────────────────

    private static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;

        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        for (ServerLevel level : server.getAllLevels()) {
            for (net.minecraft.world.entity.Entity e : level.getAllEntities()) {
                if (!(e instanceof NpcEntity npc)) continue;
                {
                if (npc.isDeadOrDying()) continue;
                NpcEntityData data = npc.getNpcData();
                long now = System.currentTimeMillis();

                // [CMB-4]: EVERY_N_SECONDS and ON_TARGET_NEARBY triggers.
                if (data.combatAbilities != null) {
                    for (int i = 0; i < data.combatAbilities.size(); i++) {
                        NpcAbility ab = data.combatAbilities.get(i);
                        boolean periodic = NpcAbility.EVERY_N_SECONDS.equals(ab.triggerCondition);
                        boolean nearby   = NpcAbility.ON_TARGET_NEARBY.equals(ab.triggerCondition);
                        if (!periodic && !nearby) continue;
                        if (!canUseAbility(npc.getUUID(), i, ab)) continue;

                        if (periodic) {
                            executeAbility(npc, data, ab);
                            recordAbilityUse(npc.getUUID(), i);
                        } else if (nearby && npc.getTarget() != null) {
                            if (npc.getTarget().distanceTo(npc) <= ab.param) {
                                executeAbility(npc, data, ab);
                                recordAbilityUse(npc.getUUID(), i);
                            }
                        }
                    }
                }

                // [CMB-5]: Arena leash — return to center if NPC wanders too far.
                if (data.arenaEnabled && !data.arenaCenter.isEmpty()) {
                    try {
                        String[] parts = data.arenaCenter.split(",");
                        double cx = Double.parseDouble(parts[0].trim());
                        double cy = Double.parseDouble(parts[1].trim());
                        double cz = Double.parseDouble(parts[2].trim());
                        double dist = npc.position().distanceTo(new Vec3(cx, cy, cz));
                        if (dist > data.arenaRadius) {
                            npc.setTarget(null);
                            npc.getNavigation().moveTo(cx, cy, cz, 1.2);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    // ── Boss phase logic ──────────────────────────────────────────────────────

    private static void checkBossPhase(NpcEntity npc, NpcEntityData data, float hpPct) {
        int currentIdx = bossPhaseIndex.getOrDefault(npc.getUUID(), -1);
        BossPhase newPhase = null;
        int newIdx = currentIdx;

        // Find the highest-indexed phase whose threshold has been crossed.
        for (int i = 0; i < data.bossPhases.size(); i++) {
            BossPhase p = data.bossPhases.get(i);
            if (hpPct <= p.hpThreshold && i > currentIdx) {
                if (newPhase == null || p.hpThreshold > newPhase.hpThreshold) {
                    newPhase = p;
                    newIdx = i;
                }
            }
        }

        if (newPhase == null) return;
        bossPhaseIndex.put(npc.getUUID(), newIdx);
        applyBossPhase(npc, data, newPhase);
    }

    private static void applyBossPhase(NpcEntity npc, NpcEntityData data, BossPhase phase) {
        // Speed multiplier.
        var speedAttr = npc.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null)
            speedAttr.setBaseValue(speedAttr.getBaseValue() * phase.speedMultiplier);

        // Phase dialog broadcast.
        if (!phase.phaseDialog.isEmpty()) {
            npc.level().players().forEach(p ->
                p.sendSystemMessage(Component.literal("§c[" + data.displayName + "] §f" + phase.phaseDialog)));
        }

        // Summon mobs.
        if (!phase.summonType.isEmpty() && phase.summonCount > 0 && npc.level() instanceof ServerLevel sl) {
            spawnMobs(sl, npc, phase.summonType, phase.summonCount);
        }
    }

    // ── Reinforcement logic ───────────────────────────────────────────────────

    private static void callReinforcements(NpcEntity npc, NpcEntityData data) {
        if (data.reinforcementType.isEmpty()) return;
        if (!(npc.level() instanceof ServerLevel sl)) return;

        npc.level().players().forEach(p ->
            p.sendSystemMessage(Component.literal("§c[" + data.displayName + "] §fНа помощь!")));

        spawnMobs(sl, npc, data.reinforcementType, data.reinforcementCount);
    }

    // ── Ability execution ─────────────────────────────────────────────────────

    private static void executeAbility(NpcEntity npc, NpcEntityData data, NpcAbility ab) {
        NpcAbilityType type = NpcAbilityType.fromName(ab.abilityType);
        LivingEntity target = npc.getTarget();

        switch (type) {
            case POISON -> {
                if (target != null) target.addEffect(
                    new MobEffectInstance(MobEffects.POISON, (int) ab.param * 20, 0));
            }
            case STUN -> {
                if (target != null) target.addEffect(
                    new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int) ab.param, 255));
            }
            case DASH -> {
                if (target != null) {
                    Vec3 dir = target.position().subtract(npc.position()).normalize();
                    npc.setDeltaMovement(dir.scale(ab.param));
                }
            }
            case SHIELD -> {
                npc.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, (int)(ab.param / 4)));
            }
            case PULL -> {
                if (target != null) {
                    Vec3 dir = npc.position().subtract(target.position()).normalize();
                    target.setDeltaMovement(dir.scale(ab.param * 0.5));
                }
            }
            case KNOCKBACK -> {
                npc.level().getEntitiesOfClass(LivingEntity.class,
                        npc.getBoundingBox().inflate(ab.param),
                        e -> e != npc && e instanceof Player)
                    .forEach(e -> {
                        Vec3 dir = e.position().subtract(npc.position()).normalize();
                        e.setDeltaMovement(dir.scale(ab.param * 0.4).add(0, 0.4, 0));
                    });
            }
            case HEAL_SELF -> {
                npc.heal(ab.param);
            }
            case SUMMON -> {
                if (npc.level() instanceof ServerLevel sl)
                    spawnMobs(sl, npc, ab.summonType, ab.summonCount);
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static boolean canUseAbility(UUID npcId, int abilityIdx, NpcAbility ab) {
        Map<Integer, Long> map = abilityCooldowns.computeIfAbsent(npcId, k -> new HashMap<>());
        long last = map.getOrDefault(abilityIdx, 0L);
        return System.currentTimeMillis() - last >= ab.cooldownSec * 1000L;
    }

    private static void recordAbilityUse(UUID npcId, int abilityIdx) {
        abilityCooldowns.computeIfAbsent(npcId, k -> new HashMap<>())
                        .put(abilityIdx, System.currentTimeMillis());
    }

    private static void spawnMobs(ServerLevel level, NpcEntity near, String entityTypeId, int count) {
        var rl = new ResourceLocation(entityTypeId);
        EntityType<?> et = ForgeRegistries.ENTITY_TYPES.getValue(rl);
        if (et == null) return;
        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2 * i) / count;
            double x = near.getX() + Math.cos(angle) * 2;
            double z = near.getZ() + Math.sin(angle) * 2;
            var entity = et.create(level);
            if (entity instanceof Mob mob) {
                mob.moveTo(x, near.getY(), z, 0, 0);
                level.addFreshEntity(mob);
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()),
                    MobSpawnType.MOB_SUMMONED, null, null);
            }
        }
    }
}

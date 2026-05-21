package com.frametrip.dragonlegacyquesttoast.entity.goal;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// [WLD-3]: NPC guards a territory — warns once, then attacks intruders.
public class NpcTerritoryGuardGoal extends Goal {

    private static final int SCAN_INTERVAL  = 40; // ticks between scans
    private static final int WARN_GRACE_TICKS = 60; // 3 s before attacking warned entity

    private final NpcEntity npc;
    private int scanTick = 0;
    // warnedAt: UUID → game tick when warning was given
    private final Map<UUID, Long> warnedAt = new HashMap<>();

    public NpcTerritoryGuardGoal(NpcEntity npc) {
        this.npc = npc;
        setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (npc.level().isClientSide) return false;
        NpcEntityData data = npc.getNpcData();
        return data.guardTerritoryEnabled;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        if (++scanTick < SCAN_INTERVAL) return;
        scanTick = 0;

        NpcEntityData data = npc.getNpcData();
        if (!data.guardTerritoryEnabled) return;

        float radius = data.guardRadius;
        long now = npc.level().getGameTime();

        // Remove stale warnings for entities no longer nearby.
        warnedAt.keySet().removeIf(id ->
            npc.level().getEntitiesOfClass(LivingEntity.class,
                    npc.getBoundingBox().inflate(radius + 20),
                    x -> x.getUUID().equals(id)).isEmpty()
        );

        // Scan for hostile intruders or hostile-flagged entities.
        npc.level().getEntitiesOfClass(LivingEntity.class,
                npc.getBoundingBox().inflate(radius),
                e -> isIntruder(e, data))
            .forEach(intruder -> {
                UUID id = intruder.getUUID();
                if (data.guardWarnFirst && !warnedAt.containsKey(id)) {
                    // Issue warning.
                    warnedAt.put(id, now);
                    npc.level().players().forEach(p ->
                        p.sendSystemMessage(Component.literal(
                            "§c[" + data.displayName + "] §fСтоять! Запретная зона!")));
                } else if (!data.guardWarnFirst || now - warnedAt.getOrDefault(id, 0L) >= WARN_GRACE_TICKS) {
                    // Grace period expired — attack.
                    npc.setTarget(intruder);
                }
            });
    }

    private boolean isIntruder(LivingEntity e, NpcEntityData data) {
        if (e == npc) return false;
        if (e.isDeadOrDying()) return false;
        // Attack monsters always; attack players only if NPC relation is HOSTILE.
        if (e instanceof Monster) return true;
        if (e instanceof Player) return "HOSTILE".equals(data.playerRelation);
        return false;
    }
}

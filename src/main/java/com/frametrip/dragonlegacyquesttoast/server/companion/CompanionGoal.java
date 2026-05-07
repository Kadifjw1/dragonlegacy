package com.frametrip.dragonlegacyquesttoast.server.companion;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.profession.NpcProfessionType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * All-in-one companion AI goal.
 * Reads CompanionMode from NpcEntityData each tick and dispatches to the
 * appropriate behaviour. Only active when NPC profession is COMPANION.
 */
public class CompanionGoal extends Goal {

    private final NpcEntity npc;

    private Player owner = null;
    private int cooldown = 0;
    private int guardIdleTick = 0;

    public CompanionGoal(NpcEntity npc) {
        this.npc = npc;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        NpcEntityData data = npc.getNpcData();
        return data.professionData != null
                && data.professionData.type == NpcProfessionType.COMPANION;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        if (--cooldown > 0) return;
        cooldown = 10;

        NpcEntityData data = npc.getNpcData();
        CompanionData cd = data.companionData;
        if (cd == null) return;

        resolveOwner(cd);

        switch (cd.mode) {
            case FOLLOW  -> tickFollow(cd);
            case WAIT    -> tickWait();
            case GUARD   -> tickGuard(cd);
            case PROTECT -> tickProtect(cd);
            case COMBAT  -> tickCombat(cd);
            case STEALTH -> tickStealth(cd);
            case PASSIVE -> tickPassive();
        }
    }

    private void tickFollow(CompanionData cd) {
        if (owner == null) return;
        double dist = npc.distanceTo(owner);
        if (dist > cd.followDistance + 1) {
            npc.getNavigation().moveTo(owner, speed(cd));
            lookAt(owner);
        } else {
            npc.getNavigation().stop();
            lookAt(owner);
        }

        if (dist > 24 && npc.level() instanceof ServerLevel) {
            npc.teleportTo(owner.getX(), owner.getY(), owner.getZ());
        }
    }

    private void tickWait() {
        npc.getNavigation().stop();
    }

    private void tickGuard(CompanionData cd) {
        if (!cd.guardPointSet) {
            tickWait();
            return;
        }

        double distSqr = npc.distanceToSqr(cd.guardX, cd.guardY, cd.guardZ);
        if (distSqr > 1.5 * 1.5) {
            npc.getNavigation().moveTo(cd.guardX, cd.guardY, cd.guardZ, speed(cd) * 0.8);
        } else {
            npc.getNavigation().stop();
            guardIdleTick++;
            if (guardIdleTick % 40 == 0) {
                npc.setYRot(npc.getYRot() + 90);
            }
        }

        if (cd.aggressiveness > 0.3f) {
            LivingEntity threat = findThreat(cd.guardX, cd.guardY, cd.guardZ, cd.guardRadius, cd);
            if (threat != null) attackTarget(threat);
        }
    }

    private void tickProtect(CompanionData cd) {
        if (owner == null) return;
        tickFollow(cd);
        LivingEntity attacker = findAttackerOf(owner);
        if (attacker != null) attackTarget(attacker);
    }

    private void tickCombat(CompanionData cd) {
        if (owner == null) {
            tickWait();
            return;
        }

        LivingEntity target = findNearestHostile(cd);
        if (target != null) {
            attackTarget(target);
            npc.getNavigation().moveTo(target, speed(cd));
        } else {
            tickFollow(cd);
        }
    }

    private void tickStealth(CompanionData cd) {
        if (owner == null) return;
        double dist = npc.distanceTo(owner);
        if (dist > cd.followDistance * 1.5) {
            npc.getNavigation().moveTo(owner, speed(cd) * 0.5);
        } else {
            npc.getNavigation().stop();
        }
        npc.setTarget(null);
    }

    private void tickPassive() {
        npc.getNavigation().stop();
        npc.setTarget(null);
    }

    private void resolveOwner(CompanionData cd) {
        if (cd.ownerUUID.isEmpty()) {
            owner = null;
            return;
        }
        if (owner != null && owner.isAlive()
                && owner.getUUID().toString().equals(cd.ownerUUID)) {
            return;
        }
        try {
            UUID ownerUuid = UUID.fromString(cd.ownerUUID);
            owner = npc.level().getPlayerByUUID(ownerUuid);
        } catch (IllegalArgumentException e) {
            owner = null;
        }
    }

    private double speed(CompanionData cd) {
        return 0.3 + cd.followSpeed * 0.4;
    }

    private void lookAt(LivingEntity target) {
        npc.getLookControl().setLookAt(target, 30f, 30f);
    }

    private void attackTarget(LivingEntity target) {
        npc.setTarget(target);
    }

    private LivingEntity findNearestHostile(CompanionData cd) {
        List<LivingEntity> nearby = npc.level().getEntitiesOfClass(
                LivingEntity.class,
                npc.getBoundingBox().inflate(16)
        );

        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;

        for (LivingEntity e : nearby) {
            if (e == npc || !e.isAlive() || e instanceof Player) continue;
            if (!npc.getSensing().hasLineOfSight(e)) continue;

            double d = npc.distanceToSqr(e);
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        return best;
    }

    private LivingEntity findAttackerOf(Player player) {
        List<LivingEntity> nearby = npc.level().getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(16)
        );

        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;

        for (LivingEntity e : nearby) {
            if (e == npc || !e.isAlive() || e instanceof Player) continue;
            if (e instanceof Mob mob && mob.getTarget() == player) {
                double d = npc.distanceToSqr(e);
                if (d < bestDist) {
                    bestDist = d;
                    best = e;
                }
            }
        }
        return best;
    }

    private LivingEntity findThreat(double gx, double gy, double gz, float radius, CompanionData cd) {
        List<LivingEntity> nearby = npc.level().getEntitiesOfClass(
                LivingEntity.class,
                npc.getBoundingBox().inflate(radius)
        );

        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        double maxDistSqr = radius * radius;

        for (LivingEntity e : nearby) {
            if (e == npc || !e.isAlive() || e instanceof Player) continue;

            double dToGuard = e.distanceToSqr(gx, gy, gz);
            if (dToGuard > maxDistSqr) continue;

            double dToNpc = npc.distanceToSqr(e);
            if (dToNpc < bestDist) {
                bestDist = dToNpc;
                best = e;
            }
        }
        return best;
    }
}

package com.frametrip.dragonlegacyquesttoast.entity.goal;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.profession.JobConditions;
import com.frametrip.dragonlegacyquesttoast.profession.NpcProfessionData;
import com.frametrip.dragonlegacyquesttoast.server.stealth.PatrolPoint;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;

// [JOB-2]: Walk an NPC through its work patrol route, gated by schedule + job conditions.
public class NpcWorkPatrolGoal extends Goal {

    private final NpcEntity npc;

    private int  pointIndex  = 0;
    private int  pauseTimer  = 0;
    private boolean pausing  = false;

    public NpcWorkPatrolGoal(NpcEntity npc) {
        this.npc = npc;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    // ── canUse / canContinueToUse ────────────────────────────────────────────

    @Override
    public boolean canUse() {
        return isActive();
    }

    @Override
    public boolean canContinueToUse() {
        return isActive();
    }

    private boolean isActive() {
        if (npc.level().isClientSide) return false;
        NpcEntityData data = npc.getNpcData();
        NpcProfessionData pd = data.professionData;
        if (pd == null || pd.workRoute == null || pd.workRoute.isEmpty()) return false;

        // [JOB-1] Work schedule gate
        if (!pd.workSchedule.isWorkTime(npc.level())) return false;

        // [JOB-3] Job conditions
        JobConditions cond = pd.jobConditions;
        if (cond != null) {
            if (cond.requireFairWeather && npc.level().isRaining()) return false;
            if (cond.requireDaytime) {
                long time = npc.level().getDayTime() % 24000;
                if (time >= 13000 && time <= 23000) return false;
            }
            if (cond.minHealthPercent > 0f) {
                float hpRatio = npc.getHealth() / npc.getMaxHealth();
                if (hpRatio < cond.minHealthPercent) return false;
            }
        }
        return true;
    }

    // ── tick ────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        NpcEntityData data = npc.getNpcData();
        NpcProfessionData pd = data.professionData;
        List<PatrolPoint> route = pd.workRoute;

        if (route == null || route.isEmpty()) return;
        if (pointIndex >= route.size()) {
            if (pd.loopWorkRoute) pointIndex = 0;
            else { stop(); return; }
        }

        PatrolPoint target = route.get(pointIndex);

        if (pausing) {
            pauseTimer--;
            if (target.forceLook) {
                float yaw = target.lookYaw;
                npc.setYRot(yaw);
                npc.yHeadRot = yaw;
            }
            if (pauseTimer <= 0) {
                pausing = false;
                pointIndex++;
                if (pointIndex >= route.size()) {
                    if (pd.loopWorkRoute) pointIndex = 0;
                    else stop();
                }
            }
            return;
        }

        npc.getNavigation().moveTo(target.x + 0.5, target.y, target.z + 0.5, 0.6);

        double dx = target.x + 0.5 - npc.getX();
        double dz = target.z + 0.5 - npc.getZ();
        if (dx * dx + dz * dz < 1.5 * 1.5) {
            npc.getNavigation().stop();
            pausing = true;
            pauseTimer = target.pauseTicks;
        }
    }

    @Override
    public void stop() {
        npc.getNavigation().stop();
    }
}

package com.frametrip.dragonlegacyquesttoast.server.companion;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** AI goal: GUARD mode — returns to the designated guard post. */
public class CompanionGuardGoal extends Goal {

    private final NpcEntity npc;

    public CompanionGuardGoal(NpcEntity npc) {
        this.npc = npc;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        CompanionData cd = npc.getNpcData().companionData;
        return cd != null && cd.mode == CompanionMode.GUARD && cd.guardPointSet;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        CompanionData cd = npc.getNpcData().companionData;
        if (cd == null) return;
        double distSq = npc.distanceToSqr(cd.guardX, cd.guardY, cd.guardZ);
        if (distSq > 4.0) {
            npc.getNavigation().moveTo(cd.guardX, cd.guardY, cd.guardZ, 0.65);
        } else {
            npc.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        npc.getNavigation().stop();
    }
}

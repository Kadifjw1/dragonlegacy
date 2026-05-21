package com.frametrip.dragonlegacyquesttoast.entity.goal;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.server.combat.FormationController;
import com.frametrip.dragonlegacyquesttoast.server.combat.FormationType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

// [CMB-1]: Leader (formationSlot == 0) coordinates target attack; others hold formation positions.
public class NpcFormationGoal extends Goal {

    private final NpcEntity npc;

    public NpcFormationGoal(NpcEntity npc) {
        this.npc = npc;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        NpcEntityData data = npc.getNpcData();
        return !data.formationId.isEmpty()
            && data.formationSlot == 0  // only leader triggers formation
            && npc.getTarget() != null
            && !npc.level().isClientSide;
    }

    @Override
    public void start() {
        issueFormationOrder();
    }

    @Override
    public void tick() {
        // Re-issue every 10 ticks so members track moving target.
        if (npc.tickCount % 10 == 0) issueFormationOrder();
    }

    @Override
    public boolean canContinueToUse() {
        return npc.getTarget() != null && npc.getTarget().isAlive()
            && !npc.getNpcData().formationId.isEmpty();
    }

    private void issueFormationOrder() {
        LivingEntity target = npc.getTarget();
        if (target == null || !(npc.level() instanceof ServerLevel sl)) return;
        NpcEntityData data = npc.getNpcData();
        FormationType type = FormationType.fromName(data.formationType);
        FormationController.setFormationTarget(npc, data.formationId, type, target, sl);
    }
}

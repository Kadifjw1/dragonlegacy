package com.frametrip.dragonlegacyquesttoast.server.companion;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class CompanionGoal extends Goal {

    private final NpcEntity npc;

    public CompanionGoal(NpcEntity npc) {
        this.npc = npc;
    }

    @Override
    public boolean canUse() {
        CompanionData cd = npc.getNpcData().companionData;
        if (cd == null) return false;
        return cd.mode == CompanionMode.FOLLOW || cd.mode == CompanionMode.WANDER;
    }

    @Override
    public void tick() {
        // Companion follow logic placeholder – full AI to be implemented.
    }
}


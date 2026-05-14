package com.frametrip.dragonlegacyquesttoast.server.companion;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.UUID;

/** AI goal: FOLLOW mode — navigates toward the owner player. */
public class CompanionGoal extends Goal {

    private final NpcEntity npc;

    public CompanionGoal(NpcEntity npc) {
        this.npc = npc;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        CompanionData cd = npc.getNpcData().companionData;
        return cd != null && cd.mode == CompanionMode.FOLLOW;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        CompanionData cd = npc.getNpcData().companionData;
        if (cd == null) return;

        Player owner = findOwner(cd.ownerUUID);
        if (owner == null) return;

        npc.getLookControl().setLookAt(owner, 30f, 30f);

        double dist = npc.distanceTo(owner);
        if (dist > cd.followDistance + 0.5) {
            npc.getNavigation().moveTo(owner, 0.65);
        } else if (dist < cd.followDistance - 0.5) {
            npc.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        npc.getNavigation().stop();
    }

    private Player findOwner(String ownerUUID) {
        if (!(npc.level() instanceof ServerLevel level)) return null;
        if (ownerUUID == null || ownerUUID.isEmpty()) {
            // No owner — follow nearest player
            return level.getNearestPlayer(npc, 32.0);
        }
        try {
            return level.getPlayerByUUID(UUID.fromString(ownerUUID));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

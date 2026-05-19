package com.frametrip.dragonlegacyquesttoast.entity.goal;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// [INT-3]: Greet the nearest player when they enter greetRange
public class NpcGreetGoal extends Goal {

    private final NpcEntity npc;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private Player target;

    public NpcGreetGoal(NpcEntity npc) {
        this.npc = npc;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        NpcEntityData data = npc.getNpcData();
        if (!data.greetEnabled || data.greetMessage == null || data.greetMessage.isEmpty()) return false;
        target = npc.level().getNearestPlayer(npc, data.greetRange);
        if (target == null) return false;
        long last = cooldowns.getOrDefault(target.getUUID(), 0L);
        return (npc.level().getGameTime() - last) > data.greetCooldownSec * 20L;
    }

    @Override
    public boolean canContinueToUse() { return false; }

    @Override
    public void start() {
        NpcEntityData data = npc.getNpcData();
        if (target == null || data.greetMessage == null || data.greetMessage.isEmpty()) return;
        if (target instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.literal(
                    "§e" + data.displayName + "§7: §f" + data.greetMessage));
        }
        cooldowns.put(target.getUUID(), npc.level().getGameTime());
    }
}

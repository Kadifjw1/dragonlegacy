package com.frametrip.dragonlegacyquesttoast.entity.goal;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.server.immersion.NpcImmersionData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

// [IMM-3]: Makes NPC turn toward a nearby NPC and periodically say a random phrase.
public class NpcConversationGoal extends Goal {

    private static final Random RNG = new Random();

    private final NpcEntity npc;
    private NpcEntity conversationPartner;
    private int       speakTimer = 0;

    public NpcConversationGoal(NpcEntity npc) {
        this.npc = npc;
        setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        NpcEntityData data = npc.getNpcData();
        if (data.immersionData == null || !data.immersionData.selfConvEnabled) return false;
        List<String> phrases = data.immersionData.selfConvPhrases;
        if (phrases == null || phrases.isEmpty()) return false;

        // Find a nearby NPC partner
        List<NpcEntity> nearby = npc.level().getEntitiesOfClass(
                NpcEntity.class, npc.getBoundingBox().inflate(5.0),
                e -> e != npc);
        if (nearby.isEmpty()) return false;
        conversationPartner = nearby.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (conversationPartner == null || !conversationPartner.isAlive()) return false;
        if (npc.distanceTo(conversationPartner) > 8.0) return false;
        NpcImmersionData imm = npc.getNpcData().immersionData;
        return imm != null && imm.selfConvEnabled && !imm.selfConvPhrases.isEmpty();
    }

    @Override
    public void start() { speakTimer = 60; }

    @Override
    public void tick() {
        if (conversationPartner != null) {
            npc.getLookControl().setLookAt(conversationPartner, 30f, 30f);
            conversationPartner.getLookControl().setLookAt(npc, 30f, 30f);
        }

        speakTimer--;
        if (speakTimer > 0) return;

        NpcEntityData data = npc.getNpcData();
        if (data.immersionData == null) return;
        List<String> phrases = data.immersionData.selfConvPhrases;
        if (phrases.isEmpty()) return;

        String phrase = phrases.get(RNG.nextInt(phrases.size()));
        npc.level().players().forEach(p -> {
            if (p instanceof ServerPlayer sp && npc.distanceTo(sp) <= 16) {
                sp.sendSystemMessage(Component.literal(
                        "§e" + data.displayName + "§7: §f" + phrase));
            }
        });

        // Next speech in 60-120 ticks (3-6 seconds)
        speakTimer = 60 + RNG.nextInt(60);
    }
}

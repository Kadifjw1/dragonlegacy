package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

// [APP-1]: Spawns decorative particles around NPCs every other client tick.
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class NpcParticleHandler {

    private static final Random RNG = new Random();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null || mc.isPaused()) return;

        // Spawn every other tick to reduce visual noise
        if (level.getGameTime() % 2 != 0) return;

        level.entitiesForRendering().forEach(entity -> {
            if (!(entity instanceof NpcEntity npc)) return;
            byte effect = npc.getNpcData().particleEffect;
            if (effect == 0) return;

            double x = npc.getX() + (RNG.nextDouble() - 0.5) * 1.0;
            double y = npc.getY() + RNG.nextDouble() * npc.getBbHeight();
            double z = npc.getZ() + (RNG.nextDouble() - 0.5) * 1.0;

            switch (effect) {
                case 1 -> level.addParticle(ParticleTypes.FLAME,       x, y, z, 0, 0.04, 0);
                case 2 -> level.addParticle(ParticleTypes.DRIPPING_WATER, x, y + 0.5, z, 0, 0, 0);
                case 3 -> level.addParticle(ParticleTypes.ENCHANT,     x, y, z,
                        (RNG.nextDouble()-0.5)*0.2, 0.1, (RNG.nextDouble()-0.5)*0.2);
                case 4 -> level.addParticle(ParticleTypes.SMOKE,       x, y, z, 0, 0.02, 0);
                case 5 -> level.addParticle(ParticleTypes.END_ROD,     x, y, z,
                        (RNG.nextDouble()-0.5)*0.1, 0.08, (RNG.nextDouble()-0.5)*0.1);
            }
        });
    }
}

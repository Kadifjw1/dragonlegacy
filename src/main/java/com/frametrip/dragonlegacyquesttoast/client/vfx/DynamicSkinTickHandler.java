package com.frametrip.dragonlegacyquesttoast.client.vfx;

import com.frametrip.dragonlegacyquesttoast.client.NpcSkinManager;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.server.vfx.DynamicSkin;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

// [VFX-4]: Evaluates dynamic skin conditions every 20 client ticks and switches textures.
@OnlyIn(Dist.CLIENT)
public class DynamicSkinTickHandler {

    private int tickCounter = 0;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (++tickCounter < 20) return;
        tickCounter = 0;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        List<NpcEntity> npcs = mc.level.getEntitiesOfClass(
                NpcEntity.class,
                mc.player.getBoundingBox().inflate(64));

        for (NpcEntity npc : npcs) {
            NpcEntityData data = npc.getNpcData();
            if (data == null || data.dynamicSkins == null || data.dynamicSkins.isEmpty()) continue;

            String newSkin = data.dynamicSkins.stream()
                    .filter(ds -> DynamicSkinConditionParser.check(ds.condition, npc))
                    .map(ds -> ds.skinName)
                    .findFirst()
                    .orElse(data.skinId);

            if (!newSkin.equals(npc.getCurrentSkinOverride())) {
                npc.setCurrentSkinOverride(newSkin);
            }
        }
    }
}

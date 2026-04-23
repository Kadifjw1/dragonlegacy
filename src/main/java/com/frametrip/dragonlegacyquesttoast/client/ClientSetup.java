package com.frametrip.dragonlegacyquesttoast.client;
 
import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.frametrip.dragonlegacyquesttoast.client.renderer.NpcEntityRenderer;
import com.frametrip.dragonlegacyquesttoast.registry.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
 
@Mod.EventBusSubscriber(modid = DragonLegacyQuestToastMod.MODID,
    bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
 
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(NpcSkinManager::init);
    }
 
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.NPC.get(), NpcEntityRenderer::new);
    }
}

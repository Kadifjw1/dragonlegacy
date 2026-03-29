package com.frametrip.dragonlegacyquesttoast;

import com.frametrip.dragonlegacyquesttoast.client.QuestToastOverlay;
import com.frametrip.dragonlegacyquesttoast.command.ModCommands;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;

@Mod(DragonLegacyQuestToastMod.MODID)
public class DragonLegacyQuestToastMod {
    public static final String MODID = "dragonlegacyquesttoast";

    public DragonLegacyQuestToastMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModNetwork.init();
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(this::registerOverlays);
        }
    }

    private void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("quest_toast_overlay", QuestToastOverlay.OVERLAY);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
}
package com.frametrip.dragonlegacyquesttoast;
 
import com.frametrip.dragonlegacyquesttoast.client.NpcDialogueOverlay;
import com.frametrip.dragonlegacyquesttoast.client.QuestToastOverlay;
import com.frametrip.dragonlegacyquesttoast.command.ModCommands;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SyncAbilitiesPacket;
import com.frametrip.dragonlegacyquesttoast.server.FireStrikeHandler;
import com.frametrip.dragonlegacyquesttoast.server.PlayerAbilityManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;
 
@Mod(DragonLegacyQuestToastMod.MODID)
public class DragonLegacyQuestToastMod {
    public static final String MODID = "dragonlegacyquesttoast";
 
    public DragonLegacyQuestToastMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
 
        ModNetwork.init();
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLogin);
        MinecraftForge.EVENT_BUS.register(new FireStrikeHandler());
 
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(this::registerOverlays);
        }
    }
 
    private void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("quest_toast_overlay", QuestToastOverlay.OVERLAY);
        event.registerAboveAll("npc_dialogue_overlay", NpcDialogueOverlay.OVERLAY);
    }
 
    private void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
 
    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new SyncAbilitiesPacket(PlayerAbilityManager.getAbilities(serverPlayer.getUUID()))
            );
        }
    }
}

package com.frametrip.dragonlegacyquesttoast;
 
import com.frametrip.dragonlegacyquesttoast.client.NpcDialogueOverlay;
import com.frametrip.dragonlegacyquesttoast.client.QuestToastOverlay;
import com.frametrip.dragonlegacyquesttoast.command.ModCommands;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SyncAbilitiesPacket;
import com.frametrip.dragonlegacyquesttoast.network.SyncDialoguesPacket;
import com.frametrip.dragonlegacyquesttoast.network.SyncNpcProfilesPacket;
import com.frametrip.dragonlegacyquesttoast.network.SyncQuestsPacket;
import com.frametrip.dragonlegacyquesttoast.server.AbilityRegistry;
import com.frametrip.dragonlegacyquesttoast.server.DialogueManager;
import com.frametrip.dragonlegacyquesttoast.server.FireAbilityHandler;
import com.frametrip.dragonlegacyquesttoast.server.IceAbilityHandler;
import com.frametrip.dragonlegacyquesttoast.server.NpcProfileManager;
import com.frametrip.dragonlegacyquesttoast.server.PlayerAbilityManager;
import com.frametrip.dragonlegacyquesttoast.server.QuestManager;
import com.frametrip.dragonlegacyquesttoast.server.StormAbilityHandler;
import com.frametrip.dragonlegacyquesttoast.server.VoidAbilityHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.PacketDistributor;
 
import java.util.Set;
 
@Mod(DragonLegacyQuestToastMod.MODID)
public class DragonLegacyQuestToastMod {
    public static final String MODID = "dragonlegacyquesttoast";
 
    public DragonLegacyQuestToastMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
 
        ModNetwork.init();
 
        // Загружаем контент с диска при старте
        QuestManager.load();
        DialogueManager.load();
        NpcProfileManager.load();
 
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onPlayerLogin);
 
        MinecraftForge.EVENT_BUS.register(new FireAbilityHandler());
        MinecraftForge.EVENT_BUS.register(new IceAbilityHandler());
        MinecraftForge.EVENT_BUS.register(new StormAbilityHandler());
        MinecraftForge.EVENT_BUS.register(new VoidAbilityHandler());
 
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(this::registerOverlays);
        }
    }
 
    private void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("quest_toast_overlay",  QuestToastOverlay.OVERLAY);
        event.registerAboveAll("npc_dialogue_overlay", NpcDialogueOverlay.OVERLAY);
    }
 
    private void registerCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }
 
    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
 
        // Creative: при первом входе выдаём все способности
        if (player.isCreative() && PlayerAbilityManager.getAbilities(player.getUUID()).isEmpty()) {
            for (String id : AbilityRegistry.getAllIds()) {
                PlayerAbilityManager.grantAbility(player.getUUID(), id);
            }
        }
 
        Set<String> abilities = PlayerAbilityManager.getAbilities(player.getUUID());
        int points = PlayerAbilityManager.getPoints(player.getUUID());
 
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncAbilitiesPacket(abilities, points));
 
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncQuestsPacket(QuestManager.getAll()));
 
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncDialoguesPacket(DialogueManager.getAll()));
 
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SyncNpcProfilesPacket(NpcProfileManager.getAll()));
    }
}

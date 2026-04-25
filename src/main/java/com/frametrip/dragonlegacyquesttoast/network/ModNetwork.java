package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void init() {
        CHANNEL.registerMessage(packetId++, QuestToastPacket.class, QuestToastPacket::encode, QuestToastPacket::decode, QuestToastPacket::handle);
        CHANNEL.registerMessage(packetId++, QuestToastConfigPacket.class, QuestToastConfigPacket::encode, QuestToastConfigPacket::decode, QuestToastConfigPacket::handle);
        CHANNEL.registerMessage(packetId++, NpcDialoguePacket.class, NpcDialoguePacket::encode, NpcDialoguePacket::decode, NpcDialoguePacket::handle);
        CHANNEL.registerMessage(packetId++, NpcDialogueConfigPacket.class, NpcDialogueConfigPacket::encode, NpcDialogueConfigPacket::decode, NpcDialogueConfigPacket::handle);
        CHANNEL.registerMessage(packetId++, OpenAwakeningScreenPacket.class, OpenAwakeningScreenPacket::encode, OpenAwakeningScreenPacket::decode, OpenAwakeningScreenPacket::handle);
        CHANNEL.registerMessage(packetId++, AwakeningBackgroundConfigPacket.class, AwakeningBackgroundConfigPacket::encode, AwakeningBackgroundConfigPacket::decode, AwakeningBackgroundConfigPacket::handle);
        CHANNEL.registerMessage(packetId++, AwakeningCenterConfigPacket.class, AwakeningCenterConfigPacket::encode, AwakeningCenterConfigPacket::decode, AwakeningCenterConfigPacket::handle);
        CHANNEL.registerMessage(packetId++, AwakeningPathsConfigPacket.class, AwakeningPathsConfigPacket::encode, AwakeningPathsConfigPacket::decode, AwakeningPathsConfigPacket::handle);
        CHANNEL.registerMessage(packetId++, OpenUiEditorMenuPacket.class, OpenUiEditorMenuPacket::encode, OpenUiEditorMenuPacket::decode, OpenUiEditorMenuPacket::handle);
        CHANNEL.registerMessage(packetId++, OpenQuestToastEditorPacket.class, OpenQuestToastEditorPacket::encode, OpenQuestToastEditorPacket::decode, OpenQuestToastEditorPacket::handle);
        CHANNEL.registerMessage(packetId++, OpenNpcDialogueEditorPacket.class, OpenNpcDialogueEditorPacket::encode, OpenNpcDialogueEditorPacket::decode, OpenNpcDialogueEditorPacket::handle);
        CHANNEL.registerMessage(packetId++, SyncAbilitiesPacket.class,  SyncAbilitiesPacket::encode,  SyncAbilitiesPacket::decode,  SyncAbilitiesPacket::handle);
        CHANNEL.registerMessage(packetId++, UnlockAbilityPacket.class, UnlockAbilityPacket::encode, UnlockAbilityPacket::decode, UnlockAbilityPacket::handle);
        CHANNEL.registerMessage(packetId++, ToggleAbilityEnabledPacket.class, ToggleAbilityEnabledPacket::encode, ToggleAbilityEnabledPacket::decode, ToggleAbilityEnabledPacket::handle);
        CHANNEL.registerMessage(packetId++, OpenAbilityToggleScreenPacket.class, OpenAbilityToggleScreenPacket::encode, OpenAbilityToggleScreenPacket::decode, OpenAbilityToggleScreenPacket::handle);
         CHANNEL.registerMessage(packetId++, OpenMainHubPacket.class,         OpenMainHubPacket::encode,         OpenMainHubPacket::decode,         OpenMainHubPacket::handle);
        CHANNEL.registerMessage(packetId++, ToggleAbilityPacket.class,       ToggleAbilityPacket::encode,       ToggleAbilityPacket::decode,       ToggleAbilityPacket::handle);
        CHANNEL.registerMessage(packetId++, SyncQuestsPacket.class,          SyncQuestsPacket::encode,          SyncQuestsPacket::decode,          SyncQuestsPacket::handle);
        CHANNEL.registerMessage(packetId++, SaveQuestPacket.class,           SaveQuestPacket::encode,           SaveQuestPacket::decode,           SaveQuestPacket::handle);
        CHANNEL.registerMessage(packetId++, SyncDialoguesPacket.class,       SyncDialoguesPacket::encode,       SyncDialoguesPacket::decode,       SyncDialoguesPacket::handle);
        CHANNEL.registerMessage(packetId++, SaveDialoguePacket.class,        SaveDialoguePacket::encode,        SaveDialoguePacket::decode,        SaveDialoguePacket::handle);
        CHANNEL.registerMessage(packetId++, SyncNpcProfilesPacket.class,     SyncNpcProfilesPacket::encode,     SyncNpcProfilesPacket::decode,     SyncNpcProfilesPacket::handle);
        CHANNEL.registerMessage(packetId++, SaveNpcProfilePacket.class,      SaveNpcProfilePacket::encode,      SaveNpcProfilePacket::decode,      SaveNpcProfilePacket::handle);
        CHANNEL.registerMessage(packetId++, SyncQuestProgressPacket.class,  SyncQuestProgressPacket::encode,  SyncQuestProgressPacket::decode,  SyncQuestProgressPacket::handle);
        CHANNEL.registerMessage(packetId++, SaveNpcEntityDataPacket.class,  SaveNpcEntityDataPacket::encode,  SaveNpcEntityDataPacket::decode,  SaveNpcEntityDataPacket::handle);
        CHANNEL.registerMessage(packetId++, SyncFactionsPacket.class,       SyncFactionsPacket::encode,       SyncFactionsPacket::decode,       SyncFactionsPacket::handle);
        CHANNEL.registerMessage(packetId++, SaveFactionPacket.class,        SaveFactionPacket::encode,        SaveFactionPacket::decode,        SaveFactionPacket::handle);
        CHANNEL.registerMessage(packetId++, SaveNpcScenePacket.class,       SaveNpcScenePacket::encode,       SaveNpcScenePacket::decode,       SaveNpcScenePacket::handle);
        CHANNEL.registerMessage(packetId++, SyncNpcScenesPacket.class,      SyncNpcScenesPacket::encode,      SyncNpcScenesPacket::decode,      SyncNpcScenesPacket::handle);
        CHANNEL.registerMessage(packetId++, NpcStartScenePacket.class,      NpcStartScenePacket::encode,      NpcStartScenePacket::decode,      NpcStartScenePacket::handle);
        CHANNEL.registerMessage(packetId++, SyncPlayerCurrencyPacket.class, SyncPlayerCurrencyPacket::encode, SyncPlayerCurrencyPacket::decode, SyncPlayerCurrencyPacket::handle);
        CHANNEL.registerMessage(packetId++, BuyTradeOfferPacket.class,      BuyTradeOfferPacket::encode,      BuyTradeOfferPacket::decode,      BuyTradeOfferPacket::handle);
        CHANNEL.registerMessage(packetId++, SellToNpcPacket.class,          SellToNpcPacket::encode,          SellToNpcPacket::decode,          SellToNpcPacket::handle);
        CHANNEL.registerMessage(packetId++, SaveTraderDataPacket.class,     SaveTraderDataPacket::encode,     SaveTraderDataPacket::decode,     SaveTraderDataPacket::handle);
        CHANNEL.registerMessage(packetId++, OpenTraderShopPacket.class,          OpenTraderShopPacket::encode,          OpenTraderShopPacket::decode,          OpenTraderShopPacket::handle);
        CHANNEL.registerMessage(packetId++, SaveTraderLayoutPacket.class,        SaveTraderLayoutPacket::encode,        SaveTraderLayoutPacket::decode,        SaveTraderLayoutPacket::handle);
        CHANNEL.registerMessage(packetId++, SaveTraderDiscountsPacket.class,     SaveTraderDiscountsPacket::encode,     SaveTraderDiscountsPacket::decode,     SaveTraderDiscountsPacket::handle);
        CHANNEL.registerMessage(packetId++, ApplyTraderLayoutPresetPacket.class, ApplyTraderLayoutPresetPacket::encode, ApplyTraderLayoutPresetPacket::decode, ApplyTraderLayoutPresetPacket::handle);
    }
}

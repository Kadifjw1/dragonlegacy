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
        CHANNEL.registerMessage(packetId++, OpenAwakeningFirePathScreenPacket.class, OpenAwakeningFirePathScreenPacket::encode, OpenAwakeningFirePathScreenPacket::decode, OpenAwakeningFirePathScreenPacket::handle);
        CHANNEL.registerMessage(packetId++, AwakeningBackgroundConfigPacket.class, AwakeningBackgroundConfigPacket::encode, AwakeningBackgroundConfigPacket::decode, AwakeningBackgroundConfigPacket::handle);
        CHANNEL.registerMessage(packetId++, AwakeningCenterConfigPacket.class, AwakeningCenterConfigPacket::encode, AwakeningCenterConfigPacket::decode, AwakeningCenterConfigPacket::handle);
        CHANNEL.registerMessage(packetId++, AwakeningPathsConfigPacket.class, AwakeningPathsConfigPacket::encode, AwakeningPathsConfigPacket::decode, AwakeningPathsConfigPacket::handle);
        CHANNEL.registerMessage(packetId++, OpenUiEditorMenuPacket.class, OpenUiEditorMenuPacket::encode, OpenUiEditorMenuPacket::decode, OpenUiEditorMenuPacket::handle);
        CHANNEL.registerMessage(packetId++, OpenQuestToastEditorPacket.class, OpenQuestToastEditorPacket::encode, OpenQuestToastEditorPacket::decode, OpenQuestToastEditorPacket::handle);
        CHANNEL.registerMessage(packetId++, OpenNpcDialogueEditorPacket.class, OpenNpcDialogueEditorPacket::encode, OpenNpcDialogueEditorPacket::decode, OpenNpcDialogueEditorPacket::handle);
        CHANNEL.registerMessage(packetId++, SyncAbilitiesPacket.class, SyncAbilitiesPacket::encode, SyncAbilitiesPacket::decode, SyncAbilitiesPacket::handle);
    }
}

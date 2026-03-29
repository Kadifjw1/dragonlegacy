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
        CHANNEL.registerMessage(
                packetId++,
                QuestToastPacket.class,
                QuestToastPacket::encode,
                QuestToastPacket::decode,
                QuestToastPacket::handle
        );
    }
}
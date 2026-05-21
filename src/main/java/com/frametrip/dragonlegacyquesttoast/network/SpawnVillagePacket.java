package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.server.world.VillagePreset;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// [WLD-4]: Client → Server: spawn a village preset at the player's position.
public class SpawnVillagePacket {

    private final String villageType; // VillagePreset.VillageType.name()
    private final String villageSize; // VillagePreset.VillageSize.name()

    public SpawnVillagePacket(VillagePreset.VillageType type, VillagePreset.VillageSize size) {
        this.villageType = type.name();
        this.villageSize = size.name();
    }

    private SpawnVillagePacket(String type, String size) {
        this.villageType = type;
        this.villageSize = size;
    }

    public static void encode(SpawnVillagePacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.villageType);
        buf.writeUtf(pkt.villageSize);
    }

    public static SpawnVillagePacket decode(FriendlyByteBuf buf) {
        return new SpawnVillagePacket(buf.readUtf(), buf.readUtf());
    }

    public static void handle(SpawnVillagePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (!player.hasPermissions(2)) return; // require OP

            VillagePreset.VillageType type;
            VillagePreset.VillageSize size;
            try {
                type = VillagePreset.VillageType.valueOf(pkt.villageType);
                size = VillagePreset.VillageSize.valueOf(pkt.villageSize);
            } catch (IllegalArgumentException e) {
                return;
            }

            if (player.level() instanceof ServerLevel sl) {
                BlockPos center = player.blockPosition();
                VillagePreset.spawnVillage(sl, center, type, size);
                player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                        "§a[WLD-4] §fДеревня «" + type.name() + "» создана!"));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

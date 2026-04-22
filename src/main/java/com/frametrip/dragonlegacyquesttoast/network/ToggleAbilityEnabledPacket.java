package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.server.PlayerAbilityManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class ToggleAbilityEnabledPacket {
    private final String abilityId;
    private final boolean enabled;

    public ToggleAbilityEnabledPacket(String abilityId, boolean enabled) {
        this.abilityId = abilityId;
        this.enabled = enabled;
    }

    public static void encode(ToggleAbilityEnabledPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.abilityId);
        buf.writeBoolean(msg.enabled);
    }

    public static ToggleAbilityEnabledPacket decode(FriendlyByteBuf buf) {
        return new ToggleAbilityEnabledPacket(buf.readUtf(), buf.readBoolean());
    }

    public static void handle(ToggleAbilityEnabledPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || player.isCreative()) return;

            PlayerAbilityManager.setAbilityEnabled(player.getUUID(), msg.abilityId, msg.enabled);
            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncAbilitiesPacket(
                            PlayerAbilityManager.getAbilities(player.getUUID()),
                            PlayerAbilityManager.getDisabledAbilities(player.getUUID()),
                            PlayerAbilityManager.getPoints(player.getUUID())
                    )
            );
        });
        ctx.setPacketHandled(true);
    }
}

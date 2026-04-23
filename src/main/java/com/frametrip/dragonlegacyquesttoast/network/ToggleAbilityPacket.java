package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.server.AbilityRegistry;
import com.frametrip.dragonlegacyquesttoast.server.PlayerAbilityManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class ToggleAbilityPacket {

    private final String abilityId;
    private final boolean enable;

    public ToggleAbilityPacket(String abilityId, boolean enable) {
        this.abilityId = abilityId;
        this.enable = enable;
    }

    public static void encode(ToggleAbilityPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.abilityId);
        buf.writeBoolean(msg.enable);
    }

    public static ToggleAbilityPacket decode(FriendlyByteBuf buf) {
        return new ToggleAbilityPacket(buf.readUtf(), buf.readBoolean());
    }

    public static void handle(ToggleAbilityPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            if (AbilityRegistry.get(msg.abilityId) == null) return;

            // В креативе все способности доступны: если способности ещё нет, выдаём её,
            // но дальше управляем только enabled/disabled состоянием.
            if (player.getAbilities().instabuild) {
                if (!PlayerAbilityManager.hasAbility(player.getUUID(), msg.abilityId)) {
                    PlayerAbilityManager.grantAbility(player.getUUID(), msg.abilityId);
                }
            } else {
                // В выживании можно переключать только уже открытые способности.
                if (!PlayerAbilityManager.hasAbility(player.getUUID(), msg.abilityId)) {
                    return;
                }
            }

            PlayerAbilityManager.setAbilityEnabled(player.getUUID(), msg.abilityId, msg.enable);

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

package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.server.AbilityDefinition;
import com.frametrip.dragonlegacyquesttoast.server.AbilityRegistry;
import com.frametrip.dragonlegacyquesttoast.server.PlayerAbilityManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class UnlockAbilityPacket {
    private final String abilityId;

    public UnlockAbilityPacket(String abilityId) {
        this.abilityId = abilityId;
    }

    public static void encode(UnlockAbilityPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.abilityId);
    }

    public static UnlockAbilityPacket decode(FriendlyByteBuf buf) {
        return new UnlockAbilityPacket(buf.readUtf());
    }

    public static void handle(UnlockAbilityPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || player.isCreative()) return;

            AbilityDefinition def = AbilityRegistry.get(msg.abilityId);
            if (def == null) return;

            for (String req : def.requires) {
                if (!PlayerAbilityManager.hasAbility(player.getUUID(), req)) return;
            }

            if (!PlayerAbilityManager.spendPoints(player.getUUID(), def.cost)) return;
            PlayerAbilityManager.grantAbility(player.getUUID(), msg.abilityId);

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

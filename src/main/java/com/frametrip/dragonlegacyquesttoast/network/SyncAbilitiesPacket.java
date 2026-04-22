package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.ClientPlayerAbilityState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class SyncAbilitiesPacket {
    private final Set<String> abilities;
    private final Set<String> disabledAbilities;
    private final int points;

    public SyncAbilitiesPacket(Set<String> abilities, Set<String> disabledAbilities, int points) {
        this.abilities = new HashSet<>(abilities);
        this.disabledAbilities = new HashSet<>(disabledAbilities);
        this.points = points;
    }

    public static void encode(SyncAbilitiesPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.abilities.size());
        for (String id : msg.abilities) {
            buf.writeUtf(id);
        }

        buf.writeInt(msg.disabledAbilities.size());
        for (String id : msg.disabledAbilities) {
            buf.writeUtf(id);
        }

        buf.writeInt(msg.points);
    }

    public static SyncAbilitiesPacket decode(FriendlyByteBuf buf) {
        int unlockedCount = buf.readInt();
        Set<String> abilities = new HashSet<>();
        for (int i = 0; i < unlockedCount; i++) {
            abilities.add(buf.readUtf());
        }

        int disabledCount = buf.readInt();
        Set<String> disabledAbilities = new HashSet<>();
        for (int i = 0; i < disabledCount; i++) {
            disabledAbilities.add(buf.readUtf());
        }

        int points = buf.readInt();
        return new SyncAbilitiesPacket(abilities, disabledAbilities, points);
    }

    public static void handle(SyncAbilitiesPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() ->
                ClientPlayerAbilityState.sync(msg.abilities, msg.disabledAbilities, msg.points)
        );
        ctx.setPacketHandled(true);
    }
}

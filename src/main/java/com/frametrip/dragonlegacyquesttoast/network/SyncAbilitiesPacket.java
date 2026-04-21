package com.frametrip.dragonlegacyquesttoast.network;
 
import com.frametrip.dragonlegacyquesttoast.client.ClientPlayerAbilityState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
 
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
 
public class SyncAbilitiesPacket {
    private final Set<String> abilities;
 
    public SyncAbilitiesPacket(Set<String> abilities) {
        this.abilities = new HashSet<>(abilities);
    }
 
    public static void encode(SyncAbilitiesPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.abilities.size());
        for (String id : msg.abilities) {
            buf.writeUtf(id);
        }
    }
 
    public static SyncAbilitiesPacket decode(FriendlyByteBuf buf) {
        int count = buf.readInt();
        Set<String> abilities = new HashSet<>();
        for (int i = 0; i < count; i++) {
            abilities.add(buf.readUtf());
        }
        return new SyncAbilitiesPacket(abilities);
    }
 
    public static void handle(SyncAbilitiesPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> ClientPlayerAbilityState.setAbilities(msg.abilities));
        ctx.setPacketHandled(true);
    }
}

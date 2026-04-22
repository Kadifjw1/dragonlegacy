package com.frametrip.dragonlegacyquesttoast.network;
 
import com.frametrip.dragonlegacyquesttoast.client.ClientPlayerAbilityState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
 
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
 
public class SyncAbilitiesPacket {
    private final Set<String> abilities;
    private final int points;
 
    public SyncAbilitiesPacket(Set<String> abilities, int points) {
        this.abilities = new HashSet<>(abilities);
        this.points = points;
    }
 
    public static void encode(SyncAbilitiesPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.abilities.size());
        for (String id : msg.abilities) buf.writeUtf(id);
        buf.writeInt(msg.points);
    }
 
    public static SyncAbilitiesPacket decode(FriendlyByteBuf buf) {
        int count = buf.readInt();
        Set<String> abilities = new HashSet<>();
        for (int i = 0; i < count; i++) abilities.add(buf.readUtf());
        int points = buf.readInt();
        return new SyncAbilitiesPacket(abilities, points);
    }
 
    public static void handle(SyncAbilitiesPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> ClientPlayerAbilityState.sync(msg.abilities, msg.points));
        ctx.setPacketHandled(true);
    }
}

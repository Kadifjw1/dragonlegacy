package com.frametrip.dragonlegacyquesttoast.network;
 
import com.frametrip.dragonlegacyquesttoast.client.ClientQuestProgressState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
 
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
 
public class SyncQuestProgressPacket {
 
    private final Map<String, Integer> progress;
    private final Set<String>          active;
    private final Set<String>          completed;
    private final Set<String>          failed;

    public SyncQuestProgressPacket(Map<String, Integer> progress, Set<String> active,
                                   Set<String> completed, Set<String> failed) {
        this.progress  = new HashMap<>(progress);
        this.active = new HashSet<>(active);
        this.completed = new HashSet<>(completed);
        this.failed = new HashSet<>(failed);
    }
 
    public static void encode(SyncQuestProgressPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.progress.size());
        msg.progress.forEach((k, v) -> { buf.writeUtf(k); buf.writeInt(v); });
            
        buf.writeInt(msg.active.size());
        msg.active.forEach(buf::writeUtf);

        buf.writeInt(msg.completed.size());
        msg.completed.forEach(buf::writeUtf);

        buf.writeInt(msg.failed.size());
        msg.failed.forEach(buf::writeUtf);
    }
 
    public static SyncQuestProgressPacket decode(FriendlyByteBuf buf) {
        int pSize = buf.readInt();
        Map<String, Integer> prog = new HashMap<>();
        for (int i = 0; i < pSize; i++) prog.put(buf.readUtf(), buf.readInt());

     int aSize = buf.readInt();
        Set<String> active = new HashSet<>();
        for (int i = 0; i < aSize; i++) active.add(buf.readUtf());
     
        int cSize = buf.readInt();
        Set<String> done = new HashSet<>();
        for (int i = 0; i < cSize; i++) done.add(buf.readUtf());
 
        int fSize = buf.readInt();
        Set<String> failed = new HashSet<>();
        for (int i = 0; i < fSize; i++) failed.add(buf.readUtf());

        return new SyncQuestProgressPacket(prog, active, done, failed);
    }
 
    public static void handle(SyncQuestProgressPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            ClientQuestProgressState.sync(msg.progress, msg.active, msg.completed, msg.failed)        
          );
        ctx.get().setPacketHandled(true);
    }
}

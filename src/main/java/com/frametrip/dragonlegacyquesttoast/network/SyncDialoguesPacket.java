package com.frametrip.dragonlegacyquesttoast.network;
 
import com.frametrip.dragonlegacyquesttoast.client.ClientDialogueState;
import com.frametrip.dragonlegacyquesttoast.server.DialogueDefinition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
 
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Supplier;
 
public class SyncDialoguesPacket {
 
    private static final Gson GSON = new Gson();
    private final String json;
 
    public SyncDialoguesPacket(List<DialogueDefinition> dialogues) {
        this.json = GSON.toJson(dialogues);
    }
 
    private SyncDialoguesPacket(String json) {
        this.json = json;
    }
 
    public static void encode(SyncDialoguesPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.json, 32767);
    }
 
    public static SyncDialoguesPacket decode(FriendlyByteBuf buf) {
        return new SyncDialoguesPacket(buf.readUtf(32767));
    }
 
    public static void handle(SyncDialoguesPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Type t = new TypeToken<List<DialogueDefinition>>() {}.getType();
            List<DialogueDefinition> list = GSON.fromJson(msg.json, t);
            ClientDialogueState.sync(list);
        });
        ctx.get().setPacketHandled(true);
    }
}

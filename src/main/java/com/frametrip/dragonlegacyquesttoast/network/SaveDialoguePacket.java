package com.frametrip.dragonlegacyquesttoast.network;
 
import com.frametrip.dragonlegacyquesttoast.server.DialogueDefinition;
import com.frametrip.dragonlegacyquesttoast.server.DialogueManager;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
 
import java.util.function.Supplier;
 
public class SaveDialoguePacket {
 
    private static final Gson GSON = new Gson();
    private final String dialogueJson;
    private final boolean delete;
 
    public SaveDialoguePacket(DialogueDefinition dialogue, boolean delete) {
        this.dialogueJson = GSON.toJson(dialogue);
        this.delete = delete;
    }
 
    private SaveDialoguePacket(String json, boolean delete) {
        this.dialogueJson = json;
        this.delete = delete;
    }
 
    public static void encode(SaveDialoguePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.dialogueJson, 32767);
        buf.writeBoolean(msg.delete);
    }
 
    public static SaveDialoguePacket decode(FriendlyByteBuf buf) {
        return new SaveDialoguePacket(buf.readUtf(32767), buf.readBoolean());
    }
 
    public static void handle(SaveDialoguePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.getAbilities().instabuild) return;
 
            DialogueDefinition dialogue = GSON.fromJson(msg.dialogueJson, DialogueDefinition.class);
            if (dialogue == null || dialogue.id == null) return;
 
            if (msg.delete) {
                DialogueManager.delete(dialogue.id);
            } else {
                DialogueManager.save(dialogue);
            }
 
            ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncDialoguesPacket(DialogueManager.getAll())
            );
        });
        ctx.get().setPacketHandled(true);
    }
}

package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.server.quest.BranchingDialog;
import com.frametrip.dragonlegacyquesttoast.server.quest.BranchingDialogManager;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// [QST-2]: Client→Server: save or update a branching dialog.
public class SaveBranchingDialogPacket {

    private static final Gson GSON = new Gson();
    private final String json;

    public SaveBranchingDialogPacket(BranchingDialog dialog) {
        this.json = GSON.toJson(dialog);
    }

    private SaveBranchingDialogPacket(String json) {
        this.json = json;
    }

    public static void encode(SaveBranchingDialogPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.json, 16384);
    }

    public static SaveBranchingDialogPacket decode(FriendlyByteBuf buf) {
        return new SaveBranchingDialogPacket(buf.readUtf(16384));
    }

    public static void handle(SaveBranchingDialogPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.getAbilities().instabuild) return;
            BranchingDialog dialog = GSON.fromJson(msg.json, BranchingDialog.class);
            if (dialog != null && dialog.id != null) {
                BranchingDialogManager.save(dialog);
                ModNetwork.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.ALL.noArg(),
                    new SyncBranchingDialogsPacket(BranchingDialogManager.getAll())
                );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

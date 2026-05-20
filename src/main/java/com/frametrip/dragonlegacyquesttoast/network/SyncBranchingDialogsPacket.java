package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.client.ClientBranchingDialogState;
import com.frametrip.dragonlegacyquesttoast.server.quest.BranchingDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Supplier;

// [QST-2]: Server→Client: sync full branching dialog list.
public class SyncBranchingDialogsPacket {

    private static final Gson GSON = new Gson();
    private final String json;

    public SyncBranchingDialogsPacket(List<BranchingDialog> dialogs) {
        this.json = GSON.toJson(dialogs);
    }

    private SyncBranchingDialogsPacket(String json) {
        this.json = json;
    }

    public static void encode(SyncBranchingDialogsPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.json, 32767);
    }

    public static SyncBranchingDialogsPacket decode(FriendlyByteBuf buf) {
        return new SyncBranchingDialogsPacket(buf.readUtf(32767));
    }

    public static void handle(SyncBranchingDialogsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Type t = new TypeToken<List<BranchingDialog>>() {}.getType();
            List<BranchingDialog> dialogs = GSON.fromJson(msg.json, t);
            ClientBranchingDialogState.sync(dialogs);
        });
        ctx.get().setPacketHandled(true);
    }
}

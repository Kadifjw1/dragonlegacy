package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.currency.CurrencyConfig;
import com.frametrip.dragonlegacyquesttoast.currency.CurrencyConfigManager;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// [ECO-1]: Client → Server: OP saves new global currency configuration.
public class SaveCurrencyConfigPacket {

    private static final Gson GSON = new Gson();
    private final String json;

    public SaveCurrencyConfigPacket(CurrencyConfig config) {
        this.json = GSON.toJson(config);
    }

    private SaveCurrencyConfigPacket(String json) {
        this.json = json;
    }

    public static void encode(SaveCurrencyConfigPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.json, 1024);
    }

    public static SaveCurrencyConfigPacket decode(FriendlyByteBuf buf) {
        return new SaveCurrencyConfigPacket(buf.readUtf(1024));
    }

    public static void handle(SaveCurrencyConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.hasPermissions(2)) return;
            CurrencyConfig cfg = GSON.fromJson(msg.json, CurrencyConfig.class);
            if (cfg == null) return;
            CurrencyConfigManager.set(cfg);
            // Broadcast to all online players
            var server = player.getServer();
            if (server != null) CurrencyConfigManager.syncToAll(server);
        });
        ctx.get().setPacketHandled(true);
    }
}

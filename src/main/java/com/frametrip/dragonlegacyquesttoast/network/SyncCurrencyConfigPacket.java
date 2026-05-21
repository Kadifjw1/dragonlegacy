package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.currency.ClientCurrencyState;
import com.frametrip.dragonlegacyquesttoast.currency.CurrencyConfig;
import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// [ECO-1]: Server → Client: syncs global currency configuration.
public class SyncCurrencyConfigPacket {

    private static final Gson GSON = new Gson();
    private final String json;

    public SyncCurrencyConfigPacket(CurrencyConfig config) {
        this.json = GSON.toJson(config);
    }

    private SyncCurrencyConfigPacket(String json) {
        this.json = json;
    }

    public static void encode(SyncCurrencyConfigPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.json, 1024);
    }

    public static SyncCurrencyConfigPacket decode(FriendlyByteBuf buf) {
        return new SyncCurrencyConfigPacket(buf.readUtf(1024));
    }

    public static void handle(SyncCurrencyConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                CurrencyConfig cfg = GSON.fromJson(msg.json, CurrencyConfig.class);
                if (cfg != null) ClientCurrencyState.syncCurrencyConfig(cfg);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

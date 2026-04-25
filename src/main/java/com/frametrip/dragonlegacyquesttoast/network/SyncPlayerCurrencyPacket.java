package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.currency.ClientCurrencyState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncPlayerCurrencyPacket {

    private final long balance;

    public SyncPlayerCurrencyPacket(long balance) {
        this.balance = balance;
    }

    public static void encode(SyncPlayerCurrencyPacket msg, FriendlyByteBuf buf) {
        buf.writeLong(msg.balance);
    }

    public static SyncPlayerCurrencyPacket decode(FriendlyByteBuf buf) {
        return new SyncPlayerCurrencyPacket(buf.readLong());
    }

    public static void handle(SyncPlayerCurrencyPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                ClientCurrencyState.setBalance(msg.balance);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

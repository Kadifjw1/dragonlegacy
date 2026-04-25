package com.frametrip.dragonlegacyquesttoast.currency;

import com.frametrip.dragonlegacyquesttoast.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CurrencyEvents {

    private static int tickCounter = 0;

    @SubscribeEvent
    public void onServerPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        tickCounter++;
        if (tickCounter % 20 != 0) return;

        sweepCoins(player);
    }

    private static void sweepCoins(ServerPlayer player) {
        var inv = player.getInventory();
        long totalCoins = 0;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.is(ModItems.LEGACY_COIN.get())) {
                totalCoins += stack.getCount();
                stack.setCount(0);
                inv.setItem(i, ItemStack.EMPTY);
            }
        }

        if (totalCoins > 0) {
            CurrencyManager.addBalance(player, totalCoins);
        }
    }
}

package com.frametrip.dragonlegacyquesttoast.server.compat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;

/**
 * [INT-API-3]: Soft Vault/economy integration.
 * Vault is Bukkit-only; on Forge we probe for compatible economy mods
 * (e.g. "economycraft", "theoneprobe-economy") via reflection.
 * Falls back gracefully — callers must fall back to CurrencyManager if unavailable.
 */
public class VaultHook {

    private static final Logger LOGGER = LogManager.getLogger("VaultHook");

    // Known Forge economy mod IDs to probe (extend as needed)
    private static final String[] ECONOMY_MOD_IDS = { "vault", "economycraft", "theeconomy" };

    private static Boolean available = null;
    private static Object  economyService = null;

    public static boolean isAvailable() {
        if (available == null) {
            available = false;
            for (String modId : ECONOMY_MOD_IDS) {
                if (ModList.get().isLoaded(modId)) {
                    available = true;
                    LOGGER.info("Economy mod detected: {}", modId);
                    break;
                }
            }
        }
        return available;
    }

    /**
     * Returns the player's balance from the Vault-compatible economy service.
     * Returns -1 if unavailable.
     */
    public static double getBalance(ServerPlayer player) {
        if (!isAvailable() || economyService == null) return -1;
        try {
            Method m = economyService.getClass().getMethod("getBalance", Object.class);
            Object result = m.invoke(economyService, player.getGameProfile().getName());
            return ((Number) result).doubleValue();
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Attempts to withdraw price from the player's Vault balance.
     * Returns false if unavailable or insufficient funds.
     */
    public static boolean withdraw(ServerPlayer player, double price) {
        if (!isAvailable() || economyService == null) return false;
        try {
            Method hasMethod  = economyService.getClass().getMethod("has", Object.class, double.class);
            boolean has = (boolean) hasMethod.invoke(economyService, player.getGameProfile().getName(), price);
            if (!has) return false;
            Method takeMethod = economyService.getClass().getMethod("withdrawPlayer", Object.class, double.class);
            takeMethod.invoke(economyService, player.getGameProfile().getName(), price);
            return true;
        } catch (Exception e) {
            LOGGER.debug("Vault withdraw failed: {}", e.getMessage());
            return false;
        }
    }
}

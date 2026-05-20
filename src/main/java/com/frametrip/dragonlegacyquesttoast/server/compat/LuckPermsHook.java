package com.frametrip.dragonlegacyquesttoast.server.compat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;

/**
 * [INT-API-2]: Soft LuckPerms integration for group-based permission checks.
 * Uses reflection to avoid hard compile-time dependency on LuckPerms.
 *
 * Condition format: "luckperms:<groupName>"
 * Example: "luckperms:vip" — only players in the "vip" LuckPerms group.
 */
public class LuckPermsHook {

    private static final Logger LOGGER = LogManager.getLogger("LuckPermsHook");
    private static final String MOD_ID = "luckperms";

    private static Boolean available = null;
    private static Object  apiInstance = null;

    public static boolean isAvailable() {
        if (available == null) {
            available = ModList.get().isLoaded(MOD_ID);
            if (available) {
                try {
                    Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider");
                    Method getMethod = providerClass.getMethod("get");
                    apiInstance = getMethod.invoke(null);
                } catch (Exception e) {
                    LOGGER.warn("LuckPerms loaded but API unavailable: {}", e.getMessage());
                    available = false;
                }
            }
        }
        return available;
    }

    /**
     * Returns true if the player belongs to the given LuckPerms group.
     * Falls back to false if LuckPerms is not available.
     */
    public static boolean playerInGroup(ServerPlayer player, String groupName) {
        if (!isAvailable() || apiInstance == null) return false;
        try {
            // LuckPerms API: api.getUserManager().getUser(uuid).getPrimaryGroup()
            Method getUserManager = apiInstance.getClass().getMethod("getUserManager");
            Object userManager    = getUserManager.invoke(apiInstance);
            Method getUser        = userManager.getClass().getMethod("getUser", java.util.UUID.class);
            Object user           = getUser.invoke(userManager, player.getUUID());
            if (user == null) return false;
            Method getPrimaryGroup = user.getClass().getMethod("getPrimaryGroup");
            String primary         = (String) getPrimaryGroup.invoke(user);
            if (groupName.equalsIgnoreCase(primary)) return true;

            // Also check inherited groups via nodes
            Method getNodes = user.getClass().getMethod("getNodes");
            Object nodes    = getNodes.invoke(user);
            // Iterate via Iterable
            for (Object node : (Iterable<?>) nodes) {
                try {
                    Method getKey = node.getClass().getMethod("getKey");
                    String key    = (String) getKey.invoke(node);
                    if (key != null && key.equalsIgnoreCase("group." + groupName)) return true;
                } catch (Exception ignored) {}
            }
            return false;
        } catch (Exception e) {
            LOGGER.debug("LuckPerms group check failed: {}", e.getMessage());
            return false;
        }
    }
}

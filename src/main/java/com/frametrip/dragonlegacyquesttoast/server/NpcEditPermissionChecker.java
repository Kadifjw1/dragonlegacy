package com.frametrip.dragonlegacyquesttoast.server;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import net.minecraft.server.level.ServerPlayer;

/**
 * [SRV-1]: Determines whether a player is allowed to edit an NPC's data.
 */
public class NpcEditPermissionChecker {

    public static boolean canEdit(ServerPlayer player, NpcEntityData data) {
        if (data == null) return true; // new NPC — allow
        if (player.hasPermissions(4)) return true; // OP4 always allowed
        return switch (data.editPermission) {
            case 0 -> player.hasPermissions(2); // any OP
            case 1 -> player.getStringUUID().equals(data.creatorUUID);
            case 2 -> data.editGroup.isEmpty() || isInGroup(player, data.editGroup);
            default -> false;
        };
    }

    private static boolean isInGroup(ServerPlayer player, String group) {
        // Simple implementation: "ops" → check OP permission; otherwise treat group as
        // a comma-separated list of player names.
        if ("ops".equalsIgnoreCase(group)) return player.hasPermissions(2);
        for (String name : group.split(",")) {
            if (name.trim().equalsIgnoreCase(player.getGameProfile().getName())) return true;
        }
        return false;
    }
}

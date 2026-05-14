package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientUtil {
    private ClientUtil() {}

    /** Returns the NpcEntity the player is currently looking at, or null. */
    public static NpcEntity getLookedAtNpc() {
        var hit = Minecraft.getInstance().hitResult;
        if (hit instanceof EntityHitResult ehr && ehr.getEntity() instanceof NpcEntity npc) {
            return npc;
        }
        return null;
    }
}

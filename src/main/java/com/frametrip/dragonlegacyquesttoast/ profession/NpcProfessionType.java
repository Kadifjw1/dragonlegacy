package com.frametrip.dragonlegacyquesttoast.profession;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum NpcProfessionType {
    NONE,
    TRADER,
    BUILDER,
    COMPANION;

    public String translationKey() {
        return "npc.profession." + DragonLegacyQuestToastMod.MODID + "." + name().toLowerCase();
    }

    @OnlyIn(Dist.CLIENT)
    public String label() {
        return net.minecraft.client.resources.language.I18n.get(translationKey());
    }
}

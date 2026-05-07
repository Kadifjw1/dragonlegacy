package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.server.animation.NpcAnimationData;
import com.frametrip.dragonlegacyquesttoast.server.gui.GuiTemplate;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores data pack presets received from the server. Populated by SyncDataPresetsPacket. */
@OnlyIn(Dist.CLIENT)
public class ClientPresetCache {

    private static final List<NpcAnimationData> animationPresets = new ArrayList<>();
    private static final List<GuiTemplate>       guiPresets       = new ArrayList<>();

    public static void sync(List<NpcAnimationData> anims, List<GuiTemplate> guis) {
        animationPresets.clear();
        animationPresets.addAll(anims);
        guiPresets.clear();
        guiPresets.addAll(guis);
    }

    public static List<NpcAnimationData> getAnimationPresets() {
        return Collections.unmodifiableList(animationPresets);
    }

    public static List<GuiTemplate> getGuiPresets() {
        return Collections.unmodifiableList(guiPresets);
    }
}

package com.frametrip.dragonlegacyquesttoast.server.data;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.util.ArrayList;
import java.util.List;

/** Manages data-pack driven presets (animation presets, GUI presets). */
public class DataPackManager implements ResourceManagerReloadListener {

    public static final DataPackManager INSTANCE = new DataPackManager();

    /** Animation preset IDs available from data packs. */
    public static List<String> animationPresets = new ArrayList<>();

    /** GUI preset IDs available from data packs. */
    public static List<String> guiPresets = new ArrayList<>();

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        animationPresets.clear();
        guiPresets.clear();
        // Data-pack loading to be implemented when the data format is finalised.
    }
}

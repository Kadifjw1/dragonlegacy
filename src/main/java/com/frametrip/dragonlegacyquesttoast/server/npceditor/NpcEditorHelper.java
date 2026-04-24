package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

/**
 * Thin wrapper so tab components can add widgets and trigger rebuilds
 * without holding a direct reference to NpcCreatorScreen.
 */
public class NpcEditorHelper {

    private final Screen screen;

    private NpcEditorHelper(Screen screen) {
        this.screen = screen;
    }

    public static NpcEditorHelper of(Screen screen) {
        return new NpcEditorHelper(screen);
    }

    /** Dummy helper that no-ops widget adds — used in render() paths. */
    public static NpcEditorHelper dummy() {
        return new NpcEditorHelper(null) {
            @Override public void addWidget(AbstractWidget w) {}
            @Override public void rebuild() {}
        };
    }

    public void addWidget(AbstractWidget widget) {
        if (screen != null) screen.addRenderableWidget(widget);
    }

    public void rebuild() {
        if (screen != null) screen.rebuildWidgets();
    }

    public Font font() {
        return Minecraft.getInstance().font;
    }
}

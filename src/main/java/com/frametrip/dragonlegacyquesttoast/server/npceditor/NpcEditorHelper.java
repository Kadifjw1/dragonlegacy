package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.client.NpcCreatorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;

/**
 * Thin wrapper so tab components can add widgets and trigger rebuilds
 * without holding a direct reference to Screen protected methods.
 */
public class NpcEditorHelper {

    private final NpcCreatorScreen screen;

    private NpcEditorHelper(NpcCreatorScreen screen) {
        this.screen = screen;
    }

    public static NpcEditorHelper of(NpcCreatorScreen screen) {
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
        if (screen != null) screen.addEditorWidget(widget);
    }

    public void rebuild() {
        if (screen != null) screen.rebuildEditorWidgets();
    }

    public Font font() {
        return Minecraft.getInstance().font;
    }
}

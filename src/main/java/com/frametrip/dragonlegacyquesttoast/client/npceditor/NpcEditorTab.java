package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.function.Consumer;

/** Contract for each tab panel in the NPC editor. */
public interface NpcEditorTab {

    /**
     * Build widgets for this tab.
     *
     * @param addWidget  add a widget to the parent screen (wraps addRenderableWidget)
     * @param rebuild    rebuild all widgets (clears then re-inits the screen)
     * @param state      unified editor state
     * @param rx         left x of the content area
     * @param oy         top y of the content area
     * @param rw         width of the content area
     */
    void init(Consumer<AbstractWidget> addWidget, Runnable rebuild,
              NpcEditorState state, int rx, int oy, int rw);

    /** Draw custom non-widget elements (labels, dividers, section cards). */
    void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my);

    /** Pull any EditBox values into the draft before switching tabs. */
    default void pullFields(NpcEditorState state) {}

    /** Optional scroll handling. Return true if consumed. */
    default boolean onMouseScrolled(double mx, double my, double delta,
                                    NpcEditorState state, int rx, int oy, int rw) {
        return false;
    }

    /** Optional click handling (for non-widget areas). Return true if consumed. */
    default boolean onMouseClicked(double mx, double my, int btn,
                                   NpcEditorState state, int rx, int oy, int rw) {
        return false;
    }
}

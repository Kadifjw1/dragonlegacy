package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;

/** Shared rendering utilities for NPC editor UI components. */
public final class NpcEditorUtils {

    private NpcEditorUtils() {}

    /**
     * Truncates {@code text} to fit within {@code maxPixels} wide,
     * appending "…" when the full text is wider.
     * Uses 8px of horizontal padding (4 each side) automatically.
     */
    public static String fitText(String text, int buttonWidth) {
        if (text == null) return "";
        Font font = Minecraft.getInstance().font;
        int maxPx = buttonWidth - 8;
        if (font.width(text) <= maxPx) return text;
        String sub = font.plainSubstrByWidth(text, maxPx - font.width("…"));
        return sub + "…";
    }

    /** Draw a 1-pixel border rectangle. */
    public static void brd(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x,     y,     x + w, y + 1, c);
        g.fill(x,     y+h-1, x + w, y + h, c);
        g.fill(x,     y,     x + 1, y + h, c);
        g.fill(x+w-1, y,     x + w, y + h, c);
    }

    /** Draw a titled section card (background + top accent line + label). */
    public static void sectionCard(GuiGraphics g, int x, int y, int w, int h,
                                   String title, int accent) {
        g.fill(x, y, x + w, y + h, 0xAA131320);
        g.fill(x, y, x + w, y + 1, accent);
        g.drawString(Minecraft.getInstance().font, "§7§l" + title, x + 4, y + 3, accent, false);
    }
}

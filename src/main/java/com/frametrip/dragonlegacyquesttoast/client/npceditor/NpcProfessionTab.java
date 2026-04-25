package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.profession.NpcProfessionData;
import com.frametrip.dragonlegacyquesttoast.profession.NpcProfessionType;
import com.frametrip.dragonlegacyquesttoast.profession.trader.TraderMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class NpcProfessionTab implements NpcEditorTab {

    public static final int ACCENT = 0xFFFFAA33;

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        NpcEntityData d = state.getDraft();
        NpcProfessionData pd = ensureProfession(d);

        // — Profession selector -----------------------------------------------
        NpcProfessionType[] types = NpcProfessionType.values();
        int bx = rx;
        for (NpcProfessionType t : types) {
            boolean sel = pd.type == t;
            add.accept(Button.builder(
                    Component.literal((sel ? "§e◉ §r" : "○ ") + t.label()),
                    b -> {
                        pd.type = t;
                        if (t == NpcProfessionType.TRADER) pd.ensureTraderData();
                        state.markDirty();
                        rebuild.run();
                    }
            ).bounds(bx, oy + 38, 130, 18).build());
            bx += 134;
        }

        // — Trader sub-section ------------------------------------------------
        if (pd.type == NpcProfessionType.TRADER) {
            pd.ensureTraderData();

            // Trader mode buttons
            TraderMode[] modes = TraderMode.values();
            int mx = rx;
            for (TraderMode m : modes) {
                boolean sel = pd.traderData.mode == m;
                add.accept(Button.builder(
                        Component.literal((sel ? "§e◉ §r" : "○ ") + m.label()),
                        b -> {
                            pd.traderData.mode = m;
                            state.markDirty();
                            rebuild.run();
                        }
                ).bounds(mx, oy + 108, 120, 18).build());
                mx += 124;
            }

            // Open shop editor button
            add.accept(Button.builder(
                    Component.literal("⚙ Настроить магазин"),
                    b -> {
                        state.markDirty();
                        Minecraft.getInstance().setScreen(
                                new com.frametrip.dragonlegacyquesttoast.client.TraderEditorScreen(
                                        state, Minecraft.getInstance().screen));
                    }
            ).bounds(rx, oy + 140, 160, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();
        NpcProfessionData pd = ensureProfession(d);

        // Profession card
        NpcEditorUtils.sectionCard(g, rx, oy, rw, 66, "ПРОФЕССИЯ NPC", ACCENT);
        g.drawString(font, "§7Выберите профессию NPC:", rx + 4, oy + 12, 0xFF888877, false);

        // Accent under selected
        NpcProfessionType[] types = NpcProfessionType.values();
        int bx = rx;
        for (NpcProfessionType t : types) {
            if (pd.type == t) {
                g.fill(bx, oy + 36, bx + 130, oy + 37, ACCENT);
            }
            bx += 134;
        }

        // Trader sub-section
        if (pd.type == NpcProfessionType.TRADER && pd.traderData != null) {
            NpcEditorUtils.sectionCard(g, rx, oy + 72, rw, 84, "НАСТРОЙКИ ТОРГОВЦА", ACCENT);
            g.drawString(font, "§7Режим торговли:", rx + 4, oy + 84, 0xFF888877, false);

            TraderMode[] modes = TraderMode.values();
            int tmx = rx;
            for (TraderMode m : modes) {
                if (pd.traderData.mode == m) {
                    g.fill(tmx, oy + 106, tmx + 120, oy + 107, ACCENT);
                }
                tmx += 124;
            }

            g.drawString(font, "§7Редактор магазина:", rx + 4, oy + 130, 0xFF888877, false);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static NpcProfessionData ensureProfession(NpcEntityData d) {
        if (d.professionData == null) d.professionData = new NpcProfessionData();
        return d.professionData;
    }
}

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

        // — Profession selector (2-column grid) --------------------------------
        NpcProfessionType[] types = NpcProfessionType.values();
        int colW = (rw - 4) / 2;
        for (int i = 0; i < types.length; i++) {
            NpcProfessionType t = types[i];
            boolean sel = pd.type == t;
            int col = i % 2;
            int row = i / 2;
            int bx = rx + col * (colW + 4);
            int by = oy + 38 + row * 22;
            String profLabel = NpcEditorUtils.fitText(t.label(), colW - 16);
            add.accept(Button.builder(
                    Component.literal((sel ? "§e◉ §r" : "○ ") + profLabel),
                    b -> {
                        pd.type = t;
                        if (t == NpcProfessionType.TRADER) pd.ensureTraderData();
                        state.markDirty();
                        rebuild.run();
                    }
            ).bounds(bx, by, colW, 18).build());
        }

        // — Trader sub-section ------------------------------------------------
        if (pd.type == NpcProfessionType.TRADER) {
            pd.ensureTraderData();
            int profRows = (types.length + 1) / 2;
            int traderBase = oy + 38 + profRows * 22 + 16;

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
                ).bounds(mx, traderBase + 24, 120, 18).build());
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
            ).bounds(rx, traderBase + 56, 160, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();
        NpcProfessionData pd = ensureProfession(d);

        // Profession card: 8 professions in 2-col grid = 4 rows of 22px + header 36px
        NpcProfessionType[] types = NpcProfessionType.values();
        int rows = (types.length + 1) / 2;
        int cardH = 36 + rows * 22 + 4;
        NpcEditorUtils.sectionCard(g, rx, oy, rw, cardH, "ПРОФЕССИЯ NPC", ACCENT);
        g.drawString(font, "§7Выберите профессию NPC:", rx + 4, oy + 12, 0xFF888877, false);

        // Accent under selected
        int colW = (rw - 4) / 2;
        for (int i = 0; i < types.length; i++) {
            NpcProfessionType t = types[i];
            if (pd.type == t) {
                int col = i % 2;
                int row = i / 2;
                int bx = rx + col * (colW + 4);
                int by = oy + 38 + row * 22;
                g.fill(bx, by + 18, bx + colW, by + 19, ACCENT);
            }
        }

        // Trader sub-section
        if (pd.type == NpcProfessionType.TRADER && pd.traderData != null) {
            int profRows = (types.length + 1) / 2;
            int traderBase = oy + 38 + profRows * 22 + 16;
            NpcEditorUtils.sectionCard(g, rx, traderBase, rw, 84, "НАСТРОЙКИ ТОРГОВЦА", ACCENT);
            g.drawString(font, "§7Режим торговли:", rx + 4, traderBase + 12, 0xFF888877, false);

            TraderMode[] modes = TraderMode.values();
            int tmx = rx;
            for (TraderMode m : modes) {
                if (pd.traderData.mode == m) {
                    g.fill(tmx, traderBase + 42, tmx + 120, traderBase + 43, ACCENT);
                }
                tmx += 124;
            }

            g.drawString(font, "§7Редактор магазина:", rx + 4, traderBase + 58, 0xFF888877, false);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static NpcProfessionData ensureProfession(NpcEntityData d) {
        if (d.professionData == null) d.professionData = new NpcProfessionData();
        return d.professionData;
    }
}

package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.ResetNpcStatsPacket;
import com.frametrip.dragonlegacyquesttoast.server.stats.NpcStatisticsData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;

// [STA-1]: Read-only NPC statistics tab with a reset button.
public class NpcStatsTab implements NpcEditorTab {

    public static final int ACCENT = 0xFF55AADD;

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        // Reset button at bottom
        add.accept(Button.builder(Component.literal("↺ Сбросить статистику"), b -> {
            ModNetwork.CHANNEL.sendToServer(new ResetNpcStatsPacket(state.getEntity().getUUID()));
            // Also clear local draft so UI updates immediately
            NpcEntityData d = state.getDraft();
            if (d.stats != null) d.stats.reset();
        }).bounds(rx, oy + 220, 160, 18).build());
    }

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();
        NpcStatisticsData s = d.stats != null ? d.stats : new NpcStatisticsData();

        int y = oy + 4;
        int labelX = rx + 4;
        int valX   = rx + 170;

        // Header
        g.fill(rx, y - 2, rx + rw, y + 12, 0x33AACCFF);
        g.drawString(font, "§b§lСТАТИСТИКА NPC", labelX, y, 0xFFAADDFF, false);
        y += 18;

        // ── Stat rows ──────────────────────────────────────────────────────────
        row(g, font, labelX, valX, y, "Убит раз:",           String.valueOf(s.timesKilled));      y += 14;
        row(g, font, labelX, valX, y, "Квестов выдано:",     String.valueOf(s.questsGiven));      y += 14;
        row(g, font, labelX, valX, y, "Предметов продано:",  String.valueOf(s.itemsSold));        y += 14;
        row(g, font, labelX, valX, y, "Диалогов начато:",    String.valueOf(s.dialogsStarted));   y += 14;
        row(g, font, labelX, valX, y, "Взаимодействий:",     String.valueOf(s.interactionCount)); y += 14;

        y += 6;
        g.fill(rx + 2, y, rx + rw - 2, y + 1, 0x44FFFFFF);
        y += 8;

        // Creation metadata
        String spawnDate = s.firstSpawnTime > 0
                ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(s.firstSpawnTime))
                : "—";
        row(g, font, labelX, valX, y, "Создан:",  spawnDate);  y += 14;
        row(g, font, labelX, valX, y, "Автор:",   s.createdBy.isEmpty() ? "—" : s.createdBy); y += 14;
    }

    private void row(GuiGraphics g, net.minecraft.client.gui.Font font,
                     int lx, int vx, int y, String label, String value) {
        g.drawString(font, "§7" + label, lx, y, 0xFFCCCCCC, false);
        g.drawString(font, "§f" + value, vx, y, 0xFFFFFFFF, false);
    }
}

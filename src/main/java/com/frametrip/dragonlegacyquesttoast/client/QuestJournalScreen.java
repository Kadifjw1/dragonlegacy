package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.server.QuestDefinition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class QuestJournalScreen extends Screen {

    private static final int W = 520;
    private static final int H = 330;
    private static final int LEFT_W = 210;

    private List<QuestDefinition> quests = new ArrayList<>();
    private int selected = -1;
    private int scroll = 0;

    public QuestJournalScreen(Screen parent) {
        super(Component.literal("Журнал заданий"));
    }

    @Override
    protected void init() {
        super.init();
        quests = new ArrayList<>(ClientQuestState.getAll());
        quests.sort(Comparator.comparing(q -> q.title == null ? "" : q.title.toLowerCase()));
        if (selected < 0 && !quests.isEmpty()) selected = 0;

        int ox = ox(), oy = oy();
        addRenderableWidget(Button.builder(Component.literal("Закрыть"), b -> onClose())
                .bounds(ox + W - 86, oy + H - 26, 78, 18).build());
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        int maxRows = Math.max(1, (H - 52) / 12);
        int maxScroll = Math.max(0, quests.size() - maxRows);
        scroll = Math.max(0, Math.min(maxScroll, scroll - (int) Math.signum(delta)));
        return true;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) return true;
        int ox = ox(), oy = oy();
        int listY = oy + 30;
        int maxRows = Math.max(1, (H - 52) / 12);
        for (int i = scroll; i < Math.min(quests.size(), scroll + maxRows); i++) {
            int y = listY + (i - scroll) * 12;
            if (mx >= ox + 6 && mx <= ox + LEFT_W - 8 && my >= y && my <= y + 11) {
                selected = i;
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int ox = ox(), oy = oy();
        g.fill(ox, oy, ox + W, oy + H, 0xDD0E0E18);
        NpcCreatorScreen.brd(g, ox, oy, W, H, 0xFF444455);
        g.fill(ox, oy, ox + W, oy + 22, 0xBB161622);
        g.drawCenteredString(font, "Журнал заданий", ox + W / 2, oy + 7, 0xFFE6D7B5);

        g.fill(ox + 4, oy + 24, ox + LEFT_W, oy + H - 30, 0xAA1A1A2A);
        NpcCreatorScreen.brd(g, ox + 4, oy + 24, LEFT_W - 4, H - 54, 0xFF333344);

        int listY = oy + 30;
        int maxRows = Math.max(1, (H - 52) / 12);
        for (int i = scroll; i < Math.min(quests.size(), scroll + maxRows); i++) {
            QuestDefinition q = quests.get(i);
            int y = listY + (i - scroll) * 12;
            boolean sel = i == selected;
            if (sel) g.fill(ox + 6, y - 1, ox + LEFT_W - 8, y + 11, 0x55FFFFFF);
            String mark = statusMark(q.id);
            String name = q.title == null ? "—" : q.title;
            if (font.width(name) > LEFT_W - 60) name = name.substring(0, Math.min(name.length(), 18)) + "..";
            g.drawString(font, mark + " " + name, ox + 8, y, sel ? 0xFFFFFFFF : 0xFFCCCCCC, false);
        }

        int rx = ox + LEFT_W + 8;
        int rw = W - LEFT_W - 14;
        g.fill(rx, oy + 24, rx + rw, oy + H - 30, 0xAA1A1A2A);
        NpcCreatorScreen.brd(g, rx, oy + 24, rw, H - 54, 0xFF333344);

        if (selected < 0 || selected >= quests.size()) {
            g.drawCenteredString(font, "Выберите задание слева", rx + rw / 2, oy + H / 2, 0xFF777788);
        } else {
            QuestDefinition q = quests.get(selected);
            int y = oy + 32;
            g.drawString(font, "§f§l" + safe(q.title), rx + 8, y, 0xFFE6D7B5, false);
            y += 14;
            g.drawString(font, "Статус: " + statusLabel(q.id), rx + 8, y, 0xFFCCCCCC, false);
            y += 12;
            g.drawString(font, "Тип: §e" + q.logicLabel(), rx + 8, y, 0xFFCCCCCC, false);
            y += 14;
            g.drawString(font, "Описание:", rx + 8, y, 0xFFAAAAAA, false);
            y += 12;
            for (var line : font.split(Component.literal(safe(q.description)), rw - 16)) {
                g.drawString(font, line, rx + 8, y, 0xFFDDDDDD, false);
                y += 10;
                if (y > oy + H - 52) break;
            }

            int progress = ClientQuestProgressState.getProgress(q.id);
            int required = Math.max(1, q.getRequiredCount());
            g.drawString(font, "Прогресс: §e" + Math.min(progress, required) + "§7/§f" + required,
                    rx + 8, oy + H - 44, 0xFFCCCCCC, false);
        }

        super.render(g, mx, my, pt);
    }

    private String statusMark(String questId) {
        if (ClientQuestProgressState.isComplete(questId)) return "§a✔";
        if (ClientQuestProgressState.isFailed(questId)) return "§c✖";
        if (ClientQuestProgressState.isActive(questId)) return "§e●";
        return "§7○";
    }

    private String statusLabel(String questId) {
        if (ClientQuestProgressState.isComplete(questId)) return "§aЗавершён";
        if (ClientQuestProgressState.isFailed(questId)) return "§cПровален";
        if (ClientQuestProgressState.isActive(questId)) return "§eАктивен";
        return "§7Не взят";
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private int ox() { return (width - W) / 2; }
    private int oy() { return (height - H) / 2; }

    @Override
    public boolean isPauseScreen() { return false; }
}

package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.client.AwakeningPathType;
import com.frametrip.dragonlegacyquesttoast.entity.FactionData;
import com.frametrip.dragonlegacyquesttoast.server.AbilityDefinition;
import com.frametrip.dragonlegacyquesttoast.server.AbilityRegistry;
import com.frametrip.dragonlegacyquesttoast.server.QuestDefinition;
import com.frametrip.dragonlegacyquesttoast.client.ClientFactionState;
import com.frametrip.dragonlegacyquesttoast.client.ClientPlayerAbilityState;
import com.frametrip.dragonlegacyquesttoast.client.ClientQuestProgressState;
import com.frametrip.dragonlegacyquesttoast.client.ClientQuestState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Журнал путешественника — 4 вкладки:
 *  1. Отношения (NPC / фракции)
 *  2. Задания (квесты с прогрессом)
 *  3. Пути (способности)
 *  4. Записки (заметки игрока)
 */
public class TravelerJournalScreen extends Screen {

    private static final int W = 560;
    private static final int H = 380;

    private static final String TAB_RELATIONS = "relations";
    private static final String TAB_QUESTS    = "quests";
    private static final String TAB_PATHS     = "paths";
    private static final String TAB_NOTES     = "notes";

    private static final String[] TABS       = { TAB_RELATIONS, TAB_QUESTS, TAB_PATHS, TAB_NOTES };
    private static final String[] TAB_LABELS = { "Отношения", "Задания", "Пути", "Записки" };

    private String activeTab = TAB_QUESTS;

    // ── Scroll / selection state ──────────────────────────────────────────────
    private int questScroll   = 0;
    private int relScroll     = 0;
    private int pathScroll    = 0;
    private int noteScroll    = 0;
    private int selectedQuest = -1;
    private int selectedPath  = 0;
    private int selectedNote  = -1;

    // ── Notes (runtime only) ──────────────────────────────────────────────────
    private static final List<String> NOTES = new ArrayList<>();
    private static String NOTES_INPUT = "";
    private EditBox noteBox;

    // ── Note search ───────────────────────────────────────────────────────────
    private static String noteSearch = "";
    private EditBox noteSearchBox;

    public TravelerJournalScreen() {
        super(Component.literal("Журнал путешественника"));
    }

    @Override
    protected void init() {
        super.init();
        int ox = ox(), oy = oy();

        int tabW = 110;
        for (int i = 0; i < TABS.length; i++) {
            final String tabId = TABS[i];
            add(Button.builder(Component.literal(activeTab.equals(tabId) ? "§e§l" + TAB_LABELS[i] : "§7" + TAB_LABELS[i]),
                    b -> { activeTab = tabId; rebuildWidgets(); })
                    .bounds(ox + 8 + i * (tabW + 4), oy + 4, tabW, 18).build());
        }

        add(Button.builder(Component.literal("✕"), b -> onClose())
                .bounds(ox + W - 26, oy + 4, 20, 18).build());

        int contentY = oy + 30;
        int contentH = H - 68;

        if (TAB_NOTES.equals(activeTab)) {
            // Search box
            noteSearchBox = new EditBox(font, ox + 8, contentY + contentH - 32, W / 2 - 14, 14,
                    Component.literal("Поиск..."));
            noteSearchBox.setValue(noteSearch);
            noteSearchBox.setResponder(v -> { noteSearch = v; noteScroll = 0; });
            add(noteSearchBox);

            // Delete selected note
            add(Button.builder(Component.literal("§c✕ Удалить"), b -> {
                List<String> filtered = filteredNotes();
                if (selectedNote >= 0 && selectedNote < filtered.size()) {
                    NOTES.remove(filtered.get(selectedNote));
                    selectedNote = Math.max(-1, selectedNote - 1);
                    rebuildWidgets();
                }
            }).bounds(ox + W / 2, contentY + contentH - 32, 80, 14).build());

            // Clear all notes
            add(Button.builder(Component.literal("§8Очистить всё"), b -> {
                NOTES.clear();
                selectedNote = -1;
                rebuildWidgets();
            }).bounds(ox + W / 2 + 84, contentY + contentH - 32, 90, 14).build());

            // New note input
            noteBox = new EditBox(font, ox + 8, contentY + contentH - 14, W - 80, 14,
                    Component.literal("Новая запись..."));
            noteBox.setValue(NOTES_INPUT);
            noteBox.setResponder(v -> NOTES_INPUT = v);
            add(noteBox);

            add(Button.builder(Component.literal("+ Добавить"), b -> {
                if (!NOTES_INPUT.isBlank()) {
                    NOTES.add(0, NOTES_INPUT.trim()); // prepend so newest is at top
                    NOTES_INPUT = "";
                    noteScroll = 0;
                    selectedNote = -1;
                    rebuildWidgets();
                }
            }).bounds(ox + W - 70, contentY + contentH - 14, 64, 14).build());
        }
    }

    private <T extends net.minecraft.client.gui.components.events.GuiEventListener
                 & net.minecraft.client.gui.components.Renderable
                 & net.minecraft.client.gui.narration.NarratableEntry> void add(T w) {
        addRenderableWidget(w);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int ox = ox(), oy = oy();

        g.fill(ox, oy, ox + W, oy + H, 0xEE0D0D18);
        brd(g, ox, oy, W, H, 0xFF554433);
        g.fill(ox, oy, ox + W, oy + 26, 0xBB1A1008);
        g.drawCenteredString(font, "§6§lЖурнал путешественника", ox + W / 2, oy + 7, 0xFFE6C97A);
        g.fill(ox, oy + 26, ox + W, oy + 27, 0xFF664422);

        int contentY = oy + 30;
        int contentH = H - 68;

        switch (activeTab) {
            case TAB_RELATIONS -> renderRelations(g, ox, contentY, contentH, mx, my);
            case TAB_QUESTS    -> renderQuests   (g, ox, contentY, contentH, mx, my);
            case TAB_PATHS     -> renderPaths    (g, ox, contentY, contentH, mx, my);
            case TAB_NOTES     -> renderNotes    (g, ox, contentY, contentH, mx, my);
        }

        super.render(g, mx, my, pt);
    }

    // ── Relations tab ─────────────────────────────────────────────────────────
    private void renderRelations(GuiGraphics g, int ox, int oy, int h, int mx, int my) {
        g.fill(ox + 4, oy, ox + W - 4, oy + h, 0xAA131320);
        brd(g, ox + 4, oy, W - 8, h, 0xFF332233);

        int x = ox + 8, y = oy + 6;
        g.drawString(font, "§6§lФракции и отношения", x, y, 0xFFE6C97A, false);
        y += 14;
        g.fill(ox + 4, y, ox + W - 4, y + 1, 0xFF443322);
        y += 6;

        List<FactionData> factions = ClientFactionState.getAll();
        int rowH = 28;
        int maxRows = (h - 30) / rowH;
        int start = Math.max(0, Math.min(relScroll, Math.max(0, factions.size() - maxRows)));

        for (int i = start; i < Math.min(factions.size(), start + maxRows); i++) {
            FactionData f = factions.get(i);
            int fy = y + (i - start) * rowH;
            int col = f.color | 0xFF000000;

            boolean hover = mx >= ox + 5 && mx <= ox + W - 5 && my >= fy - 1 && my < fy + rowH - 2;
            g.fill(ox + 5, fy - 1, ox + W - 5, fy + rowH - 3, hover ? 0x33FFFFFF : (i % 2 == 0 ? 0x22FFFFFF : 0x11FFFFFF));
            g.fill(ox + 5, fy - 1, ox + 9, fy + rowH - 3, col);

            g.drawString(font, "§f§l" + f.name, x + 6, fy + 2, 0xFFFFFFFF, false);

            String rel = f.relations.getOrDefault("player", "NEUTRAL");
            int repVal = f.reputation.getOrDefault("player", 0);
            int rc = "FRIENDLY".equals(rel) ? 0xFF44DD66 : "HOSTILE".equals(rel) ? 0xFFDD4444 : 0xFFAAAAAA;
            g.drawString(font, relLabel(rel), ox + W - 120, fy + 2, rc, false);

            // Reputation bar
            int barX = x + 6;
            int barW = W - 160;
            int barY = fy + 14;
            g.fill(barX, barY, barX + barW, barY + 6, 0xFF222233);
            int repNorm = Math.max(0, Math.min(100, repVal + 50)); // [-50..50] → [0..100]
            int filled = (int)(barW * repNorm / 100.0);
            g.fill(barX, barY, barX + filled, barY + 6, rc);
            g.fill(barX + barW / 2, barY - 1, barX + barW / 2 + 1, barY + 7, 0xFF666688); // center marker
            g.drawString(font, "§8" + (repVal >= 0 ? "+" : "") + repVal, barX + barW + 4, barY - 1, 0xFF888899, false);
        }

        if (factions.isEmpty()) {
            g.drawCenteredString(font, "§8Фракции ещё не созданы", ox + W / 2, oy + h / 2, 0xFF666677);
        }
    }

    // ── Quests tab ────────────────────────────────────────────────────────────
    private void renderQuests(GuiGraphics g, int ox, int oy, int h, int mx, int my) {
        int leftW = 200;
        int rx = ox + leftW + 6;
        int rw = W - leftW - 14;

        g.fill(ox + 4, oy, ox + leftW, oy + h, 0xAA131320);
        brd(g, ox + 4, oy, leftW - 4, h, 0xFF332222);

        List<QuestDefinition> quests = new ArrayList<>(ClientQuestState.getAll());
        quests.sort((a, b) -> {
            boolean aa = ClientQuestProgressState.isActive(a.id);
            boolean ba = ClientQuestProgressState.isActive(b.id);
            if (aa != ba) return Boolean.compare(ba, aa);
            boolean ac = ClientQuestProgressState.isComplete(a.id);
            boolean bc = ClientQuestProgressState.isComplete(b.id);
            return Boolean.compare(ac, bc);
        });

        // Quest counts header
        long active = quests.stream().filter(q -> ClientQuestProgressState.isActive(q.id)).count();
        long done   = quests.stream().filter(q -> ClientQuestProgressState.isComplete(q.id)).count();
        g.drawString(font, "§7Всего: §f" + quests.size() + "  §eАктивно: §f" + active + "  §aВыполнено: §f" + done,
                ox + 6, oy + 2, 0xFF888899, false);

        int rowH = 24;
        int maxRows = (h - 12) / rowH;
        int start = Math.max(0, Math.min(questScroll, Math.max(0, quests.size() - maxRows)));

        for (int i = start; i < Math.min(quests.size(), start + maxRows); i++) {
            QuestDefinition q = quests.get(i);
            int qy = oy + 12 + (i - start) * rowH;
            boolean sel   = (i == selectedQuest);
            boolean active2 = ClientQuestProgressState.isActive(q.id);
            boolean done2   = ClientQuestProgressState.isComplete(q.id);

            int bg = sel ? 0x66FFCC44 : (active2 ? 0x33FFCC44 : (done2 ? 0x22448844 : (i % 2 == 0 ? 0x22FFFFFF : 0x00000000)));
            g.fill(ox + 5, qy, ox + leftW - 1, qy + rowH - 1, bg);
            if (sel) g.fill(ox + 5, qy, ox + 6, qy + rowH - 1, 0xFFFFCC44);

            String mark = done2 ? "§a✔" : active2 ? "§e◉" : "§8○";
            String titleColor = done2 ? "§7" : active2 ? "§f" : "§8";
            g.drawString(font, mark + " " + titleColor + fitStr(safe(q.title), leftW - 24), ox + 8, qy + 4, 0xFFFFFFFF, false);
            g.drawString(font, "§8" + questTypeShort(q), ox + 8, qy + 14, 0xFF666677, false);
        }

        if (quests.isEmpty()) {
            g.drawCenteredString(font, "§8Квестов ещё нет", ox + leftW / 2, oy + h / 2, 0xFF666677);
        }

        g.fill(rx, oy, rx + rw, oy + h, 0xAA131320);
        brd(g, rx, oy, rw, h, 0xFF332222);

        if (selectedQuest >= 0 && selectedQuest < quests.size()) {
            QuestDefinition q = quests.get(selectedQuest);
            int dy = oy + 6;

            String titleType = safe(q.questType);
            int typeCol = "main".equalsIgnoreCase(titleType) ? 0xFFFFCC44 : "daily".equalsIgnoreCase(titleType) ? 0xFF44CCFF : 0xFFAAAAAA;
            g.drawString(font, "§8[" + questTypeShort(q) + "]", rx + 6, dy, typeCol, false); dy += 12;
            g.drawString(font, "§f§l" + safe(q.title), rx + 6, dy, 0xFFFFFFFF, false); dy += 14;
            g.fill(rx + 6, dy, rx + rw - 6, dy + 1, 0xFF443322); dy += 5;

            boolean isActive   = ClientQuestProgressState.isActive(q.id);
            boolean isComplete = ClientQuestProgressState.isComplete(q.id);
            boolean isFailed   = ClientQuestProgressState.isFailed(q.id);
            String status;
            int sc;
            if (isComplete)     { status = "✔ Выполнено"; sc = 0xFF44DD66; }
            else if (isFailed)  { status = "✖ Провалено"; sc = 0xFFDD4444; }
            else if (isActive)  { status = "◉ Активно";   sc = 0xFFFFCC44; }
            else                { status = "○ Не начато"; sc = 0xFF888899; }
            g.drawString(font, status, rx + 6, dy, sc, false); dy += 12;

            if (q.giverNpcId != null && !q.giverNpcId.isBlank()) {
                g.drawString(font, "§7Выдаёт: §f" + q.giverNpcId, rx + 6, dy, 0xFFCCCCCC, false); dy += 12;
            }
            g.drawString(font, "§8" + q.logicLabel(), rx + 6, dy, 0xFF666677, false); dy += 12;

            g.fill(rx + 6, dy, rx + rw - 6, dy + 1, 0xFF332222); dy += 4;
            g.drawString(font, "§7Описание:", rx + 6, dy, 0xFF9999AA, false); dy += 10;
            String desc = safe(q.description);
            for (var line : font.split(Component.literal(desc), rw - 16)) {
                if (dy > oy + h - 56) { g.drawString(font, "§8...", rx + 6, dy, 0xFF444455, false); break; }
                g.drawString(font, line, rx + 6, dy, 0xFFDDDDDD, false);
                dy += 10;
            }

            // Objectives with checkmarks
            if (q.objectives != null && !q.objectives.isEmpty()) {
                dy = Math.max(dy, oy + h - 56);
                g.fill(rx + 6, dy, rx + rw - 6, dy + 1, 0xFF332222); dy += 4;
                g.drawString(font, "§7Цели:", rx + 6, dy, 0xFF9999AA, false); dy += 10;
                for (String obj : q.objectives) {
                    if (dy > oy + h - 22) break;
                    g.drawString(font, "§8▸ §f" + obj, rx + 8, dy, 0xFFCCCCCC, false);
                    dy += 10;
                }
            }

            // Progress bar
            int prog = ClientQuestProgressState.getProgress(q.id);
            int need = Math.max(1, q.getRequiredCount());
            int barY = oy + h - 18;
            g.fill(rx + 6, barY, rx + rw - 6, barY + 8, 0xFF222233);
            int barW = rw - 12;
            int filled = (int)(barW * Math.min(1.0, prog / (double) need));
            if (filled > 0) g.fill(rx + 6, barY, rx + 6 + filled, barY + 8, isComplete ? 0xFF44CC77 : 0xFF3399CC);
            brd(g, rx + 6, barY, barW, 8, 0xFF334455);
            g.drawCenteredString(font, "§f" + Math.min(prog, need) + "§7/§f" + need, rx + 6 + barW / 2, barY, 0xFFCCCCCC);
        } else {
            g.drawCenteredString(font, "§8Выберите квест", rx + rw / 2, oy + h / 2, 0xFF666677);
        }
    }

    // ── Paths tab ─────────────────────────────────────────────────────────────
    private void renderPaths(GuiGraphics g, int ox, int oy, int h, int mx, int my) {
        String[] pathLabels = { "§c🔥 Огонь", "§b❄ Лёд", "§e⚡ Буря", "§5☽ Пустота" };
        int[] pathCols      = { 0xFFFF6644, 0xFF66CCFF, 0xFFFFDD44, 0xFFAA44FF };

        int tabW = (W - 16) / 4;
        for (int i = 0; i < pathLabels.length; i++) {
            int bx = ox + 8 + i * (tabW + 2);
            boolean sel = (selectedPath == i);
            g.fill(bx, oy, bx + tabW, oy + 16, sel ? 0xBB222244 : 0x44111122);
            if (sel) { g.fill(bx, oy, bx + tabW, oy + 2, pathCols[i]); }
            g.drawCenteredString(font, pathLabels[i], bx + tabW / 2, oy + 4, pathCols[i]);
        }

        AwakeningPathType currentPath = AwakeningPathType.values()[selectedPath];
        int col = pathCols[selectedPath];
        int ay = oy + 22;

        List<AbilityDefinition> abilities = AbilityRegistry.getForPath(currentPath);
        int unlocked = (int) abilities.stream().filter(a -> ClientPlayerAbilityState.hasAbility(a.id)).count();
        int points   = ClientPlayerAbilityState.getPoints();

        g.fill(ox + 4, ay, ox + W - 4, ay + 14, 0x44000000);
        g.drawString(font, "Открыто: §e" + unlocked + "§7/§f" + abilities.size(), ox + 8, ay + 2, 0xFFCCCCCC, false);
        g.drawString(font, "Очков: §e" + points, ox + W / 2, ay + 2, 0xFFCCCCCC, false);
        // Overall progress bar
        if (!abilities.isEmpty()) {
            int barX = ox + W - 160, barY = ay + 4;
            int barW = 140;
            g.fill(barX, barY, barX + barW, barY + 6, 0xFF222233);
            int filled = (int)(barW * unlocked / (double)abilities.size());
            g.fill(barX, barY, barX + filled, barY + 6, col);
        }
        ay += 18;

        int rowH = 42;
        int maxRows = (h - 40) / rowH;
        int startA = Math.max(0, Math.min(pathScroll, Math.max(0, abilities.size() - maxRows)));

        for (int i = startA; i < Math.min(abilities.size(), startA + maxRows); i++) {
            AbilityDefinition a = abilities.get(i);
            boolean has  = ClientPlayerAbilityState.hasAbility(a.id);
            boolean ena  = ClientPlayerAbilityState.isEnabled(a.id);
            boolean canAfford = points >= a.cost;

            int bg = has ? (ena ? 0x33449933 : 0x22888800) : (canAfford ? 0x22003333 : 0x11FFFFFF);
            g.fill(ox + 8, ay, ox + W - 8, ay + rowH - 2, bg);
            g.fill(ox + 8, ay, ox + 12, ay + rowH - 2, col);
            brd(g, ox + 8, ay, W - 16, rowH - 2, has ? col : 0xFF333344);

            // Tier stars
            StringBuilder stars = new StringBuilder("§8");
            for (int s = 0; s < Math.min(5, a.tier); s++) stars.append("★");
            for (int s = a.tier; s < 5; s++) stars.append("☆");

            String status = has ? (ena ? "§a✔ Активно" : "§7⏸ Откл.") : (canAfford ? "§e○ Доступно" : "§8✖ Заперто");
            g.drawString(font, "§f§l" + a.name, ox + 16, ay + 4, has ? 0xFFFFFFFF : (canAfford ? 0xFFCCCCCC : 0xFF888888), false);
            g.drawString(font, stars.toString(), ox + W - 120, ay + 4, col, false);
            g.drawString(font, status, ox + W - 80, ay + 4, 0xFFFFFFFF, false);
            g.drawString(font, "§8" + (a.description != null ? truncate(a.description, 64) : ""), ox + 16, ay + 16, 0xFF999999, false);
            g.drawString(font, "§8Уровень: §7" + a.tier
                    + "  §8Стоимость: " + (canAfford ? "§e" : "§c") + a.cost + " §8оч.",
                    ox + 16, ay + 28, 0xFF888888, false);
            ay += rowH;
        }

        if (abilities.isEmpty()) {
            g.drawCenteredString(font, "§8Нет способностей на этом пути", ox + W / 2, oy + h / 2, 0xFF666677);
        }
    }

    // ── Notes tab ─────────────────────────────────────────────────────────────
    private void renderNotes(GuiGraphics g, int ox, int oy, int h, int mx, int my) {
        g.fill(ox + 4, oy, ox + W - 4, oy + h, 0xAA0D1208);
        brd(g, ox + 4, oy, W - 8, h, 0xFF335533);

        int listH = h - 70;
        List<String> filtered = filteredNotes();
        int total = NOTES.size();
        String header = "§7Заметки: §e" + filtered.size()
                + (filtered.size() != total ? "§7/§f" + total : "")
                + (noteSearch.isBlank() ? "" : " §8(фильтр: " + noteSearch + ")");
        g.drawString(font, header, ox + 8, oy + 4, 0xFF88AA66, false);

        // List area
        g.fill(ox + 4, oy + 14, ox + W - 4, oy + listH, 0x22000000);

        int rowH = 14;
        int maxRows = (listH - 16) / rowH;
        int maxScroll = Math.max(0, filtered.size() - maxRows);
        noteScroll = Math.max(0, Math.min(noteScroll, maxScroll));

        int ny = oy + 16;
        for (int i = noteScroll; i < Math.min(filtered.size(), noteScroll + maxRows); i++) {
            boolean sel = (i == selectedNote);
            if (sel) g.fill(ox + 5, ny - 1, ox + W - 5, ny + rowH - 1, 0x44AAFFAA);
            String num = "§8[" + (i + 1) + "] ";
            String txt = safe(filtered.get(i));
            String line = num + (sel ? "§f" : "§a") + fitStr(txt, W - 40);
            g.drawString(font, line, ox + 8, ny, sel ? 0xFFFFFFFF : 0xFFCCCCCC, false);
            ny += rowH;
        }

        if (filtered.isEmpty()) {
            if (NOTES.isEmpty()) {
                g.drawCenteredString(font, "§8Нет записей. Добавьте первую!", ox + W / 2, oy + h / 2 - 30, 0xFF556655);
            } else {
                g.drawCenteredString(font, "§8Ничего не найдено по запросу", ox + W / 2, oy + h / 2 - 30, 0xFF556655);
            }
        }

        // Scroll indicators
        if (maxScroll > 0) {
            g.drawString(font, "§8▲ " + noteScroll, ox + W - 40, oy + 16, 0xFF445544, false);
            g.drawString(font, "§8▼ " + (maxScroll - noteScroll), ox + W - 40, oy + listH - 12, 0xFF445544, false);
        }

        // Separator
        g.fill(ox + 4, oy + listH, ox + W - 4, oy + listH + 1, 0xFF335533);
        g.drawString(font, "§8Поиск:", ox + 8, oy + listH + 4, 0xFF667766, false);
        g.drawString(font, "§8Новая запись:", ox + 8, oy + h - 24, 0xFF667766, false);
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────
    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        int dir = -(int) Math.signum(delta);
        switch (activeTab) {
            case TAB_QUESTS    -> questScroll = Math.max(0, questScroll + dir);
            case TAB_RELATIONS -> relScroll   = Math.max(0, relScroll + dir);
            case TAB_PATHS     -> pathScroll  = Math.max(0, pathScroll + dir);
            case TAB_NOTES     -> {
                List<String> filtered = filteredNotes();
                int maxScroll = Math.max(0, filtered.size() - ((H - 68 - 70) / 14));
                noteScroll = Math.max(0, Math.min(maxScroll, noteScroll + dir));
            }
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) return true;
        int ox = ox(), oy = oy();
        int contentY = oy + 30;
        int contentH = H - 68;

        if (TAB_PATHS.equals(activeTab)) {
            int tabW = (W - 16) / 4;
            for (int i = 0; i < 4; i++) {
                int bx = ox + 8 + i * (tabW + 2);
                if (mx >= bx && mx <= bx + tabW && my >= contentY && my <= contentY + 16) {
                    selectedPath = i;
                    return true;
                }
            }
        }

        if (TAB_QUESTS.equals(activeTab)) {
            int leftW = 200;
            List<QuestDefinition> quests = new ArrayList<>(ClientQuestState.getAll());
            int rowH = 24;
            for (int i = questScroll; i < quests.size(); i++) {
                int qy = contentY + 12 + (i - questScroll) * rowH;
                if (mx >= ox + 5 && mx <= ox + leftW - 1 && my >= qy && my <= qy + rowH - 1) {
                    selectedQuest = i;
                    return true;
                }
            }
        }

        if (TAB_NOTES.equals(activeTab)) {
            int listH = contentH - 70;
            int rowH = 14;
            int maxRows = (listH - 16) / rowH;
            List<String> filtered = filteredNotes();
            int ny = contentY + 16;
            for (int i = noteScroll; i < Math.min(filtered.size(), noteScroll + maxRows); i++) {
                if (mx >= ox + 5 && mx <= ox + W - 5 && my >= ny - 1 && my < ny + rowH - 1) {
                    selectedNote = (selectedNote == i) ? -1 : i; // toggle selection
                    return true;
                }
                ny += rowH;
            }
        }

        return false;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private List<String> filteredNotes() {
        if (noteSearch == null || noteSearch.isBlank()) return new ArrayList<>(NOTES);
        List<String> result = new ArrayList<>();
        String lower = noteSearch.toLowerCase();
        for (String n : NOTES) {
            if (n.toLowerCase().contains(lower)) result.add(n);
        }
        return result;
    }

    private int ox() { return (width  - W) / 2; }
    private int oy() { return (height - H) / 2; }

    @Override
    public boolean isPauseScreen() { return false; }

    private static void brd(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    private String fitStr(String s, int maxPx) {
        if (font.width(s) <= maxPx) return s;
        while (s.length() > 0 && font.width(s + "…") > maxPx) s = s.substring(0, s.length() - 1);
        return s + "…";
    }

    private static String relLabel(String rel) {
        return switch (rel) {
            case "FRIENDLY" -> "Дружественная";
            case "HOSTILE"  -> "Враждебная";
            default         -> "Нейтральная";
        };
    }

    private static String questTypeShort(QuestDefinition q) {
        if ("main".equalsIgnoreCase(q.questType))   return "Основной";
        if ("daily".equalsIgnoreCase(q.questType))  return "Ежедневный";
        return "Второстепенный";
    }
}

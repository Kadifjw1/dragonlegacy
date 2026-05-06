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
    private static final int H = 360;

    // ── Tab ids ───────────────────────────────────────────────────────────────
    private static final String TAB_RELATIONS = "relations";
    private static final String TAB_QUESTS    = "quests";
    private static final String TAB_PATHS     = "paths";
    private static final String TAB_NOTES     = "notes";

    private static final String[] TABS = { TAB_RELATIONS, TAB_QUESTS, TAB_PATHS, TAB_NOTES };
    private static final String[] TAB_LABELS = { "Отношения", "Задания", "Пути", "Записки" };

    private String activeTab = TAB_QUESTS;

    // ── Scroll state per tab ──────────────────────────────────────────────────
    private int questScroll   = 0;
    private int relScroll     = 0;
    private int pathScroll    = 0;
    private int selectedQuest = -1;
    private int selectedPath  = 0;   // 0=FIRE,1=ICE,2=STORM,3=VOID

    // ── Notes storage (runtime only; cleared on relog) ────────────────────────
    private static final List<String> NOTES = new ArrayList<>();
    private static String NOTES_INPUT = "";

    public TravelerJournalScreen() {
        super(Component.literal("Журнал путешественника"));
    }

    // ── Init ──────────────────────────────────────────────────────────────────
    @Override
    protected void init() {
        super.init();
        int ox = ox(), oy = oy();

        // Tab buttons
        int tabW = 110;
        for (int i = 0; i < TABS.length; i++) {
            final String tabId = TABS[i];
            add(Button.builder(Component.literal(TAB_LABELS[i]),
                    b -> { activeTab = tabId; rebuildWidgets(); })
                    .bounds(ox + 8 + i * (tabW + 4), oy + 4, tabW, 18).build());
        }

        add(Button.builder(Component.literal("✕ Закрыть"), b -> onClose())
                .bounds(ox + W - 76, oy + 4, 68, 18).build());

        // Notes tab: input line
        if (TAB_NOTES.equals(activeTab)) {
            var noteBox = new net.minecraft.client.gui.components.EditBox(
                    font, ox + 10, oy + H - 38, W - 90, 16,
                    Component.literal("Заметка..."));
            noteBox.setValue(NOTES_INPUT);
            noteBox.setResponder(v -> NOTES_INPUT = v);
            add(noteBox);
            add(Button.builder(Component.literal("+ Добавить"), b -> {
                if (!NOTES_INPUT.isBlank()) {
                    NOTES.add(NOTES_INPUT.trim());
                    NOTES_INPUT = "";
                    rebuildWidgets();
                }
            }).bounds(ox + W - 78, oy + H - 38, 68, 16).build());
        }
    }

private <T extends net.minecraft.client.gui.components.events.GuiEventListener
             & net.minecraft.client.gui.components.Renderable
             & net.minecraft.client.gui.narration.NarratableEntry> void add(T w) {
        addRenderableWidget(w);
    }

// ── Render ────────────────────────────────────────────────────────────────
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
        int x = ox + 8, y = oy + 4;

        g.drawString(font, "§7Фракции:", x, y, 0xFFAABBCC, false);
        y += 14;

        List<FactionData> factions = ClientFactionState.getAll();
        int maxRows = (h - 40) / 16;
        int start = Math.max(0, Math.min(relScroll, Math.max(0, factions.size() - maxRows)));
        for (int i = start; i < Math.min(factions.size(), start + maxRows); i++) {
            FactionData f = factions.get(i);
            int col = f.color | 0xFF000000;
            g.fill(x, y - 1, x + W - 20, y + 12, 0x33FFFFFF);
            g.fill(x, y - 1, x + 4, y + 12, col);
            g.drawString(font, f.name, x + 8, y, 0xFFFFFFFF, false);
            String rel = f.relations.getOrDefault("player", "NEUTRAL");
            int rc = "FRIENDLY".equals(rel) ? 0xFF44DD66 : "HOSTILE".equals(rel) ? 0xFFDD4444 : 0xFFAAAAAA;
            g.drawString(font, relLabel(rel), x + W - 100, y, rc, false);
            y += 16;
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
            return Boolean.compare(ba, aa);
        });

        int rowH = 24;
        int maxRows = h / rowH;
        int start = Math.max(0, Math.min(questScroll, Math.max(0, quests.size() - maxRows)));

        for (int i = start; i < Math.min(quests.size(), start + maxRows); i++) {
            QuestDefinition q = quests.get(i);
            int qy = oy + (i - start) * rowH;
            boolean sel = (i == selectedQuest);
            boolean active = ClientQuestProgressState.isActive(q.id);
            boolean done   = ClientQuestProgressState.isComplete(q.id);

            int bg = sel ? 0x55FFFFFF : (i % 2 == 0 ? 0x22FFFFFF : 0x00000000);
            g.fill(ox + 5, qy, ox + leftW - 1, qy + rowH - 1, bg);

            String mark = done ? "§a✔" : active ? "§e◉" : "§8○";
            g.drawString(font, mark + " §f" + safe(q.title), ox + 8, qy + 4, 0xFFFFFFFF, false);
            g.drawString(font, "§8" + questTypeShort(q), ox + 8, qy + 14, 0xFF888899, false);
        }

        if (quests.isEmpty()) {
            g.drawCenteredString(font, "§8Квестов ещё нет", ox + leftW / 2, oy + h / 2, 0xFF666677);
        }

        g.fill(rx, oy, rx + rw, oy + h, 0xAA131320);
        brd(g, rx, oy, rw, h, 0xFF332222);

        if (selectedQuest >= 0 && selectedQuest < quests.size()) {
            QuestDefinition q = quests.get(selectedQuest);
            int dy = oy + 6;
            g.drawString(font, "§f§l" + safe(q.title), rx + 6, dy, 0xFFE6C97A, false); dy += 14;

            String status;
            int sc;
            if (ClientQuestProgressState.isComplete(q.id)) { status = "Выполнено"; sc = 0xFF44DD66; }
            else if (ClientQuestProgressState.isFailed(q.id)) { status = "Провалено"; sc = 0xFFDD4444; }
            else if (ClientQuestProgressState.isActive(q.id)) { status = "Активно"; sc = 0xFFFFCC44; }
            else { status = "Не начато"; sc = 0xFF888899; }
            g.drawString(font, "Статус: §r", rx + 6, dy, 0xFFAAAAAA, false);
            g.drawString(font, status, rx + 6 + font.width("Статус: "), dy, sc, false); dy += 12;

            if (q.giverNpcId != null && !q.giverNpcId.isBlank()) {
                g.drawString(font, "§7От NPC: §f" + q.giverNpcId, rx + 6, dy, 0xFFCCCCCC, false); dy += 12;
            }

            g.drawString(font, "§8" + q.logicLabel(), rx + 6, dy, 0xFF888899, false); dy += 14;
            g.drawString(font, "§7Описание:", rx + 6, dy, 0xFFAAAAAA, false); dy += 10;

            String desc = safe(q.description);
            for (var line : font.split(Component.literal(desc), rw - 12)) {
                g.drawString(font, line, rx + 6, dy, 0xFFDDDDDD, false);
                dy += 10;
                if (dy > oy + h - 42) break;
            }

      // Progress bar
            int prog = ClientQuestProgressState.getProgress(q.id);
            int need = Math.max(1, q.getRequiredCount());
            int barY = oy + h - 36;
            g.drawString(font, "Прогресс: §e" + Math.min(prog, need) + "§7/§f" + need, rx + 6, barY, 0xFFCCCCCC, false);
            int barW = rw - 12;
            g.fill(rx + 6, barY + 12, rx + 6 + barW, barY + 18, 0xFF333344);
            int filled = (int) (barW * Math.min(1.0, prog / (double) need));
            if (filled > 0) g.fill(rx + 6, barY + 12, rx + 6 + filled, barY + 18, 0xFF44CC77);

            // Objectives
            if (q.objectives != null && !q.objectives.isEmpty()) {
                int oy2 = barY + 22;
                g.drawString(font, "§7Цели:", rx + 6, oy2, 0xFF888899, false);
                oy2 += 10;
                for (String obj : q.objectives) {
                    if (oy2 > oy + h - 6) break;
                    g.drawString(font, "§8• §f" + obj, rx + 8, oy2, 0xFFCCCCCC, false);
                    oy2 += 10;
                }
            }
        } else {
            g.drawCenteredString(font, "§8Выберите квест слева", rx + rw / 2, oy + h / 2, 0xFF666677);
        }
    }

    // ── Paths tab ─────────────────────────────────────────────────────────────
    private void renderPaths(GuiGraphics g, int ox, int oy, int h, int mx, int my) {
        String[] pathIds    = { "FIRE", "ICE", "STORM", "VOID" };
        String[] pathLabels = { "§c🔥 Огонь", "§b❄ Лёд", "§e⚡ Буря", "§5☽ Пустота" };
        int[] pathCols      = { 0xFFFF6644, 0xFF66CCFF, 0xFFFFDD44, 0xFFAA44FF };

        int tabW = (W - 16) / 4;
        for (int i = 0; i < pathIds.length; i++) {
            int bx = ox + 8 + i * (tabW + 2);
            boolean sel = (selectedPath == i);
            g.fill(bx, oy, bx + tabW, oy + 14, sel ? 0x88FFFFFF : 0x44FFFFFF);
            g.drawCenteredString(font, pathLabels[i], bx + tabW / 2, oy + 3, pathCols[i]);
        }

        AwakeningPathType currentPath = AwakeningPathType.values()[selectedPath];
        int col = pathCols[selectedPath];
        int ay = oy + 18;

        List<AbilityDefinition> abilities = AbilityRegistry.getForPath(currentPath);
        int unlocked = (int) abilities.stream()
                .filter(a -> ClientPlayerAbilityState.hasAbility(a.id)).count();
        g.drawString(font, "Открыто: §e" + unlocked + "§7/§f" + abilities.size(), ox + 8, ay, 0xFFCCCCCC, false);
        g.drawString(font, "Очков пробуждения: §e" + ClientPlayerAbilityState.getPoints(), ox + 200, ay, 0xFFCCCCCC, false);
        ay += 14;

        int rowH = 40;
        int maxRows = (h - 32) / rowH;
        int startA = Math.max(0, Math.min(pathScroll, Math.max(0, abilities.size() - maxRows)));
        for (int i = startA; i < Math.min(abilities.size(), startA + maxRows); i++) {
            AbilityDefinition a = abilities.get(i);
            boolean has = ClientPlayerAbilityState.hasAbility(a.id);
            boolean ena = ClientPlayerAbilityState.isEnabled(a.id);

            int bg = has ? (ena ? 0x33449933 : 0x22888800) : 0x11FFFFFF;
            g.fill(ox + 8, ay, ox + W - 8, ay + rowH - 2, bg);
            g.fill(ox + 8, ay, ox + 12, ay + rowH - 2, col);

            String status = has ? (ena ? "§aАктивно" : "§eОтключено") : "§8Заперто";
            g.drawString(font, "§f§l" + a.name, ox + 16, ay + 4, has ? 0xFFFFFFFF : 0xFF888888, false);
            g.drawString(font, status, ox + W - 80, ay + 4, 0xFFFFFFFF, false);
            g.drawString(font, "§8" + (a.description != null ? truncate(a.description, 60) : ""), ox + 16, ay + 16, 0xFF999999, false);
            g.drawString(font, "§8Уровень: §7" + a.tier + "  Стоимость: §e" + a.cost, ox + 16, ay + 26, 0xFF888888, false);
            ay += rowH;
        }

        if (abilities.isEmpty()) {
            g.drawCenteredString(font, "§8Нет способностей на этом пути", ox + W / 2, oy + h / 2, 0xFF666677);
        }
    }

// ── Notes tab ─────────────────────────────────────────────────────────────
    private void renderNotes(GuiGraphics g, int ox, int oy, int h, int mx, int my) {
        g.fill(ox + 4, oy, ox + W - 4, oy + h - 44, 0xAA0D1208);
        brd(g, ox + 4, oy, W - 8, h - 44, 0xFF335533);
        g.drawString(font, "§7Заметки путешественника:", ox + 8, oy + 4, 0xFF88AA66, false);

        int ny = oy + 16;
        int maxRows = (h - 66) / 12;
        int start = Math.max(0, NOTES.size() - maxRows);
        for (int i = start; i < NOTES.size(); i++) {
            if (ny > oy + h - 50) break;
            g.drawString(font, "§8[" + (i + 1) + "] §f" + NOTES.get(i), ox + 8, ny, 0xFFCCCCCC, false);
            ny += 12;
        }

        if (NOTES.isEmpty()) {
            g.drawCenteredString(font, "§8Пока нет записей. Добавьте первую!", ox + W / 2, oy + h / 2 - 20, 0xFF556655);
        }

        g.fill(ox + 4, oy + h - 44, ox + W - 4, oy + h - 2, 0xAA131320);
        brd(g, ox + 4, oy + h - 44, W - 8, 42, 0xFF335533);
        g.drawString(font, "§8Новая запись:", ox + 8, oy + h - 42, 0xFF667766, false);
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────
    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        int dir = -(int) Math.signum(delta);
        switch (activeTab) {
            case TAB_QUESTS    -> questScroll = Math.max(0, questScroll + dir);
            case TAB_RELATIONS -> relScroll   = Math.max(0, relScroll + dir);
            case TAB_PATHS     -> pathScroll  = Math.max(0, pathScroll + dir);
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (super.mouseClicked(mx, my, button)) return true;
        int ox = ox(), oy = oy();

        if (TAB_PATHS.equals(activeTab)) {
            String[] pathIds = { "FIRE", "ICE", "STORM", "VOID" };
            int tabW = (W - 16) / 4;
            for (int i = 0; i < pathIds.length; i++) {
                int bx = ox + 8 + i * (tabW + 2);
                if (mx >= bx && mx <= bx + tabW && my >= oy + 30 && my <= oy + 44) {
                    selectedPath = i;
                    return true;
                }
            }
        }

        if (TAB_QUESTS.equals(activeTab)) {
            int leftW = 200;
            List<QuestDefinition> quests = new ArrayList<>(ClientQuestState.getAll());
            int rowH = 24;
            int contentY = oy + 30;
            for (int i = questScroll; i < quests.size(); i++) {
                int qy = contentY + (i - questScroll) * rowH;
                if (mx >= ox + 5 && mx <= ox + leftW - 1 && my >= qy && my <= qy + rowH - 1) {
                    selectedQuest = i;
                    return true;
                }
            }
        }

        return false;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
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

    private static String relLabel(String rel) {
        return switch (rel) {
            case "FRIENDLY" -> "Дружественная";
            case "HOSTILE"  -> "Враждебная";
            default         -> "Нейтральная";
        };
    }

    private static String questTypeShort(QuestDefinition q) {
        if ("main".equalsIgnoreCase(q.questType)) return "Основной";
        if ("daily".equalsIgnoreCase(q.questType)) return "Ежедневный";
        return "Второстепенный";
    }
}

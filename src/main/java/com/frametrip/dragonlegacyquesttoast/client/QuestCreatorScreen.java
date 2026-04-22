package com.frametrip.dragonlegacyquesttoast.client;
 
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SaveQuestPacket;
import com.frametrip.dragonlegacyquesttoast.server.QuestDefinition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
 
import java.util.ArrayList;
import java.util.List;
 
public class QuestCreatorScreen extends Screen {
 
    private static final int W  = 460;
    private static final int H  = 300;
    private static final int LW = 130; // ширина левой панели
 
    private static final int C_BG     = 0xDD0E0E18;
    private static final int C_BORDER = 0xFF444455;
    private static final int C_PANEL  = 0xAA1A1A2A;
    private static final int C_HEAD   = 0xFFE6D7B5;
    private static final int C_SEC    = 0xFF888877;
 
    private final Screen parent;
 
    // Список квестов
    private List<QuestDefinition> quests = new ArrayList<>();
    private int selectedIdx = -1;
    private int listScroll  = 0;
 
    // Текущий редактируемый квест (копия)
    private QuestDefinition editing = null;
 
    // Поля ввода
    private EditBox fTitle, fDesc, fGiver, fReward;
    private EditBox[] fObjectives = new EditBox[4];
 
    public QuestCreatorScreen(Screen parent) {
        super(Component.literal("Редактор квестов"));
        this.parent = parent;
    }
 
    @Override
    protected void init() {
        super.init();
        quests = ClientQuestState.getAll();
 
        int ox = (width - W) / 2;
        int oy = (height - H) / 2;
        int rx = ox + LW + 8;  // начало правой панели
        int rw = W - LW - 20;  // ширина правой панели
 
        // ── Левая панель: кнопки ──────────────────────────────────────────────
        addRenderableWidget(Button.builder(Component.literal("+ Новый"), b -> createNew())
            .bounds(ox + 4, oy + H - 28, LW - 8, 20).build());
 
        // ── Правая панель: поля редактора ─────────────────────────────────────
        int fy = oy + 36;
        fTitle = field(rx, fy, rw, "Название квеста"); fy += 24;
        fDesc  = field(rx, fy, rw, "Описание");        fy += 24;
        fGiver = field(rx, fy, rw, "ID персонажа-NPC"); fy += 24;
        fReward = field(rx, fy, rw, "Награда (текст)"); fy += 24;
 
        for (int i = 0; i < fObjectives.length; i++) {
            fObjectives[i] = field(rx, fy, rw, "Цель " + (i + 1));
            fy += 22;
        }
 
        // Кнопки действий
        int bY = oy + H - 28;
        addRenderableWidget(Button.builder(Component.literal("Сохранить"), b -> save())
            .bounds(rx, bY, 96, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Удалить"), b -> delete())
            .bounds(rx + 100, bY, 80, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Тип: " + (editing != null ? editing.typeLabel() : "—")), b -> cycleType())
            .bounds(rx + 184, bY, 90, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Назад"), b -> {
            if (minecraft != null) minecraft.setScreen(parent);
        }).bounds(ox + W - 66, oy + H - 28, 60, 20).build());
 
        loadFields();
    }
 
    private EditBox field(int x, int y, int w, String hint) {
        EditBox eb = new EditBox(font, x, y, w, 18, Component.literal(hint));
        eb.setHint(Component.literal(hint).withStyle(s -> s.withColor(0xFF777777)));
        eb.setMaxLength(256);
        addRenderableWidget(eb);
        return eb;
    }
 
    private void loadFields() {
        if (editing == null) {
            if (fTitle != null) fTitle.setValue("");
            if (fDesc  != null) fDesc.setValue("");
            if (fGiver != null) fGiver.setValue("");
            if (fReward != null) fReward.setValue("");
            for (EditBox f : fObjectives) if (f != null) f.setValue("");
            return;
        }
        fTitle.setValue(editing.title);
        fDesc.setValue(editing.description);
        fGiver.setValue(editing.giverNpcId);
        fReward.setValue(editing.rewardText);
        for (int i = 0; i < fObjectives.length; i++) {
            fObjectives[i].setValue(i < editing.objectives.size() ? editing.objectives.get(i) : "");
        }
    }
 
    private void pullFields() {
        if (editing == null) return;
        editing.title       = fTitle.getValue();
        editing.description = fDesc.getValue();
        editing.giverNpcId  = fGiver.getValue();
        editing.rewardText  = fReward.getValue();
        editing.objectives.clear();
        for (EditBox f : fObjectives) {
            String v = f.getValue().trim();
            if (!v.isEmpty()) editing.objectives.add(v);
        }
    }
 
    private void createNew() {
        pullFields();
        editing = new QuestDefinition();
        selectedIdx = -1;
        rebuildScreen();
    }
 
    private void save() {
        if (editing == null) return;
        pullFields();
        if (editing.title.isBlank()) return;
        ModNetwork.CHANNEL.sendToServer(new SaveQuestPacket(editing, false));
        // Оптимистичное обновление локального состояния
        ClientQuestState.sync(mergeIntoList(quests, editing));
        quests = ClientQuestState.getAll();
        rebuildScreen();
    }
 
    private void delete() {
        if (editing == null) return;
        ModNetwork.CHANNEL.sendToServer(new SaveQuestPacket(editing, true));
        quests.removeIf(q -> q.id.equals(editing.id));
        ClientQuestState.sync(quests);
        editing = null;
        selectedIdx = -1;
        rebuildScreen();
    }
 
    private void cycleType() {
        if (editing == null) return;
        String[] types = QuestDefinition.TYPES;
        int cur = 0;
        for (int i = 0; i < types.length; i++) if (types[i].equals(editing.questType)) { cur = i; break; }
        editing.questType = types[(cur + 1) % types.length];
        rebuildScreen();
    }
 
    private void selectQuest(int idx) {
        pullFields();
        selectedIdx = idx;
        editing = idx >= 0 && idx < quests.size() ? quests.get(idx).copy() : null;
        rebuildScreen();
    }
 
    private void rebuildScreen() {
        clearWidgets();
        init();
    }
 
    private static List<QuestDefinition> mergeIntoList(List<QuestDefinition> list, QuestDefinition q) {
        List<QuestDefinition> result = new ArrayList<>();
        boolean found = false;
        for (QuestDefinition e : list) {
            if (e.id.equals(q.id)) { result.add(q); found = true; }
            else result.add(e);
        }
        if (!found) result.add(q);
        return result;
    }
 
    @Override public boolean isPauseScreen() { return false; }
 
    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(parent);
    }
 
    // ── Рендер ────────────────────────────────────────────────────────────────
 
    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
 
        int ox = (width - W) / 2;
        int oy = (height - H) / 2;
 
        // Основная панель
        g.fill(ox, oy, ox + W, oy + H, C_BG);
        drawBorder(g, ox, oy, W, H, C_BORDER);
 
        // Заголовок
        g.fill(ox, oy, ox + W, oy + 22, 0xBB161622);
        drawBorder(g, ox, oy, W, 22, 0xFF333355);
        g.drawCenteredString(font, "Редактор квестов", ox + W / 2, oy + 7, C_HEAD);
 
        // Левая панель
        g.fill(ox, oy + 22, ox + LW, oy + H, C_PANEL);
        drawBorder(g, ox, oy + 22, LW, H - 22, 0xFF333344);
        g.drawString(font, "КВЕСТЫ", ox + 6, oy + 26, C_SEC, false);
 
        // Список квестов
        int listY = oy + 38;
        int maxRows = (H - 68) / 12;
        for (int i = listScroll; i < Math.min(quests.size(), listScroll + maxRows); i++) {
            int rowY = listY + (i - listScroll) * 12;
            boolean sel = i == selectedIdx;
            if (sel) g.fill(ox + 2, rowY - 1, ox + LW - 2, rowY + 11, 0x55FFFFFF);
            String title = quests.get(i).title;
            if (font.width(title) > LW - 12) title = title.substring(0, 10) + "..";
            g.drawString(font, (sel ? "> " : "  ") + title, ox + 4, rowY, sel ? 0xFFFFFF : 0xAAAAAA, false);
        }
 
        // Правая панель — подписи полей
        int rx = ox + LW + 8;
        int fy = oy + 28;
        for (String label : new String[]{"Название:", "Описание:", "NPC:", "Награда:",
                                          "Цель 1:", "Цель 2:", "Цель 3:", "Цель 4:"}) {
            g.drawString(font, label, rx, fy, 0xFF888888, false);
            fy += label.startsWith("Цель") ? 22 : 24;
        }
 
        if (editing == null) {
            g.drawCenteredString(font, "Выберите квест или создайте новый",
                                 ox + LW + (W - LW) / 2, oy + H / 2, 0xFF555566);
        }
 
        super.render(g, mx, my, pt);
    }
 
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 0) {
            int ox = (width - W) / 2;
            int oy = (height - H) / 2;
            int listY = oy + 38;
            int maxRows = (H - 68) / 12;
 
            for (int i = listScroll; i < Math.min(quests.size(), listScroll + maxRows); i++) {
                int rowY = listY + (i - listScroll) * 12;
                if (mx >= ox + 2 && mx < ox + LW - 2 && my >= rowY - 1 && my < rowY + 11) {
                    selectQuest(i);
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, btn);
    }
 
    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        int ox = (width - W) / 2;
        int oy = (height - H) / 2;
        if (mx >= ox && mx < ox + LW) {
            listScroll = Math.max(0, Math.min(quests.size() - 1, listScroll - (int) delta));
            return true;
        }
        return super.mouseScrolled(mx, my, delta);
    }
 
    // ── Utils ─────────────────────────────────────────────────────────────────
 
    private static void drawBorder(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x,         y,         x + w, y + 1,     c);
        g.fill(x,         y + h - 1, x + w, y + h,     c);
        g.fill(x,         y,         x + 1, y + h,     c);
        g.fill(x + w - 1, y,         x + w, y + h,     c);
    }
}

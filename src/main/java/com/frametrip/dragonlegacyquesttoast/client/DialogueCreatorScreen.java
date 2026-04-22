package com.frametrip.dragonlegacyquesttoast.client;
 
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SaveDialoguePacket;
import com.frametrip.dragonlegacyquesttoast.server.DialogueDefinition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
 
import java.util.ArrayList;
import java.util.List;
 
public class DialogueCreatorScreen extends Screen {
 
    private static final int W  = 460;
    private static final int H  = 300;
    private static final int LW = 130;
    private static final int MAX_LINES = 7;
 
    private final Screen parent;
 
    private List<DialogueDefinition> dialogues = new ArrayList<>();
    private int selectedIdx = -1;
    private int listScroll  = 0;
 
    private DialogueDefinition editing = null;
 
    private EditBox fNpcName;
    private EditBox[] fLines = new EditBox[MAX_LINES];
 
    public DialogueCreatorScreen(Screen parent) {
        super(Component.literal("Редактор диалогов"));
        this.parent = parent;
    }
 
    @Override
    protected void init() {
        super.init();
        dialogues = ClientDialogueState.getAll();
 
        int ox = (width - W) / 2;
        int oy = (height - H) / 2;
        int rx = ox + LW + 8;
        int rw = W - LW - 20;
 
        addRenderableWidget(Button.builder(Component.literal("+ Новый"), b -> createNew())
            .bounds(ox + 4, oy + H - 28, LW - 8, 20).build());
 
        fNpcName = new EditBox(font, rx, oy + 36, rw, 18, Component.literal("Имя NPC"));
        fNpcName.setHint(Component.literal("Имя NPC").withStyle(s -> s.withColor(0xFF777777)));
        fNpcName.setMaxLength(64);
        addRenderableWidget(fNpcName);
 
        int fy = oy + 62;
        for (int i = 0; i < MAX_LINES; i++) {
            fLines[i] = new EditBox(font, rx, fy, rw, 18, Component.literal("Строка " + (i + 1)));
            fLines[i].setHint(Component.literal("Фраза " + (i + 1)).withStyle(s -> s.withColor(0xFF555566)));
            fLines[i].setMaxLength(512);
            addRenderableWidget(fLines[i]);
            fy += 22;
        }
 
        int bY = oy + H - 28;
        addRenderableWidget(Button.builder(Component.literal("Сохранить"), b -> save())
            .bounds(rx, bY, 96, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Удалить"), b -> delete())
            .bounds(rx + 100, bY, 80, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Назад"), b -> {
            if (minecraft != null) minecraft.setScreen(parent);
        }).bounds(ox + W - 66, bY, 60, 20).build());
 
        loadFields();
    }
 
    private void loadFields() {
        if (editing == null) {
            if (fNpcName != null) fNpcName.setValue("");
            for (EditBox f : fLines) if (f != null) f.setValue("");
            return;
        }
        fNpcName.setValue(editing.npcName);
        for (int i = 0; i < MAX_LINES; i++) {
            fLines[i].setValue(i < editing.lines.size() ? editing.lines.get(i) : "");
        }
    }
 
    private void pullFields() {
        if (editing == null) return;
        editing.npcName = fNpcName.getValue();
        editing.lines.clear();
        for (EditBox f : fLines) {
            String v = f.getValue();
            if (!v.isBlank()) editing.lines.add(v);
        }
    }
 
    private void createNew() {
        pullFields();
        editing = new DialogueDefinition();
        selectedIdx = -1;
        rebuildScreen();
    }
 
    private void save() {
        if (editing == null) return;
        pullFields();
        if (editing.npcName.isBlank()) return;
        ModNetwork.CHANNEL.sendToServer(new SaveDialoguePacket(editing, false));
        ClientDialogueState.sync(mergeIntoList(dialogues, editing));
        dialogues = ClientDialogueState.getAll();
        rebuildScreen();
    }
 
    private void delete() {
        if (editing == null) return;
        ModNetwork.CHANNEL.sendToServer(new SaveDialoguePacket(editing, true));
        dialogues.removeIf(d -> d.id.equals(editing.id));
        ClientDialogueState.sync(dialogues);
        editing = null;
        selectedIdx = -1;
        rebuildScreen();
    }
 
    private void selectDialogue(int idx) {
        pullFields();
        selectedIdx = idx;
        editing = idx >= 0 && idx < dialogues.size() ? dialogues.get(idx).copy() : null;
        rebuildScreen();
    }
 
    private void rebuildScreen() { clearWidgets(); init(); }
 
    private static List<DialogueDefinition> mergeIntoList(List<DialogueDefinition> list, DialogueDefinition d) {
        List<DialogueDefinition> result = new ArrayList<>();
        boolean found = false;
        for (DialogueDefinition e : list) {
            if (e.id.equals(d.id)) { result.add(d); found = true; } else result.add(e);
        }
        if (!found) result.add(d);
        return result;
    }
 
    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { if (minecraft != null) minecraft.setScreen(parent); }
 
    // ── Рендер ────────────────────────────────────────────────────────────────
 
    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
 
        int ox = (width - W) / 2;
        int oy = (height - H) / 2;
 
        g.fill(ox, oy, ox + W, oy + H, 0xDD0E0E18);
        drawBorder(g, ox, oy, W, H, 0xFF444455);
 
        // Заголовок
        g.fill(ox, oy, ox + W, oy + 22, 0xBB161622);
        drawBorder(g, ox, oy, W, 22, 0xFF333355);
        g.drawCenteredString(font, "Редактор диалогов NPC", ox + W / 2, oy + 7, 0xFFE6D7B5);
 
        // Левая панель
        g.fill(ox, oy + 22, ox + LW, oy + H, 0xAA1A1A2A);
        drawBorder(g, ox, oy + 22, LW, H - 22, 0xFF333344);
        g.drawString(font, "ДИАЛОГИ", ox + 6, oy + 26, 0xFF888877, false);
 
        int listY = oy + 38;
        int maxRows = (H - 68) / 12;
        for (int i = listScroll; i < Math.min(dialogues.size(), listScroll + maxRows); i++) {
            int rowY = listY + (i - listScroll) * 12;
            boolean sel = i == selectedIdx;
            if (sel) g.fill(ox + 2, rowY - 1, ox + LW - 2, rowY + 11, 0x55FFFFFF);
            String name = dialogues.get(i).npcName;
            if (font.width(name) > LW - 14) name = name.substring(0, 9) + "..";
            g.drawString(font, (sel ? "> " : "  ") + name, ox + 4, rowY, sel ? 0xFFFFFF : 0xAAAAAA, false);
        }
 
        // Подписи правой панели
        int rx = ox + LW + 8;
        g.drawString(font, "Имя NPC:", rx, oy + 28, 0xFF888888, false);
        g.drawString(font, "Фразы:", rx, oy + 56, 0xFF888888, false);
 
        if (editing == null) {
            g.drawCenteredString(font, "Выберите диалог или создайте новый",
                                 ox + LW + (W - LW) / 2, oy + H / 2, 0xFF555566);
        } else {
            g.drawString(font, "ID: " + editing.id, rx, oy + 288 - H + oy + H - 42, 0xFF444455, false);
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
            for (int i = listScroll; i < Math.min(dialogues.size(), listScroll + maxRows); i++) {
                int rowY = listY + (i - listScroll) * 12;
                if (mx >= ox + 2 && mx < ox + LW - 2 && my >= rowY - 1 && my < rowY + 11) {
                    selectDialogue(i);
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, btn);
    }
 
    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        int ox = (width - W) / 2;
        if (mx >= ox && mx < ox + LW) {
            listScroll = Math.max(0, Math.min(Math.max(0, dialogues.size() - 1), listScroll - (int) delta));
            return true;
        }
        return super.mouseScrolled(mx, my, delta);
    }
 
    private static void drawBorder(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x,         y,         x + w, y + 1,     c);
        g.fill(x,         y + h - 1, x + w, y + h,     c);
        g.fill(x,         y,         x + 1, y + h,     c);
        g.fill(x + w - 1, y,         x + w, y + h,     c);
    }
}

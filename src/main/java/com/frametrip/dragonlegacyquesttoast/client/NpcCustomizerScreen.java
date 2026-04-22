package com.frametrip.dragonlegacyquesttoast.client;
 
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SaveNpcProfilePacket;
import com.frametrip.dragonlegacyquesttoast.server.DialogueDefinition;
import com.frametrip.dragonlegacyquesttoast.server.NpcProfile;
import com.frametrip.dragonlegacyquesttoast.server.QuestDefinition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
 
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
 
public class NpcCustomizerScreen extends Screen {
 
    private static final int W  = 470;
    private static final int H  = 310;
    private static final int LW = 130;
 
    private final Screen parent;
 
    private List<NpcProfile> profiles = new ArrayList<>();
    private int selectedIdx = -1;
    private int listScroll  = 0;
 
    private NpcProfile editing = null;
 
    private EditBox fName;
 
    public NpcCustomizerScreen(Screen parent) {
        super(Component.literal("Персонажи NPC"));
        this.parent = parent;
    }
 
    @Override
    protected void init() {
        super.init();
        profiles = ClientNpcProfileState.getAll();
 
        int ox = (width - W) / 2;
        int oy = (height - H) / 2;
        int rx = ox + LW + 8;
        int rw = W - LW - 20;
 
        addRenderableWidget(Button.builder(Component.literal("+ Новый"), b -> createNew())
            .bounds(ox + 4, oy + H - 28, LW - 8, 20).build());
 
        // Поле имени
        fName = new EditBox(font, rx, oy + 36, rw, 18, Component.literal("Имя персонажа"));
        fName.setHint(Component.literal("Имя персонажа").withStyle(s -> s.withColor(0xFF777777)));
        fName.setMaxLength(64);
        addRenderableWidget(fName);
 
        // Кнопки изменения частей тела
        if (editing != null) {
            int partY = oy + 60;
            int partLabelW = 80;
            for (Map.Entry<String, String[]> entry : NpcProfile.PART_OPTIONS.entrySet()) {
                String key = entry.getKey();
                String label = NpcProfile.PART_LABELS.getOrDefault(key, key);
                final String partKey = key;
                addRenderableWidget(Button.builder(Component.literal("<"), b -> {
                    editing.cyclePart(partKey, -1);
                    rebuildScreen();
                }).bounds(rx + partLabelW, partY, 18, 16).build());
                addRenderableWidget(Button.builder(Component.literal(">"), b -> {
                    editing.cyclePart(partKey, 1);
                    rebuildScreen();
                }).bounds(rx + partLabelW + 60, partY, 18, 16).build());
                partY += 20;
            }
 
            // Диалог
            List<DialogueDefinition> dialogues = ClientDialogueState.getAll();
            int dY = partY + 4;
            String curDlg = editing.dialogueId.isEmpty() ? "—" : editing.dialogueId;
            addRenderableWidget(Button.builder(Component.literal("Диал: " + curDlg), b -> cycleDialogue(dialogues))
                .bounds(rx, dY, rw, 18).build());
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
        if (fName != null) fName.setValue(editing != null ? editing.displayName : "");
    }
 
    private void pullFields() {
        if (editing == null || fName == null) return;
        editing.displayName = fName.getValue();
    }
 
    private void createNew() {
        pullFields();
        editing = new NpcProfile();
        selectedIdx = -1;
        rebuildScreen();
    }
 
    private void save() {
        if (editing == null) return;
        pullFields();
        if (editing.displayName.isBlank()) return;
        ModNetwork.CHANNEL.sendToServer(new SaveNpcProfilePacket(editing, false));
        ClientNpcProfileState.sync(mergeIntoList(profiles, editing));
        profiles = ClientNpcProfileState.getAll();
        rebuildScreen();
    }
 
    private void delete() {
        if (editing == null) return;
        ModNetwork.CHANNEL.sendToServer(new SaveNpcProfilePacket(editing, true));
        profiles.removeIf(p -> p.id.equals(editing.id));
        ClientNpcProfileState.sync(profiles);
        editing = null;
        selectedIdx = -1;
        rebuildScreen();
    }
 
    private void cycleDialogue(List<DialogueDefinition> dialogues) {
        if (editing == null || dialogues.isEmpty()) return;
        String cur = editing.dialogueId;
        int idx = -1;
        for (int i = 0; i < dialogues.size(); i++) {
            if (dialogues.get(i).id.equals(cur)) { idx = i; break; }
        }
        editing.dialogueId = idx < 0 || idx >= dialogues.size() - 1
            ? (dialogues.isEmpty() ? "" : dialogues.get(0).id)
            : dialogues.get(idx + 1).id;
        rebuildScreen();
    }
 
    private void selectProfile(int idx) {
        pullFields();
        selectedIdx = idx;
        editing = idx >= 0 && idx < profiles.size() ? profiles.get(idx).copy() : null;
        rebuildScreen();
    }
 
    private void rebuildScreen() { clearWidgets(); init(); }
 
    private static List<NpcProfile> mergeIntoList(List<NpcProfile> list, NpcProfile p) {
        List<NpcProfile> result = new ArrayList<>();
        boolean found = false;
        for (NpcProfile e : list) {
            if (e.id.equals(p.id)) { result.add(p); found = true; } else result.add(e);
        }
        if (!found) result.add(p);
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
        g.drawCenteredString(font, "Персонажи NPC", ox + W / 2, oy + 7, 0xFFE6D7B5);
 
        // Левая панель
        g.fill(ox, oy + 22, ox + LW, oy + H, 0xAA1A1A2A);
        drawBorder(g, ox, oy + 22, LW, H - 22, 0xFF333344);
        g.drawString(font, "ПРОФИЛИ", ox + 6, oy + 26, 0xFF888877, false);
 
        int listY = oy + 38;
        int maxRows = (H - 68) / 12;
        for (int i = listScroll; i < Math.min(profiles.size(), listScroll + maxRows); i++) {
            int rowY = listY + (i - listScroll) * 12;
            boolean sel = i == selectedIdx;
            if (sel) g.fill(ox + 2, rowY - 1, ox + LW - 2, rowY + 11, 0x55FFFFFF);
            String name = profiles.get(i).displayName;
            if (font.width(name) > LW - 14) name = name.substring(0, 9) + "..";
            g.drawString(font, (sel ? "> " : "  ") + name, ox + 4, rowY, sel ? 0xFFFFFF : 0xAAAAAA, false);
        }
 
        // Правая панель
        int rx = ox + LW + 8;
        g.drawString(font, "Имя:", rx, oy + 28, 0xFF888888, false);
 
        if (editing != null) {
            g.drawString(font, "Внешность:", rx, oy + 58, 0xFF888877, false);
            int partY = oy + 60;
            int labelW = 80;
            for (Map.Entry<String, String[]> entry : NpcProfile.PART_OPTIONS.entrySet()) {
                String key = entry.getKey();
                String partLabel = NpcProfile.PART_LABELS.getOrDefault(key, key);
                String val = editing.getPartLabel(key);
                g.drawString(font, partLabel + ":", rx, partY + 3, 0xFF777788, false);
                // Значение между кнопками < и >
                g.drawCenteredString(font, val, rx + labelW + 29, partY + 3, 0xFFCCCCCC);
                partY += 20;
            }
        } else {
            g.drawCenteredString(font, "Выберите профиль или создайте новый",
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
            for (int i = listScroll; i < Math.min(profiles.size(), listScroll + maxRows); i++) {
                int rowY = listY + (i - listScroll) * 12;
                if (mx >= ox + 2 && mx < ox + LW - 2 && my >= rowY - 1 && my < rowY + 11) {
                    selectProfile(i);
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
            listScroll = Math.max(0, Math.min(Math.max(0, profiles.size() - 1), listScroll - (int) delta));
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

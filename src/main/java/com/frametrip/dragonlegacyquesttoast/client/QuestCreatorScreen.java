package com.frametrip.dragonlegacyquesttoast.client;
 
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SaveQuestPacket;
import com.frametrip.dragonlegacyquesttoast.server.NpcProfile;
import com.frametrip.dragonlegacyquesttoast.server.QuestDefinition;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
 
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
 
public class QuestCreatorScreen extends Screen {

    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int W = 560, H = 400, LW = 140;
    private static final int FIELD_H = 18;
 
    // Basic tab — Y offsets from oy
    private static final int TY_TTL_LBL  = 44;
    private static final int TY_TTL_EDIT = 55;
    private static final int TY_TTL_STY  = 75;
    private static final int TY_DSC_LBL  = 92;
    private static final int TY_DSC_EDIT = 103;
    private static final int TY_DSC_STY  = 123;
    private static final int TY_RWD_LBL  = 140;
    private static final int TY_RWD_EDIT = 151;
    private static final int TY_NPC_ROW  = 175;
    private static final int TY_TYP_ROW  = 198;
 
    // Logic tab — Y offsets from oy
    private static final int TL_TYPE_ROW = 44;
    private static final int TL_FIELD_Y0 = 68;
    private static final int TL_FIELD_H  = 22;
    private static final int TL_LABEL_W  = 220;
 
    // Style bar (relative X from rx)
    private static final int SB_LT = 0,  SB_GT = 44, SB_CX = 62;
    private static final int SB_H  = 12, SW    = 12;
 
    // Colour palette
    private static final int[] COLORS = {
        0xFFFFFF, 0xFFFF55, 0xFFAA00, 0xFF5555,
        0x55FF55, 0x55FFFF, 0x5555FF, 0xAA55FF,
        0xAAAAAA, 0x555555
    };
    private static final int[] FONT_SIZES = {6, 7, 8, 9, 10, 12, 14};
 
    // ── State ─────────────────────────────────────────────────────────────────
    private final Screen parent;
    private List<QuestDefinition> quests = new ArrayList<>();
    private int selectedIdx = -1, listScroll = 0;
    private QuestDefinition editing = null;
 
    private boolean tabBasic         = true;
    private boolean npcDropdownOpen  = false;
    private boolean logicDropdownOpen= false;
 
    private EditBox fTitle, fDesc, fReward;
    private final Map<String, EditBox> logicBoxes = new LinkedHashMap<>();
 
    public QuestCreatorScreen(Screen parent) {
        super(Component.literal("Редактор квестов"));
        this.parent = parent;
    }

 // ── Init ──────────────────────────────────────────────────────────────────
    @Override
    protected void init() {
        super.init();
        quests = ClientQuestState.getAll();
        logicBoxes.clear();
        fTitle = fDesc = fReward = null;
 
        int ox = ox(), oy = oy(), rx = rx(), rw = rw();
 
        btn("+ Новый",    ox + 4,       oy + H - 28, LW - 8, 20, this::createNew);
        btn("Сохранить",  rx,           oy + H - 28, 96,     20, this::save);
        btn("Удалить",    rx + 100,     oy + H - 28, 80,     20, this::delete);
        btn("Назад",      ox + W - 66,  oy + H - 28, 60,     20,
            () -> { if (minecraft != null) minecraft.setScreen(parent); });
 
        if (editing == null) return;
 
        if (tabBasic) {
            int fw = rw - 68;
            fTitle  = eb(rx,      oy + TY_TTL_EDIT, fw,  "Название квеста");
            fDesc   = eb(rx,      oy + TY_DSC_EDIT, fw,  "Описание квеста");
            fReward = eb(rx,      oy + TY_RWD_EDIT, rw,  "Текст награды");
        } else {
            String[][] fields = QuestDefinition.LOGIC_FIELDS
                .getOrDefault(editing.questLogicType, new String[0][]);
            int fy = oy + TL_FIELD_Y0;
            for (String[] f : fields) {
                logicBoxes.put(f[0], eb(rx + TL_LABEL_W + 4, fy, rw - TL_LABEL_W - 4, f[1]));
                fy += TL_FIELD_H;
            }
        }
        loadFields();
    }
 
    private void btn(String label, int x, int y, int w, int h, Runnable r) {
        addRenderableWidget(net.minecraft.client.gui.components.Button
            .builder(Component.literal(label), b -> r.run()).bounds(x, y, w, h).build());
    }
 
    private EditBox eb(int x, int y, int w, String hint) {
        EditBox b = new EditBox(font, x, y, w, FIELD_H, Component.literal(hint));
        b.setHint(Component.literal(hint).withStyle(s -> s.withColor(0xFF777777)));
        b.setMaxLength(256);
        addRenderableWidget(b);
        return b;
    }
 
    // ── Field I/O ─────────────────────────────────────────────────────────────
    private void loadFields() {
         if (editing == null) return;
        if (tabBasic) {
            if (fTitle  != null) fTitle .setValue(editing.title       != null ? editing.title       : "");
            if (fDesc   != null) fDesc  .setValue(editing.description != null ? editing.description : "");
            if (fReward != null) fReward.setValue(editing.rewardText  != null ? editing.rewardText  : "");
        } else {
            logicBoxes.forEach((k, v) -> v.setValue(editing.logicData.getOrDefault(k, "")));
        }
    }
 
    private void pullFields() {
        if (editing == null) return;
         if (tabBasic) {
            if (fTitle  != null) editing.title       = fTitle .getValue();
            if (fDesc   != null) editing.description = fDesc  .getValue();
            if (fReward != null) editing.rewardText  = fReward.getValue();
        } else {
            logicBoxes.forEach((k, v) -> editing.logicData.put(k, v.getValue()));
        }
    }
 
    // ── Actions ───────────────────────────────────────────────────────────────
    private void createNew()  { pullFields(); editing = new QuestDefinition(); selectedIdx = -1; rebuildScreen(); }
 
    private void save() {
        if (editing == null) return;
        pullFields();
        if (editing.title == null || editing.title.isBlank()) return;
        ModNetwork.CHANNEL.sendToServer(new SaveQuestPacket(editing, false));
        ClientQuestState.sync(merge(quests, editing));
        quests = ClientQuestState.getAll();
        rebuildScreen();
    }
 
    private void delete() {
        if (editing == null) return;
        ModNetwork.CHANNEL.sendToServer(new SaveQuestPacket(editing, true));
        quests.removeIf(q -> q.id.equals(editing.id));
        ClientQuestState.sync(quests);
         editing = null; selectedIdx = -1;
        rebuildScreen();
    }
 
    private void selectQuest(int idx) {
        pullFields();
        selectedIdx = idx;
        editing = (idx >= 0 && idx < quests.size()) ? quests.get(idx).copy() : null;
        rebuildScreen();
    }
 
    private void rebuildScreen() { clearWidgets(); init(); }
 
    private static List<QuestDefinition> merge(List<QuestDefinition> list, QuestDefinition q) {
        List<QuestDefinition> r = new ArrayList<>(); boolean found = false;
        for (QuestDefinition e : list) { if (e.id.equals(q.id)) { r.add(q); found = true; } else r.add(e); }
        if (!found) r.add(q);
        return r;
    }
 
    // ── Render ────────────────────────────────────────────────────────────────
    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int ox = ox(), oy = oy(), rx = rx(), rw = rw();
 
        g.fill(ox, oy, ox + W, oy + H, 0xDD0E0E18);
        brd(g, ox, oy, W, H, 0xFF444455);
 
        // Header
        g.fill(ox, oy, ox + W, oy + 22, 0xBB161622);
        brd(g, ox, oy, W, 22, 0xFF333355);
        g.drawCenteredString(font, "Редактор квестов", ox + W / 2, oy + 7, 0xFFE6D7B5);
 
        // Left panel
        g.fill(ox, oy + 22, ox + LW, oy + H, 0xAA1A1A2A);
        brd(g, ox, oy + 22, LW, H - 22, 0xFF333344);
        g.drawString(font, "КВЕСТЫ", ox + 6, oy + 26, 0xFF888877, false);
 
        int listY = oy + 38, maxR = (H - 68) / 12;
        for (int i = listScroll; i < Math.min(quests.size(), listScroll + maxR); i++) {
            int ry = listY + (i - listScroll) * 12;
            boolean sel = (i == selectedIdx);
            if (sel) g.fill(ox + 2, ry - 1, ox + LW - 2, ry + 11, 0x55FFFFFF);
            String t = quests.get(i).title;
            if (t == null) t = "—";
            if (font.width(t) > LW - 12) t = t.substring(0, Math.min(t.length(), 9)) + "..";
            g.drawString(font, (sel ? "> " : "  ") + t, ox + 4, ry, sel ? 0xFFFFFF : 0xAAAAAA, false);
        }
 
        // Right side
        if (editing == null) {
            g.drawCenteredString(font, "Выберите квест или создайте новый",
            ox + LW + (W - LW) / 2, oy + H / 2, 0xFF555566);
        } else {
            // Tab buttons
            tabBtn(g, mx, my, rx,       oy + 22, 90,  18, "Основное",      tabBasic);
            tabBtn(g, mx, my, rx + 94,  oy + 22, 120, 18, "Логика квеста", !tabBasic);
 
            if (tabBasic) renderBasic(g, mx, my, oy, rx, rw);
            else          renderLogic(g, mx, my, oy, rx, rw);
        }
 
        super.render(g, mx, my, pt); // widgets
 
        // Dropdowns on top
        if (npcDropdownOpen)   drawNpcDrop  (g, mx, my, oy, rx, rw);
        if (logicDropdownOpen) drawLogicDrop(g, mx, my, oy, rx, rw);
    }
 
    private void tabBtn(GuiGraphics g, int mx, int my,
                        int x, int y, int w, int h, String lbl, boolean active) {
        boolean hov = in(mx, my, x, y, w, h);
        g.fill(x, y, x + w, y + h, active ? 0xBB1A1A3A : (hov ? 0x55111122 : 0x33111122));
        brd(g, x, y, w, h, active ? 0xFF5555BB : 0xFF333344);
        g.drawCenteredString(font, lbl, x + w / 2, y + 5, active ? 0xFFE6D7B5 : 0xFF777777);
    }
 
    private void renderBasic(GuiGraphics g, int mx, int my, int oy, int rx, int rw) {
        int fw = rw - 68, biuX = rx + fw + 2;
 
        // Title
        g.drawString(font, "Название:", rx, oy + TY_TTL_LBL, 0xFF888888, false);
        drawBIU(g, mx, my, biuX, oy + TY_TTL_EDIT, editing.titleBold, editing.titleItalic, editing.titleUnderline);
        drawStyleBar(g, mx, my, rx, oy + TY_TTL_STY, editing.titleFontSize, editing.titleColor);
 
        // Desc
        g.drawString(font, "Описание:", rx, oy + TY_DSC_LBL, 0xFF888888, false);
        drawBIU(g, mx, my, biuX, oy + TY_DSC_EDIT, editing.descBold, editing.descItalic, editing.descUnderline);
        drawStyleBar(g, mx, my, rx, oy + TY_DSC_STY, editing.descFontSize, editing.descColor);
 
        // Reward
        g.drawString(font, "Награда:", rx, oy + TY_RWD_LBL, 0xFF888888, false);
 
        // NPC
        g.drawString(font, "Дает квест:", rx, oy + TY_NPC_ROW + 4, 0xFF888888, false);
        dropBtn(g, mx, my, rx + 82, oy + TY_NPC_ROW, rw - 82, 18,
                npcName(editing.giverNpcId), npcDropdownOpen);
 
        // Quest type
        g.drawString(font, "Тип квеста:", rx, oy + TY_TYP_ROW + 4, 0xFF888888, false);
        dropBtn(g, mx, my, rx + 82, oy + TY_TYP_ROW, 120, 18, editing.typeLabel(), false);
    }
 
    private void renderLogic(GuiGraphics g, int mx, int my, int oy, int rx, int rw) {
        g.drawString(font, "Логика квеста:", rx, oy + TL_TYPE_ROW + 4, 0xFF888888, false);
        dropBtn(g, mx, my, rx + 106, oy + TL_TYPE_ROW, rw - 106, 18,
                editing.logicLabel(), logicDropdownOpen);
 
        String[][] fields = QuestDefinition.LOGIC_FIELDS
            .getOrDefault(editing.questLogicType, new String[0][]);
        int fy = oy + TL_FIELD_Y0;
        for (String[] f : fields) {
            String lbl = f[1];
            if (font.width(lbl) > TL_LABEL_W - 4)
                lbl = lbl.substring(0, Math.min(lbl.length(), 22)) + "..";
            g.drawString(font, lbl + ":", rx, fy + 4, 0xFF888877, false);
            fy += TL_FIELD_H;
        }
    }
 
    // B / I / U toggle buttons
    private void drawBIU(GuiGraphics g, int mx, int my,
                          int x, int y, boolean b, boolean i, boolean u) {
        String[] lbl = {"B", "I", "U"};
        boolean[] on = {b, i, u};
        for (int k = 0; k < 3; k++) {
            int bx = x + k * 20;
            boolean hov = in(mx, my, bx, y, 18, 18), act = on[k];
            g.fill(bx, y, bx + 18, y + 18, act ? 0xFF1E1E44 : 0xFF111122);
            brd(g, bx, y, 18, 18, act ? 0xFF6666DD : (hov ? 0xFF555577 : 0xFF333344));
            g.drawCenteredString(font, lbl[k], bx + 9, y + 5, act ? 0xFF9999FF : 0xFF777777);
        }
    }
 
    // Font-size + colour swatch bar
    private void drawStyleBar(GuiGraphics g, int mx, int my,
                               int rx, int y, int sz, int col) {
        boolean lh = in(mx, my, rx + SB_LT, y, 14, SB_H);
        g.fill(rx + SB_LT, y, rx + SB_LT + 14, y + SB_H, lh ? 0xFF222244 : 0xFF111122);
        brd(g, rx + SB_LT, y, 14, SB_H, 0xFF334455);
        g.drawCenteredString(font, "<", rx + SB_LT + 7, y + 2, 0xFFAAAAAA);
 
        g.drawString(font, sz + "px", rx + SB_LT + 17, y + 2, 0xFFCCCCCC, false);
 
        boolean rh = in(mx, my, rx + SB_GT, y, 14, SB_H);
        g.fill(rx + SB_GT, y, rx + SB_GT + 14, y + SB_H, rh ? 0xFF222244 : 0xFF111122);
        brd(g, rx + SB_GT, y, 14, SB_H, 0xFF334455);
        g.drawCenteredString(font, ">", rx + SB_GT + 7, y + 2, 0xFFAAAAAA);
 
        for (int i = 0; i < COLORS.length; i++) {
            int cx = rx + SB_CX + i * 14;
            boolean sel = (col == COLORS[i]);
            g.fill(cx, y, cx + SW, y + SB_H, 0xFF000000 | COLORS[i]);
            if (sel)                              brd(g, cx - 1, y - 1, SW + 2, SB_H + 2, 0xFFFFFFFF);
            else if (in(mx, my, cx, y, SW, SB_H)) brd(g, cx,     y,     SW,     SB_H,     0xFFAAAAAA);
        }
    }
 
    private void dropBtn(GuiGraphics g, int mx, int my,
                          int x, int y, int w, int h, String lbl, boolean open) {
        boolean hov = in(mx, my, x, y, w, h);
        g.fill(x, y, x + w, y + h, open ? 0xFF1E1E44 : (hov ? 0xFF1A1A33 : 0xFF111122));
        brd(g, x, y, w, h, open ? 0xFF5555BB : (hov ? 0xFF444466 : 0xFF333344));
        if (font.width(lbl) > w - 18) lbl = lbl.substring(0, Math.max(0, lbl.length() - 4)) + "...";
        g.drawString(font, lbl, x + 4, y + (h - 8) / 2, 0xFFCCCCCC, false);
        g.drawString(font, "▼", x + w - 11, y + (h - 8) / 2, 0xFF888888, false);
    }
 
    // NPC dropdown overlay
    private void drawNpcDrop(GuiGraphics g, int mx, int my, int oy, int rx, int rw) {
        List<NpcProfile> npcs = ClientNpcProfileState.getAll();
        int ddX = rx + 82, ddY = oy + TY_NPC_ROW + 20;
        int ddW = rw - 82,  rows = Math.min(npcs.size() + 1, 8);
        g.fill(ddX, ddY, ddX + ddW, ddY + rows * 14, 0xFF0E0E1E);
        brd(g, ddX, ddY, ddW, rows * 14, 0xFF5555BB);
 
        boolean h0 = in(mx, my, ddX, ddY, ddW, 14);
        if (h0) g.fill(ddX, ddY, ddX + ddW, ddY + 14, 0x44FFFFFF);
        g.drawString(font, "— нет —", ddX + 4, ddY + 3, 0xFF888888, false);
 
        for (int i = 0; i < npcs.size() && i < rows - 1; i++) {
            int iy = ddY + 14 + i * 14;
            if (in(mx, my, ddX, iy, ddW, 14)) g.fill(ddX, iy, ddX + ddW, iy + 14, 0x44FFFFFF);
            boolean sel = npcs.get(i).id.equals(editing.giverNpcId);
            g.drawString(font, (sel ? "● " : "  ") + npcs.get(i).displayName,
                         ddX + 4, iy + 3, sel ? 0xFFE6D7B5 : 0xFFCCCCCC, false);
        }
    }
 
    // Logic-type dropdown overlay
    private void drawLogicDrop(GuiGraphics g, int mx, int my, int oy, int rx, int rw) {
        int ddX = rx + 106, ddY = oy + TL_TYPE_ROW + 20;
        int ddW = rw - 106, ddH = QuestDefinition.LOGIC_IDS.length * 14;
        g.fill(ddX, ddY, ddX + ddW, ddY + ddH, 0xFF0E0E1E);
        brd(g, ddX, ddY, ddW, ddH, 0xFF5555BB);
        for (int i = 0; i < QuestDefinition.LOGIC_IDS.length; i++) {
            int iy = ddY + i * 14;
            if (in(mx, my, ddX, iy, ddW, 14)) g.fill(ddX, iy, ddX + ddW, iy + 14, 0x44FFFFFF);
            boolean sel = QuestDefinition.LOGIC_IDS[i].equals(editing.questLogicType);
            g.drawString(font, (sel ? "● " : "  ") + QuestDefinition.LOGIC_LABELS[i],
                         ddX + 4, iy + 3, sel ? 0xFFE6D7B5 : 0xFFCCCCCC, false);
        }
    }

    // ── Mouse clicks ──────────────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mxd, double myd, int btn) {
        if (btn != 0) return super.mouseClicked(mxd, myd, btn);
        int mx = (int) mxd, my = (int) myd;
        int ox = ox(), oy = oy(), rx = rx(), rw = rw();
 
        // Close dropdowns first
        if (npcDropdownOpen) {
            List<NpcProfile> npcs = ClientNpcProfileState.getAll();
            int ddX = rx + 82, ddY = oy + TY_NPC_ROW + 20, ddW = rw - 82;
            if (in(mx, my, ddX, ddY, ddW, 14)) { editing.giverNpcId = ""; }
            else for (int i = 0; i < npcs.size() && i < 7; i++) {
                int iy = ddY + 14 + i * 14;
                if (in(mx, my, ddX, iy, ddW, 14)) { editing.giverNpcId = npcs.get(i).id; break; }
            }
            npcDropdownOpen = false;
            return true;
        }
        if (logicDropdownOpen) {
            int ddX = rx + 106, ddY = oy + TL_TYPE_ROW + 20, ddW = rw - 106;
            for (int i = 0; i < QuestDefinition.LOGIC_IDS.length; i++) {
                int iy = ddY + i * 14;
                if (in(mx, my, ddX, iy, ddW, 14)) {
                    pullFields();
                    editing.questLogicType = QuestDefinition.LOGIC_IDS[i];
                    editing.logicData.clear();
                    logicDropdownOpen = false;
                    rebuildScreen();
                    return true;
                }
            }
           logicDropdownOpen = false;
            return true;
        }
     
        // Quest list
        int listY = oy + 38, maxR = (H - 68) / 12;
        for (int i = listScroll; i < Math.min(quests.size(), listScroll + maxR); i++) {
            int ry = listY + (i - listScroll) * 12;
            if (in(mx, my, ox + 2, ry - 1, LW - 4, 12)) { selectQuest(i); return true; }
        }
 
        if (editing != null) {
            // Tab buttons
            if (tabBasic  && in(mx, my, rx + 94, oy + 22, 120, 18)) { pullFields(); tabBasic = false; rebuildScreen(); return true; }
            if (!tabBasic && in(mx, my, rx,       oy + 22, 90,  18)) { pullFields(); tabBasic = true;  rebuildScreen(); return true; }
 
            if (tabBasic) clickBasic(mx, my, oy, rx, rw);
            else          clickLogic(mx, my, oy, rx, rw);
        }
 
        return super.mouseClicked(mxd, myd, btn);
    }
    
    private void clickBasic(int mx, int my, int oy, int rx, int rw) {
        int fw = rw - 68, biuX = rx + fw + 2;
 
        // B/I/U – title
        if (in(mx, my, biuX,      oy + TY_TTL_EDIT, 18, 18)) { pullFields(); editing.titleBold      = !editing.titleBold;      return; }
        if (in(mx, my, biuX + 20, oy + TY_TTL_EDIT, 18, 18)) { pullFields(); editing.titleItalic    = !editing.titleItalic;    return; }
        if (in(mx, my, biuX + 40, oy + TY_TTL_EDIT, 18, 18)) { pullFields(); editing.titleUnderline = !editing.titleUnderline; return; }
 
        // Style bar – title
        clickStyleBar(mx, my, rx, oy + TY_TTL_STY, true);
 
        // B/I/U – desc
        if (in(mx, my, biuX,      oy + TY_DSC_EDIT, 18, 18)) { pullFields(); editing.descBold      = !editing.descBold;      return; }
        if (in(mx, my, biuX + 20, oy + TY_DSC_EDIT, 18, 18)) { pullFields(); editing.descItalic    = !editing.descItalic;    return; }
        if (in(mx, my, biuX + 40, oy + TY_DSC_EDIT, 18, 18)) { pullFields(); editing.descUnderline = !editing.descUnderline; return; }
 
        // Style bar – desc
        clickStyleBar(mx, my, rx, oy + TY_DSC_STY, false);
 
        // NPC dropdown
        if (in(mx, my, rx + 82, oy + TY_NPC_ROW, rw - 82, 18)) { npcDropdownOpen = true; return; }
 
        // Quest type cycle
        if (in(mx, my, rx + 82, oy + TY_TYP_ROW, 120, 18)) {
            pullFields(); cycleType(); return;
        }
    }
 
    private void clickStyleBar(int mx, int my, int rx, int barY, boolean forTitle) {
        if (in(mx, my, rx + SB_LT, barY, 14, SB_H)) {
            pullFields();
            if (forTitle) editing.titleFontSize = prevSz(editing.titleFontSize);
            else          editing.descFontSize  = prevSz(editing.descFontSize);
            return;
        }
        if (in(mx, my, rx + SB_GT, barY, 14, SB_H)) {
            pullFields();
            if (forTitle) editing.titleFontSize = nextSz(editing.titleFontSize);
            else          editing.descFontSize  = nextSz(editing.descFontSize);
            return;
        }
        for (int i = 0; i < COLORS.length; i++) {
            if (in(mx, my, rx + SB_CX + i * 14, barY, SW, SB_H)) {
                pullFields();
                if (forTitle) editing.titleColor = COLORS[i];
                else          editing.descColor  = COLORS[i];
                return;
            }
        }
    }
 
    private void clickLogic(int mx, int my, int oy, int rx, int rw) {
        if (in(mx, my, rx + 106, oy + TL_TYPE_ROW, rw - 106, 18)) logicDropdownOpen = true;
    }
 
    private void cycleType() {
        String[] t = QuestDefinition.TYPES;
        int c = 0;
        for (int i = 0; i < t.length; i++) if (t[i].equals(editing.questType)) { c = i; break; }
        editing.questType = t[(c + 1) % t.length];
    }
 
    @Override
    public boolean mouseScrolled(double mx, double my, double d) {
        if (mx >= ox() && mx < ox() + LW)
            listScroll = Math.max(0, Math.min(Math.max(0, quests.size() - 1), listScroll - (int) d));
        return super.mouseScrolled(mx, my, d);
    }
 
    // ── Helpers ───────────────────────────────────────────────────────────────
    private int ox() { return (width  - W) / 2; }
    private int oy() { return (height - H) / 2; }
    private int rx() { return ox() + LW + 8; }
    private int rw() { return W - LW - 20; }
 
    private static boolean in(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
 
    private String npcName(String id) {
        if (id == null || id.isEmpty()) return "— Выберите NPC —";
        for (NpcProfile p : ClientNpcProfileState.getAll())
            if (p.id.equals(id)) return p.displayName;
        return id;
    }
 
    private int prevSz(int cur) {
        for (int i = FONT_SIZES.length - 1; i >= 0; i--) if (FONT_SIZES[i] < cur) return FONT_SIZES[i];
        return FONT_SIZES[0];
    }
 private int nextSz(int cur) {
        for (int sz : FONT_SIZES) if (sz > cur) return sz;
        return FONT_SIZES[FONT_SIZES.length - 1];
    }
 
    private static void brd(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);  g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);  g.fill(x + w - 1, y, x + w, y + h, c);
    }
 
    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { if (minecraft != null) minecraft.setScreen(parent); }
}

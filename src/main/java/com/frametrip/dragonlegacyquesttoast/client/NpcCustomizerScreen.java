package com.frametrip.dragonlegacyquesttoast.client;
 
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SaveNpcProfilePacket;
import com.frametrip.dragonlegacyquesttoast.server.DialogueDefinition;
import com.frametrip.dragonlegacyquesttoast.server.NpcProfile;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
 
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
 
public class NpcCustomizerScreen extends Screen {
 
    // ── Layout ────────────────────────────────────────────────────────────────
    private static final int W  = 540;
    private static final int H  = 340;
    private static final int LW = 140;  // left list panel
    private static final int PW = 110;  // right preview panel width
 
    // Preview figure position (relative to preview panel)
    private static final int FIG_X  = 16;  // figure center X within preview panel
    private static final int FIG_TY = 24;  // figure top Y within preview panel
 
    // Body-part colours per option index (cycled)
    private static final int[] SKIN_TONES   = {0xFFDEB887, 0xFFD2691E, 0xFF8B5C2A, 0xFFFAD5A5};
    private static final int[] HAIR_COLOURS = {0xFF1C1C1C, 0xFF8B4513, 0xFFFFD700, 0xFFCC5500, 0xFFAAAAAA, 0xFFFF69B4};
 
    // ── State ─────────────────────────────────────────────────────────────────
    private final Screen parent;
    private List<NpcProfile> profiles = new ArrayList<>();
    private int selectedIdx = -1, listScroll = 0;
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
 
        int ox = ox(), oy = oy();
        int rx = ox + LW + 8;
        int rw = W - LW - PW - 20;  // centre panel (controls)
 
        // Left: new button
        addRenderableWidget(Button.builder(Component.literal("+ Новый"), b -> createNew())
            .bounds(ox + 4, oy + H - 28, LW - 8, 20).build());
 
         // Name field
        fName = new EditBox(font, rx, oy + 36, rw, 18, Component.literal("Имя персонажа"));
        fName.setHint(Component.literal("Имя персонажа").withStyle(s -> s.withColor(0xFF777777)));
        fName.setMaxLength(64);
        addRenderableWidget(fName);
 
        // Part cycle buttons
        if (editing != null) {
            int partY = oy + 60;
            for (Map.Entry<String, String[]> entry : NpcProfile.PART_OPTIONS.entrySet()) {
                String key = entry.getKey();
                String lbl = NpcProfile.PART_LABELS.getOrDefault(key, key);
                int btnLblW = font.width(lbl + ":") + 4;
 
                addRenderableWidget(Button.builder(Component.literal("◀"), b -> {
                    editing.cyclePart(key, -1); rebuildScreen();
                }).bounds(rx + btnLblW, partY, 16, 14).build());
 
                addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
                    editing.cyclePart(key, 1); rebuildScreen();
                }).bounds(rx + btnLblW + 62, partY, 16, 14).build());
 
                partY += 20;
            }
 
           // Dialogue selector
            List<DialogueDefinition> dialogues = ClientDialogueState.getAll();
            int dY = oy + 60 + NpcProfile.PART_OPTIONS.size() * 20 + 6;
            String dlgLabel = editing.dialogueId.isEmpty() ? "— нет диалога —" : editing.dialogueId;
            addRenderableWidget(Button.builder(Component.literal("Диалог: " + dlgLabel),
                b -> { cycleDialogue(dialogues); rebuildScreen(); })
                .bounds(rx, dY, rw, 16).build());
        }

        //Bottom buttons
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
 
     // ── Actions ───────────────────────────────────────────────────────────────
    private void createNew()  { pullFields(); editing = new NpcProfile(); selectedIdx = -1; rebuildScreen(); }
 
    private void save() {
        if (editing == null) return;
        pullFields();
        if (editing.displayName.isBlank()) return;
        ModNetwork.CHANNEL.sendToServer(new SaveNpcProfilePacket(editing, false));
        ClientNpcProfileState.sync(merge(profiles, editing));
        profiles = ClientNpcProfileState.getAll();
        rebuildScreen();
    }
 
    private void delete() {
        if (editing == null) return;
        ModNetwork.CHANNEL.sendToServer(new SaveNpcProfilePacket(editing, true));
        profiles.removeIf(p -> p.id.equals(editing.id));
        ClientNpcProfileState.sync(profiles);
        editing = null; selectedIdx = -1;
        rebuildScreen();
    }
 
    private void selectProfile(int idx) {
        pullFields();
        selectedIdx = idx;
        editing = (idx >= 0 && idx < profiles.size()) ? profiles.get(idx).copy() : null;
        rebuildScreen();
    }
 
    private void cycleDialogue(List<DialogueDefinition> dialogues) {
        if (editing == null || dialogues.isEmpty()) return;
        int cur = -1;
        for (int i = 0; i < dialogues.size(); i++)
            if (dialogues.get(i).id.equals(editing.dialogueId)) { cur = i; break; }
        editing.dialogueId = (cur < 0 || cur >= dialogues.size() - 1)
            ? "" : dialogues.get(cur + 1).id;
    }
 
    private void rebuildScreen() { clearWidgets(); init(); }
 
    private static List<NpcProfile> merge(List<NpcProfile> list, NpcProfile p) {
        List<NpcProfile> r = new ArrayList<>(); boolean found = false;
        for (NpcProfile e : list) { if (e.id.equals(p.id)) { r.add(p); found = true; } else r.add(e); }
        if (!found) r.add(p);
        return r;
    }
 
    // ── Render ────────────────────────────────────────────────────────────────
    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);

        int ox = ox(), oy = oy();
        int rx = ox + LW + 8;
        int rw = W - LW - PW - 20;
        int px = ox + W - PW - 4;   // preview panel X
 
        // Main panel
        g.fill(ox, oy, ox + W, oy + H, 0xDD0E0E18);
        brd(g, ox, oy, W, H, 0xFF444455);
 
        // Header
        g.fill(ox, oy, ox + W, oy + 22, 0xBB161622);
        brd(g, ox, oy, W, 22, 0xFF333355);
        g.drawCenteredString(font, "Персонажи NPC", ox + W / 2, oy + 7, 0xFFE6D7B5);
 
         // Left list panel
        g.fill(ox, oy + 22, ox + LW, oy + H, 0xAA1A1A2A);
        brd(g, ox, oy + 22, LW, H - 22, 0xFF333344);
        g.drawString(font, "ПРОФИЛИ", ox + 6, oy + 26, 0xFF888877, false);
 
         int listY = oy + 38, maxR = (H - 68) / 12;
        for (int i = listScroll; i < Math.min(profiles.size(), listScroll + maxR); i++) {
            int ry = listY + (i - listScroll) * 12;
            boolean sel = (i == selectedIdx);
            if (sel) g.fill(ox + 2, ry - 1, ox + LW - 2, ry + 11, 0x55FFFFFF);
            String name = profiles.get(i).displayName;
             if (font.width(name) > LW - 14) name = name.substring(0, Math.min(name.length(), 9)) + "..";
            g.drawString(font, (sel ? "> " : "  ") + name, ox + 4, ry, sel ? 0xFFFFFF : 0xAAAAAA, false);
        }
 
        
        // Right preview panel background
        g.fill(px, oy + 22, px + PW + 2, oy + H, 0xAA111122);
        brd(g, px, oy + 22, PW + 2, H - 22, 0xFF333355);
        g.drawCenteredString(font, "Превью", px + (PW + 2) / 2, oy + 26, 0xFF888877);
 
        // Centre panel — part controls
        if (editing == null) {
            g.drawCenteredString(font, "Выберите профиль",
                ox + LW + (W - LW - PW) / 2, oy + H / 2, 0xFF555566);
        } else {
            renderControls(g, rx, oy, rw);
            renderPreview(g, px + 8, oy + 36);
        }
 
        super.render(g, mx, my, pt);
    }
 
    private void renderControls(GuiGraphics g, int rx, int oy, int rw) {
        g.drawString(font, "Имя:", rx, oy + 28, 0xFF888888, false);
 
        if (editing != null) {
             g.drawString(font, "Внешность:", rx, oy + 57, 0xFF888877, false);
            int partY = oy + 60;
            for (Map.Entry<String, String[]> entry : NpcProfile.PART_OPTIONS.entrySet()) {
                String key = entry.getKey();
                String lbl = NpcProfile.PART_LABELS.getOrDefault(key, key);
                String val = editing.getPartLabel(key);
                int lblW = font.width(lbl + ":") + 4;
                g.drawString(font, lbl + ":", rx, partY + 2, 0xFF777788, false);
                g.drawCenteredString(font, val, rx + lblW + 8 + 24, partY + 2, 0xFFCCCCCC);
                partY += 20;
            }
        }
    }
  
    // ── 2D figure preview ─────────────────────────────────────────────────────
    private void renderPreview(GuiGraphics g, int px, int py) {
        if (editing == null) return;
 
        int eyes   = editing.bodyParts.getOrDefault("eyes",     0);
        int mouth  = editing.bodyParts.getOrDefault("mouth",    0);
        int hair   = editing.bodyParts.getOrDefault("hair",     0);
        int rArm   = editing.bodyParts.getOrDefault("rightArm", 0);
        int lArm   = editing.bodyParts.getOrDefault("leftArm",  0);
        int torso  = editing.bodyParts.getOrDefault("torso",    0);
        int rLeg   = editing.bodyParts.getOrDefault("rightLeg", 0);
        int lLeg   = editing.bodyParts.getOrDefault("leftLeg",  0);
 
        int skin  = SKIN_TONES[0];   // constant for simplicity
        int shirt = 0xFF4A80A0;      // blue-ish shirt
        int pants = 0xFF334466;      // dark pants
 
        int torsoW = (torso == 1) ? 22 : (torso == 2) ? 14 : 18;
        int torsoX = px + FIG_X - torsoW / 2;
 
        // Head
        int headX = px + FIG_X - 12, headY = py + FIG_TY;
        g.fill(headX, headY, headX + 24, headY + 22, skin);
        brd(g, headX, headY, 24, 22, 0xFF333333);
 
        // Hair
        if (hair > 0) {
            int hc = HAIR_COLOURS[Math.min(hair - 1, HAIR_COLOURS.length - 1)];
            switch (hair) {
                case 1 -> g.fill(headX + 2,  headY - 3, headX + 22, headY + 2,  hc); // short
                case 2 -> { // long
                    g.fill(headX + 2, headY - 4, headX + 22, headY + 2, hc);
                    g.fill(headX,     headY + 2, headX + 4,  headY + 20, hc);
                    g.fill(headX + 20,headY + 2, headX + 24, headY + 20, hc);
                }
                case 3 -> g.fill(headX + 9,  headY - 6, headX + 15, headY + 2,  hc); // bun
                case 4 -> { // spiky
                    for (int s = 0; s < 4; s++)
                        g.fill(headX + 3 + s * 5, headY - 5 - s % 2 * 2, headX + 6 + s * 5, headY, hc);
                }
                case 5 -> { // curly
                    g.fill(headX + 2,  headY - 3, headX + 22, headY + 2, hc);
                    g.fill(headX - 2,  headY + 2, headX + 4,  headY + 8, hc);
                    g.fill(headX + 20, headY + 2, headX + 26, headY + 8, hc);
                }
            }
        }
 
        // Eyes
        int eyeY = headY + 7;
        switch (eyes) {
            case 0 -> { g.fill(headX+5,  eyeY, headX+8,  eyeY+3, 0xFF333333); g.fill(headX+16, eyeY, headX+19, eyeY+3, 0xFF333333); } // normal
            case 1 -> { g.fill(headX+5,  eyeY, headX+9,  eyeY+2, 0xFF993333); g.fill(headX+15, eyeY, headX+19, eyeY+2, 0xFF993333); } // angry
            case 2 -> { g.fill(headX+5,  eyeY+1, headX+8, eyeY+3, 0xFF555555); g.fill(headX+16, eyeY+1, headX+19, eyeY+3, 0xFF555555); } // sleepy
            case 3 -> { g.fill(headX+4,  eyeY-1, headX+9,  eyeY+4, 0xFF5599CC); g.fill(headX+15, eyeY-1, headX+20, eyeY+4, 0xFF5599CC); } // wide
            case 4 -> { g.fill(headX+5,  eyeY+1, headX+9,  eyeY+2, 0xFF333333); g.fill(headX+15, eyeY+1, headX+19, eyeY+2, 0xFF333333); } // squint
        }
 
        // Mouth
        int mouthY = headY + 15;
        switch (mouth) {
            case 0 -> g.fill(headX+8,  mouthY, headX+16, mouthY+1, 0xFF333333); // neutral
            case 1 -> { g.fill(headX+7, mouthY, headX+9,  mouthY+2, 0xFF333333); g.fill(headX+9, mouthY+2, headX+15, mouthY+3, 0xFF333333); g.fill(headX+15, mouthY, headX+17, mouthY+2, 0xFF333333); } // smile
            case 2 -> { g.fill(headX+7, mouthY+2, headX+9, mouthY+4, 0xFF333333); g.fill(headX+9, mouthY, headX+15, mouthY+1, 0xFF333333); g.fill(headX+15, mouthY+2, headX+17, mouthY+4, 0xFF333333); } // frown
            case 3 -> g.fill(headX+8,  mouthY-1, headX+16, mouthY+3, 0xFF333333); // open
            case 4 -> { g.fill(headX+6, mouthY, headX+18, mouthY+1, 0xFF333333); g.fill(headX+7, mouthY+1, headX+9, mouthY+2, 0xFFFFFFFF); g.fill(headX+15, mouthY+1, headX+17, mouthY+2, 0xFFFFFFFF); } // grin
        }
 
        // Body
        int bodyY = headY + 22;
        g.fill(torsoX, bodyY, torsoX + torsoW, bodyY + 28, shirt);
        brd(g, torsoX, bodyY, torsoW, 28, 0xFF333355);
 
        // Right arm (drawn to the right of body)
        int raX = torsoX + torsoW + 1;
        int raBaseY = bodyY;
        switch (rArm) {
            case 0 -> g.fill(raX, raBaseY,     raX + 7, raBaseY + 24, shirt); // normal
            case 1 -> g.fill(raX, raBaseY - 14, raX + 7, raBaseY + 10, shirt); // raised
            case 2 -> g.fill(raX, raBaseY + 6,  raX + 7, raBaseY + 30, shirt); // lowered
            case 3 -> { g.fill(raX, raBaseY, raX + 7, raBaseY + 12, shirt); g.fill(raX + 3, raBaseY + 10, raX + 14, raBaseY + 20, shirt); } // bent
        }
 
        // Left arm
        int laX = torsoX - 8;
        switch (lArm) {
            case 0 -> g.fill(laX, raBaseY,     laX + 7, raBaseY + 24, shirt);
            case 1 -> g.fill(laX, raBaseY - 14, laX + 7, raBaseY + 10, shirt);
            case 2 -> g.fill(laX, raBaseY + 6,  laX + 7, raBaseY + 30, shirt);
            case 3 -> { g.fill(laX - 7, raBaseY + 10, laX + 7, raBaseY + 20, shirt); g.fill(laX, raBaseY, laX + 7, raBaseY + 12, shirt); }
        }
 
        // Legs
        int legY = bodyY + 28;
        int legW = torsoW / 2 - 1;
        int rLegX = torsoX + torsoW / 2 + 1;
        int lLegX = torsoX;
 
        drawLeg(g, rLegX, legY, legW, rLeg, pants);
        drawLeg(g, lLegX, legY, legW, lLeg, pants);
    }
 
    private void drawLeg(GuiGraphics g, int x, int y, int w, int type, int color) {
        switch (type) {
            case 0 -> g.fill(x, y,     x + w, y + 30, color); // straight
            case 1 -> { g.fill(x, y, x + w, y + 15, color); g.fill(x - 4, y + 15, x + w - 4, y + 30, color); } // forward
            case 2 -> { g.fill(x, y, x + w, y + 15, color); g.fill(x + 4, y + 15, x + w + 4, y + 30, color); } // backward
            case 3 -> { g.fill(x, y, x + w, y + 15, color); g.fill(x - 3, y + 13, x + w - 3, y + 28, color); } // bent
        }
        brd(g, x, y, w, 30, 0xFF222233);
    }
 
    // ── Mouse ─────────────────────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 0) {
            int ox = ox(), oy = oy();
            int listY = oy + 38, maxR = (H - 68) / 12;
            for (int i = listScroll; i < Math.min(profiles.size(), listScroll + maxR); i++) {
                int ry = listY + (i - listScroll) * 12;
                if (mx >= ox + 2 && mx < ox + LW - 2 && my >= ry - 1 && my < ry + 11) {
                    selectProfile(i); return true;
                }
            }
        }
        return super.mouseClicked(mx, my, btn);
    }
 
    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (mx >= ox() && mx < ox() + LW)
            listScroll = Math.max(0, Math.min(Math.max(0, profiles.size() - 1), listScroll - (int) delta));
        return super.mouseScrolled(mx, my, delta);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private int ox() { return (width  - W) / 2; }
    private int oy() { return (height - H) / 2; }
 
    private static void brd(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);  g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);  g.fill(x + w - 1, y, x + w, y + h, c);
    }
 
    @Override public boolean isPauseScreen() { return false; }
    @Override public void onClose() { if (minecraft != null) minecraft.setScreen(parent); }
}

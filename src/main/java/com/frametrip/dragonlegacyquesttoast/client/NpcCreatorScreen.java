package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.client.npceditor.*;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Pose;

import java.util.function.Consumer;

/**
 * Refactored NPC editor screen.
 *
 * Layout:
 *   [Left sidebar: tabs]  [Center: active tab content]  [Right: 3D preview + summary]
 *
 * Top bar shows NPC name, dirty indicator, Save / Reset / Close buttons.
 * Each tab is a separate NpcEditorTab component.
 */
public class NpcCreatorScreen extends Screen {

    // ── Layout constants ──────────────────────────────────────────────────────
    private static final int W         = 740;
    private static final int H         = 470;
    private static final int SIDEBAR_W = 132;
    private static final int PREVIEW_W = 210;
    private static final int CONTENT_W = W - SIDEBAR_W - PREVIEW_W;
    private static final int TOP_H     = 28;
    private static final int BOT_H     = 28;

    // ── Tab definitions ───────────────────────────────────────────────────────
    private static final String[] TAB_LABELS = {
            "  Информация", "  Взаимодействие", "  Анимация", "  Отношения"
    };
    private static final int[] TAB_ACCENT = {
            NpcInfoTab.ACCENT,
            NpcInteractionTab.ACCENT,
            NpcAnimationTab.ACCENT,
            NpcRelationsTab.ACCENT
    };
    private static final NpcEditorTab[] TAB_INSTANCES = {
            new NpcInfoTab(),
            new NpcInteractionTab(),
            new NpcAnimationTab(),
            new NpcRelationsTab()
    };

    // ── State ─────────────────────────────────────────────────────────────────
    private final NpcEditorState editorState;
    private int activeTab = 0;

    public NpcCreatorScreen(NpcEntity entity) {
        super(Component.literal("Настройка NPC"));
        this.editorState = new NpcEditorState(entity);
    }

    // ── Initialization ────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();
        int ox = ox(), oy = oy();
        int rx = ox + SIDEBAR_W + 8;
        int rw = CONTENT_W - 16;

        // Top bar: Save / Reset / Close
        int topBtnY = oy + 5;
        addRenderableWidget(Button.builder(Component.literal("💾 Сохранить"), b -> save())
                .bounds(ox + W - 210, topBtnY, 80, 18).build());
        addRenderableWidget(Button.builder(Component.literal("↺ Сбросить"), b -> {
                    editorState.reset();
                    rebuildWidgets();
                })
                .bounds(ox + W - 126, topBtnY, 70, 18).build());
        addRenderableWidget(Button.builder(Component.literal("✕ Закрыть"),
                b -> onClose()).bounds(ox + W - 52, topBtnY, 46, 18).build());

        // Sidebar: tab buttons
        for (int i = 0; i < TAB_LABELS.length; i++) {
            final int tab = i;
            addRenderableWidget(Button.builder(
                    Component.literal(TAB_LABELS[i]),
                    b -> {
                        TAB_INSTANCES[activeTab].pullFields(editorState);
                        activeTab = tab;
                        rebuildWidgets();
                    }
            ).bounds(ox + 4, oy + TOP_H + 8 + i * 40, SIDEBAR_W - 8, 34).build());
        }

        // Active tab widgets
        Consumer<AbstractWidget> addWidget = this::addRenderableWidget;
        TAB_INSTANCES[activeTab].init(addWidget, this::rebuildWidgets, editorState, rx, oy + TOP_H + 18, rw);
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);

        int ox = ox(), oy = oy();
        int rx = ox + SIDEBAR_W + 8;
        int rw = CONTENT_W - 16;
        int px = ox + SIDEBAR_W + CONTENT_W;

        // ── Main frame ────────────────────────────────────────────────────────
        g.fill(ox, oy, ox + W, oy + H, 0xEE0A0A14);
        brd(g, ox, oy, W, H, 0xFF3A3A55);

        // ── Top bar ───────────────────────────────────────────────────────────
        g.fill(ox, oy, ox + W, oy + TOP_H, 0xBB12121E);
        brd(g, ox, oy, W, TOP_H, 0xFF444466);

        String title = "§e⚙ §fНастройка NPC§e ⚙";
        g.drawString(font, title, ox + 8, oy + 9, 0xFFE6D7B5, false);

        // NPC name in top bar
        String npcName = editorState.getDraft().displayName;
        g.drawString(font, "§8»  §7" + npcName, ox + 120, oy + 9, 0xFFAAAAAA, false);

        // Dirty indicator
        if (editorState.isDirty()) {
            g.drawString(font, "§e● §7Несохранённые изменения", ox + W - 340, oy + 9, 0xFFEECC44, false);
        }

        // ── Sidebar ───────────────────────────────────────────────────────────
        g.fill(ox, oy + TOP_H, ox + SIDEBAR_W, oy + H, 0x99101020);
        brd(g, ox, oy + TOP_H, SIDEBAR_W, H - TOP_H, 0xFF2A2A44);
        g.drawString(font, "§7ВКЛАДКИ", ox + 8, oy + TOP_H + 4, 0xFF666677, false);

        // Active tab accent stripe
        int tabY = oy + TOP_H + 8 + activeTab * 40;
        g.fill(ox + 2, tabY + 1, ox + 4, tabY + 33, TAB_ACCENT[activeTab]);

        // ── Content area ──────────────────────────────────────────────────────
        g.fill(ox + SIDEBAR_W, oy + TOP_H, px, oy + H - BOT_H, 0x88090912);
        g.fill(ox + SIDEBAR_W, oy + TOP_H, ox + SIDEBAR_W + 1, oy + H, 0xFF2A2A44);
        g.fill(px, oy + TOP_H, px + 1, oy + H, 0xFF2A2A44);

        // Tab header with accent
        g.fill(rx, oy + TOP_H + 4, rx + rw, oy + TOP_H + 5, TAB_ACCENT[activeTab]);
        g.drawString(font, "§l" + TAB_LABELS[activeTab].trim(),
                rx, oy + TOP_H + 8, TAB_ACCENT[activeTab], false);

        // Active tab custom rendering
        TAB_INSTANCES[activeTab].render(g, editorState, rx, oy + TOP_H + 18, rw, mx, my);

        // ── Preview panel ─────────────────────────────────────────────────────
        g.fill(px, oy + TOP_H, ox + W, oy + H, 0x99121220);
        brd(g, px, oy + TOP_H, PREVIEW_W, H - TOP_H, 0xFF2A2A44);
        g.drawCenteredString(font, "§7ПРЕВЬЮ", px + PREVIEW_W / 2, oy + TOP_H + 4, 0xFF888877);
        g.fill(px, oy + TOP_H + 14, ox + W, oy + TOP_H + 15, 0xFF333344);

        renderPreview(g, px, oy, mx, my);
        renderSummary(g, px, oy);

        // ── Bottom bar ────────────────────────────────────────────────────────
        g.fill(ox, oy + H - BOT_H, ox + W, oy + H, 0x99101020);
        brd(g, ox, oy + H - BOT_H, W, BOT_H, 0xFF2A2A44);

        super.render(g, mx, my, pt);
    }

    // ── 3D Preview ────────────────────────────────────────────────────────────

    private void renderPreview(GuiGraphics g, int panelX, int oy, int mx, int my) {
        NpcEntity entity = editorState.getEntity();
        if (entity == null || !entity.isAlive()) return;

        NpcEntityData backup = entity.getNpcData().copy();
        Pose backupPose = entity.getPose();

        try {
            NpcEntityData draft = editorState.getDraft();
            entity.setNpcData(draft.copy());
            entity.setCustomName(Component.literal(draft.displayName));
            entity.setPose("CROUCHING".equals(draft.idlePose) ? Pose.CROUCHING : Pose.STANDING);

            int cx = panelX + PREVIEW_W / 2;
            int cy = oy + H - 100;
            InventoryScreen.renderEntityInInventoryFollowsMouse(g, cx, cy, 52, mx, my, entity);
        } finally {
            entity.setNpcData(backup);
            entity.setCustomName(Component.literal(backup.displayName));
            entity.setPose(backupPose);
        }
    }

    // ── Summary panel ─────────────────────────────────────────────────────────

    private void renderSummary(GuiGraphics g, int px, int oy) {
        NpcEntityData d = editorState.getDraft();
        int sx = px + 6;
        int sy = oy + H - 100 + 60;

        g.fill(px + 2, sy - 4, px + PREVIEW_W - 2, sy - 3, 0xFF333344);
        g.drawString(font, "§7§lСводка:", sx, sy, 0xFF888877, false);

        int y = sy + 12;
        g.drawString(font, "§8Имя:  §f" + d.displayName, sx, y, 0xFFCCCCCC, false);
        y += 10;
        g.drawString(font, "§8Скин: §f" + (d.skinId.equals("default") ? "по умолчанию" : d.skinId),
                sx, y, 0xFFCCCCCC, false);
        y += 10;
        String rel = NpcEntityData.relationLabel(d.playerRelation);
        int relCol = "FRIENDLY".equals(d.playerRelation) ? 0xFF44EE55 :
                "HOSTILE".equals(d.playerRelation) ? 0xFFEE4444 : 0xFFAAAAAA;
        g.drawString(font, "§8Отн.:  ", sx, y, 0xFFCCCCCC, false);
        g.drawString(font, rel, sx + font.width("Отн.:  ") + 6, y, relCol, false);
        y += 10;

        int questCount = d.questIds.size();
        g.drawString(font, "§8Квесты: §f" + questCount, sx, y, 0xFFCCCCCC, false);
        y += 10;

        boolean hasScene = !d.sceneId.isEmpty();
        boolean hasDlg   = !d.dialogueId.isEmpty();
        String dlgStatus = hasScene ? "§aСцена" : hasDlg ? "§eДиалог" : "§8нет";
        g.drawString(font, "§8Диалог: " + dlgStatus, sx, y, 0xFFCCCCCC, false);
        y += 10;

        g.drawString(font, "§8Поза:   §f" + NpcEntityData.idlePoseLabel(d.idlePose), sx, y, 0xFFCCCCCC, false);
    }

    // ── Input handlers ────────────────────────────────────────────────────────

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        int rx = ox() + SIDEBAR_W + 8;
        int rw = CONTENT_W - 16;
        int tabOy = oy() + TOP_H + 18;
        if (TAB_INSTANCES[activeTab].onMouseScrolled(mx, my, delta, editorState, rx, tabOy, rw)) {
            rebuildWidgets();
            return true;
        }
        return super.mouseScrolled(mx, my, delta);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int rx = ox() + SIDEBAR_W + 8;
        int rw = CONTENT_W - 16;
        int tabOy = oy() + TOP_H + 18;
        if (TAB_INSTANCES[activeTab].onMouseClicked(mx, my, btn, editorState, rx, tabOy, rw)) {
            rebuildWidgets();
            return true;
        }
        return super.mouseClicked(mx, my, btn);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void save() {
        TAB_INSTANCES[activeTab].pullFields(editorState);
        editorState.save();
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(null);
    }

    // ── Public bridges for helper classes ────────────────────────────────────

    public <T extends net.minecraft.client.gui.components.events.GuiEventListener
            & net.minecraft.client.gui.components.Renderable
            & net.minecraft.client.gui.narration.NarratableEntry> T addEditorWidget(T widget) {
        return super.addRenderableWidget(widget);
    }

    public void rebuildEditorWidgets() {
        super.rebuildWidgets();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    @Override
    public boolean isPauseScreen() { return false; }

    private int ox() { return (width  - W) / 2; }
    private int oy() { return (height - H) / 2; }

    static void brd(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x,     y,     x + w, y + 1, c);
        g.fill(x,     y+h-1, x + w, y + h, c);
        g.fill(x,     y,     x + 1, y + h, c);
        g.fill(x+w-1, y,     x + w, y + h, c);
    }
}

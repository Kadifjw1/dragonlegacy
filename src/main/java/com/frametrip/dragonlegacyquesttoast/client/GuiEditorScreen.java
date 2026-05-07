package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SaveNpcEntityDataPacket;
import com.frametrip.dragonlegacyquesttoast.server.gui.GuiElementData;
import com.frametrip.dragonlegacyquesttoast.server.gui.GuiElementType;
import com.frametrip.dragonlegacyquesttoast.server.gui.GuiTemplate;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.UUID;

/**
 * Visual GUI editor.
 *
 * Layout (780 × 520):
 *   Left  130px — template list + "Add" buttons
 *   Centre 430px — canvas (real-time preview, element selection by click)
 *   Right 200px — element palette (left half) + properties (right half, selected element)
 *
 * Canvas coordinate system: 0–100 × 0–100 (relative to canvas area).
 */
public class GuiEditorScreen extends Screen {

    private static final int W = 780, H = 520;
    private static final int LIST_W    = 130;
    private static final int CANVAS_W  = 430;
    private static final int RIGHT_W   = W - LIST_W - CANVAS_W;   // 220
    private static final int PAL_W     = 100;                      // element palette inside right
    private static final int PROP_W    = RIGHT_W - PAL_W;         // properties panel

    private static final int C_BG     = 0xFF0D0D1A;
    private static final int C_PANEL  = 0xFF14142B;
    private static final int C_BORDER = 0xFF2A2A44;
    private static final int C_ACCENT = 0xFFFF8844;
    private static final int C_CANVAS = 0xFF1A1A30;
    private static final int C_SEL    = 0xAAFFCC44;
    private static final int C_TEXT   = 0xFFCCCCDD;
    private static final int C_DIM    = 0xFF555566;

    private final NpcEntity npc;         // null when opened in air (library mode)
    private final UUID npcUuid;
    private final NpcEntityData draft;

    // ── Editor state ──────────────────────────────────────────────────────────
    private GuiTemplate selectedTemplate = null;
    private GuiElementData selectedElement = null;
    private int templateScroll = 0;

    // Property edit boxes (rebuilt each init)
    private EditBox propTextBox;
    private EditBox propColorBox;
    private EditBox propWBox, propHBox;

    private int guiLeft, guiTop;

    // ── Canvas geometry (screen pixels) ──────────────────────────────────────
    private int canvasX, canvasY;   // top-left of canvas area in screen pixels
    private static final int CANVAS_PX_W = CANVAS_W - 8;
    private static final int CANVAS_PX_H = H - 44;

    public GuiEditorScreen(NpcEntity npc) {
        super(Component.literal("Редактор интерфейсов"));
        this.npc    = npc;
        this.npcUuid = npc != null ? npc.getUUID() : null;
        this.draft  = npc != null ? npc.getNpcData().copy() : new NpcEntityData();
    }

    @Override
    protected void init() {
        guiLeft  = (width  - W) / 2;
        guiTop   = (height - H) / 2;
        canvasX  = guiLeft + LIST_W + 4;
        canvasY  = guiTop  + 28;
        rebuild();
    }

    private void rebuild() {
        clearWidgets();
        propTextBox  = null;
        propColorBox = null;
        propWBox = propHBox = null;

        int lx = guiLeft + 4;
        int ly = guiTop  + 4;

  // ── Template list ─────────────────────────────────────────────────────
        addRenderableWidget(Button.builder(Component.literal("+ Магазин"), b -> {
            GuiTemplate t = new GuiTemplate("Магазин", "shop");
            draft.guiTemplates.add(t);
            selectedTemplate = t;
            selectedElement  = null;
            rebuild();
        }).bounds(lx, ly, LIST_W - 4, 13).build());
        ly += 15;

        addRenderableWidget(Button.builder(Component.literal("+ Журнал"), b -> {
            GuiTemplate t = new GuiTemplate("Журнал", "journal");
            draft.guiTemplates.add(t);
            selectedTemplate = t;
            selectedElement  = null;
            rebuild();
        }).bounds(lx, ly, LIST_W - 4, 13).build());
        ly += 15;

        addRenderableWidget(Button.builder(Component.literal("+ Пустой"), b -> {
            GuiTemplate t = new GuiTemplate("Интерфейс", "custom");
            draft.guiTemplates.add(t);
            selectedTemplate = t;
            selectedElement  = null;
            rebuild();
        }).bounds(lx, ly, LIST_W - 4, 13).build());
        ly += 18;

        List<GuiTemplate> templates = draft.guiTemplates;
        int visTpl = 14;
        templateScroll = Math.max(0, Math.min(templateScroll, Math.max(0, templates.size() - visTpl)));
        for (int i = templateScroll; i < Math.min(templates.size(), templateScroll + visTpl); i++) {
            GuiTemplate t = templates.get(i);
            boolean sel = t == selectedTemplate;
            final int fi = i;
            addRenderableWidget(Button.builder(
                    Component.literal((sel ? "§e▶ " : "  ") + truncate(t.name, 12)),
                    b -> {
                        selectedTemplate = draft.guiTemplates.get(fi);
                        selectedElement  = null;
                        rebuild();
                    }
            ).bounds(lx, ly, LIST_W - 4, 12).build());
            ly += 13;
        }

        // Template action buttons
        if (selectedTemplate != null) {
            int btnY = guiTop + H - 36;
            addRenderableWidget(Button.builder(Component.literal("⧉"), b -> {
                GuiTemplate copy = selectedTemplate.copy();
                draft.guiTemplates.add(copy);
                selectedTemplate = copy;
                rebuild();
            }).bounds(lx, btnY, 20, 12).build());
            addRenderableWidget(Button.builder(Component.literal("§c✕"), b -> {
                draft.guiTemplates.remove(selectedTemplate);
                selectedTemplate = draft.guiTemplates.isEmpty() ? null : draft.guiTemplates.get(0);
                selectedElement  = null;
                rebuild();
            }).bounds(lx + 22, btnY, 20, 12).build());
        }

        // Save / Close
        addRenderableWidget(Button.builder(Component.literal("§aSохранить"), b -> save())
                .bounds(guiLeft + W - 104, guiTop + H - 16, 50, 13).build());
        addRenderableWidget(Button.builder(Component.literal("§cЗакрыть"), b -> onClose())
                .bounds(guiLeft + W - 52, guiTop + H - 16, 48, 13).build());

        if (selectedTemplate == null) return;

        // ── Element palette (right panel, left half) ──────────────────────────
        int px = guiLeft + LIST_W + CANVAS_W + 8;
        int py = guiTop + 28;
        for (GuiElementType type : GuiElementType.values()) {
            addRenderableWidget(Button.builder(
                    Component.literal(type.icon() + " " + type.label()),
                    b -> {
                        GuiElementData el = new GuiElementData(type);
                        selectedTemplate.elements.add(el);
                        selectedElement = el;
                        rebuild();
                    }
            ).bounds(px, py, PAL_W - 2, 13).build());
            py += 15;
        }

        // Remove selected element
        if (selectedElement != null) {
            addRenderableWidget(Button.builder(Component.literal("§c✕ Удалить"), b -> {
                selectedTemplate.elements.remove(selectedElement);
                selectedElement = null;
                rebuild();
            }).bounds(px, py + 4, PAL_W - 2, 13).build());
        }

        // ── Properties panel (right panel, right half) ────────────────────────
        if (selectedElement == null) return;
        GuiElementData el = selectedElement;
        int rpx = px + PAL_W;
        int rpy = guiTop + 28;

  // Size controls
        addRenderableWidget(Button.builder(Component.literal("◀W"), b -> {
            el.w = Math.max(5, el.w - 5); rebuild();
        }).bounds(rpx, rpy, 24, 12).build());
        addRenderableWidget(Button.builder(Component.literal("W▶"), b -> {
            el.w = Math.min(100, el.w + 5); rebuild();
        }).bounds(rpx + 26, rpy, 24, 12).build());
        rpy += 14;

        addRenderableWidget(Button.builder(Component.literal("◀H"), b -> {
            el.h = Math.max(5, el.h - 5); rebuild();
        }).bounds(rpx, rpy, 24, 12).build());
        addRenderableWidget(Button.builder(Component.literal("H▶"), b -> {
            el.h = Math.min(100, el.h + 5); rebuild();
        }).bounds(rpx + 26, rpy, 24, 12).build());
        rpy += 14;

        // Position controls
        addRenderableWidget(Button.builder(Component.literal("←"), b -> {
            el.x = Math.max(0, el.x - 2); rebuild();
        }).bounds(rpx + 12, rpy, 16, 12).build());
        addRenderableWidget(Button.builder(Component.literal("→"), b -> {
            el.x = Math.min(100 - el.w, el.x + 2); rebuild();
        }).bounds(rpx + 30, rpy, 16, 12).build());
        rpy += 14;

        addRenderableWidget(Button.builder(Component.literal("↑"), b -> {
            el.y = Math.max(0, el.y - 2); rebuild();
        }).bounds(rpx + 12, rpy, 16, 12).build());
        addRenderableWidget(Button.builder(Component.literal("↓"), b -> {
            el.y = Math.min(100 - el.h, el.y + 2); rebuild();
        }).bounds(rpx + 30, rpy, 16, 12).build());
        rpy += 18;

        // Text / Color properties
        if (el.props.containsKey("text")) {
            propTextBox = new EditBox(font, rpx, rpy, PROP_W - 4, 12, Component.literal("Текст"));
            propTextBox.setValue(el.prop("text", ""));
            addRenderableWidget(propTextBox);
            rpy += 14;
        }

        if (el.props.containsKey("color")) {
            propColorBox = new EditBox(font, rpx, rpy, PROP_W - 4, 12, Component.literal("Цвет #RRGGBB"));
            propColorBox.setValue(el.prop("color", "#CCCCDD"));
            addRenderableWidget(propColorBox);
            rpy += 14;
        }

        // Extra: columns for ITEM_GRID
        if (el.type == GuiElementType.ITEM_GRID) {
            addRenderableWidget(Button.builder(Component.literal("Кол. ◀"), b -> {
                int cols = parseInt(el.prop("columns", "3"), 3);
                el.prop("columns", String.valueOf(Math.max(1, cols - 1)));
                rebuild();
            }).bounds(rpx, rpy, 48, 12).build());
            addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
                int cols = parseInt(el.prop("columns", "3"), 3);
                el.prop("columns", String.valueOf(Math.min(8, cols + 1)));
                rebuild();
            }).bounds(rpx + 50, rpy, 16, 12).build());
        }
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(guiLeft, guiTop, guiLeft + W, guiTop + H, C_BG);
        border(g, guiLeft, guiTop, W, H);

        // Left panel
        g.fill(guiLeft, guiTop, guiLeft + LIST_W, guiTop + H, C_PANEL);
        border(g, guiLeft, guiTop, LIST_W, H);
        g.drawString(font, "§lИнтерфейсы", guiLeft + 4, guiTop + H - 26, C_ACCENT, false);

        // Canvas area
        g.fill(canvasX, canvasY, canvasX + CANVAS_PX_W, canvasY + CANVAS_PX_H, C_CANVAS);
        border(g, canvasX, canvasY, CANVAS_PX_W, CANVAS_PX_H);

        if (selectedTemplate != null) {
            renderCanvas(g, mx, my);
        } else {
            g.drawCenteredString(font, "§8Выберите или создайте интерфейс",
                    canvasX + CANVAS_PX_W / 2, canvasY + CANVAS_PX_H / 2, C_DIM);
        }

        // Right panel background
        int rpx = guiLeft + LIST_W + CANVAS_W + 4;
        g.fill(rpx, guiTop, guiLeft + W, guiTop + H, C_PANEL);
        border(g, rpx, guiTop, RIGHT_W - 4, H);
        g.drawString(font, "§7Элементы", rpx + 4, guiTop + 4, C_ACCENT, false);

        // Properties labels
        if (selectedElement != null) {
            int prx = rpx + PAL_W + 4;
            int pry = guiTop + 28;
            g.drawString(font, "§7" + selectedElement.type.label(), prx, pry - 14, C_TEXT, false);
            g.drawString(font, "§8W§f" + selectedElement.w + " §8H§f" + selectedElement.h,
                    prx, pry + 2, C_DIM, false);
            g.drawString(font, "§8X§f" + selectedElement.x + " §8Y§f" + selectedElement.y,
                    prx, pry + 14, C_DIM, false);
        }

        // Title bar
        g.fill(guiLeft, guiTop, guiLeft + W, guiTop + 16, 0xFF0A0A18);
        String tplName = selectedTemplate != null ? "  §7— §e" + selectedTemplate.name : "";
        g.drawString(font, "§l⚙ РЕДАКТОР ИНТЕРФЕЙСОВ" + tplName, guiLeft + 4, guiTop + 4, C_ACCENT, false);

        super.render(g, mx, my, pt);
    }

private void renderCanvas(GuiGraphics g, int mx, int my) {
        for (GuiElementData el : selectedTemplate.elements) {
            int ex = canvasX + (int)(el.x * CANVAS_PX_W / 100f);
            int ey = canvasY + (int)(el.y * CANVAS_PX_H / 100f);
            int ew = (int)(el.w * CANVAS_PX_W / 100f);
            int eh = (int)(el.h * CANVAS_PX_H / 100f);

            int col = parseColor(el.prop("color", "#14142B"));
            int alpha = parseInt(el.prop("alpha", "200"), 200);
            int fillCol = (alpha << 24) | (col & 0x00FFFFFF);

            g.fill(ex, ey, ex + ew, ey + eh, fillCol);

            if (el.type == GuiElementType.BUTTON) {
                border(g, ex, ey, ew, eh);
                g.drawCenteredString(font, el.prop("text", ""), ex + ew / 2, ey + eh / 2 - 4, 0xFFCCCCFF);
            } else if (el.type == GuiElementType.TEXT) {
                g.drawString(font, truncate(el.prop("text", ""), 30), ex + 2, ey + 2,
                        parseColor(el.prop("color", "#CCCCDD")), false);
            } else if (el.type == GuiElementType.ITEM_GRID) {
                border(g, ex, ey, ew, eh);
                g.drawString(font, "§8⊞ " + el.prop("columns", "3") + " кол.", ex + 2, ey + 2, C_DIM, false);
            } else if (el.type == GuiElementType.SCROLL_AREA) {
                border(g, ex, ey, ew, eh);
                g.drawString(font, "§8≡", ex + 2, ey + 2, C_DIM, false);
            } else {
                border(g, ex, ey, ew, eh);
            }

            // Selection highlight
            if (el == selectedElement) {
                g.fill(ex, ey, ex + ew, ey + 1, C_SEL);
                g.fill(ex, ey + eh - 1, ex + ew, ey + eh, C_SEL);
                g.fill(ex, ey, ex + 1, ey + eh, C_SEL);
                g.fill(ex + ew - 1, ey, ex + ew, ey + eh, C_SEL);
            }
        }

        // Type label at top of canvas
        g.drawString(font, "§8Тип: §7" + selectedTemplate.templateType,
                canvasX + 4, canvasY + 4, C_DIM, false);
    }

    // ── Mouse handling ────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        // Click on canvas → select element
        if (selectedTemplate != null
                && mx >= canvasX && mx <= canvasX + CANVAS_PX_W
                && my >= canvasY && my <= canvasY + CANVAS_PX_H) {
            int cx = (int)((mx - canvasX) * 100 / CANVAS_PX_W);
            int cy = (int)((my - canvasY) * 100 / CANVAS_PX_H);
            GuiElementData hit = null;
            // Iterate reversed so top-most element is selected first
            var els = selectedTemplate.elements;
            for (int i = els.size() - 1; i >= 0; i--) {
                GuiElementData el = els.get(i);
                if (cx >= el.x && cx <= el.x + el.w && cy >= el.y && cy <= el.y + el.h) {
                    hit = el;
                    break;
                }
            }
            if (hit != selectedElement) {
                selectedElement = hit;
                pullFields();
                rebuild();
            }
            return true;
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (mx >= guiLeft && mx <= guiLeft + LIST_W) {
            int max = Math.max(0, draft.guiTemplates.size() - 14);
            templateScroll = Math.max(0, Math.min(max, templateScroll - (int)Math.signum(delta)));
            return true;
        }
        return super.mouseScrolled(mx, my, delta);
    }

    // ── Field sync ────────────────────────────────────────────────────────────

    private void pullFields() {
        if (selectedElement == null) return;
        if (propTextBox  != null) selectedElement.prop("text",  propTextBox.getValue());
        if (propColorBox != null) selectedElement.prop("color", propColorBox.getValue());
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    private void save() {
        pullFields();
        if (npc != null) {
            ModNetwork.CHANNEL.sendToServer(new SaveNpcEntityDataPacket(npcUuid, draft));
            npc.setNpcData(draft);
        }
    }

    @Override
    public void onClose() {
        save();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() { return false; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void border(GuiGraphics g, int x, int y, int w, int h) {
        int c = 0xFF2A2A44;
        g.fill(x,         y,         x + w, y + 1,     c);
        g.fill(x,         y + h - 1, x + w, y + h,     c);
        g.fill(x,         y,         x + 1, y + h,     c);
        g.fill(x + w - 1, y,         x + w, y + h,     c);
    }

    private static int parseColor(String hex) {
        try {
            return (int) Long.parseLong(hex.replace("#", ""), 16);
        } catch (Exception e) {
            return 0x14142B;
        }
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}

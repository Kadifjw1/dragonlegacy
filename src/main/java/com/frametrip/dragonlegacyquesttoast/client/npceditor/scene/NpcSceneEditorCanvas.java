package com.frametrip.dragonlegacyquesttoast.client.npceditor.scene;

import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcChoiceOption;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcSceneNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

import static com.frametrip.dragonlegacyquesttoast.client.npceditor.scene.NpcSceneEditorScreen.*;

final class NpcSceneEditorCanvas {
    private NpcSceneEditorCanvas() {}

    private static final int NODE_W = 116;
    private static final int NODE_H = 42;

    static void init(NpcSceneEditorScreen scr, int ox, int oy) {
        if (scr.draftScene == null) return;
        int palX = paletteX(ox), palY = zoneY(oy);
        int x = canvasX(ox), y = zoneY(oy);
        int by = palY + 16;
        addSectionTitle(scr, palX + 4, by, "Старт / Диалог", scr.catStartOpen, () -> {
            scr.catStartOpen = !scr.catStartOpen;
            scr.rebuildAll();
        });
        by += 12;
        if (scr.catStartOpen) {
            addNodeButton(scr, palX + 4, by, "+ Speech", NpcSceneNode.TYPE_SPEECH); by += 16;
            addNodeButton(scr, palX + 4, by, "+ Question", NpcSceneNode.TYPE_QUESTION); by += 16;
            addNodeButton(scr, palX + 4, by, "+ Action", NpcSceneNode.TYPE_ACTION); by += 16;
            addNodeButton(scr, palX + 4, by, "+ End", NpcSceneNode.TYPE_END); by += 18;
        }
        if (scr.editorLevel >= 2) {
            addSectionTitle(scr, palX + 4, by, "Логика / Поток", scr.catLogicOpen, () -> {
                scr.catLogicOpen = !scr.catLogicOpen;
                scr.rebuildAll();
            });
            by += 12;
            if (scr.catLogicOpen) {
                addNodeButton(scr, palX + 4, by, "+ Condition", NpcSceneNode.TYPE_CONDITION); by += 16;
                addNodeButton(scr, palX + 4, by, "+ Delay", NpcSceneNode.TYPE_DELAY); by += 16;
                addNodeButton(scr, palX + 4, by, "+ Branch", NpcSceneNode.TYPE_BRANCH); by += 18;
            }
        }
        if (scr.editorLevel >= 3) {
            addSectionTitle(scr, palX + 4, by, "Постановка", scr.catStagingOpen, () -> {
                scr.catStagingOpen = !scr.catStagingOpen;
                scr.rebuildAll();
            });
            by += 12;
            if (scr.catStagingOpen) {
                addActionNodeButton(scr, palX + 4, by, "+ Animation", NpcSceneNode.ACTION_PLAY_ANIMATION); by += 16;
                addActionNodeButton(scr, palX + 4, by, "+ LookAt", NpcSceneNode.ACTION_LOOK_AT); by += 16;
                addActionNodeButton(scr, palX + 4, by, "+ Move", NpcSceneNode.ACTION_MOVE_TO); by += 16;
                addActionNodeButton(scr, palX + 4, by, "+ Camera", NpcSceneNode.ACTION_CAMERA); by += 16;
                addActionNodeButton(scr, palX + 4, by, "+ Sound", NpcSceneNode.ACTION_PLAY_SOUND); by += 16;
                addActionNodeButton(scr, palX + 4, by, "+ Effect", NpcSceneNode.ACTION_EFFECT); by += 16;
                addActionNodeButton(scr, palX + 4, by, "+ Emote", NpcSceneNode.ACTION_EMOTE); by += 16;
            }
        }

        scr.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                net.minecraft.network.chat.Component.literal("🧭 Центр"), b -> {
                    scr.canvasPanX = 0;
                    scr.canvasPanY = 0;
                    scr.canvasZoom = 1.0f;
                }).bounds(palX + 4, palY + 148, 62, 14).build());
        scr.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                net.minecraft.network.chat.Component.literal("⤢ Вписать"), b -> autoFit(scr)
        ).bounds(palX + 68, palY + 148, 62, 14).build());
        scr.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                net.minecraft.network.chat.Component.literal("＋"), b -> scr.canvasZoom = Math.min(2.0f, scr.canvasZoom + 0.1f)
        ).bounds(palX + 4, palY + 164, 30, 14).build());
        scr.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                net.minecraft.network.chat.Component.literal("－"), b -> scr.canvasZoom = Math.max(0.4f, scr.canvasZoom - 0.1f)
        ).bounds(palX + 36, palY + 164, 30, 14).build());
        scr.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                net.minecraft.network.chat.Component.literal("↺ Auto"), b -> autoLayout(scr)
        ).bounds(palX + 68, palY + 164, 62, 14).build());
        scr.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                net.minecraft.network.chat.Component.literal(layoutModeLabel(scr.autoLayoutMode)),
                b -> {
                    scr.autoLayoutMode = (scr.autoLayoutMode + 1) % 4;
                    scr.rebuildAll();
                }
        ).bounds(palX + 4, palY + 180, 126, 14).build());

        scr.nodeSearchBox = new net.minecraft.client.gui.components.EditBox(Minecraft.getInstance().font,
                x + 4, y + 16, 128, 14, net.minecraft.network.chat.Component.literal("Поиск блока"));
        scr.nodeSearchBox.setValue(scr.nodeSearch == null ? "" : scr.nodeSearch);
        scr.addRenderableWidget(scr.nodeSearchBox);
        scr.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                net.minecraft.network.chat.Component.literal("Найти"),
                b -> jumpToSearch(scr)
        ).bounds(x + 134, y + 16, 42, 14).build());

        int actY = y + zoneH() - 14;
        scr.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                net.minecraft.network.chat.Component.literal("⎘ Дубль"), b -> duplicateSelected(scr)
        ).bounds(x + 4, actY, 64, 14).build());
        scr.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                net.minecraft.network.chat.Component.literal("🗑 Удалить"), b -> deleteSelected(scr)
        ).bounds(x + 70, actY, 64, 14).build());
        scr.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                net.minecraft.network.chat.Component.literal("★ Старт"), b -> {
                    if (!scr.selectedNodeId.isEmpty()) scr.draftScene.startNodeId = scr.selectedNodeId;
                }
        ).bounds(x + 136, actY, 64, 14).build());
    }

    static void render(NpcSceneEditorScreen scr, GuiGraphics g, int ox, int oy, int mx, int my) {
        var font = Minecraft.getInstance().font;
        int py = zoneY(oy);
        g.fill(paletteX(ox), py, paletteX(ox) + PALETTE_W, py + zoneH(), 0xAA131320);
        brd(g, paletteX(ox), py, PALETTE_W, zoneH(), 0xFF2A2A44);
        g.drawString(font, "§lПалитра", paletteX(ox) + 4, py + 4, ACCENT_NODE, false);

        int x = canvasX(ox), y = zoneY(oy), w = CANVAS_W, h = zoneH();
        g.fill(x, y, x + w, y + h, 0xAA0F1018);
        brd(g, x, y, w, h, 0xFF2A2A44);
        g.drawString(font, "§lCanvas " + (scr.readOnlyMode ? "§8[READ]" : "§a[EDIT]"), x + 4, y + 4, ACCENT_NODE, false);

        if (scr.draftScene == null) return;

        // grid
        int step = Math.max(14, (int) (28 * scr.canvasZoom));
        for (int gx = x + ((int) scr.canvasPanX % step); gx < x + w; gx += step) g.fill(gx, y, gx + 1, y + h, 0x22334455);
        for (int gy = y + ((int) scr.canvasPanY % step); gy < y + h; gy += step) g.fill(x, gy, x + w, gy + 1, 0x22334455);

        for (NpcSceneNode node : scr.draftScene.nodes) drawEdges(scr, g, x, y, node, mx, my);
        for (NpcSceneNode node : scr.draftScene.nodes) drawNode(scr, g, x, y, node);

        drawMiniMap(scr, g, x + w - 124, y + 6, 118, 86);
        g.drawString(font, "Zoom: " + Math.round(scr.canvasZoom * 100) + "%", x + w - 74, y + h - 12, 0xFF8899BB, false);
    }

    static boolean mouseClicked(NpcSceneEditorScreen scr, double mx, double my, int button, int ox, int oy) {
        if (scr.draftScene == null) return false;
        int x = canvasX(ox), y = zoneY(oy), w = CANVAS_W, h = zoneH();
        if (mx < x || my < y || mx > x + w || my > y + h) return false;
        if (button == 2) {
            scr.canvasPanning = true;
            scr.lastMouseX = mx;
            scr.lastMouseY = my;
            return true;
        }
        if (button == 0) {
            NpcSceneNode hit = findNodeAt(scr, mx, my, x, y);
            if (hit != null) {
                scr.selectedNodeId = hit.id;
                scr.canvasDraggingNode = !scr.readOnlyMode;
                scr.lastMouseX = mx;
                scr.lastMouseY = my;
                return true;
            }
        }
        return false;
    }

    static boolean mouseDragged(NpcSceneEditorScreen scr, double mx, double my, int button) {
        if (scr.canvasPanning) {
            scr.canvasPanX += (float) (mx - scr.lastMouseX);
            scr.canvasPanY += (float) (my - scr.lastMouseY);
            scr.lastMouseX = mx;
            scr.lastMouseY = my;
            return true;
        }
        if (scr.canvasDraggingNode && !scr.selectedNodeId.isEmpty() && scr.draftScene != null) {
            NpcSceneNode node = scr.draftScene.getNode(scr.selectedNodeId);
            if (node != null) {
                node.canvasX += (int) ((mx - scr.lastMouseX) / scr.canvasZoom);
                node.canvasY += (int) ((my - scr.lastMouseY) / scr.canvasZoom);
                snapToGrid(node);
                scr.lastMouseX = mx;
                scr.lastMouseY = my;
                return true;
            }
        }
        return false;
    }

    static boolean mouseReleased(NpcSceneEditorScreen scr) {
        boolean used = scr.canvasPanning || scr.canvasDraggingNode;
        scr.canvasPanning = false;
        scr.canvasDraggingNode = false;
        return used;
    }

    static boolean mouseScrolled(NpcSceneEditorScreen scr, double mx, double my, double delta, int ox, int oy) {
        int x = canvasX(ox), y = zoneY(oy), w = CANVAS_W, h = zoneH();
        if (mx < x || my < y || mx > x + w || my > y + h) return false;
        if (delta > 0) scr.canvasZoom = Math.min(2.0f, scr.canvasZoom + 0.1f);
        else scr.canvasZoom = Math.max(0.4f, scr.canvasZoom - 0.1f);
        return true;
    }

    static int canvasX(int ox) { return ox + PAD + COL1_W + COL_GAP + PALETTE_W + COL_GAP; }
    static int paletteX(int ox) { return ox + PAD + COL1_W + COL_GAP; }
    static int zoneY(int oy) { return oy + TOP_H + 4; }
    static int zoneH() { return H - TOP_H - BOT_H - 8; }

    private static void drawNode(NpcSceneEditorScreen scr, GuiGraphics g, int x, int y, NpcSceneNode node) {
        int sx = x + (int) (scr.canvasPanX + node.canvasX * scr.canvasZoom);
        int sy = y + (int) (scr.canvasPanY + node.canvasY * scr.canvasZoom);
        int w = (int) (NODE_W * scr.canvasZoom);
        int h = (int) (NODE_H * scr.canvasZoom);
        int col = colorOfNodeType(node.type);
        if (node.id.equals(scr.draftScene.startNodeId)) col = 0xFF44CC88;
        if (node.id.equals(scr.draftScene.startNodeId)) {
            drawRoundedRect(g, sx, sy, w, h, 6, 0xCC11131D, node.id.equals(scr.selectedNodeId) ? 0xFFFFFFFF : 0xFF334466);
            g.fill(sx + 4, sy, sx + w - 4, sy + 2, col);
        } else if (NpcSceneNode.TYPE_CONDITION.equals(node.type)) {
            drawDiamond(g, sx, sy, w, h, 0xCC11131D, node.id.equals(scr.selectedNodeId) ? 0xFFFFFFFF : 0xFF334466);
        } else if (NpcSceneNode.TYPE_END.equals(node.type)) {
            g.fill(sx, sy + h / 3, sx + w, sy + h - h / 3, 0xCC11131D);
            brd(g, sx, sy + h / 3, w, h / 3, node.id.equals(scr.selectedNodeId) ? 0xFFFFFFFF : 0xFF334466);
        } else {
            g.fill(sx, sy, sx + w, sy + h, 0xCC11131D);
            g.fill(sx, sy, sx + w, sy + 2, col);
            brd(g, sx, sy, w, h, node.id.equals(scr.selectedNodeId) ? 0xFFFFFFFF : 0xFF334466);
        }
        var font = Minecraft.getInstance().font;
        g.drawString(font, NpcSceneNode.typeLabel(node.type), sx + 4, sy + 4, col, false);
        g.drawString(font, node.displayLabel(), sx + 4, sy + 16, 0xFFDDE2FF, false);
        g.drawString(font, "#" + node.id, sx + 4, sy + h - 10, 0xFF8899BB, false);
    }

    private static void drawEdges(NpcSceneEditorScreen scr, GuiGraphics g, int x, int y, NpcSceneNode node, int mx, int my) {
        int fromX = x + (int) (scr.canvasPanX + node.canvasX * scr.canvasZoom) + (int) (NODE_W * scr.canvasZoom);
        int fromY = y + (int) (scr.canvasPanY + node.canvasY * scr.canvasZoom) + (int) (NODE_H * scr.canvasZoom / 2f);
        for (Edge edge : outgoing(node)) {
            String to = edge.toNodeId;
            NpcSceneNode target = scr.draftScene.getNode(to);
            if (target == null) continue;
            int toX = x + (int) (scr.canvasPanX + target.canvasX * scr.canvasZoom);
            int toY = y + (int) (scr.canvasPanY + target.canvasY * scr.canvasZoom) + (int) (NODE_H * scr.canvasZoom / 2f);
            drawLine(g, fromX, fromY, toX, toY, edge.color);
            g.drawString(Minecraft.getInstance().font, edge.label, (fromX + toX) / 2 + 2, (fromY + toY) / 2 - 6, edge.color, false);
        }
    }

    private static List<Edge> outgoing(NpcSceneNode n) {
        List<Edge> out = new java.util.ArrayList<>();
        switch (n.type) {
            case NpcSceneNode.TYPE_SPEECH, NpcSceneNode.TYPE_DELAY -> out.add(new Edge(n.nextNodeId, "далее", 0xFF88AADD));
            case NpcSceneNode.TYPE_ACTION -> out.add(new Edge(n.actionNextNodeId, "после", 0xFF88AADD));
            case NpcSceneNode.TYPE_CONDITION -> {
                out.add(new Edge(n.trueNextNodeId, "TRUE", 0xFF4EDC7A));
                out.add(new Edge(n.falseNextNodeId, "FALSE", 0xFFDD5C5C));
            }
            case NpcSceneNode.TYPE_QUESTION -> {
                if (n.choices != null) {
                    int i = 1;
                    for (NpcChoiceOption c : n.choices) out.add(new Edge(c.nextNodeId, "Ответ " + (i++), 0xFF5C9BFF));
                }
            }
            case NpcSceneNode.TYPE_BRANCH -> {
                if (n.branchOptions != null) {
                    int i = 1;
                    for (NpcChoiceOption c : n.branchOptions) out.add(new Edge(c.nextNodeId, "Ветка " + (i++), 0xFFE0BB4E));
                }
            }
            default -> {}
        }
        out.removeIf(v -> v.toNodeId == null || v.toNodeId.isBlank());
        return out;
    }

    private static void drawLine(GuiGraphics g, int x0, int y0, int x1, int y1, int color) {
        int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
        if (steps <= 0) return;
        for (int i = 0; i <= steps; i++) {
            float t = i / (float) steps;
            int x = (int) (x0 + (x1 - x0) * t);
            int y = (int) (y0 + (y1 - y0) * t);
            g.fill(x, y, x + 1, y + 1, color);
        }
    }
    private static void drawRoundedRect(GuiGraphics g, int x, int y, int w, int h, int r, int fill, int border) {
        g.fill(x + r, y, x + w - r, y + h, fill);
        g.fill(x, y + r, x + w, y + h - r, fill);
        brd(g, x, y, w, h, border);
    }
    private static void drawDiamond(GuiGraphics g, int x, int y, int w, int h, int fill, int border) {
        int cx = x + w / 2;
        for (int i = 0; i < h / 2; i++) {
            int hw = (int) ((w / 2f) * (i / (float) (h / 2)));
            g.fill(cx - hw, y + i, cx + hw, y + i + 1, fill);
            g.fill(cx - hw, y + h - i - 1, cx + hw, y + h - i, fill);
        }
        brd(g, x + w / 4, y, w / 2, h, border);
    }

    private static NpcSceneNode findNodeAt(NpcSceneEditorScreen scr, double mx, double my, int x, int y) {
        for (int i = scr.draftScene.nodes.size() - 1; i >= 0; i--) {
            NpcSceneNode node = scr.draftScene.nodes.get(i);
            int sx = x + (int) (scr.canvasPanX + node.canvasX * scr.canvasZoom);
            int sy = y + (int) (scr.canvasPanY + node.canvasY * scr.canvasZoom);
            int w = (int) (NODE_W * scr.canvasZoom);
            int h = (int) (NODE_H * scr.canvasZoom);
            if (mx >= sx && my >= sy && mx <= sx + w && my <= sy + h) return node;
        }
        return null;
    }

    private static void addNodeButton(NpcSceneEditorScreen scr, int x, int y, String label, String type) {
        scr.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                net.minecraft.network.chat.Component.literal(label), b -> {
                    if (scr.draftScene == null || scr.readOnlyMode) return;
                    NpcSceneNode n = scr.draftScene.addNode(type);
                    n.canvasX = 40 + scr.draftScene.nodes.size() * 14;
                    n.canvasY = 30 + scr.draftScene.nodes.size() * 10;
                    if (NpcSceneNode.TYPE_BRANCH.equals(type)) {
                        NpcChoiceOption o1 = new NpcChoiceOption(); o1.text = "Ветка A"; o1.actionParam = "1";
                        NpcChoiceOption o2 = new NpcChoiceOption(); o2.text = "Ветка B"; o2.actionParam = "1";
                        n.branchOptions.add(o1); n.branchOptions.add(o2);
                    }
                    scr.selectedNodeId = n.id;
                    scr.editingChoiceId = "";
                    scr.rebuildAll();
                }).bounds(x, y, 126, 14).build());
    }
    private static void addActionNodeButton(NpcSceneEditorScreen scr, int x, int y, String label, String actionType) {
        scr.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                net.minecraft.network.chat.Component.literal(label), b -> {
                    if (scr.draftScene == null || scr.readOnlyMode) return;
                    NpcSceneNode n = scr.draftScene.addNode(NpcSceneNode.TYPE_ACTION);
                    n.actionType = actionType;
                    n.canvasX = 40 + scr.draftScene.nodes.size() * 14;
                    n.canvasY = 30 + scr.draftScene.nodes.size() * 10;
                    scr.selectedNodeId = n.id;
                    scr.rebuildAll();
                }).bounds(x, y, 126, 14).build());
    }
    private static void addSectionTitle(NpcSceneEditorScreen scr, int x, int y, String title, boolean open, Runnable onToggle) {
        scr.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                net.minecraft.network.chat.Component.literal((open ? "▼ " : "▶ ") + title), b -> onToggle.run()
        ).bounds(x, y, 126, 10).build());
    }

    private static void autoLayout(NpcSceneEditorScreen scr) {
        if (scr.draftScene == null) return;
        int i = 0;
        for (NpcSceneNode node : scr.draftScene.nodes) {
            if (scr.autoLayoutMode == 0) { // vertical tree
                node.canvasX = (i % 4) * 140 + 20;
                node.canvasY = (i / 4) * 90 + 20;
            } else if (scr.autoLayoutMode == 1) { // horizontal tree
                node.canvasX = (i / 4) * 170 + 20;
                node.canvasY = (i % 4) * 80 + 20;
            } else if (scr.autoLayoutMode == 2) { // compact
                node.canvasX = (i % 6) * 110 + 20;
                node.canvasY = (i / 6) * 70 + 20;
            }
            i++;
        }
    }

    private static void autoFit(NpcSceneEditorScreen scr) {
        if (scr.draftScene == null || scr.draftScene.nodes.isEmpty()) return;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        for (NpcSceneNode node : scr.draftScene.nodes) {
            minX = Math.min(minX, node.canvasX);
            minY = Math.min(minY, node.canvasY);
        }
        scr.canvasPanX = -minX + 18;
        scr.canvasPanY = -minY + 18;
        scr.canvasZoom = 0.9f;
    }

    private static void drawMiniMap(NpcSceneEditorScreen scr, GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, 0x99101018);
        brd(g, x, y, w, h, 0xFF3F4968);
        if (scr.draftScene == null || scr.draftScene.nodes.isEmpty()) return;
        for (NpcSceneNode node : scr.draftScene.nodes) {
            int nx = x + Math.max(2, Math.min(w - 4, node.canvasX / 8 + 4));
            int ny = y + Math.max(2, Math.min(h - 4, node.canvasY / 8 + 4));
            g.fill(nx, ny, nx + 3, ny + 3, colorOfNodeType(node.type));
        }
    }

    private static void duplicateSelected(NpcSceneEditorScreen scr) {
        if (scr.readOnlyMode || scr.draftScene == null || scr.selectedNodeId.isEmpty()) return;
        NpcSceneNode src = scr.draftScene.getNode(scr.selectedNodeId);
        if (src == null) return;
        NpcSceneNode dup = src.copy();
        dup.id = java.util.UUID.randomUUID().toString().substring(0, 8);
        dup.canvasX += 24;
        dup.canvasY += 24;
        scr.draftScene.nodes.add(dup);
        scr.selectedNodeId = dup.id;
        scr.rebuildAll();
    }

    private static void deleteSelected(NpcSceneEditorScreen scr) {
        if (scr.readOnlyMode || scr.draftScene == null || scr.selectedNodeId.isEmpty()) return;
        scr.draftScene.removeNode(scr.selectedNodeId);
        scr.selectedNodeId = "";
        scr.editingChoiceId = "";
        scr.rebuildAll();
    }
    private static void jumpToSearch(NpcSceneEditorScreen scr) {
        if (scr.draftScene == null || scr.nodeSearch == null || scr.nodeSearch.isBlank()) return;
        String q = scr.nodeSearch.toLowerCase();
        for (NpcSceneNode n : scr.draftScene.nodes) {
            if (n.id.toLowerCase().contains(q) || n.displayLabel().toLowerCase().contains(q)) {
                scr.selectedNodeId = n.id;
                scr.canvasPanX = -n.canvasX + 60;
                scr.canvasPanY = -n.canvasY + 60;
                return;
            }
        }
    }
    private static void snapToGrid(NpcSceneNode node) {
        int step = 10;
        node.canvasX = Math.round(node.canvasX / (float) step) * step;
        node.canvasY = Math.round(node.canvasY / (float) step) * step;
    }
    private static String layoutModeLabel(int mode) {
        return switch (mode) {
            case 1 -> "Layout: Гориз.";
            case 2 -> "Layout: Компакт";
            case 3 -> "Layout: Свобод.";
            default -> "Layout: Вертик.";
        };
    }
    private record Edge(String toNodeId, String label, int color) {}
}

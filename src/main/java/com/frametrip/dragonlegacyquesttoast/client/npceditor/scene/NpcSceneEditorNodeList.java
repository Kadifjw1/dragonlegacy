package com.frametrip.dragonlegacyquesttoast.client.npceditor.scene;

import com.frametrip.dragonlegacyquesttoast.client.npceditor.NpcEditorUtils;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcSceneNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import static com.frametrip.dragonlegacyquesttoast.client.npceditor.scene.NpcSceneEditorScreen.*;

/** Center zone — list of nodes for the selected scene + add buttons. */
final class NpcSceneEditorNodeList {

    private NpcSceneEditorNodeList() {}

    private static final int ROW_H = 18;
    private static final int VISIBLE_ROWS = 14;

    static void init(NpcSceneEditorScreen scr, int ox, int oy) {
        if (scr.draftScene == null) return;

        int x = ox + PAD + COL1_W + COL_GAP;
        int y = oy + TOP_H + 4;
        var nodes = scr.draftScene.nodes;
        int from = Math.min(scr.nodeScroll, Math.max(0, nodes.size() - VISIBLE_ROWS));
        int to = Math.min(nodes.size(), from + VISIBLE_ROWS);

        // Node rows
        for (int i = from; i < to; i++) {
            NpcSceneNode n = nodes.get(i);
            boolean selected = n.id.equals(scr.selectedNodeId);
            boolean isStart  = n.id.equals(scr.draftScene.startNodeId);
            String typeIcon = switch (n.type) {
                case NpcSceneNode.TYPE_SPEECH    -> "💬";
                case NpcSceneNode.TYPE_QUESTION  -> "❓";
                case NpcSceneNode.TYPE_ACTION    -> "⚡";
                case NpcSceneNode.TYPE_CONDITION -> "❖";
                case NpcSceneNode.TYPE_END       -> "■";
                default -> "•";
            };
            String label = (selected ? "§e▶ " : "  ")
                    + (isStart ? "§a★" : " ")
                    + " " + typeIcon + " "
                    + nodePreview(n);
            final String nid = n.id;
            scr.addRenderableWidget(Button.builder(Component.literal(label), b -> {
                scr.pullAllFields();
                scr.selectedNodeId = nid;
                scr.editingChoiceId = "";
                scr.rebuildAll();
            }).bounds(x + 2, y + 14 + (i - from) * ROW_H, COL2_W - 4, ROW_H - 2).build());
        }

        // Add-node strip + node ops
        int btnY = oy + TOP_H + H - TOP_H - BOT_H - 8 - 4 - 16 * 5 - 16;

        addNodeButton(scr, x + 2, btnY,                     COL2_W - 4, "+ Фраза",   NpcSceneNode.TYPE_SPEECH);
        addNodeButton(scr, x + 2, btnY + 16,                COL2_W - 4, "+ Вопрос",  NpcSceneNode.TYPE_QUESTION);
        addNodeButton(scr, x + 2, btnY + 32,                COL2_W - 4, "+ Действие",NpcSceneNode.TYPE_ACTION);
        addNodeButton(scr, x + 2, btnY + 48,                COL2_W - 4, "+ Условие", NpcSceneNode.TYPE_CONDITION);
        addNodeButton(scr, x + 2, btnY + 64,                COL2_W - 4, "+ Конец",   NpcSceneNode.TYPE_END);

        boolean haveSelection = !scr.selectedNodeId.isEmpty()
                && scr.draftScene.getNode(scr.selectedNodeId) != null;

        scr.addRenderableWidget(Button.builder(Component.literal("⎘ Дублировать"), b -> {
            if (!haveSelection) return;
            scr.pullAllFields();
            NpcSceneNode src = scr.draftScene.getNode(scr.selectedNodeId);
            NpcSceneNode dup = src.copy();
            dup.id = java.util.UUID.randomUUID().toString().substring(0, 8);
            scr.draftScene.nodes.add(dup);
            scr.selectedNodeId = dup.id;
            scr.rebuildAll();
        }).bounds(x + 2, btnY + 80, (COL2_W - 4) / 2 - 1, 14).build());

        scr.addRenderableWidget(Button.builder(Component.literal("🗑 Удалить"), b -> {
            if (!haveSelection) return;
            scr.pullAllFields();
            scr.draftScene.removeNode(scr.selectedNodeId);
            scr.selectedNodeId = "";
            scr.editingChoiceId = "";
            scr.rebuildAll();
        }).bounds(x + 2 + (COL2_W - 4) / 2 + 1, btnY + 80,
                  (COL2_W - 4) / 2 - 1, 14).build());

        scr.addRenderableWidget(Button.builder(Component.literal("★ Сделать стартовым"), b -> {
            if (!haveSelection) return;
            scr.pullAllFields();
            scr.draftScene.startNodeId = scr.selectedNodeId;
            scr.rebuildAll();
        }).bounds(x + 2, btnY + 96, COL2_W - 4, 14).build());
    }

    static void render(NpcSceneEditorScreen scr, GuiGraphics g, int ox, int oy, int mx, int my) {
        var font = Minecraft.getInstance().font;
        int x = ox + PAD + COL1_W + COL_GAP;
        int y = oy + TOP_H + 4;
        int h = H - TOP_H - BOT_H - 8;

        g.fill(x, y, x + COL2_W, y + h, 0xAA131320);
        g.fill(x, y, x + COL2_W, y + 1, ACCENT_NODE);
        NpcEditorUtils.brd(g, x, y, COL2_W, h, 0xFF2A2A44);
        g.drawString(font, "§l§7УЗЛЫ", x + 4, y + 3, ACCENT_NODE, false);

        if (scr.draftScene == null) {
            g.drawString(font, "§8Сначала создайте", x + 6, y + 24, 0xFF555566, false);
            g.drawString(font, "§8или выберите сцену", x + 6, y + 36, 0xFF555566, false);
        } else {
            int total = scr.draftScene.nodes.size();
            if (total > VISIBLE_ROWS) {
                g.drawString(font, "§8" + (scr.nodeScroll + 1) + "–"
                                + Math.min(total, scr.nodeScroll + VISIBLE_ROWS) + "/" + total,
                        x + COL2_W - 50, y + 3, 0xFF666677, false);
            }
        }
    }

    private static String nodePreview(NpcSceneNode n) {
        String s = switch (n.type) {
            case NpcSceneNode.TYPE_SPEECH    -> n.text;
            case NpcSceneNode.TYPE_QUESTION  -> n.text;
            case NpcSceneNode.TYPE_ACTION    -> NpcSceneNode.actionLabel(n.actionType);
            case NpcSceneNode.TYPE_CONDITION -> NpcSceneNode.condLabel(n.conditionType);
            case NpcSceneNode.TYPE_END       -> "конец";
            default -> n.id;
        };
        if (s == null) s = "";
        if (s.isBlank()) s = "§8(пусто)";
        return s.length() > 14 ? s.substring(0, 14) + "…" : s;
    }

    private static void addNodeButton(NpcSceneEditorScreen scr, int x, int y, int w,
                                      String label, String nodeType) {
        scr.addRenderableWidget(Button.builder(Component.literal(label), b -> {
            if (scr.draftScene == null) return;
            scr.pullAllFields();
            NpcSceneNode n = scr.draftScene.addNode(nodeType);
            scr.selectedNodeId = n.id;
            scr.editingChoiceId = "";
            scr.rebuildAll();
        }).bounds(x, y, w, 14).build());
    }
}

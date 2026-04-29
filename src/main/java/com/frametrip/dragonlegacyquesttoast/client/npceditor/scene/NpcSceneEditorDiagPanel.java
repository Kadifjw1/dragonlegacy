package com.frametrip.dragonlegacyquesttoast.client.npceditor.scene;

import com.frametrip.dragonlegacyquesttoast.client.npceditor.NpcEditorUtils;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcSceneValidator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.frametrip.dragonlegacyquesttoast.client.npceditor.scene.NpcSceneEditorScreen.*;

/** Bottom zone — preview, validation buttons + issue list. */
final class NpcSceneEditorDiagPanel {

    private NpcSceneEditorDiagPanel() {}

    static void init(NpcSceneEditorScreen scr, int ox, int oy) {
        int x = ox + PAD;
        int w = W - PAD * 2;
        int y = oy + H - BOT_H + 4;

        // Action buttons row
        int btnW = 128;
        int gap = 4;
        scr.addRenderableWidget(Button.builder(Component.literal("✓ Проверить"), b -> {
            scr.pullAllFields();
            scr.runDiagnostics();
            scr.rebuildAll();
        }).bounds(x, y, btnW, 16).build());

        scr.addRenderableWidget(Button.builder(Component.literal("▶ Предпросмотр"), b -> {
            scr.pullAllFields();
            scr.runPreview(null);
        }).bounds(x + btnW + gap, y, btnW, 16).build());

        scr.addRenderableWidget(Button.builder(Component.literal("▶▷ Тест узла"), b -> {
            scr.pullAllFields();
            String start = scr.selectedNodeId.isEmpty()
                    ? (scr.draftScene == null ? null : scr.draftScene.startNodeId)
                    : scr.selectedNodeId;
            scr.runPreview(start);
        }).bounds(x + (btnW + gap) * 2, y, btnW, 16).build());

        scr.addRenderableWidget(Button.builder(Component.literal("↺ Автовыравнивание"), b -> {
            scr.autoLayoutMode = 0;
            NpcSceneEditorCanvas.runAutoLayout(scr);
            scr.rebuildAll();
        }).bounds(x + (btnW + gap) * 3, y, btnW + 14, 16).build());

        scr.addRenderableWidget(Button.builder(Component.literal("⌖ Центр графа"), b -> {
            scr.canvasPanX = 0;
            scr.canvasPanY = 0;
            scr.canvasZoom = 1.0f;
        }).bounds(x + (btnW + gap) * 4 + 14, y, btnW - 10, 16).build());

        // Counts at right
        int errorN = (int) scr.issues.stream().filter(i -> i.level == NpcSceneValidator.Level.ERROR).count();
        int warnN  = (int) scr.issues.stream().filter(i -> i.level == NpcSceneValidator.Level.WARN).count();
        int nodeN = scr.draftScene == null ? 0 : scr.draftScene.nodes.size();
        int edgeN = scr.draftScene == null ? 0 : scr.draftScene.nodes.stream().mapToInt(n -> {
            if (n.nextNodeId != null && !n.nextNodeId.isBlank()) return 1;
            if (n.actionNextNodeId != null && !n.actionNextNodeId.isBlank()) return 1;
            int out = 0;
            if (n.trueNextNodeId != null && !n.trueNextNodeId.isBlank()) out++;
            if (n.falseNextNodeId != null && !n.falseNextNodeId.isBlank()) out++;
            if (n.choices != null) out += (int) n.choices.stream().filter(c -> c.nextNodeId != null && !c.nextNodeId.isBlank()).count();
            if (n.branchOptions != null) out += (int) n.branchOptions.stream().filter(c -> c.nextNodeId != null && !c.nextNodeId.isBlank()).count();
            return out;
        }).sum();
        scr.addRenderableWidget(Button.builder(
                Component.literal("§cОшибки: " + errorN + "  §eПредупр.: " + warnN + "  §bУзлы: " + nodeN + "  §dВетки: " + edgeN),
                b -> {}
        ).bounds(x + w - 278, y, 278, 16).build());
    }

    static void render(NpcSceneEditorScreen scr, GuiGraphics g, int ox, int oy, int mx, int my) {
        var font = Minecraft.getInstance().font;
        int x = ox + PAD;
        int w = W - PAD * 2;
        int y = oy + H - BOT_H;

        // Card
        g.fill(x, y, x + w, y + BOT_H - PAD, 0xAA131320);
        g.fill(x, y, x + w, y + 1, ACCENT_DIAG);
        NpcEditorUtils.brd(g, x, y, w, BOT_H - PAD, 0xFF2A2A44);
        g.drawString(font, "§l§7ДИАГНОСТИКА И ПРЕДПРОСМОТР", x + 4, y + 3, ACCENT_DIAG, false);

        // Issues list
        int listY = y + 26;
        int rowH = 11;
        int maxRows = (BOT_H - PAD - 30) / rowH;
        List<NpcSceneValidator.Issue> issues = scr.issues;
        int from = Math.min(scr.issueScroll, Math.max(0, issues.size() - maxRows));
        int to = Math.min(issues.size(), from + maxRows);

        if (issues.isEmpty()) {
            g.drawString(font, "§8(нет результатов — нажмите «Проверить сцену»)",
                    x + 8, listY, 0xFF555566, false);
        } else {
            for (int i = from; i < to; i++) {
                NpcSceneValidator.Issue is = issues.get(i);
                String prefix = is.level == NpcSceneValidator.Level.ERROR ? "§c⨯ " : "§e⚠ ";
                String node = is.nodeId == null || is.nodeId.isEmpty() ? "" : "§8[" + is.nodeId + "]§r ";
                String text = prefix + node + is.message;
                if (text.length() > 200) text = text.substring(0, 200) + "…";
                g.drawString(font, text, x + 8, listY + (i - from) * rowH, 0xFFCCCCCC, false);
            }
            if (issues.size() > maxRows) {
                g.drawString(font, "§8" + (from + 1) + "–" + to + "/" + issues.size(),
                        x + w - 70, y + 3, 0xFF666677, false);
            }
        }
    }
}

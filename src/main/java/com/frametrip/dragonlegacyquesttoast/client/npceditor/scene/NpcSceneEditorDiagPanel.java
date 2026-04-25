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
        int btnW = 140;
        int gap = 4;
        scr.addRenderableWidget(Button.builder(Component.literal("✓ Проверить сцену"), b -> {
            scr.pullAllFields();
            scr.runDiagnostics();
            scr.rebuildAll();
        }).bounds(x, y, btnW, 16).build());

        scr.addRenderableWidget(Button.builder(Component.literal("▶ Предпросмотр"), b -> {
            scr.pullAllFields();
            scr.runPreview(null);
        }).bounds(x + btnW + gap, y, btnW, 16).build());

        scr.addRenderableWidget(Button.builder(Component.literal("▶▷ С текущего узла"), b -> {
            scr.pullAllFields();
            String start = scr.selectedNodeId.isEmpty()
                    ? (scr.draftScene == null ? null : scr.draftScene.startNodeId)
                    : scr.selectedNodeId;
            scr.runPreview(start);
        }).bounds(x + (btnW + gap) * 2, y, btnW + 6, 16).build());

        // Counts at right
        int errorN = (int) scr.issues.stream().filter(i -> i.level == NpcSceneValidator.Level.ERROR).count();
        int warnN  = (int) scr.issues.stream().filter(i -> i.level == NpcSceneValidator.Level.WARN).count();
        scr.addRenderableWidget(Button.builder(
                Component.literal("§cОшибок: " + errorN + "  §eПредупр.: " + warnN),
                b -> {}
        ).bounds(x + w - 180, y, 180, 16).build());
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

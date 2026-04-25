package com.frametrip.dragonlegacyquesttoast.client.npceditor.scene;

import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcChoiceOption;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcSceneNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.UUID;

import static com.frametrip.dragonlegacyquesttoast.client.npceditor.scene.NpcSceneEditorScreen.*;

/**
 * Right-zone editors for question nodes and individual choice options.
 * Two modes: question (shows choice list) or choice (single-choice editor).
 */
final class NpcSceneEditorChoicePanel {

    private NpcSceneEditorChoicePanel() {}

    // ── Question editor (shows choices list) ───────────────────────────────
    static void initQuestion(NpcSceneEditorScreen scr, int x, int y, NpcSceneNode q) {
        var font = Minecraft.getInstance().font;

        // Question text
        scr.nodeTextBox = new EditBox(font, x + 8, y + 8, COL3_W - 16, 16,
                Component.literal("Текст вопроса"));
        scr.nodeTextBox.setMaxLength(256);
        scr.nodeTextBox.setValue(q.text);
        scr.addRenderableWidget(scr.nodeTextBox);

        // Speaker name (optional, override NPC default)
        scr.nodeSpeakerBox = new EditBox(font, x + 8, y + 30, (COL3_W - 16) / 2 - 2, 14,
                Component.literal("Говорящий"));
        scr.nodeSpeakerBox.setMaxLength(48);
        scr.nodeSpeakerBox.setValue(q.speakerName);
        scr.nodeSpeakerBox.setHint(Component.literal("Имя…").withStyle(s -> s.withColor(0xFF555566)));
        scr.addRenderableWidget(scr.nodeSpeakerBox);

        // Emotion cycle
        int ei = NpcSceneEditorNodePanel.indexOf(NpcSceneNode.EMOTION_IDS, q.emotion);
        scr.addRenderableWidget(Button.builder(
                Component.literal("◀▶ " + NpcSceneNode.emotionLabel(q.emotion)),
                b -> {
                    scr.pullAllFields();
                    q.emotion = NpcSceneNode.EMOTION_IDS[(ei + 1) % NpcSceneNode.EMOTION_IDS.length];
                    scr.rebuildAll();
                }
        ).bounds(x + 8 + (COL3_W - 16) / 2 + 2, y + 30, (COL3_W - 16) / 2 - 2, 14).build());

        // Add choice button
        scr.addRenderableWidget(Button.builder(Component.literal("+ Ответ"), b -> {
            scr.pullAllFields();
            NpcChoiceOption c = new NpcChoiceOption();
            c.text = "Новый ответ";
            q.choices.add(c);
            scr.editingChoiceId = c.id;
            scr.rebuildAll();
        }).bounds(x + 8, y + 50, (COL3_W - 16) / 2 - 2, 14).build());

        scr.addRenderableWidget(Button.builder(Component.literal("§8Кол-во: " + q.choices.size()),
                b -> {}).bounds(x + 8 + (COL3_W - 16) / 2 + 2, y + 50,
                                (COL3_W - 16) / 2 - 2, 14).build());

        // Choices list (compact rows)
        int rowY = y + 70;
        int rowH = 16;
        int maxRows = 9;
        int from = 0;
        int to = Math.min(q.choices.size(), maxRows);
        for (int i = from; i < to; i++) {
            NpcChoiceOption c = q.choices.get(i);
            String t = c.text == null || c.text.isBlank() ? "(пусто)" : c.text;
            if (t.length() > 28) t = t.substring(0, 28) + "…";
            String mark = (c.actionType != null && !c.actionType.isBlank()) ? "§6⚡" : "  ";
            mark += (c.conditionType != null && !c.conditionType.isBlank()) ? "§d?" : "  ";
            String label = "§e▶ §f" + t + " §7" + mark;
            final String cid = c.id;
            int rowx = x + 8;
            int rowwBtn = COL3_W - 16 - 60;
            scr.addRenderableWidget(Button.builder(Component.literal(label), b -> {
                scr.pullAllFields();
                scr.editingChoiceId = cid;
                scr.rebuildAll();
            }).bounds(rowx, rowY + i * rowH, rowwBtn, rowH - 2).build());

            // Up / Down / Del
            final int idx = i;
            scr.addRenderableWidget(Button.builder(Component.literal("▲"), b -> {
                if (idx <= 0) return;
                scr.pullAllFields();
                java.util.Collections.swap(q.choices, idx, idx - 1);
                scr.rebuildAll();
            }).bounds(rowx + rowwBtn + 2, rowY + i * rowH, 14, rowH - 2).build());

            scr.addRenderableWidget(Button.builder(Component.literal("▼"), b -> {
                if (idx >= q.choices.size() - 1) return;
                scr.pullAllFields();
                java.util.Collections.swap(q.choices, idx, idx + 1);
                scr.rebuildAll();
            }).bounds(rowx + rowwBtn + 18, rowY + i * rowH, 14, rowH - 2).build());

            scr.addRenderableWidget(Button.builder(Component.literal("✕"), b -> {
                scr.pullAllFields();
                q.choices.remove(idx);
                scr.rebuildAll();
            }).bounds(rowx + rowwBtn + 34, rowY + i * rowH, 14, rowH - 2).build());

            scr.addRenderableWidget(Button.builder(Component.literal("⎘"), b -> {
                scr.pullAllFields();
                NpcChoiceOption dup = c.copy();
                dup.id = UUID.randomUUID().toString().substring(0, 6);
                q.choices.add(idx + 1, dup);
                scr.rebuildAll();
            }).bounds(rowx + rowwBtn + 50, rowY + i * rowH, 14, rowH - 2).build());
        }
    }

    // ── Single-choice editor (when editingChoiceId is set) ─────────────────
    static void init(NpcSceneEditorScreen scr, int x, int y, NpcSceneNode question) {
        if (question.choices == null) return;
        NpcChoiceOption c = null;
        for (NpcChoiceOption o : question.choices) if (scr.editingChoiceId.equals(o.id)) { c = o; break; }
        if (c == null) { scr.editingChoiceId = ""; scr.rebuildAll(); return; }
        final NpcChoiceOption choice = c;
        var font = Minecraft.getInstance().font;

        // Back button
        scr.addRenderableWidget(Button.builder(
                Component.literal("◀ К вопросу"),
                b -> {
                    scr.pullAllFields();
                    scr.editingChoiceId = "";
                    scr.rebuildAll();
                }
        ).bounds(x + 8, y + 8, 100, 14).build());

        // Choice text
        scr.choiceTextBox = new EditBox(font, x + 8, y + 26, COL3_W - 16, 16,
                Component.literal("Текст ответа"));
        scr.choiceTextBox.setMaxLength(160);
        scr.choiceTextBox.setValue(choice.text);
        scr.addRenderableWidget(scr.choiceTextBox);

        // Next-node cycler
        NpcSceneEditorNodePanel.cycleNodeNext(scr, x + 8, y + 48, COL3_W - 16,
                "→ Перейти к", choice.nextNodeId,
                id -> { scr.pullAllFields(); choice.nextNodeId = id; scr.rebuildAll(); });

        // ── Condition for showing this choice ─────────────────────────────
        scr.addRenderableWidget(Button.builder(
                Component.literal("§l§7Условие показа"),
                b -> {}
        ).bounds(x + 8, y + 68, COL3_W - 16, 12).build());

        String[] condIdsWithEmpty = withEmpty(NpcSceneNode.COND_IDS);
        int ci = indexOfOrZero(condIdsWithEmpty, choice.conditionType);
        scr.addRenderableWidget(Button.builder(
                Component.literal("◀▶ " + (ci == 0 ? "§7всегда показывать"
                        : NpcSceneNode.condLabel(choice.conditionType))),
                b -> {
                    scr.pullAllFields();
                    choice.conditionType = condIdsWithEmpty[(ci + 1) % condIdsWithEmpty.length];
                    scr.rebuildAll();
                }
        ).bounds(x + 8, y + 82, COL3_W - 16, 14).build());

        scr.choiceCondParamBox = new EditBox(font, x + 8, y + 100, COL3_W - 16, 14,
                Component.literal("Параметр условия"));
        scr.choiceCondParamBox.setMaxLength(160);
        scr.choiceCondParamBox.setValue(choice.conditionParam);
        scr.choiceCondParamBox.setHint(Component.literal("…").withStyle(s -> s.withColor(0xFF555566)));
        scr.addRenderableWidget(scr.choiceCondParamBox);

        // ── Action on choose ──────────────────────────────────────────────
        scr.addRenderableWidget(Button.builder(
                Component.literal("§l§7Действие при выборе"),
                b -> {}
        ).bounds(x + 8, y + 120, COL3_W - 16, 12).build());

        String[] actIdsWithEmpty = withEmpty(NpcSceneNode.ACTION_IDS);
        int ai = indexOfOrZero(actIdsWithEmpty, choice.actionType);
        scr.addRenderableWidget(Button.builder(
                Component.literal("◀▶ " + (ai == 0 ? "§7без действия"
                        : NpcSceneNode.actionLabel(choice.actionType))),
                b -> {
                    scr.pullAllFields();
                    choice.actionType = actIdsWithEmpty[(ai + 1) % actIdsWithEmpty.length];
                    scr.rebuildAll();
                }
        ).bounds(x + 8, y + 134, COL3_W - 16, 14).build());

        scr.choiceActionParamBox = new EditBox(font, x + 8, y + 152, COL3_W - 16, 14,
                Component.literal("Параметр действия"));
        scr.choiceActionParamBox.setMaxLength(160);
        scr.choiceActionParamBox.setValue(choice.actionParam);
        scr.choiceActionParamBox.setHint(Component.literal("…").withStyle(s -> s.withColor(0xFF555566)));
        scr.addRenderableWidget(scr.choiceActionParamBox);
    }

    private static String[] withEmpty(String[] arr) {
        String[] out = new String[arr.length + 1];
        out[0] = "";
        System.arraycopy(arr, 0, out, 1, arr.length);
        return out;
    }

    private static int indexOfOrZero(String[] arr, String val) {
        if (val == null) return 0;
        for (int i = 0; i < arr.length; i++) if (arr[i].equals(val)) return i;
        return 0;
    }
}

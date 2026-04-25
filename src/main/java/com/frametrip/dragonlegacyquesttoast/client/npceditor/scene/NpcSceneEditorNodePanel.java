package com.frametrip.dragonlegacyquesttoast.client.npceditor.scene;

import com.frametrip.dragonlegacyquesttoast.client.ClientQuestState;
import com.frametrip.dragonlegacyquesttoast.client.dialogue.ClientNpcSceneState;
import com.frametrip.dragonlegacyquesttoast.client.npceditor.NpcEditorUtils;
import com.frametrip.dragonlegacyquesttoast.server.QuestDefinition;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcScene;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcSceneNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.frametrip.dragonlegacyquesttoast.client.npceditor.scene.NpcSceneEditorScreen.*;

/** Right zone — node property editor. Dispatches per node type. */
final class NpcSceneEditorNodePanel {

    private NpcSceneEditorNodePanel() {}

    static void init(NpcSceneEditorScreen scr, int ox, int oy) {
        if (scr.draftScene == null) return;
        int x = ox + PAD + COL1_W + COL_GAP + COL2_W + COL_GAP;
        int y = oy + TOP_H + 4 + 14; // leave room for header

        if (scr.selectedNodeId.isEmpty()) {
            initSceneProps(scr, x, y);
            return;
        }
        NpcSceneNode n = scr.draftScene.getNode(scr.selectedNodeId);
        if (n == null) return;

        if (!scr.editingChoiceId.isEmpty()) {
            NpcSceneEditorChoicePanel.init(scr, x, y, n);
            return;
        }

        switch (n.type) {
            case NpcSceneNode.TYPE_SPEECH    -> initSpeech(scr, x, y, n);
            case NpcSceneNode.TYPE_QUESTION  -> NpcSceneEditorChoicePanel.initQuestion(scr, x, y, n);
            case NpcSceneNode.TYPE_ACTION    -> initAction(scr, x, y, n);
            case NpcSceneNode.TYPE_CONDITION -> initCondition(scr, x, y, n);
            case NpcSceneNode.TYPE_END       -> initEnd(scr, x, y);
            default -> {}
        }
    }

    static void render(NpcSceneEditorScreen scr, GuiGraphics g, int ox, int oy, int mx, int my) {
        var font = Minecraft.getInstance().font;
        int x = ox + PAD + COL1_W + COL_GAP + COL2_W + COL_GAP;
        int y = oy + TOP_H + 4;
        int h = H - TOP_H - BOT_H - 8;

        g.fill(x, y, x + COL3_W, y + h, 0xAA131320);
        g.fill(x, y, x + COL3_W, y + 1, ACCENT_EDIT);
        NpcEditorUtils.brd(g, x, y, COL3_W, h, 0xFF2A2A44);

        String title;
        if (scr.draftScene == null) {
            title = "§l§7РЕДАКТОР";
            g.drawString(font, title, x + 4, y + 3, ACCENT_EDIT, false);
            g.drawString(font, "§8Создайте или выберите сцену слева",
                    x + 12, y + 28, 0xFF555566, false);
            return;
        }
        if (scr.selectedNodeId.isEmpty()) {
            title = "§l§7СВОЙСТВА СЦЕНЫ";
        } else if (!scr.editingChoiceId.isEmpty()) {
            title = "§l§7ОТВЕТ";
        } else {
            NpcSceneNode n = scr.draftScene.getNode(scr.selectedNodeId);
            String tlabel = n == null ? "?" : NpcSceneNode.typeLabel(n.type);
            int tcol = n == null ? 0xFFCCCCCC : NpcSceneEditorScreen.colorOfNodeType(n.type);
            title = "§l§7УЗЕЛ — ";
            g.drawString(font, title, x + 4, y + 3, ACCENT_EDIT, false);
            g.drawString(font, tlabel, x + 4 + font.width(title), y + 3, tcol, false);
            return;
        }
        g.drawString(font, title, x + 4, y + 3, ACCENT_EDIT, false);
    }

    // ── Scene properties (when no node selected) ───────────────────────────
    private static void initSceneProps(NpcSceneEditorScreen scr, int x, int y) {
        NpcScene s = scr.draftScene;
        var font = Minecraft.getInstance().font;

        scr.sceneNameBox = new EditBox(font, x + 8, y + 8, COL3_W - 16, 16,
                Component.literal("Название"));
        scr.sceneNameBox.setMaxLength(64);
        scr.sceneNameBox.setValue(s.name == null ? "" : s.name);
        scr.addRenderableWidget(scr.sceneNameBox);

        // Type cycle
        int ti = indexOf(NpcScene.TYPE_IDS, s.type);
        scr.addRenderableWidget(Button.builder(
                Component.literal("◀▶ Тип: " + NpcScene.typeLabel(s.type)),
                    b -> {
                    scr.pullAllFields();
                    s.type = NpcScene.TYPE_IDS[(ti + 1) % NpcScene.TYPE_IDS.length];
                    scr.rebuildAll();
                }
        ).bounds(x + 8, y + 30, COL3_W - 16, 14).build());

        // Repeatable / Enabled toggles
        scr.addRenderableWidget(Button.builder(
                Component.literal(s.repeatable ? "§a↻ Повторяемая" : "§7↻ Однократная"),
                b -> { scr.pullAllFields(); s.repeatable = !s.repeatable; scr.rebuildAll(); }
        ).bounds(x + 8, y + 48, (COL3_W - 16) / 2 - 2, 14).build());

        scr.addRenderableWidget(Button.builder(
                Component.literal(s.enabled ? "§a● Включена" : "§c○ Выключена"),
                b -> { scr.pullAllFields(); s.enabled = !s.enabled; scr.rebuildAll(); }
        ).bounds(x + 8 + (COL3_W - 16) / 2 + 2, y + 48,
                (COL3_W - 16) / 2 - 2, 14).build());

        // Description
        scr.sceneDescBox = new EditBox(font, x + 8, y + 70, COL3_W - 16, 16,
                Component.literal("Описание"));
        scr.sceneDescBox.setMaxLength(200);
        scr.sceneDescBox.setValue(s.description == null ? "" : s.description);
        scr.addRenderableWidget(scr.sceneDescBox);

        // Start node cycler
        cycleNodeNext(scr, x + 8, y + 92, COL3_W - 16,
                "★ Старт", s.startNodeId,
                id -> { scr.pullAllFields(); s.startNodeId = id; scr.rebuildAll(); });
    }

    // ── Speech editor ──────────────────────────────────────────────────────
    private static void initSpeech(NpcSceneEditorScreen scr, int x, int y, NpcSceneNode n) {
        var font = Minecraft.getInstance().font;

        scr.nodeTextBox = new EditBox(font, x + 8, y + 8, COL3_W - 16, 16,
                Component.literal("Текст фразы"));
        scr.nodeTextBox.setMaxLength(256);
        scr.nodeTextBox.setValue(n.text);
        scr.addRenderableWidget(scr.nodeTextBox);

        scr.nodeSpeakerBox = new EditBox(font, x + 8, y + 30, (COL3_W - 16) / 2 - 2, 14,
                Component.literal("Говорящий"));
        scr.nodeSpeakerBox.setMaxLength(48);
        scr.nodeSpeakerBox.setValue(n.speakerName);
        scr.nodeSpeakerBox.setHint(Component.literal("Имя…").withStyle(s -> s.withColor(0xFF555566)));
        scr.addRenderableWidget(scr.nodeSpeakerBox);

        // Emotion cycle
        int ei = indexOf(NpcSceneNode.EMOTION_IDS, n.emotion);
        scr.addRenderableWidget(Button.builder(
                Component.literal("◀▶ " + NpcSceneNode.emotionLabel(n.emotion)),
                b -> {
                    scr.pullAllFields();
                    n.emotion = NpcSceneNode.EMOTION_IDS[(ei + 1) % NpcSceneNode.EMOTION_IDS.length];
                    scr.rebuildAll();
                }
        ).bounds(x + 8 + (COL3_W - 16) / 2 + 2, y + 30, (COL3_W - 16) / 2 - 2, 14).build());

        // Animation + Sound
        scr.nodeAnimBox = new EditBox(font, x + 8, y + 48, (COL3_W - 16) / 2 - 2, 14,
                Component.literal("Анимация"));
        scr.nodeAnimBox.setMaxLength(64);
        scr.nodeAnimBox.setValue(n.animationId);
        scr.nodeAnimBox.setHint(Component.literal("ID анимации").withStyle(s -> s.withColor(0xFF555566)));
        scr.addRenderableWidget(scr.nodeAnimBox);

        scr.nodeSoundBox = new EditBox(font, x + 8 + (COL3_W - 16) / 2 + 2, y + 48,
                (COL3_W - 16) / 2 - 2, 14, Component.literal("Звук"));
        scr.nodeSoundBox.setMaxLength(64);
        scr.nodeSoundBox.setValue(n.soundId);
        scr.nodeSoundBox.setHint(Component.literal("minecraft:…").withStyle(s -> s.withColor(0xFF555566)));
        scr.addRenderableWidget(scr.nodeSoundBox);

        // Next-node cycler
        cycleNodeNext(scr, x + 8, y + 70, COL3_W - 16,
                "→ Следующий", n.nextNodeId,
                id -> { scr.pullAllFields(); n.nextNodeId = id; scr.rebuildAll(); });

        // Incoming refs hint
        renderIncomingHint(scr, x + 8, y + 92, n.id);
    }

    // ── Action editor ──────────────────────────────────────────────────────
    private static void initAction(NpcSceneEditorScreen scr, int x, int y, NpcSceneNode n) {
        var font = Minecraft.getInstance().font;

        // Action type cycle
        int ai = indexOf(NpcSceneNode.ACTION_IDS, n.actionType);
        scr.addRenderableWidget(Button.builder(
                Component.literal("◀▶ " + NpcSceneNode.actionLabel(n.actionType)),
                b -> {
                    scr.pullAllFields();
                    n.actionType = NpcSceneNode.ACTION_IDS[(ai + 1) % NpcSceneNode.ACTION_IDS.length];
                    scr.rebuildAll();
                }
        ).bounds(x + 8, y + 8, COL3_W - 16, 14).build());

        // Param: free text + helper picker for some types
        scr.nodeActionParamBox = new EditBox(font, x + 8, y + 26, COL3_W - 16, 16,
                  Component.literal("Параметр"));
        scr.nodeActionParamBox.setMaxLength(160);
        scr.nodeActionParamBox.setValue(n.actionParam);
        scr.nodeActionParamBox.setHint(actionHint(n.actionType));
        scr.addRenderableWidget(scr.nodeActionParamBox);

        // Quest picker for quest actions
        if (NpcSceneNode.ACTION_GIVE_QUEST.equals(n.actionType)
                || NpcSceneNode.ACTION_COMPLETE_QUEST.equals(n.actionType)
                || NpcSceneNode.ACTION_FAIL_QUEST.equals(n.actionType)) {
            initQuestPicker(scr, x + 8, y + 48, COL3_W - 16,
                    n.actionParam, id -> n.actionParam = id);
        }
        // Scene picker for OPEN_SCENE
        if (NpcSceneNode.ACTION_OPEN_SCENE.equals(n.actionType)) {
            initScenePicker(scr, x + 8, y + 48, COL3_W - 16,
                    n.actionParam, id -> n.actionParam = id);
        }
        // Relation cycler for SET_RELATION
        if (NpcSceneNode.ACTION_SET_RELATION.equals(n.actionType)) {
            String[] vals = {"FRIENDLY", "NEUTRAL", "HOSTILE"};
            int ri = Math.max(0, indexOf(vals, n.actionParam == null ? "" : n.actionParam.toUpperCase()));
            scr.addRenderableWidget(Button.builder(
                    Component.literal("◀▶ Значение: " + vals[ri]),
                    b -> {
                        scr.pullAllFields();
                        n.actionParam = vals[(ri + 1) % vals.length];
                        scr.rebuildAll();
                    }
            ).bounds(x + 8, y + 48, COL3_W - 16, 14).build());
        }

        cycleNodeNext(scr, x + 8, y + 70, COL3_W - 16,
                "→ Следующий", n.actionNextNodeId,
                id -> { scr.pullAllFields(); n.actionNextNodeId = id; scr.rebuildAll(); });

        renderIncomingHint(scr, x + 8, y + 92, n.id);
    }

    // ── Condition editor ───────────────────────────────────────────────────
    private static void initCondition(NpcSceneEditorScreen scr, int x, int y, NpcSceneNode n) {
        var font = Minecraft.getInstance().font;

        int ci = indexOf(NpcSceneNode.COND_IDS, n.conditionType);
        scr.addRenderableWidget(Button.builder(
                Component.literal("◀▶ " + NpcSceneNode.condLabel(n.conditionType)),
                b -> {
                    scr.pullAllFields();
                    n.conditionType = NpcSceneNode.COND_IDS[(ci + 1) % NpcSceneNode.COND_IDS.length];
                    scr.rebuildAll();
                }
        ).bounds(x + 8, y + 8, COL3_W - 16, 14).build());

        scr.nodeCondParamBox = new EditBox(font, x + 8, y + 26, COL3_W - 16, 16,
                Component.literal("Параметр"));
        scr.nodeCondParamBox.setMaxLength(160);
        scr.nodeCondParamBox.setValue(n.conditionParam);
        scr.nodeCondParamBox.setHint(condHint(n.conditionType));
        scr.addRenderableWidget(scr.nodeCondParamBox);

        // Quest picker for quest-related conditions
        if (NpcSceneNode.COND_QUEST_ACTIVE.equals(n.conditionType)
                || NpcSceneNode.COND_QUEST_COMPLETE.equals(n.conditionType)
                || NpcSceneNode.COND_QUEST_NOT_TAKEN.equals(n.conditionType)) {
            initQuestPicker(scr, x + 8, y + 48, COL3_W - 16,
                    n.conditionParam, id -> n.conditionParam = id);
        }

        cycleNodeNext(scr, x + 8, y + 70, COL3_W - 16,
                "✓ TRUE  →", n.trueNextNodeId,
                id -> { scr.pullAllFields(); n.trueNextNodeId = id; scr.rebuildAll(); });

        cycleNodeNext(scr, x + 8, y + 88, COL3_W - 16,
                "✗ FALSE →", n.falseNextNodeId,
                id -> { scr.pullAllFields(); n.falseNextNodeId = id; scr.rebuildAll(); });

        renderIncomingHint(scr, x + 8, y + 110, n.id);
    }

    // ── End editor ─────────────────────────────────────────────────────────
    private static void initEnd(NpcSceneEditorScreen scr, int x, int y) {
        scr.addRenderableWidget(Button.builder(
                Component.literal("§7Узел завершает сцену"),
                b -> {}
        ).bounds(x + 8, y + 8, COL3_W - 16, 14).build());
    }

    // ── Helpers (also reused by question/choice panel) ─────────────────────
    static void cycleNodeNext(NpcSceneEditorScreen scr, int x, int y, int w,
                              String label, String currentId,
                              java.util.function.Consumer<String> apply) {
        List<NpcSceneNode> nodes = scr.draftScene.nodes;
        int cur = -1;
        for (int i = 0; i < nodes.size(); i++)
            if (nodes.get(i).id.equals(currentId)) { cur = i; break; }
        String label2 = (currentId == null || currentId.isEmpty()) ? "§8(конец)"
                : nodes.stream().filter(nn -> nn.id.equals(currentId))
                       .map(nn -> "§f" + shortLabel(nn))
                       .findFirst().orElse("§c" + currentId);

  scr.addRenderableWidget(Button.builder(
                Component.literal(label + " " + label2),
                b -> {
                    scr.pullAllFields();
                    int next = (cur + 1) % (nodes.size() + 1);
                    apply.accept(next >= nodes.size() ? "" : nodes.get(next).id);
                    scr.rebuildAll();
                }
        ).bounds(x, y, w, 14).build());
    }

    static String shortLabel(NpcSceneNode n) {
        String s = n.type.equals(NpcSceneNode.TYPE_END) ? "конец" : n.displayLabel();
        if (s.length() > 28) s = s.substring(0, 28) + "…";
        return s;
    }

    static int indexOf(String[] arr, String val) {
        for (int i = 0; i < arr.length; i++) if (arr[i].equals(val)) return i;
        return 0;
    }

    private static Component actionHint(String type) {
        String h = switch (type) {
            case NpcSceneNode.ACTION_GIVE_QUEST,
                 NpcSceneNode.ACTION_COMPLETE_QUEST,
                 NpcSceneNode.ACTION_FAIL_QUEST -> "ID квеста";
            case NpcSceneNode.ACTION_SET_RELATION -> "FRIENDLY/NEUTRAL/HOSTILE";
            case NpcSceneNode.ACTION_SET_FACTION_RELATION -> "<faction>:<value>";
            case NpcSceneNode.ACTION_GIVE_ITEM,
                 NpcSceneNode.ACTION_TAKE_ITEM -> "minecraft:diamond[*count]";
            case NpcSceneNode.ACTION_PLAY_SOUND -> "minecraft:entity.villager.yes";
            case NpcSceneNode.ACTION_PLAY_ANIMATION -> "ID анимации";
            case NpcSceneNode.ACTION_OPEN_SCENE -> "ID сцены";
            default -> "";
        };
        return Component.literal(h).withStyle(s -> s.withColor(0xFF555566));
    }

    private static Component condHint(String type) {
        String h = switch (type) {
            case NpcSceneNode.COND_QUEST_ACTIVE,
                 NpcSceneNode.COND_QUEST_COMPLETE,
                 NpcSceneNode.COND_QUEST_NOT_TAKEN -> "ID квеста";
            case NpcSceneNode.COND_HAS_ITEM,
                 NpcSceneNode.COND_NOT_HAS_ITEM -> "minecraft:diamond";
            case NpcSceneNode.COND_RELATION -> "FRIENDLY/NEUTRAL/HOSTILE";
            case NpcSceneNode.COND_FACTION  -> "ID фракции";
            case NpcSceneNode.COND_PATH_STAGE -> "номер стадии";
            case NpcSceneNode.COND_HAS_ABILITY -> "ID способности";
            default -> "(нет параметра)";
        };
        return Component.literal(h).withStyle(s -> s.withColor(0xFF555566));
    }

    private static void initQuestPicker(NpcSceneEditorScreen scr, int x, int y, int w,
                                        String currentId,
                                        java.util.function.Consumer<String> apply) {
        List<QuestDefinition> quests = ClientQuestState.getAll();
        if (quests.isEmpty()) return;
        int cur = -1;
        for (int i = 0; i < quests.size(); i++)
            if (quests.get(i).id.equals(currentId)) { cur = i; break; }
        String label = (cur < 0) ? "§7подобрать квест"
                : "§e" + (quests.get(cur).title == null ? quests.get(cur).id : quests.get(cur).title);
        if (label.length() > 30) label = label.substring(0, 30) + "…";

        scr.addRenderableWidget(Button.builder(
                Component.literal("◀▶ " + label),
                b -> {
                    scr.pullAllFields();
                    int next = (cur + 1) % quests.size();
                    apply.accept(quests.get(next).id);
                    scr.rebuildAll();
                }
        ).bounds(x, y, w, 14).build());
    }

    private static void initScenePicker(NpcSceneEditorScreen scr, int x, int y, int w,
                                        String currentId,
                                        java.util.function.Consumer<String> apply) {
        List<NpcScene> scenes = ClientNpcSceneState.getAll();
        if (scenes.isEmpty()) return;
        int cur = -1;
        for (int i = 0; i < scenes.size(); i++)
            if (scenes.get(i).id.equals(currentId)) { cur = i; break; }
        String label = (cur < 0) ? "§7выбрать сцену"
                : "§a" + (scenes.get(cur).name == null ? scenes.get(cur).id : scenes.get(cur).name);
        if (label.length() > 30) label = label.substring(0, 30) + "…";

   scr.addRenderableWidget(Button.builder(
                Component.literal("◀▶ " + label),
                b -> {
                    scr.pullAllFields();
                    int next = (cur + 1) % scenes.size();
                    apply.accept(scenes.get(next).id);
                    scr.rebuildAll();
                }
        ).bounds(x, y, w, 14).build());
    }

    private static void renderIncomingHint(NpcSceneEditorScreen scr, int x, int y, String nodeId) {
        if (scr.draftScene == null) return;
        List<String> incoming = scr.draftScene.incomingOf(nodeId);
        // Just a button that, when clicked, jumps to the first incoming node.
        if (incoming.isEmpty()) return;
        String first = incoming.get(0);
        scr.addRenderableWidget(Button.builder(
                Component.literal("§8← Входящих: " + incoming.size() + "  (перейти)"),
                b -> {
                    scr.pullAllFields();
                    scr.selectedNodeId = first;
                    scr.editingChoiceId = "";
                    scr.rebuildAll();
                }
        ).bounds(x, y, COL3_W - 16, 12).build());
    }
}

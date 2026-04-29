package com.frametrip.dragonlegacyquesttoast.client.npceditor.scene;

import com.frametrip.dragonlegacyquesttoast.client.npceditor.NpcEditorUtils;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcScene;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcSceneNode;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcSceneTemplates;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcSceneValidator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.List;

import static com.frametrip.dragonlegacyquesttoast.client.npceditor.scene.NpcSceneEditorScreen.*;

/** Left zone — list of scenes + create/delete + filter. */
final class NpcSceneEditorSceneList {

    private NpcSceneEditorSceneList() {}

    private static final int ROW_H = 22;
    private static final int VISIBLE_ROWS = 14;
    private static final String[] FILTERS = {"all", "quest", "repeatable", "with_questions", "errors"};
    private static final String[] FILTER_LABELS = {
            "Все", "Квесты", "Повторяемые", "С вопросами", "С ошибками"
    };

    static void init(NpcSceneEditorScreen scr, int ox, int oy) {
        int x = ox + PAD;
        int y = oy + TOP_H + 4;

        // Search box
        scr.sceneSearchBox = new EditBox(Minecraft.getInstance().font,
                x + 2, y + 14, COL1_W - 4, 14, Component.literal("Поиск"));
        scr.sceneSearchBox.setMaxLength(48);
        scr.sceneSearchBox.setHint(Component.literal("🔍 Поиск...").withStyle(s -> s.withColor(0xFF555566)));
        scr.sceneSearchBox.setValue(scr.sceneSearch);
        scr.sceneSearchBox.setResponder(v -> scr.sceneSearch = v);
        scr.addRenderableWidget(scr.sceneSearchBox);

        // Filter cycle button
        int fi = filterIndex(scr.sceneFilter);
        scr.addRenderableWidget(Button.builder(
                Component.literal("⚐ " + FILTER_LABELS[fi]),
                b -> {
                    scr.pullAllFields();
                    scr.sceneFilter = FILTERS[(fi + 1) % FILTERS.length];
                    scr.sceneScroll = 0;
                    scr.rebuildAll();
                }
        ).bounds(x + 2, y + 30, COL1_W - 4, 14).build());

        // Scene rows
        List<NpcScene> scenes = scr.filteredScenes();
        int rowsY = y + 48;
        int from = Math.min(scr.sceneScroll, Math.max(0, scenes.size() - VISIBLE_ROWS));
        int to = Math.min(scenes.size(), from + VISIBLE_ROWS);
        for (int i = from; i < to; i++) {
            NpcScene s = scenes.get(i);
            boolean selected = scr.draftScene != null && s.id.equals(scr.draftScene.id);
            String prefix = selected ? "§e▶ " : "  ";
            String name = s.name == null ? s.id : s.name;
            if (name.length() > 18) name = name.substring(0, 18) + "…";
            // brief indicators: quests / errors
            String marks = "";
            if (NpcSceneValidator.validate(s).stream().anyMatch(is -> is.level == NpcSceneValidator.Level.ERROR))
                marks += "§c!";
            if (s.nodes.stream().anyMatch(n -> NpcSceneNode.TYPE_QUESTION.equals(n.type))) marks += "§e?";
            if (s.nodes.stream().anyMatch(n -> NpcSceneNode.TYPE_ACTION.equals(n.type))) marks += "§6⚡";
            if (s.repeatable) marks += "§a↻";

            final NpcScene captured = s;
            scr.addRenderableWidget(Button.builder(
                    Component.literal(prefix + "§f" + name + " §8#" + s.id + " §7" + marks),
                    b -> {
                        scr.pullAllFields();
                        scr.selectScene(captured);
                        scr.rebuildAll();
                    }
            ).bounds(x + 2, rowsY + (i - from) * ROW_H, COL1_W - 4, ROW_H - 2).build());
        }

        // Bottom: create / dup / del / link
        int btnY = oy + TOP_H + 4 + 4 + 14 + 4 + 14 + 4 + VISIBLE_ROWS * ROW_H + 4;

        // Template cycle
        int ti = templateIndex(scr.templateId);
        scr.addRenderableWidget(Button.builder(
                Component.literal("◇ " + NpcSceneTemplates.TEMPLATE_LABELS[ti]),
                b -> {
                    scr.pullAllFields();
                    scr.templateId = NpcSceneTemplates.TEMPLATE_IDS[(ti + 1) % NpcSceneTemplates.TEMPLATE_IDS.length];
                    scr.rebuildAll();
                }
        ).bounds(x + 2, btnY, COL1_W - 4, 14).build());

        scr.addRenderableWidget(Button.builder(Component.literal("+ Создать"), b -> {
            scr.pullAllFields();
            scr.createSceneFromTemplate();
            scr.rebuildAll();
        }).bounds(x + 2, btnY + 16, COL1_W - 4, 14).build());

        scr.addRenderableWidget(Button.builder(Component.literal("⎘ Дублировать"), b -> {
            scr.pullAllFields();
            scr.duplicateCurrentScene();
            scr.rebuildAll();
        }).bounds(x + 2, btnY + 32, COL1_W - 4, 14).build());

        scr.addRenderableWidget(Button.builder(Component.literal("🗑 Удалить"), b -> {
            scr.pullAllFields();
            scr.deleteCurrentScene();
            scr.rebuildAll();
        }).bounds(x + 2, btnY + 48, COL1_W - 4, 14).build());

        // Link to NPC
        boolean linked = scr.draftScene != null && scr.draftScene.id.equals(scr.npcState.getDraft().sceneId);
        scr.addRenderableWidget(Button.builder(
                Component.literal(linked ? "§a★ Привязана" : "☆ Привязать к NPC"),
                b -> {
                    if (scr.draftScene == null) return;
                    scr.pullAllFields();
                    scr.saveDraftToServer();
                    scr.npcState.getDraft().sceneId = scr.draftScene.id;
                    scr.npcState.markDirty();
                    scr.rebuildAll();
                }
        ).bounds(x + 2, btnY + 64, COL1_W - 4, 14).build());
    }

    static void render(NpcSceneEditorScreen scr, GuiGraphics g, int ox, int oy, int mx, int my) {
        var font = Minecraft.getInstance().font;
        int x = ox + PAD;
        int y = oy + TOP_H + 4;
        int h = H - TOP_H - BOT_H - 8;

        // Card frame
        g.fill(x, y, x + COL1_W, y + h, 0xAA131320);
        g.fill(x, y, x + COL1_W, y + 1, ACCENT_SCENE);
        NpcEditorUtils.brd(g, x, y, COL1_W, h, 0xFF2A2A44);
        g.drawString(font, "§l§7СЦЕНЫ NPC", x + 4, y + 3, ACCENT_SCENE, false);

        int total = scr.filteredScenes().size();
        if (total > VISIBLE_ROWS) {
            g.drawString(font, "§8" + (scr.sceneScroll + 1) + "–"
                    + Math.min(total, scr.sceneScroll + VISIBLE_ROWS) + "/" + total,
                    x + COL1_W - 50, y + 3, 0xFF666677, false);
        }
    }

    private static int filterIndex(String f) {
        for (int i = 0; i < FILTERS.length; i++) if (FILTERS[i].equals(f)) return i;
        return 0;
    }
    private static int templateIndex(String t) {
        for (int i = 0; i < NpcSceneTemplates.TEMPLATE_IDS.length; i++)
            if (NpcSceneTemplates.TEMPLATE_IDS[i].equals(t)) return i;
        return 0;
    }
}

package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.client.ClientDialogueState;
import com.frametrip.dragonlegacyquesttoast.client.ClientQuestState;
import com.frametrip.dragonlegacyquesttoast.client.dialogue.ClientNpcSceneState;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SaveNpcScenePacket;
import com.frametrip.dragonlegacyquesttoast.server.DialogueDefinition;
import com.frametrip.dragonlegacyquesttoast.server.QuestDefinition;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcScene;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcSceneNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Tab 1 — Взаимодействие: сцена диалога, старый диалог, квесты, поиск. */
public class NpcInteractionTab implements NpcEditorTab {

    public static final int ACCENT = 0xFF44CC88;
    private static final int ROWS = 8;

    private int questScroll = 0;
    private EditBox searchField;
    private String searchQuery = "";

    // Scene editor sub-view
    private boolean sceneEditorOpen = false;
    private NpcScene draftScene = null;
    private String selectedNodeId = null;
    private EditBox nodeTextField;
    private EditBox nodeActionParamField;

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        NpcEntityData d = state.getDraft();

        if (sceneEditorOpen && draftScene != null) {
            initSceneEditor(add, rebuild, state, d, rx, oy, rw);
            return;
        }

        // ── Scene selector ───────────────────────────────────────────────────
        List<NpcScene> scenes = ClientNpcSceneState.getAll();

        add.accept(Button.builder(Component.literal("◀"), b -> {
            int i = indexOfId(scenes.stream().map(s -> s.id).toList(), d.sceneId);
            d.sceneId = (i <= 0) ? "" : scenes.get(i - 1).id;
            state.markDirty();
            rebuild.run();
        }).bounds(rx, oy + 30, 20, 16).build());

        add.accept(Button.builder(Component.literal("▶"), b -> {
            int i = indexOfId(scenes.stream().map(s -> s.id).toList(), d.sceneId);
            if (!scenes.isEmpty())
                d.sceneId = (i < 0 || i >= scenes.size() - 1) ? scenes.get(0).id : scenes.get(i + 1).id;
            state.markDirty();
            rebuild.run();
        }).bounds(rx + 22 + 120, oy + 30, 20, 16).build());

        add.accept(Button.builder(Component.literal("✕"), b -> {
            d.sceneId = "";
            state.markDirty();
            rebuild.run();
        }).bounds(rx + 22 + 144, oy + 30, 20, 16).build());

        add.accept(Button.builder(Component.literal("+ Создать сцену"), b -> {
            draftScene = new NpcScene();
            sceneEditorOpen = true;
            rebuild.run();
        }).bounds(rx, oy + 50, 110, 14).build());

        NpcScene selScene = ClientNpcSceneState.get(d.sceneId);
        if (selScene != null) {
            add.accept(Button.builder(Component.literal("✎ Редактировать"), b -> {
                draftScene = selScene.copy();
                sceneEditorOpen = true;
                rebuild.run();
            }).bounds(rx + 114, oy + 50, 114, 14).build());
        }

        // ── Legacy dialogue selector ─────────────────────────────────────────
        List<DialogueDefinition> dlgs = ClientDialogueState.getAll();

        add.accept(Button.builder(Component.literal("◀"), b -> {
            int i = indexOfId(dlgs.stream().map(dd -> dd.id).toList(), d.dialogueId);
            d.dialogueId = (i <= 0) ? "" : dlgs.get(i - 1).id;
            state.markDirty();
            rebuild.run();
        }).bounds(rx, oy + 88, 20, 16).build());

        add.accept(Button.builder(Component.literal("▶"), b -> {
            int i = indexOfId(dlgs.stream().map(dd -> dd.id).toList(), d.dialogueId);
            if (!dlgs.isEmpty())
                d.dialogueId = (i < 0 || i >= dlgs.size() - 1) ? dlgs.get(0).id : dlgs.get(i + 1).id;
            state.markDirty();
            rebuild.run();
        }).bounds(rx + 22 + 120, oy + 88, 20, 16).build());

        add.accept(Button.builder(Component.literal("✕"), b -> {
            d.dialogueId = "";
            state.markDirty();
            rebuild.run();
        }).bounds(rx + 22 + 144, oy + 88, 20, 16).build());

        // ── Quest search ─────────────────────────────────────────────────────
        searchField = new EditBox(Minecraft.getInstance().font,
                rx, oy + 120, rw, 14, Component.literal("Поиск квестов"));
        searchField.setMaxLength(48);
        searchField.setValue(searchQuery);
        searchField.setHint(Component.literal("🔍 Поиск...").withStyle(s -> s.withColor(0xFF555566)));
        searchField.setResponder(v -> { searchQuery = v; rebuild.run(); });
        add.accept(searchField);

        // ── Quest list ───────────────────────────────────────────────────────
        List<QuestDefinition> quests = filteredQuests();
        int qY = oy + 140;
        for (int i = questScroll; i < Math.min(quests.size(), questScroll + ROWS); i++) {
            QuestDefinition q = quests.get(i);
            boolean linked = d.questIds.contains(q.id);
            add.accept(Button.builder(
                    Component.literal((linked ? "§a[✓]§r " : "§7[ ]§r ") + q.title),
                    b -> {
                        if (d.questIds.contains(q.id)) d.questIds.remove(q.id);
                        else d.questIds.add(q.id);
                        state.markDirty();
                        rebuild.run();
                    }
            ).bounds(rx, qY + (i - questScroll) * 18, rw, 16).build());
        }
    }

    // ── Scene editor ──────────────────────────────────────────────────────────

    private void initSceneEditor(Consumer<AbstractWidget> add, Runnable rebuild,
                                 NpcEditorState state, NpcEntityData d,
                                 int rx, int oy, int rw) {
        if (draftScene == null) draftScene = new NpcScene();

        int listW = rw / 2 - 4;
        int nodeY = oy + 24;

        // Node list (left half of content area)
        for (int i = 0; i < draftScene.nodes.size(); i++) {
            var node = draftScene.nodes.get(i);
            boolean sel = node.id.equals(selectedNodeId);
            final String nid = node.id;
            add.accept(Button.builder(
                    Component.literal((sel ? "§e▶ §r" : "   ") + node.displayLabel()),
                    b -> { selectedNodeId = nid; rebuild.run(); }
            ).bounds(rx, nodeY + i * 18, listW, 16).build());
        }

        // Add node buttons
        int addY = nodeY + draftScene.nodes.size() * 18 + 4;
        add.accept(Button.builder(Component.literal("+ Речь"), b -> {
            draftScene.addNode(NpcSceneNode.TYPE_SPEECH);
            rebuild.run();
        }).bounds(rx, addY, listW / 3 - 2, 14).build());
        add.accept(Button.builder(Component.literal("+ Вопрос"), b -> {
            draftScene.addNode(NpcSceneNode.TYPE_QUESTION);
            rebuild.run();
        }).bounds(rx + listW / 3 + 1, addY, listW / 3 - 2, 14).build());
        add.accept(Button.builder(Component.literal("+ Действие"), b -> {
            draftScene.addNode(NpcSceneNode.TYPE_ACTION);
            rebuild.run();
        }).bounds(rx + (listW / 3) * 2 + 2, addY, listW / 3 - 2, 14).build());

        // Right half: node editor
        int edX = rx + listW + 8;
        int edW = rw - listW - 8;

        if (selectedNodeId != null) {
            NpcSceneNode node = draftScene.getNode(selectedNodeId);
            if (node != null) {
                nodeTextField = new EditBox(Minecraft.getInstance().font,
                        edX, oy + 30, edW, 16, Component.literal("Текст узла"));
                nodeTextField.setMaxLength(256);
                nodeTextField.setValue(node.text);
                add.accept(nodeTextField);

                if (NpcSceneNode.TYPE_ACTION.equals(node.type)) {
                    String[] actions = {NpcSceneNode.ACTION_GIVE_QUEST,
                            NpcSceneNode.ACTION_COMPLETE_QUEST, NpcSceneNode.ACTION_SET_RELATION};
                    add.accept(Button.builder(
                            Component.literal("◀▶ " + node.actionType),
                            b -> {
                                int i = indexOf(actions, node.actionType);
                                node.actionType = actions[Math.floorMod(i + 1, actions.length)];
                                rebuild.run();
                            }
                    ).bounds(edX, oy + 50, edW, 14).build());

                    nodeActionParamField = new EditBox(Minecraft.getInstance().font,
                            edX, oy + 68, edW, 14, Component.literal("Параметр"));
                    nodeActionParamField.setMaxLength(128);
                    nodeActionParamField.setValue(node.actionParam);
                    add.accept(nodeActionParamField);
                }

                // Next node selector (for speech & action)
                if (!NpcSceneNode.TYPE_QUESTION.equals(node.type)) {
                    List<NpcSceneNode> nodes = draftScene.nodes;
                    add.accept(Button.builder(Component.literal("→ Следующий"), b -> {
                        pullSceneNodeFields();
                        String curNext = NpcSceneNode.TYPE_ACTION.equals(node.type)
                                ? node.actionNextNodeId : node.nextNodeId;
                        int cur = indexOf(nodes.stream().map(n -> n.id).toList(), curNext);
                        String nid = (cur < 0 || cur >= nodes.size() - 1) ? "" : nodes.get(cur + 1).id;
                        if (NpcSceneNode.TYPE_ACTION.equals(node.type)) node.actionNextNodeId = nid;
                        else node.nextNodeId = nid;
                        rebuild.run();
                    }).bounds(edX, oy + 86, edW, 14).build());
                }

                add.accept(Button.builder(Component.literal("★ Стартовый"), b -> {
                    draftScene.startNodeId = selectedNodeId;
                    rebuild.run();
                }).bounds(edX, oy + 104, edW, 14).build());

                add.accept(Button.builder(Component.literal("🗑 Удалить"), b -> {
                    draftScene.removeNode(selectedNodeId);
                    selectedNodeId = null;
                    rebuild.run();
                }).bounds(edX, oy + 122, edW, 14).build());
            }
        }

        // Bottom buttons: Save / Cancel
        add.accept(Button.builder(Component.literal("Сохранить сцену"), b -> {
            pullSceneNodeFields();
            ModNetwork.CHANNEL.sendToServer(new SaveNpcScenePacket(draftScene, false));
            List<NpcScene> updated = new ArrayList<>(ClientNpcSceneState.getAll());
            updated.removeIf(sc -> sc.id.equals(draftScene.id));
            updated.add(draftScene);
            ClientNpcSceneState.sync(updated);
            d.sceneId = draftScene.id;
            state.markDirty();
            sceneEditorOpen = false;
            draftScene = null;
            selectedNodeId = null;
            rebuild.run();
        }).bounds(rx, oy + 290, 120, 18).build());

        add.accept(Button.builder(Component.literal("Отмена"), b -> {
            sceneEditorOpen = false;
            draftScene = null;
            selectedNodeId = null;
            rebuild.run();
        }).bounds(rx + 124, oy + 290, 70, 18).build());
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();

        if (sceneEditorOpen && draftScene != null) {
            renderSceneEditor(g, d, rx, oy, rw);
            return;
        }

        NpcEditorUtils.sectionCard(g, rx, oy, rw, 68, "СЦЕНА ДИАЛОГА", ACCENT);
        g.drawString(font, "§7Активная сцена:", rx + 4, oy + 14, 0xFF888877, false);
        List<NpcScene> scenes = ClientNpcSceneState.getAll();
        String scLabel = d.sceneId.isEmpty() ? "§8— не задана —" :
                scenes.stream().filter(sc -> sc.id.equals(d.sceneId))
                        .map(sc -> "§a" + sc.name + " §8[" + sc.nodes.size() + " узлов]")
                        .findFirst().orElse("§c" + d.sceneId);
        g.drawCenteredString(font, scLabel, rx + 22 + 60, oy + 33, 0xFFCCCCCC);

        NpcEditorUtils.sectionCard(g, rx, oy + 70, rw, 28, "ДИАЛОГ (УСТАРЕВШИЙ)", ACCENT);
        g.drawString(font, "§7Диалог:", rx + 4, oy + 80, 0xFF888877, false);
        List<DialogueDefinition> dlgs = ClientDialogueState.getAll();
        String dlgLabel = d.dialogueId.isEmpty() ? "§8— не задан —" :
                dlgs.stream().filter(dd -> dd.id.equals(d.dialogueId))
                        .map(dd -> "§7" + dd.npcName).findFirst().orElse("§c" + d.dialogueId);
        g.drawCenteredString(font, dlgLabel, rx + 22 + 60, oy + 91, 0xFFCCCCCC);

        List<QuestDefinition> quests = filteredQuests();
        int total = ClientQuestState.getAll().size();
        NpcEditorUtils.sectionCard(g, rx, oy + 102, rw, 14,
                "КВЕСТЫ (" + d.questIds.size() + "/" + total + " выбрано)", ACCENT);

        if (quests.isEmpty() && !searchQuery.isBlank()) {
            g.drawString(font, "§8Нет совпадений по запросу «" + searchQuery + "»",
                    rx + 4, oy + 142, 0xFF555566, false);
        } else if (ClientQuestState.getAll().isEmpty()) {
            g.drawString(font, "§8Квестов ещё нет...", rx + 4, oy + 142, 0xFF555566, false);
        }

        if (quests.size() > ROWS) {
            g.drawString(font, "§8↑↓ прокрутка", rx + rw - 60, oy + 140, 0xFF444455, false);
        }
    }

    private void renderSceneEditor(GuiGraphics g, NpcEntityData d, int rx, int oy, int rw) {
        var font = Minecraft.getInstance().font;
        NpcEditorUtils.sectionCard(g, rx, oy, rw, 18, "РЕДАКТОР СЦЕНЫ: " + draftScene.name, ACCENT);

        int listW = rw / 2 - 4;
        g.drawString(font, "§7Узлы:", rx, oy + 14, 0xFF888877, false);

        int edX = rx + listW + 8;
        int edW = rw - listW - 8;
        g.fill(edX - 2, oy + 20, edX - 1, oy + 280, 0xFF333344);
        g.drawString(font, "§7Редактор:", edX, oy + 14, 0xFF888877, false);

        if (selectedNodeId != null) {
            NpcSceneNode node = draftScene.getNode(selectedNodeId);
            if (node != null) {
                String typeColor = switch (node.type) {
                    case NpcSceneNode.TYPE_SPEECH   -> "§b";
                    case NpcSceneNode.TYPE_QUESTION -> "§e";
                    case NpcSceneNode.TYPE_ACTION   -> "§c";
                    default -> "§7";
                };
                g.drawString(font, typeColor + "▶ " + node.type.toUpperCase(), edX, oy + 22, 0xFFAAAAAA, false);
                g.drawString(font, "§7Текст:", edX, oy + 24, 0xFF888877, false);

                if (NpcSceneNode.TYPE_ACTION.equals(node.type)) {
                    g.drawString(font, "§7Тип действия:", edX, oy + 48, 0xFF888877, false);
                    g.drawString(font, "§7Параметр:", edX, oy + 65, 0xFF888877, false);
                }

                String nextId = NpcSceneNode.TYPE_ACTION.equals(node.type)
                        ? node.actionNextNodeId : node.nextNodeId;
                if (!NpcSceneNode.TYPE_QUESTION.equals(node.type)) {
                    String nextLabel = (nextId == null || nextId.isEmpty()) ? "§8(конец)" : "§f→ " + nextId;
                    g.drawString(font, nextLabel, edX, oy + 83, 0xFF888877, false);
                }

                if (draftScene.startNodeId.equals(node.id)) {
                    g.drawString(font, "§a★ Стартовый узел", edX, oy + 140, 0xFF44EE55, false);
                }
            }
        } else {
            g.drawString(font, "§8← Выбери узел слева", edX, oy + 60, 0xFF444455, false);
        }
    }

    @Override
    public void pullFields(NpcEditorState state) {
        if (searchField != null) searchQuery = searchField.getValue();
        pullSceneNodeFields();
    }

    private void pullSceneNodeFields() {
        if (draftScene == null || selectedNodeId == null) return;
        NpcSceneNode node = draftScene.getNode(selectedNodeId);
        if (node == null) return;
        if (nodeTextField != null) node.text = nodeTextField.getValue();
        if (nodeActionParamField != null) node.actionParam = nodeActionParamField.getValue();
    }

    @Override
    public boolean onMouseScrolled(double mx, double my, double delta,
                                   NpcEditorState state, int rx, int oy, int rw) {
        if (sceneEditorOpen) return false;
        int total = filteredQuests().size();
        questScroll = Math.max(0, Math.min(Math.max(0, total - ROWS),
                questScroll - (int) Math.signum(delta)));
        return true;
    }

    // ── Util ──────────────────────────────────────────────────────────────────

    private List<QuestDefinition> filteredQuests() {
        List<QuestDefinition> all = ClientQuestState.getAll();
        if (searchQuery == null || searchQuery.isBlank()) return all;
        String q = searchQuery.toLowerCase();
        return all.stream().filter(qd -> qd.title.toLowerCase().contains(q)).toList();
    }

    private static int indexOfId(List<String> list, String id) {
        for (int i = 0; i < list.size(); i++) if (list.get(i).equals(id)) return i;
        return -1;
    }

    private static int indexOf(List<String> list, String val) {
        if (val == null) return -1;
        for (int i = 0; i < list.size(); i++) if (list.get(i).equals(val)) return i;
        return -1;
    }

    private static int indexOf(String[] arr, String val) {
        for (int i = 0; i < arr.length; i++) if (arr[i].equals(val)) return i;
        return 0;
    }
}

package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.client.ClientDialogueState;
import com.frametrip.dragonlegacyquesttoast.client.ClientQuestState;
import com.frametrip.dragonlegacyquesttoast.client.NpcCreatorScreen;
import com.frametrip.dragonlegacyquesttoast.client.dialogue.ClientNpcSceneState;
import com.frametrip.dragonlegacyquesttoast.client.dialogue.NpcSceneController;
import com.frametrip.dragonlegacyquesttoast.client.npceditor.scene.NpcSceneEditorScreen;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.server.chat.ChatMatchMode;
import com.frametrip.dragonlegacyquesttoast.server.chat.ChatReactionType;
import com.frametrip.dragonlegacyquesttoast.server.chat.ChatTrigger;
import com.frametrip.dragonlegacyquesttoast.server.chat.NpcChatConfig;
import com.frametrip.dragonlegacyquesttoast.network.SaveNpcScenePacket;
import com.frametrip.dragonlegacyquesttoast.server.DialogueDefinition;
import com.frametrip.dragonlegacyquesttoast.server.QuestDefinition;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.LegacyDialogueMigration;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcScene;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcSceneNode;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcSceneValidator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Compact "Interaction" tab. Displays the NPC's currently linked scene,
 * a convert-legacy button when applicable, a one-click preview, and the
 * quest list. Heavy editing happens in the dedicated NpcSceneEditorScreen.
 */
public class NpcInteractionTab implements NpcEditorTab {

    public static final int ACCENT = 0xFF44CC88;
    private static final int ROWS = 6;

    private int questScroll = 0;
    private EditBox searchField;
    private String searchQuery = "";

    // chat trigger state
    private int    chatTriggerScroll   = 0;
    private int    selectedTriggerIdx  = -1;
    private EditBox chatRadiusBox, chatCooldownBox, triggerNameBox, triggerPhraseBox;

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        NpcEntityData d = state.getDraft();
        var mc = Minecraft.getInstance();

         // ── Linked scene controls ────────────────────────────────────────────
        List<NpcScene> scenes = ClientNpcSceneState.getAll();
        int sceneY = oy + 26;
        
        // Cycle prev/next selected scene
        add.accept(Button.builder(Component.literal("◀"), b -> {
            int i = indexOfId(scenes.stream().map(s -> s.id).toList(), d.sceneId);
            d.sceneId = (i <= 0) ? "" : scenes.get(i - 1).id;
            state.markDirty();
            rebuild.run();
        }).bounds(rx, sceneY, 18, 14).build());

        add.accept(Button.builder(Component.literal("▶"), b -> {
            int i = indexOfId(scenes.stream().map(s -> s.id).toList(), d.sceneId);
            if (!scenes.isEmpty())
                d.sceneId = (i < 0 || i >= scenes.size() - 1) ? scenes.get(0).id : scenes.get(i + 1).id;
            state.markDirty();
            rebuild.run();
        }).bounds(rx + 20, sceneY, 18, 14).build());

        add.accept(Button.builder(Component.literal("✕"), b -> {
            d.sceneId = "";
            state.markDirty();
            rebuild.run();
        }).bounds(rx + 40, sceneY, 18, 14).build());

       // Open editor
        add.accept(Button.builder(Component.literal("✎ Открыть редактор сцен"), b -> {
            // The active screen is the NpcCreatorScreen; pass it as parent.
            if (mc.screen instanceof NpcCreatorScreen npc) {
                mc.setScreen(new NpcSceneEditorScreen(npc, state));
            }
        }).bounds(rx + 64, sceneY, rw - 64, 14).build());

        // Preview the currently linked scene
        if (!d.sceneId.isEmpty()) {
            add.accept(Button.builder(Component.literal("▶ Предпросмотр"), b -> {
                NpcScene sc = ClientNpcSceneState.get(d.sceneId);
                if (sc != null) {
                    Minecraft.getInstance().setScreen(null);
                    NpcSceneController.startScene(d.displayName, sc, sc.startNodeId, true);
                }
            }).bounds(rx, sceneY + 18, 110, 14).build());
        }

        // ── Legacy dialogue → scene migration ────────────────────────────────
        boolean hasLegacy = !d.dialogueId.isEmpty();
        boolean hasScene  = !d.sceneId.isEmpty();
        if (hasLegacy && !hasScene) {
            add.accept(Button.builder(
                    Component.literal("⤷ Преобразовать диалог в сцену"),
                    b -> {
                        DialogueDefinition dlg = findDialogue(d.dialogueId);
                        if (dlg == null) return;
                        NpcScene scene = LegacyDialogueMigration.fromLegacy(dlg);
                        ModNetwork.CHANNEL.sendToServer(new SaveNpcScenePacket(scene, false));
                        // Update local cache for immediate feedback
                        List<NpcScene> updated = new ArrayList<>(ClientNpcSceneState.getAll());
                        updated.add(scene);
                        ClientNpcSceneState.sync(updated);
                        d.sceneId = scene.id;
                        state.markDirty();
                        rebuild.run();
                    }
            ).bounds(rx + 116, sceneY + 18, rw - 116, 14).build());
        }

        // Legacy dialogue cycling (kept as fallback)
        List<DialogueDefinition> dlgs = ClientDialogueState.getAll();
        int dlgY = oy + 80;
        add.accept(Button.builder(Component.literal("◀"), b -> {
            int i = indexOfId(dlgs.stream().map(dd -> dd.id).toList(), d.dialogueId);
            d.dialogueId = (i <= 0) ? "" : dlgs.get(i - 1).id;
            state.markDirty();
            rebuild.run();
        }).bounds(rx, dlgY, 18, 14).build());

        add.accept(Button.builder(Component.literal("▶"), b -> {
            int i = indexOfId(dlgs.stream().map(dd -> dd.id).toList(), d.dialogueId);
            if (!dlgs.isEmpty())
                d.dialogueId = (i < 0 || i >= dlgs.size() - 1) ? dlgs.get(0).id : dlgs.get(i + 1).id;
            state.markDirty();
            rebuild.run();
        }).bounds(rx + 20, dlgY, 18, 14).build());

        add.accept(Button.builder(Component.literal("✕"), b -> {
            d.dialogueId = "";
            state.markDirty();
            rebuild.run();
        }).bounds(rx + 40, dlgY, 18, 14).build());

        // ── Quest search + list ──────────────────────────────────────────────
        searchField = new EditBox(mc.font, rx, oy + 132, rw, 14, Component.literal("Поиск квестов"));
        searchField.setMaxLength(48);
        searchField.setValue(searchQuery);
        searchField.setHint(Component.literal("🔍 Поиск...").withStyle(s -> s.withColor(0xFF555566)));
        searchField.setResponder(v -> { searchQuery = v; rebuild.run(); });
        add.accept(searchField);

        List<QuestDefinition> quests = filteredQuests();
        int qY = oy + 152;
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

        // ── Chat Triggers ────────────────────────────────────────────────────
        initChatTriggers(add, rebuild, state, d, mc, rx, oy + 260, rw);
    }

    private void initChatTriggers(Consumer<AbstractWidget> add, Runnable rebuild,
                                   NpcEditorState state, NpcEntityData d,
                                   net.minecraft.client.Minecraft mc,
                                   int rx, int sy, int rw) {
        NpcChatConfig cfg = ensureChat(d);
        int y = sy;

        // Enable toggle + global settings
        add.accept(Button.builder(
                Component.literal(cfg.enabled ? "§a◉ Реакция на чат: ВКЛ" : "§8○ Реакция на чат"),
                b -> { cfg.enabled = !cfg.enabled; state.markDirty(); rebuild.run(); }
        ).bounds(rx, y, 160, 16).build());

        add.accept(Button.builder(Component.literal("◀ Радиус"), b -> {
            cfg.radius = Math.max(1f, cfg.radius - 2f);
            state.markDirty(); rebuild.run();
        }).bounds(rx + 166, y, 60, 16).build());
        add.accept(Button.builder(Component.literal("▶"), b -> {
            cfg.radius = Math.min(64f, cfg.radius + 2f);
            state.markDirty(); rebuild.run();
        }).bounds(rx + 230, y, 18, 16).build());
        y += 22;

        if (!cfg.enabled) return;

        // Trigger list
        add.accept(Button.builder(Component.literal("+ Триггер"), b -> {
            ChatTrigger t = new ChatTrigger();
            t.phrases.add("привет");
            cfg.triggers.add(t);
            selectedTriggerIdx = cfg.triggers.size() - 1;
            state.markDirty(); rebuild.run();
        }).bounds(rx, y, 80, 14).build());

        add.accept(Button.builder(Component.literal("✕"), b -> {
            if (selectedTriggerIdx >= 0 && selectedTriggerIdx < cfg.triggers.size()) {
                cfg.triggers.remove(selectedTriggerIdx);
                selectedTriggerIdx = Math.max(-1, selectedTriggerIdx - 1);
                state.markDirty(); rebuild.run();
            }
        }).bounds(rx + 84, y, 18, 14).build());
        y += 18;

        int visTriggers = 3;
        int maxScroll   = Math.max(0, cfg.triggers.size() - visTriggers);
        chatTriggerScroll = Math.max(0, Math.min(chatTriggerScroll, maxScroll));

        for (int i = chatTriggerScroll; i < Math.min(cfg.triggers.size(), chatTriggerScroll + visTriggers); i++) {
            ChatTrigger t = cfg.triggers.get(i);
            boolean sel   = i == selectedTriggerIdx;
            final int idx = i;
            add.accept(Button.builder(
                    Component.literal((sel ? "§e▶ " : "  ") + truncate(t.name, 14)
                            + " §8[" + t.phrases.size() + " фраз]"),
                    b -> { selectedTriggerIdx = idx; rebuild.run(); }
            ).bounds(rx, y + (i - chatTriggerScroll) * 18, rw, 16).build());
        }
        y += visTriggers * 18 + 4;

        // Selected trigger editor
        if (selectedTriggerIdx >= 0 && selectedTriggerIdx < cfg.triggers.size()) {
            ChatTrigger t = cfg.triggers.get(selectedTriggerIdx);

            triggerNameBox = new EditBox(mc.font, rx, y, 100, 14, Component.literal("Имя триггера"));
            triggerNameBox.setValue(t.name);
            add.accept(triggerNameBox);

            // Match mode cycle
            add.accept(Button.builder(
                    Component.literal("§7" + t.matchMode.label()),
                    b -> {
                        ChatMatchMode[] modes = ChatMatchMode.values();
                        t.matchMode = modes[(t.matchMode.ordinal() + 1) % modes.length];
                        state.markDirty(); rebuild.run();
                    }
            ).bounds(rx + 106, y, 110, 14).build());
            y += 18;

            triggerPhraseBox = new EditBox(mc.font, rx, y, rw - 50, 14, Component.literal("Добавить фразу"));
            triggerPhraseBox.setHint(Component.literal("Новая фраза...").withStyle(s -> s.withColor(0xFF444455)));
            add.accept(triggerPhraseBox);
            add.accept(Button.builder(Component.literal("+ Фраза"), b -> {
                if (triggerPhraseBox != null && !triggerPhraseBox.getValue().isBlank()) {
                    t.phrases.add(triggerPhraseBox.getValue().trim());
                    triggerPhraseBox.setValue("");
                    state.markDirty(); rebuild.run();
                }
            }).bounds(rx + rw - 46, y, 46, 14).build());
            y += 18;

            // Reaction type selector
            add.accept(Button.builder(
                    Component.literal("§7Реакция: §f" + t.reaction.type.label()),
                    b -> {
                        ChatReactionType[] types = ChatReactionType.values();
                        t.reaction.type = types[(t.reaction.type.ordinal() + 1) % types.length];
                        state.markDirty(); rebuild.run();
                    }
            ).bounds(rx, y, rw, 14).build());
            y += 18;

            // Reaction param: scene selector for scene types, text for SPEECH
            if (t.reaction.type == ChatReactionType.START_SCENE || t.reaction.type == ChatReactionType.START_NODE) {
                List<NpcScene> scenes = ClientNpcSceneState.getAll();
                add.accept(Button.builder(
                        Component.literal("§7Сцена: §e" + sceneLabel(t.reaction.param, scenes)),
                        b -> {
                            t.reaction.param = cycleScene(t.reaction.param, scenes);
                            state.markDirty(); rebuild.run();
                        }
                ).bounds(rx, y, rw, 14).build());
            } else if (t.reaction.type == ChatReactionType.GIVE_QUEST || t.reaction.type == ChatReactionType.UPDATE_QUEST) {
                List<QuestDefinition> qAll = ClientQuestState.getAll();
                add.accept(Button.builder(
                        Component.literal("§7Квест: §e" + questLabel(t.reaction.param, qAll)),
                        b -> {
                            t.reaction.param = cycleQuest(t.reaction.param, qAll);
                            state.markDirty(); rebuild.run();
                        }
                ).bounds(rx, y, rw, 14).build());
            } else {
                EditBox paramBox = new EditBox(mc.font, rx, y, rw, 14, Component.literal("Параметр"));
                paramBox.setValue(t.reaction.param);
                paramBox.setResponder(v -> { t.reaction.param = v; state.markDirty(); });
                add.accept(paramBox);
            }
        }
    }

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();

        NpcEditorUtils.sectionCard(g, rx, oy, rw, 60, "СЦЕНА ДИАЛОГА", ACCENT);
        NpcScene linked = d.sceneId.isEmpty() ? null : ClientNpcSceneState.get(d.sceneId);
        String label;
        if (linked == null) label = "§8— сцена не выбрана —";
        else {
            String name = linked.name == null ? linked.id : linked.name;
            int errors = (int) NpcSceneValidator.validate(linked).stream()
                    .filter(i -> i.level == NpcSceneValidator.Level.ERROR).count();
            label = "§a" + name + " §8[" + linked.nodes.size() + " узлов]"
                    + (errors > 0 ? " §c⨯" + errors : "");
        }
        g.drawCenteredString(font, label, rx + rw / 2, oy + 44, 0xFFCCCCCC);

        NpcEditorUtils.sectionCard(g, rx, oy + 70, rw, 24, "ДИАЛОГ (УСТАРЕВШИЙ)", ACCENT);
        List<DialogueDefinition> dlgs = ClientDialogueState.getAll();
        String dlgLabel = d.dialogueId.isEmpty() ? "§8— не задан —" :
                dlgs.stream().filter(dd -> dd.id.equals(d.dialogueId))
                        .map(dd -> "§7" + dd.npcName).findFirst().orElse("§c" + d.dialogueId);
        g.drawCenteredString(font, dlgLabel, rx + 60, oy + 83, 0xFFCCCCCC);

        List<QuestDefinition> quests = filteredQuests();
        int total = ClientQuestState.getAll().size();
        NpcEditorUtils.sectionCard(g, rx, oy + 118, rw, 14,
                "КВЕСТЫ (" + d.questIds.size() + "/" + total + " выбрано)", ACCENT);

        if (quests.isEmpty() && !searchQuery.isBlank()) {
            g.drawString(font, "§8Нет совпадений по запросу «" + searchQuery + "»",
                    rx + 4, oy + 154, 0xFF555566, false);
        } else if (ClientQuestState.getAll().isEmpty()) {
            g.drawString(font, "§8Квестов ещё нет...", rx + 4, oy + 154, 0xFF555566, false);
        }

        if (quests.size() > ROWS) {
            g.drawString(font, "§8↑↓ прокрутка", rx + rw - 60, oy + 152, 0xFF444455, false);
        }
        
        // ── Chat triggers summary ─────────────────────────────────────────────
        NpcChatConfig cfg = ensureChat(d);
        int sy = oy + 260;
        NpcEditorUtils.sectionCard(g, rx, sy - 2, rw, 14,
                "ЧАТ-ТРИГГЕРЫ" + (cfg.enabled ? " §a(ВКЛ)" : " §8(ВЫКЛ)"), ACCENT);
        if (cfg.enabled) {
            g.drawString(font, "§7Радиус: §f" + (int)cfg.radius + "б  §7Триггеров: §f" + cfg.triggers.size(),
                    rx + 4, sy + 14, 0xFF888877, false);

            if (selectedTriggerIdx >= 0 && selectedTriggerIdx < cfg.triggers.size()) {
                ChatTrigger t = cfg.triggers.get(selectedTriggerIdx);
                g.drawString(font, "§7Фразы: §8" + String.join(", ", t.phrases.subList(0, Math.min(t.phrases.size(), 3))),
                        rx + 4, sy + 110, 0xFF666677, false);
            }
        }
    }

    @Override
    public void pullFields(NpcEditorState state) {
        if (searchField != null) searchQuery = searchField.getValue();

        NpcChatConfig cfg = ensureChat(state.getDraft());
        if (selectedTriggerIdx >= 0 && selectedTriggerIdx < cfg.triggers.size()) {
            ChatTrigger t = cfg.triggers.get(selectedTriggerIdx);
            if (triggerNameBox != null && !triggerNameBox.getValue().isBlank())
                t.name = triggerNameBox.getValue();
        }
    }

    @Override
    public boolean onMouseScrolled(double mx, double my, double delta,
                                   NpcEditorState state, int rx, int oy, int rw) {
        int total = filteredQuests().size();
        questScroll = Math.max(0, Math.min(Math.max(0, total - ROWS),
                questScroll - (int) Math.signum(delta)));
        return true;
    }

    // ── Util ──────────────────────────────────────────────────────────────────

    private static NpcChatConfig ensureChat(NpcEntityData d) {
        if (d.chatConfig == null) d.chatConfig = new NpcChatConfig();
        return d.chatConfig;
    }

    private static String cycleScene(String current, List<NpcScene> scenes) {
        if (scenes.isEmpty()) return "";
        int i = -1;
        for (int j = 0; j < scenes.size(); j++) if (scenes.get(j).id.equals(current)) { i = j; break; }
        if (i < 0 || i >= scenes.size() - 1) return scenes.get(0).id;
        return scenes.get(i + 1).id;
    }

    private static String sceneLabel(String id, List<NpcScene> scenes) {
        if (id == null || id.isBlank()) return "§8— нет —";
        return scenes.stream().filter(s -> s.id.equals(id))
                .map(s -> s.name != null ? s.name : s.id).findFirst().orElse("§c" + id);
    }

    private static String cycleQuest(String current, List<QuestDefinition> quests) {
        if (quests.isEmpty()) return "";
        int i = -1;
        for (int j = 0; j < quests.size(); j++) if (quests.get(j).id.equals(current)) { i = j; break; }
        if (i < 0 || i >= quests.size() - 1) return quests.get(0).id;
        return quests.get(i + 1).id;
    }

    private static String questLabel(String id, List<QuestDefinition> quests) {
        if (id == null || id.isBlank()) return "§8— нет —";
        return quests.stream().filter(q -> q.id.equals(id))
                .map(q -> q.title).findFirst().orElse("§c" + id);
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
    
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

    private static DialogueDefinition findDialogue(String id) {
        for (DialogueDefinition d : ClientDialogueState.getAll())
            if (id.equals(d.id)) return d;
        return null;
    }
}

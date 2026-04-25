package com.frametrip.dragonlegacyquesttoast.client.npceditor.scene;

import com.frametrip.dragonlegacyquesttoast.client.NpcCreatorScreen;
import com.frametrip.dragonlegacyquesttoast.client.dialogue.ClientNpcSceneState;
import com.frametrip.dragonlegacyquesttoast.client.dialogue.NpcSceneController;
import com.frametrip.dragonlegacyquesttoast.client.npceditor.NpcEditorState;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SaveNpcScenePacket;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcScene;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcSceneNode;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcSceneTemplates;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcSceneValidator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Full-screen scene editor for NPC dialogue scenes.
 * Layout (780×560):
 *   ┌─────────────────────────────────────────────────────┐
 *   │ Top bar: title, Save, Close                         │
 *   ├──────────┬──────────┬───────────────────────────────┤
 *   │ Scenes   │ Nodes    │ Node editor (per-type)        │
 *   │  list    │  list    │                               │
 *   ├──────────┴──────────┴───────────────────────────────┤
 *   │ Bottom: validate, preview, diagnostics list         │
 *   └─────────────────────────────────────────────────────┘
 */
public class NpcSceneEditorScreen extends Screen {

    // ── Layout ─────────────────────────────────────────────────────────────
    public static final int W = 780, H = 560;
    public static final int TOP_H = 28;
    public static final int BOT_H = 116;
    public static final int PAD = 8;
    public static final int COL1_W = 140;          // scene list
    public static final int COL2_W = 140;          // node list
    public static final int COL_GAP = 4;
    public static final int COL3_W = W - PAD * 2 - COL1_W - COL2_W - COL_GAP * 2;

    // ── Colors ─────────────────────────────────────────────────────────────
    public static final int ACCENT_SCENE  = 0xFF44CC88;
    public static final int ACCENT_NODE   = 0xFF44CCDD;
    public static final int ACCENT_EDIT   = 0xFFEEAA44;
    public static final int ACCENT_DIAG   = 0xFFCC4488;

    public static final int COLOR_SPEECH    = 0xFF55BBFF;
    public static final int COLOR_QUESTION  = 0xFFEECC44;
    public static final int COLOR_ACTION    = 0xFFFF7755;
    public static final int COLOR_CONDITION = 0xFFCC66EE;
    public static final int COLOR_END       = 0xFF888899;

    // ── State ──────────────────────────────────────────────────────────────
    public final NpcCreatorScreen parent;
    public final NpcEditorState npcState;

    public NpcScene draftScene;
    public String selectedNodeId = "";
    public String editingChoiceId = "";

    public String sceneSearch = "";
    public String sceneFilter = "all"; // all | quest | repeatable | with_questions | errors
    public int sceneScroll = 0;
    public int nodeScroll = 0;
    public int issueScroll = 0;
    public List<NpcSceneValidator.Issue> issues = new ArrayList<>();
    public String templateId = NpcSceneTemplates.TPL_EMPTY;

    // EditBox references — pulled into the draft before any rebuild
    public EditBox sceneSearchBox;
    public EditBox sceneNameBox;
    public EditBox sceneDescBox;
    public EditBox nodeTextBox;
    public EditBox nodeSpeakerBox;
    public EditBox nodeAnimBox;
    public EditBox nodeSoundBox;
    public EditBox nodeActionParamBox;
    public EditBox nodeCondParamBox;
    public EditBox choiceTextBox;
    public EditBox choiceCondParamBox;
    public EditBox choiceActionParamBox;

public NpcSceneEditorScreen(NpcCreatorScreen parent, NpcEditorState npcState) {
        super(Component.literal("Редактор сцен NPC"));
        this.parent = parent;
        this.npcState = npcState;
        // Pre-load NPC's current scene if any
        NpcEntityData d = npcState.getDraft();
        if (!d.sceneId.isEmpty()) {
            NpcScene s = ClientNpcSceneState.get(d.sceneId);
            if (s != null) draftScene = s.copy();
        }
    }

    // ── Init / rebuild ─────────────────────────────────────────────────────
    @Override
    protected void init() {
        super.init();
        int ox = ox(), oy = oy();

        // Top bar buttons
        addRenderableWidget(Button.builder(Component.literal("✕ Назад"), b -> {
            saveDraftToServer();
            onClose();
        }).bounds(ox + W - 90, oy + 4, 80, 18).build());

        addRenderableWidget(Button.builder(Component.literal("💾 Сохранить"), b -> {
            saveDraftToServer();
            rebuildAll();
        }).bounds(ox + W - 180, oy + 4, 80, 18).build());

        // Zone columns and bottom panel
        NpcSceneEditorSceneList.init(this, ox, oy);
        NpcSceneEditorNodeList.init(this, ox, oy);
        NpcSceneEditorNodePanel.init(this, ox, oy);
        NpcSceneEditorDiagPanel.init(this, ox, oy);
    }
  
    public void rebuildAll() {
        super.rebuildWidgets();
    }

    /** Public bridge: package-helpers add widgets via this. */
    @Override
    public <T extends net.minecraft.client.gui.components.events.GuiEventListener
            & net.minecraft.client.gui.components.Renderable
            & net.minecraft.client.gui.narration.NarratableEntry> T addRenderableWidget(T widget) {
        return super.addRenderableWidget(widget);
    }

    public void pullAllFields() {
        if (sceneSearchBox != null) sceneSearch = sceneSearchBox.getValue();
        if (draftScene != null) {
            if (sceneNameBox != null) draftScene.name        = sceneNameBox.getValue();
            if (sceneDescBox != null) draftScene.description = sceneDescBox.getValue();
            NpcSceneNode n = draftScene.getNode(selectedNodeId);
            if (n != null) {
                if (nodeTextBox != null)         n.text           = nodeTextBox.getValue();
                if (nodeSpeakerBox != null)      n.speakerName    = nodeSpeakerBox.getValue();
                if (nodeAnimBox != null)         n.animationId    = nodeAnimBox.getValue();
                if (nodeSoundBox != null)        n.soundId        = nodeSoundBox.getValue();
                if (nodeActionParamBox != null)  n.actionParam    = nodeActionParamBox.getValue();
                if (nodeCondParamBox != null)    n.conditionParam = nodeCondParamBox.getValue();
                if (!editingChoiceId.isEmpty() && n.choices != null) {
                    for (var c : n.choices) {
                        if (editingChoiceId.equals(c.id)) {
                            if (choiceTextBox != null)         c.text           = choiceTextBox.getValue();
                            if (choiceCondParamBox != null)    c.conditionParam = choiceCondParamBox.getValue();
                            if (choiceActionParamBox != null)  c.actionParam    = choiceActionParamBox.getValue();
                            break;
                        }
                    }
                }
            }
        }
    }
  
    // ── Render ─────────────────────────────────────────────────────────────
    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int ox = ox(), oy = oy();

        // Main frame
        g.fill(ox, oy, ox + W, oy + H, 0xEE0A0A14);
        brd(g, ox, oy, W, H, 0xFF3A3A55);

        // Top bar
        g.fill(ox, oy, ox + W, oy + TOP_H, 0xBB12121E);
        brd(g, ox, oy, W, TOP_H, 0xFF444466);
        g.drawString(font, "§e⚙ §fРедактор сцен NPC §8— §7" + npcState.getDraft().displayName,
                ox + 8, oy + 9, 0xFFE6D7B5, false);

        // Zones
        NpcSceneEditorSceneList.render(this, g, ox, oy, mx, my);
        NpcSceneEditorNodeList.render(this, g, ox, oy, mx, my);
        NpcSceneEditorNodePanel.render(this, g, ox, oy, mx, my);
        NpcSceneEditorDiagPanel.render(this, g, ox, oy, mx, my);

        super.render(g, mx, my, pt);
    }

    // ── Persistence ────────────────────────────────────────────────────────
    public void saveDraftToServer() {
        pullAllFields();
        if (draftScene == null) return;
        ModNetwork.CHANNEL.sendToServer(new SaveNpcScenePacket(draftScene, false));
        // Update local cache for immediate visibility
        List<NpcScene> updated = new ArrayList<>(ClientNpcSceneState.getAll());
        updated.removeIf(s -> s.id.equals(draftScene.id));
        updated.add(draftScene.copy());
        ClientNpcSceneState.sync(updated);
        // If NPC has no scene yet, link this one
        NpcEntityData d = npcState.getDraft();
        if (d.sceneId.isEmpty()) {
            d.sceneId = draftScene.id;
            npcState.markDirty();
        }
    }

    public void deleteCurrentScene() {
        if (draftScene == null) return;
        ModNetwork.CHANNEL.sendToServer(new SaveNpcScenePacket(draftScene, true));
        List<NpcScene> updated = new ArrayList<>(ClientNpcSceneState.getAll());
        updated.removeIf(s -> s.id.equals(draftScene.id));
        ClientNpcSceneState.sync(updated);
        // Unlink from NPC if it was linked
        NpcEntityData d = npcState.getDraft();
        if (draftScene.id.equals(d.sceneId)) {
            d.sceneId = "";
            npcState.markDirty();
        }
        draftScene = null;
        selectedNodeId = "";
        editingChoiceId = "";
    }

    public void duplicateCurrentScene() {
        if (draftScene == null) return;
        pullAllFields();
        NpcScene copy = draftScene.copy();
        copy.id = java.util.UUID.randomUUID().toString().substring(0, 8);
        copy.name = draftScene.name + " (копия)";
        // Save the original first, then switch to the copy
        saveDraftToServer();
        draftScene = copy;
        selectedNodeId = "";
        editingChoiceId = "";
        saveDraftToServer();
    }

    public void selectScene(NpcScene s) {
        if (draftScene != null && (s == null || !draftScene.id.equals(s.id))) {
            saveDraftToServer();
        }
        draftScene = (s == null) ? null : s.copy();
        selectedNodeId = "";
        editingChoiceId = "";
        issues = new ArrayList<>();
    }

    public void createSceneFromTemplate() {
        if (draftScene != null) saveDraftToServer();
        draftScene = NpcSceneTemplates.create(templateId);
        selectedNodeId = "";
        editingChoiceId = "";
        issues = new ArrayList<>();
    }
  
    // ── Filtered scene list ────────────────────────────────────────────────
    public List<NpcScene> filteredScenes() {
        List<NpcScene> all = new ArrayList<>(ClientNpcSceneState.getAll());
        // Pull in current draft (in case it's unsaved/new)
        if (draftScene != null) {
            all.removeIf(s -> s.id.equals(draftScene.id));
            all.add(draftScene);
        }
        all.sort(Comparator.comparing(s -> s.name == null ? "" : s.name.toLowerCase()));
        String q = sceneSearch == null ? "" : sceneSearch.toLowerCase();
        return all.stream().filter(s -> {
            if (!q.isBlank()
                    && !(s.name != null && s.name.toLowerCase().contains(q))
                    && !s.id.toLowerCase().contains(q)) return false;
            return switch (sceneFilter) {
                case "quest" -> NpcScene.TYPE_QUEST_OFFER.equals(s.type)
                        || NpcScene.TYPE_QUEST_PROGRESS.equals(s.type)
                        || NpcScene.TYPE_QUEST_COMPLETE.equals(s.type);
                case "repeatable"     -> s.repeatable;
                case "with_questions" -> s.nodes.stream().anyMatch(n -> NpcSceneNode.TYPE_QUESTION.equals(n.type));
                case "errors"         -> NpcSceneValidator.validate(s).stream().anyMatch(i -> i.level == NpcSceneValidator.Level.ERROR);
                default -> true;
            };
        }).toList();
    }

    public void runDiagnostics() {
        pullAllFields();
        if (draftScene == null) { issues = new ArrayList<>(); return; }
        issues = NpcSceneValidator.validate(draftScene);
        issueScroll = 0;
    }

    public void runPreview(String startNodeId) {
        pullAllFields();
        if (draftScene == null) return;
        Minecraft mc = Minecraft.getInstance();
        // Close this screen so the overlay is visible
        mc.setScreen(null);
        NpcSceneController.startScene(npcState.getDraft().displayName, draftScene.copy(),
                startNodeId == null ? draftScene.startNodeId : startNodeId, true);
    }

    // ── Mouse scroll: route to columns by region ───────────────────────────
    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        int ox = ox(), oy = oy();
        int zonesY = oy + TOP_H + 4;
        int zonesH = H - TOP_H - BOT_H - 8;
        if (my >= zonesY && my <= zonesY + zonesH) {
            int sceneX = ox + PAD;
            int nodeX  = sceneX + COL1_W + COL_GAP;
            int editX  = nodeX + COL2_W + COL_GAP;
            int dir = -(int) Math.signum(delta);
            if (mx >= sceneX && mx <= sceneX + COL1_W) {
                sceneScroll = Math.max(0, sceneScroll + dir);
                return true;
            } else if (mx >= nodeX && mx <= nodeX + COL2_W) {
                nodeScroll = Math.max(0, nodeScroll + dir);
                return true;
            } else if (mx >= editX && mx <= editX + COL3_W) {
                // future: scroll editor when overflowing
            }
        }
        // Bottom diagnostics scroll
        if (my >= oy + H - BOT_H && my <= oy + H) {
            int dir = -(int) Math.signum(delta);
            issueScroll = Math.max(0, issueScroll + dir);
            return true;
        }
        return super.mouseScrolled(mx, my, delta);
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    public void onClose() {
        if (parent != null) Minecraft.getInstance().setScreen(parent);
        else super.onClose();
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    public int ox() { return (width  - W) / 2; }
    public int oy() { return (height - H) / 2; }

    public static void brd(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x,     y,     x + w, y + 1, c);
        g.fill(x,     y+h-1, x + w, y + h, c);
        g.fill(x,     y,     x + 1, y + h, c);
        g.fill(x+w-1, y,     x + w, y + h, c);
    }

    public static int colorOfNodeType(String type) {
        return switch (type) {
            case NpcSceneNode.TYPE_SPEECH    -> COLOR_SPEECH;
            case NpcSceneNode.TYPE_QUESTION  -> COLOR_QUESTION;
            case NpcSceneNode.TYPE_ACTION    -> COLOR_ACTION;
            case NpcSceneNode.TYPE_CONDITION -> COLOR_CONDITION;
            case NpcSceneNode.TYPE_END       -> COLOR_END;
            default -> 0xFFCCCCCC;
        };
    }
}

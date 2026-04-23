package com.frametrip.dragonlegacyquesttoast.client;
 
import com.frametrip.dragonlegacyquesttoast.entity.FactionData;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SaveFactionPacket;
import com.frametrip.dragonlegacyquesttoast.network.SaveNpcEntityDataPacket;
import com.frametrip.dragonlegacyquesttoast.server.NpcProfile;
import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Pose;
 
import java.util.List;
import java.util.Map;
 
public class NpcCreatorScreen extends Screen {
 
    // ── Layout constants ──────────────────────────────────────────────────────
    private static final int W          = 720;
    private static final int H          = 460;
    private static final int SIDEBAR_W  = 130;
    private static final int PREVIEW_W  = 200;
    private static final int CONTENT_W  = W - SIDEBAR_W - PREVIEW_W;
 
    // Tab definitions
    private static final String[] TAB_LABELS = {
        "  Информация", "  Взаимодействие", "  Анимация", "  Отношения"
    };
    private static final int[] TAB_ACCENT = {
        0xFF4488EE, 0xFF44CC88, 0xFFFF8844, 0xFFCC55EE
    };
 
    // ── State ─────────────────────────────────────────────────────────────────
    private final NpcEntity npcEntity;
    private NpcEntityData   draftData;
    private int             activeTab        = 0;
 
    private int             questScroll      = 0;
    private int             factionScroll    = 0;
    private boolean         editingFaction   = false;
    private FactionData     draftFaction     = null;
 
    // Widgets that hold state across render calls
    private EditBox nameField;
    private EditBox factionNameField;
    private EditBox factionDescField;
 
    private static final Gson GSON = new Gson();
 
    // ── Color swatches (Tab 4 faction editor) ─────────────────────────────────
    private static final int SWATCH_COUNT = 8;
    private static final int[] SWATCH_COLORS = {
        0xFF4466EE, 0xFFEE4444, 0xFF44EE55, 0xFFEEEE33,
        0xFFEE44EE, 0xFF33EEEE, 0xFFEEAA00, 0xFF999999
    };
 
    // ── Constructor ───────────────────────────────────────────────────────────
    public NpcCreatorScreen(NpcEntity entity) {
        super(Component.literal("Настройка NPC"));
        this.npcEntity = entity;
        this.draftData = entity.getNpcData().copy();
    }
 
    // ── Init (rebuild all widgets) ────────────────────────────────────────────
    @Override
    protected void init() {
        super.init();
        int ox = ox(), oy = oy();
        int rx = ox + SIDEBAR_W + 8;
        int rw = CONTENT_W - 16;
 
        // Sidebar tab buttons
        for (int i = 0; i < TAB_LABELS.length; i++) {
            final int tab = i;
            boolean active = activeTab == i;
            addRenderableWidget(Button.builder(
                Component.literal(TAB_LABELS[i]),
                b -> { pullFields(); activeTab = tab; rebuildScreen(); }
            ).bounds(ox + 4, oy + 30 + i * 40, SIDEBAR_W - 8, 34).build());
        }
 
        // Tab content
        switch (activeTab) {
            case 0 -> initInfo(rx, oy, rw);
            case 1 -> initInteraction(rx, oy, rw);
            case 2 -> initAnimation(rx, oy, rw);
            case 3 -> initRelations(rx, oy, rw);
        }
 
        // Bottom row
        int bY = oy + H - 26;
        addRenderableWidget(Button.builder(Component.literal("Сохранить"), b -> save())
            .bounds(rx, bY, 110, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Закрыть"),
            b -> onClose()).bounds(ox + W - 72, bY, 66, 20).build());
    }
 
    // ── Tab 0 — Информация ────────────────────────────────────────────────────
    private void initInfo(int rx, int oy, int rw) {
        // Name
        nameField = new EditBox(font, rx, oy + 36, rw, 18, Component.literal("Имя NPC"));
        nameField.setMaxLength(64);
        nameField.setValue(draftData.displayName);
        nameField.setHint(Component.literal("Имя персонажа...").withStyle(s -> s.withColor(0xFF555566)));
        addRenderableWidget(nameField);
 
        // Skin selector
        List<String> skins = NpcSkinManager.getAvailableSkins();
        addRenderableWidget(Button.builder(Component.literal("◀"), b -> {
            pullFields();
            List<String> ss = NpcSkinManager.getAvailableSkins();
            int i = ss.indexOf(draftData.skinId);
            draftData.skinId = ss.get(Math.floorMod(i - 1, ss.size()));
            rebuildScreen();
        }).bounds(rx, oy + 74, 18, 16).build());
 
        addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
            pullFields();
            List<String> ss = NpcSkinManager.getAvailableSkins();
            int i = ss.indexOf(draftData.skinId);
            draftData.skinId = ss.get(Math.floorMod(i + 1, ss.size()));
            rebuildScreen();
        }).bounds(rx + 20 + 90, oy + 74, 18, 16).build());
 
        addRenderableWidget(Button.builder(Component.literal("Открыть папку"),
            b -> NpcSkinManager.openSkinsFolder())
            .bounds(rx, oy + 94, 100, 14).build());
 
        addRenderableWidget(Button.builder(Component.literal("Обновить"), b -> {
            NpcSkinManager.refresh(); rebuildScreen();
        }).bounds(rx + 104, oy + 94, 72, 14).build());
 
        // Body parts
        int partY = oy + 118;
        for (Map.Entry<String, String[]> entry : NpcProfile.PART_OPTIONS.entrySet()) {
            String key = entry.getKey();
            String[] opts = entry.getValue();
 
            addRenderableWidget(Button.builder(Component.literal("◀"), b -> {
                pullFields();
                int cur = draftData.bodyParts.getOrDefault(key, 0);
                draftData.bodyParts.put(key, Math.floorMod(cur - 1, opts.length));
                rebuildScreen();
            }).bounds(rx + 72, partY, 18, 14).build());
 
            addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
                pullFields();
                int cur = draftData.bodyParts.getOrDefault(key, 0);
                draftData.bodyParts.put(key, Math.floorMod(cur + 1, opts.length));
                rebuildScreen();
            }).bounds(rx + 72 + 20 + 72, partY, 18, 14).build());
 
            partY += 18;
        }
    }
 
    // ── Tab 1 — Взаимодействие ────────────────────────────────────────────────
    private void initInteraction(int rx, int oy, int rw) {
        List<com.frametrip.dragonlegacyquesttoast.server.DialogueDefinition> dlgs =
            ClientDialogueState.getAll();
 
        // Dialogue cycle
        addRenderableWidget(Button.builder(Component.literal("◀"), b -> {
            int i = indexOfId(dlgs.stream().map(d -> d.id).toList(), draftData.dialogueId);
            draftData.dialogueId = i <= 0 ? "" : dlgs.get(i - 1).id;
            rebuildScreen();
        }).bounds(rx, oy + 46, 18, 16).build());
 
        addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
            int i = indexOfId(dlgs.stream().map(d -> d.id).toList(), draftData.dialogueId);
            if (dlgs.isEmpty()) return;
            draftData.dialogueId = (i < 0 || i >= dlgs.size() - 1) ? dlgs.get(0).id : dlgs.get(i + 1).id;
            rebuildScreen();
        }).bounds(rx + 20 + 130, oy + 46, 18, 16).build());
 
        addRenderableWidget(Button.builder(Component.literal("✕ Убрать диалог"), b -> {
            draftData.dialogueId = ""; rebuildScreen();
        }).bounds(rx + 20 + 130 + 22, oy + 46, 100, 16).build());
 
        // Quest list (toggle buttons)
        List<com.frametrip.dragonlegacyquesttoast.server.QuestDefinition> quests =
            ClientQuestState.getAll();
 
        int visibleRows = 10;
        int qY = oy + 90;
        for (int i = questScroll; i < Math.min(quests.size(), questScroll + visibleRows); i++) {
            var quest = quests.get(i);
            boolean linked = draftData.questIds.contains(quest.id);
            addRenderableWidget(Button.builder(
                Component.literal((linked ? "§a[✓]§r " : "§7[ ]§r ") + quest.title),
                b -> {
                    if (draftData.questIds.contains(quest.id))
                        draftData.questIds.remove(quest.id);
                    else
                        draftData.questIds.add(quest.id);
                    rebuildScreen();
                }
            ).bounds(rx, qY + (i - questScroll) * 18, rw, 16).build());
        }
    }
 
    // ── Tab 2 — Анимация ──────────────────────────────────────────────────────
    private void initAnimation(int rx, int oy, int rw) {
        // Idle pose
        addRenderableWidget(Button.builder(Component.literal("◀"), b -> {
            int i = indexOf(NpcEntityData.IDLE_POSES, draftData.idlePose);
            draftData.idlePose = NpcEntityData.IDLE_POSES[
                Math.floorMod(i - 1, NpcEntityData.IDLE_POSES.length)];
            rebuildScreen();
        }).bounds(rx, oy + 46, 18, 16).build());
 
        addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
            int i = indexOf(NpcEntityData.IDLE_POSES, draftData.idlePose);
            draftData.idlePose = NpcEntityData.IDLE_POSES[
                Math.floorMod(i + 1, NpcEntityData.IDLE_POSES.length)];
            rebuildScreen();
        }).bounds(rx + 100, oy + 46, 18, 16).build());
 
        // Walk speed
        addRenderableWidget(Button.builder(Component.literal("◀"), b -> {
            draftData.walkSpeed = Math.max(0.0f, Math.round((draftData.walkSpeed - 0.1f) * 10) / 10.0f);
            rebuildScreen();
        }).bounds(rx, oy + 74, 18, 16).build());
 
        addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
            draftData.walkSpeed = Math.min(1.0f, Math.round((draftData.walkSpeed + 0.1f) * 10) / 10.0f);
            rebuildScreen();
        }).bounds(rx + 100, oy + 74, 18, 16).build());
 
        // Look at player toggle
        addRenderableWidget(Button.builder(
            Component.literal("Смотреть на игрока: " + (draftData.lookAtPlayer ? "§aВКЛ§r" : "§cВЫКЛ§r")),
            b -> { draftData.lookAtPlayer = !draftData.lookAtPlayer; rebuildScreen(); }
        ).bounds(rx, oy + 100, rw, 18).build());
 
        // Right arm pose
        addRenderableWidget(Button.builder(Component.literal("◀"), b -> {
            int i = indexOf(NpcEntityData.ARM_POSES, draftData.rightArmPose);
            draftData.rightArmPose = NpcEntityData.ARM_POSES[
                Math.floorMod(i - 1, NpcEntityData.ARM_POSES.length)];
            rebuildScreen();
        }).bounds(rx, oy + 132, 18, 16).build());
 
        addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
            int i = indexOf(NpcEntityData.ARM_POSES, draftData.rightArmPose);
            draftData.rightArmPose = NpcEntityData.ARM_POSES[
                Math.floorMod(i + 1, NpcEntityData.ARM_POSES.length)];
            rebuildScreen();
        }).bounds(rx + 130, oy + 132, 18, 16).build());
 
        // Left arm pose
        addRenderableWidget(Button.builder(Component.literal("◀"), b -> {
            int i = indexOf(NpcEntityData.ARM_POSES, draftData.leftArmPose);
            draftData.leftArmPose = NpcEntityData.ARM_POSES[
                Math.floorMod(i - 1, NpcEntityData.ARM_POSES.length)];
            rebuildScreen();
        }).bounds(rx, oy + 162, 18, 16).build());
 
        addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
            int i = indexOf(NpcEntityData.ARM_POSES, draftData.leftArmPose);
            draftData.leftArmPose = NpcEntityData.ARM_POSES[
                Math.floorMod(i + 1, NpcEntityData.ARM_POSES.length)];
            rebuildScreen();
        }).bounds(rx + 130, oy + 162, 18, 16).build());
 
        // Lock body rotation toggle
        addRenderableWidget(Button.builder(
            Component.literal("Фикс. поворот тела: " + (draftData.lockBodyRotation ? "§aВКЛ§r" : "§cВЫКЛ§r")),
            b -> { draftData.lockBodyRotation = !draftData.lockBodyRotation; rebuildScreen(); }
        ).bounds(rx, oy + 190, rw, 18).build());
 
        // Body yaw (only when locked)
        if (draftData.lockBodyRotation) {
            addRenderableWidget(Button.builder(Component.literal("◀"), b -> {
                draftData.bodyYaw = (draftData.bodyYaw - 15 + 360) % 360;
                rebuildScreen();
            }).bounds(rx, oy + 216, 18, 16).build());
 
            addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
                draftData.bodyYaw = (draftData.bodyYaw + 15) % 360;
                rebuildScreen();
            }).bounds(rx + 90, oy + 216, 18, 16).build());
        }
    }
 
    // ── Tab 3 — Отношения ─────────────────────────────────────────────────────
    private void initRelations(int rx, int oy, int rw) {
        if (editingFaction) {
            initFactionEditor(rx, oy, rw);
            return;
        }
 
        // Player relation radio buttons
        int relX = rx;
        for (String rel : NpcEntityData.RELATIONS) {
            boolean sel = draftData.playerRelation.equals(rel);
            addRenderableWidget(Button.builder(
                Component.literal((sel ? "§e◉§r " : "○ ") + NpcEntityData.relationLabel(rel)),
                b -> { draftData.playerRelation = rel; rebuildScreen(); }
            ).bounds(relX, oy + 46, 110, 18).build());
            relX += 114;
        }
 
        // Faction selector
        List<FactionData> factions = ClientFactionState.getAll();
        addRenderableWidget(Button.builder(Component.literal("◀"), b -> {
            int i = factionIndexOf(factions, draftData.factionId);
            draftData.factionId = (i <= 0) ? "" : factions.get(i - 1).id;
            rebuildScreen();
        }).bounds(rx, oy + 80, 18, 16).build());
 
        addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
            int i = factionIndexOf(factions, draftData.factionId);
            if (!factions.isEmpty())
                draftData.factionId = (i < 0 || i >= factions.size() - 1)
                    ? factions.get(0).id : factions.get(i + 1).id;
            rebuildScreen();
        }).bounds(rx + 20 + 120, oy + 80, 18, 16).build());
 
        addRenderableWidget(Button.builder(Component.literal("✕"), b -> {
            draftData.factionId = ""; rebuildScreen();
        }).bounds(rx + 20 + 120 + 22, oy + 80, 20, 16).build());
 
        // Create faction
        addRenderableWidget(Button.builder(Component.literal("+ Создать фракцию"), b -> {
            editingFaction = true;
            draftFaction = new FactionData();
            rebuildScreen();
        }).bounds(rx, oy + 104, 130, 18).build());
 
        // Edit selected faction
        FactionData sel = ClientFactionState.get(draftData.factionId);
        if (sel != null) {
            addRenderableWidget(Button.builder(Component.literal("✎ Редактировать"), b -> {
                editingFaction = true;
                draftFaction = sel.copy();
                rebuildScreen();
            }).bounds(rx + 134, oy + 104, 120, 18).build());
        }
 
        // Faction relations list (read-only labels, drawn in render)
    }
 
    private void initFactionEditor(int rx, int oy, int rw) {
        if (draftFaction == null) draftFaction = new FactionData();
 
        // Name field
        factionNameField = new EditBox(font, rx, oy + 48, rw, 18, Component.literal("Название"));
        factionNameField.setMaxLength(48);
        factionNameField.setValue(draftFaction.name);
        addRenderableWidget(factionNameField);
 
        // Description field
        factionDescField = new EditBox(font, rx, oy + 82, rw, 18, Component.literal("Описание"));
        factionDescField.setMaxLength(128);
        factionDescField.setValue(draftFaction.description);
        addRenderableWidget(factionDescField);
 
        // Relation toggles for other factions
        List<FactionData> others = ClientFactionState.getAll().stream()
            .filter(f -> !f.id.equals(draftFaction.id)).toList();
 
        int relY = oy + 150;
        for (FactionData other : others) {
            addRenderableWidget(Button.builder(Component.literal("◀▶ " +
                NpcEntityData.relationLabel(draftFaction.getRelationTo(other.id))),
                b -> {
                    String[] rels = NpcEntityData.RELATIONS;
                    int i = indexOf(rels, draftFaction.getRelationTo(other.id));
                    draftFaction.setRelationTo(other.id, rels[Math.floorMod(i + 1, rels.length)]);
                    rebuildScreen();
                }
            ).bounds(rx + 100, relY, 120, 14).build());
            relY += 18;
        }
 
        // Save / Cancel faction
        int bY = oy + H - 26;
        addRenderableWidget(Button.builder(Component.literal("Сохранить фракцию"), b -> {
            pullFactionFields();
            if (!draftFaction.name.isBlank()) {
                ModNetwork.CHANNEL.sendToServer(new SaveFactionPacket(draftFaction, false));
                ClientFactionState.sync(mergeFaction(ClientFactionState.getAll(), draftFaction));
                if (draftData.factionId.isEmpty()) draftData.factionId = draftFaction.id;
            }
            editingFaction = false; draftFaction = null;
            rebuildScreen();
        }).bounds(rx, bY, 140, 20).build());
 
        addRenderableWidget(Button.builder(Component.literal("Удалить фракцию"), b -> {
            ModNetwork.CHANNEL.sendToServer(new SaveFactionPacket(draftFaction, true));
            List<FactionData> updated = new java.util.ArrayList<>(ClientFactionState.getAll());
            updated.removeIf(f -> f.id.equals(draftFaction.id));
            ClientFactionState.sync(updated);
            if (draftData.factionId.equals(draftFaction.id)) draftData.factionId = "";
            editingFaction = false; draftFaction = null;
            rebuildScreen();
        }).bounds(rx + 144, bY, 130, 20).build());
 
        addRenderableWidget(Button.builder(Component.literal("Отмена"), b -> {
            editingFaction = false; draftFaction = null; rebuildScreen();
        }).bounds(rx + 278, bY, 80, 20).build());
    }
 
    // ── Render ────────────────────────────────────────────────────────────────
    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
 
        int ox = ox(), oy = oy();
        int rx = ox + SIDEBAR_W + 8;
        int rw = CONTENT_W - 16;
        int px = ox + SIDEBAR_W + CONTENT_W; // preview panel left edge
 
        // Main panel
        g.fill(ox, oy, ox + W, oy + H, 0xEE0A0A14);
        brd(g, ox, oy, W, H, 0xFF3A3A55);
 
        // Header
        g.fill(ox, oy, ox + W, oy + 24, 0xBB12121E);
        brd(g, ox, oy, W, 24, 0xFF444466);
        g.drawCenteredString(font, "§e⚙ §fНастройка NPC§e ⚙", ox + W / 2, oy + 7, 0xFFE6D7B5);
 
        // Sidebar
        g.fill(ox, oy + 24, ox + SIDEBAR_W, oy + H, 0x99101020);
        brd(g, ox, oy + 24, SIDEBAR_W, H - 24, 0xFF2A2A44);
        g.drawString(font, "§7ВКЛАДКИ", ox + 8, oy + 28, 0xFF666677, false);
 
        // Active tab accent line
        int tabY = oy + 30 + activeTab * 40;
        g.fill(ox + 2, tabY + 1, ox + 4, tabY + 33, TAB_ACCENT[activeTab]);
 
        // Content area
        g.fill(ox + SIDEBAR_W, oy + 24, px, oy + H - 28, 0x88090912);
 
        // Preview panel
        g.fill(px, oy + 24, ox + W, oy + H, 0x99121220);
        brd(g, px, oy + 24, PREVIEW_W, H - 24, 0xFF2A2A44);
        g.drawCenteredString(font, "§7Превью", px + PREVIEW_W / 2, oy + 28, 0xFF888877);
 
        // Dividers
        g.fill(ox + SIDEBAR_W, oy + 24, ox + SIDEBAR_W + 1, oy + H, 0xFF2A2A44);
        g.fill(px, oy + 24, px + 1, oy + H, 0xFF2A2A44);
 
        // Bottom bar
        g.fill(ox, oy + H - 28, ox + W, oy + H, 0x99101020);
        brd(g, ox, oy + H - 28, W, 28, 0xFF2A2A44);
 
        // 3D Entity preview
        renderPreview3D(g, px, oy, mx, my);
 
        // Tab-specific text labels
        switch (activeTab) {
            case 0 -> renderInfoLabels(g, rx, oy, rw);
            case 1 -> renderInteractionLabels(g, rx, oy, rw);
            case 2 -> renderAnimationLabels(g, rx, oy, rw);
            case 3 -> renderRelationsLabels(g, rx, oy, rw);
        }
 
        super.render(g, mx, my, pt);
    }
 
    // ── 3D Preview rendering ──────────────────────────────────────────────────
    private void renderPreview3D(GuiGraphics g, int panelX, int oy, int mx, int my) {
        if (npcEntity == null || !npcEntity.isAlive()) return;
 
        // Temporarily apply draft data on the client entity for live preview
        String backupJson  = npcEntity.entityData.get(NpcEntity.DATA_NPC_JSON);
        Pose   backupPose  = npcEntity.getPose();
 
        try {
            npcEntity.entityData.set(NpcEntity.DATA_NPC_JSON, GSON.toJson(draftData));
            npcEntity.setCustomName(net.minecraft.network.chat.Component.literal(draftData.displayName));
            npcEntity.setPose("CROUCHING".equals(draftData.idlePose) ? Pose.CROUCHING : Pose.STANDING);
 
            int cx = panelX + PREVIEW_W / 2;
            int cy = oy + H - 90;
            InventoryScreen.renderEntityInInventoryFollowsMouse(g, cx, cy, 50, mx, my, npcEntity);
        } finally {
            npcEntity.entityData.set(NpcEntity.DATA_NPC_JSON, backupJson);
            NpcEntityData original = GSON.fromJson(backupJson, NpcEntityData.class);
            if (original != null)
                npcEntity.setCustomName(net.minecraft.network.chat.Component.literal(original.displayName));
            npcEntity.setPose(backupPose);
        }
    }
 
    // ── Label rendering per tab ───────────────────────────────────────────────
    private void renderInfoLabels(GuiGraphics g, int rx, int oy, int rw) {
        header(g, rx, oy + 7, "ИНФОРМАЦИЯ О ПЕРСОНАЖЕ");
        g.drawString(font, "§7Имя NPC:", rx, oy + 27, 0xFF888877, false);
 
        g.drawString(font, "§7Скин / Текстура:", rx, oy + 64, 0xFF888877, false);
        String skinName = draftData.skinId.equals("default") ? "§7По умолчанию" : "§f" + draftData.skinId;
        g.drawCenteredString(font, skinName, rx + 20 + 45, oy + 77, 0xFFCCCCCC);
 
        g.drawString(font, "§7Внешность персонажа:", rx, oy + 108, 0xFF888877, false);
 
        int partY = oy + 118;
        for (Map.Entry<String, String[]> entry : NpcProfile.PART_OPTIONS.entrySet()) {
            String key = entry.getKey();
            String label = NpcProfile.PART_LABELS.getOrDefault(key, key);
            String value = entry.getValue()[draftData.bodyParts.getOrDefault(key, 0)];
            g.drawString(font, "§8" + label + ":", rx, partY + 1, 0xFF777788, false);
            g.drawCenteredString(font, "§f" + value, rx + 72 + 10 + 36, partY + 2, 0xFFCCCCCC);
            partY += 18;
        }
    }
 
    private void renderInteractionLabels(GuiGraphics g, int rx, int oy, int rw) {
        header(g, rx, oy + 7, "ВЗАИМОДЕЙСТВИЕ");
 
        g.drawString(font, "§7Диалог при обращении:", rx, oy + 30, 0xFF888877, false);
        List<com.frametrip.dragonlegacyquesttoast.server.DialogueDefinition> dlgs = ClientDialogueState.getAll();
        String dlgLabel = draftData.dialogueId.isEmpty() ? "§8— нет —" :
            dlgs.stream().filter(d -> d.id.equals(draftData.dialogueId))
                .map(d -> "§f" + d.npcName + " §8[" + d.id + "]")
                .findFirst().orElse("§c" + draftData.dialogueId);
        g.drawCenteredString(font, dlgLabel, rx + 20 + 65, oy + 49, 0xFFCCCCCC);
 
        g.drawString(font, "§7Связанные квесты  §8(" +
            draftData.questIds.size() + " выбрано):", rx, oy + 74, 0xFF888877, false);
 
        List<com.frametrip.dragonlegacyquesttoast.server.QuestDefinition> quests = ClientQuestState.getAll();
        if (quests.isEmpty()) {
            g.drawString(font, "§8Квестов ещё нет...", rx + 4, oy + 94, 0xFF555566, false);
        }
    }
 
    private void renderAnimationLabels(GuiGraphics g, int rx, int oy, int rw) {
        header(g, rx, oy + 7, "АНИМАЦИЯ");
 
        g.drawString(font, "§7Поза:", rx, oy + 36, 0xFF888877, false);
        g.drawCenteredString(font, "§f" + NpcEntityData.idlePoseLabel(draftData.idlePose),
            rx + 20 + 40, oy + 49, 0xFFCCCCCC);
 
        g.drawString(font, "§7Скорость ходьбы:", rx, oy + 64, 0xFF888877, false);
        g.drawCenteredString(font, "§f" + String.format("%.1f", draftData.walkSpeed),
            rx + 20 + 40, oy + 77, 0xFFCCCCCC);
 
        g.drawString(font, "§7Поза правой руки:", rx, oy + 122, 0xFF888877, false);
        g.drawCenteredString(font, "§f" + NpcEntityData.armPoseLabel(draftData.rightArmPose),
            rx + 20 + 55, oy + 135, 0xFFCCCCCC);
 
        g.drawString(font, "§7Поза левой руки:", rx, oy + 152, 0xFF888877, false);
        g.drawCenteredString(font, "§f" + NpcEntityData.armPoseLabel(draftData.leftArmPose),
            rx + 20 + 55, oy + 165, 0xFFCCCCCC);
 
        if (draftData.lockBodyRotation) {
            g.drawString(font, "§7Угол поворота тела:", rx, oy + 206, 0xFF888877, false);
            g.drawCenteredString(font, "§f" + (int) draftData.bodyYaw + "°",
                rx + 20 + 35, oy + 219, 0xFFCCCCCC);
        }
    }
 
    private void renderRelationsLabels(GuiGraphics g, int rx, int oy, int rw) {
        if (editingFaction && draftFaction != null) {
            renderFactionEditorLabels(g, rx, oy, rw);
            return;
        }
 
        header(g, rx, oy + 7, "ОТНОШЕНИЯ");
 
        g.drawString(font, "§7Отношение NPC к игроку:", rx, oy + 30, 0xFF888877, false);
 
        g.drawString(font, "§7Фракция NPC:", rx, oy + 70, 0xFF888877, false);
        FactionData fac = ClientFactionState.get(draftData.factionId);
        String facLabel = fac == null ? "§8Без фракции" : "§f" + fac.name;
        g.drawCenteredString(font, facLabel, rx + 20 + 60, oy + 83, 0xFFCCCCCC);
 
        // Faction relations
        if (fac != null && !ClientFactionState.getAll().isEmpty()) {
            g.drawString(font, "§7Отношения фракции:", rx, oy + 132, 0xFF888877, false);
            int ry = oy + 146;
            for (FactionData other : ClientFactionState.getAll()) {
                if (other.id.equals(fac.id)) continue;
                String rel = fac.getRelationTo(other.id);
                int relColor = "FRIENDLY".equals(rel) ? 0xFF44EE55 :
                    "HOSTILE".equals(rel) ? 0xFFEE4444 : 0xFFAAAAAA;
                g.drawString(font, "§f" + other.name + "§r: ", rx + 4, ry, 0xFFCCCCCC, false);
                g.drawString(font, NpcEntityData.relationLabel(rel), rx + 4 + font.width(other.name + ": ") + 2, ry, relColor, false);
                ry += 12;
                if (ry > oy + H - 60) break;
            }
        }
    }
 
    private void renderFactionEditorLabels(GuiGraphics g, int rx, int oy, int rw) {
        header(g, rx, oy + 7, "РЕДАКТОР ФРАКЦИИ");
 
        g.drawString(font, "§7Название фракции:", rx, oy + 36, 0xFF888877, false);
        g.drawString(font, "§7Описание:", rx, oy + 70, 0xFF888877, false);
 
        g.drawString(font, "§7Цвет фракции:", rx, oy + 104, 0xFF888877, false);
        // Color swatches
        int swatchX = rx;
        int swatchY = oy + 116;
        for (int i = 0; i < SWATCH_COUNT; i++) {
            int col = SWATCH_COLORS[i];
            g.fill(swatchX + i * 22, swatchY, swatchX + i * 22 + 18, swatchY + 18, col);
            if (draftFaction.color == col) {
                brd(g, swatchX + i * 22 - 1, swatchY - 1, 20, 20, 0xFFFFFFFF);
            } else {
                brd(g, swatchX + i * 22, swatchY, 18, 18, 0xFF444444);
            }
        }
 
        // Other faction relations
        List<FactionData> others = ClientFactionState.getAll().stream()
            .filter(f -> !f.id.equals(draftFaction.id)).toList();
 
        if (!others.isEmpty()) {
            g.drawString(font, "§7Отношения с другими фракциями:", rx, oy + 142, 0xFF888877, false);
            int ry = oy + 150;
            for (FactionData other : others) {
                g.drawString(font, "§f" + other.name + ":", rx, ry + 1, 0xFFCCCCCC, false);
                ry += 18;
            }
        }
    }
 
    // ── Mouse handling ────────────────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        // Color swatch clicks in faction editor (Tab 3)
        if (activeTab == 3 && editingFaction && draftFaction != null) {
            int ox = ox(), oy = oy();
            int rx = ox + SIDEBAR_W + 8;
            int swatchX = rx;
            int swatchY = oy + 116;
            for (int i = 0; i < SWATCH_COUNT; i++) {
                int x0 = swatchX + i * 22, x1 = x0 + 18;
                int y0 = swatchY, y1 = swatchY + 18;
                if (mx >= x0 && mx < x1 && my >= y0 && my < y1) {
                    draftFaction.color = SWATCH_COLORS[i];
                    rebuildScreen();
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, btn);
    }
 
    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (activeTab == 1) {
            int total = ClientQuestState.getAll().size();
            questScroll = Math.max(0, Math.min(Math.max(0, total - 10),
                questScroll - (int) Math.signum(delta)));
            rebuildScreen();
            return true;
        }
        return super.mouseScrolled(mx, my, delta);
    }
 
    // ── Save / Close ──────────────────────────────────────────────────────────
    private void save() {
        pullFields();
        if (draftData.displayName.isBlank()) draftData.displayName = "NPC";
        ModNetwork.CHANNEL.sendToServer(
            new SaveNpcEntityDataPacket(npcEntity.getUUID(), draftData)
        );
        // Apply immediately on client side for instant feedback
        npcEntity.setNpcData(draftData);
    }
 
    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(null);
    }
 
    // ── Field sync helpers ────────────────────────────────────────────────────
    private void pullFields() {
        if (activeTab == 0 && nameField != null)
            draftData.displayName = nameField.getValue().isBlank() ? "NPC" : nameField.getValue();
        if (activeTab == 3 && editingFaction) pullFactionFields();
    }
 
    private void pullFactionFields() {
        if (draftFaction == null) return;
        if (factionNameField != null) draftFaction.name = factionNameField.getValue();
        if (factionDescField != null) draftFaction.description = factionDescField.getValue();
    }
 
    private void rebuildScreen() { clearWidgets(); init(); }
 
    // ── Drawing helpers ───────────────────────────────────────────────────────
    private void header(GuiGraphics g, int x, int y, String text) {
        g.fill(x, y + 14, x + CONTENT_W - 16, y + 15, TAB_ACCENT[activeTab]);
        g.drawString(font, "§l" + text, x, y, TAB_ACCENT[activeTab], false);
    }
 
    private static void brd(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }
 
    private int ox() { return (width  - W) / 2; }
    private int oy() { return (height - H) / 2; }
 
    // ── Utilities ─────────────────────────────────────────────────────────────
    private static int indexOf(String[] arr, String val) {
        for (int i = 0; i < arr.length; i++) if (arr[i].equals(val)) return i;
        return 0;
    }
 
    private static int indexOfId(List<String> list, String id) {
        for (int i = 0; i < list.size(); i++) if (list.get(i).equals(id)) return i;
        return -1;
    }
 
    private static int factionIndexOf(List<FactionData> factions, String id) {
        for (int i = 0; i < factions.size(); i++) if (factions.get(i).id.equals(id)) return i;
        return -1;
    }
 
    private static java.util.List<FactionData> mergeFaction(
            java.util.List<FactionData> list, FactionData f) {
        java.util.List<FactionData> r = new java.util.ArrayList<>();
        boolean found = false;
        for (FactionData e : list) {
            if (e.id.equals(f.id)) { r.add(f); found = true; } else r.add(e);
        }
        if (!found) r.add(f);
        return r;
    }
 
    @Override public boolean isPauseScreen() { return false; }
}

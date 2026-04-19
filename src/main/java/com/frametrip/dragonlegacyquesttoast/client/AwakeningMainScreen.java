package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;

import java.util.List;

public class AwakeningMainScreen extends Screen {
    private static final ResourceLocation BG_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_main_bg_320x220.png");

    private static final ResourceLocation CENTER_FRAME_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_center_frame_96x96.png");

    private static final ResourceLocation FIRE_SEAL_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/path_fire_seal_48x48.png");
    private static final ResourceLocation FIRE_SEAL_ACTIVE_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/path_fire_seal_active_48x48.png");

    private static final ResourceLocation ICE_SEAL_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/path_ice_seal_48x48.png");
    private static final ResourceLocation ICE_SEAL_ACTIVE_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/path_ice_seal_active_48x48.png");

    private static final ResourceLocation STORM_SEAL_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/path_storm_seal_48x48.png");
    private static final ResourceLocation STORM_SEAL_ACTIVE_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/path_storm_seal_active_48x48.png");

    private static final ResourceLocation VOID_SEAL_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/path_void_seal_48x48.png");
    private static final ResourceLocation VOID_SEAL_ACTIVE_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/path_void_seal_active_48x48.png");

    private static final ResourceLocation ATTRIBUTES_PANEL_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_attributes_panel_120x80.png");

    private static final ResourceLocation BODY_ICON_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/attribute_body_icon_16x16.png");
    private static final ResourceLocation MIND_ICON_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/attribute_mind_icon_16x16.png");
    private static final ResourceLocation SPIRIT_ICON_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/attribute_spirit_icon_16x16.png");
    private static final ResourceLocation BOND_ICON_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/attribute_bond_icon_16x16.png");

    private enum EditTarget {
        BACKGROUND("BG"),
        CENTER("CENTER"),
        PLAYER("PLAYER"),
        FIRE("FIRE"),
        ICE("ICE"),
        STORM("STORM"),
        VOID("VOID"),
        ATTRIBUTES_PANEL("ATTR-P"),
        ATTRIBUTES_ROW("ATTR-R"),
        ATTRIBUTES_ICON("ATTR-I"),
        ATTRIBUTES_NAME("ATTR-N"),
        ATTRIBUTES_VALUE("ATTR-V"),
        ATTRIBUTES_HITBOX("ATTR-H"),
        ATTRIBUTES_TOOLTIP("ATTR-TIP");

        private final String label;

        EditTarget(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public EditTarget next() {
            EditTarget[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }

        public EditTarget prev() {
            EditTarget[] values = values();
            return values[(this.ordinal() - 1 + values.length) % values.length];
        }
    }

    public enum AttributeType {
        BODY("Тело", 5, "Физическая мощь, стойкость и выживание.", BODY_ICON_TEXTURE),
        MIND("Разум", 2, "Знания, мышление, понимание и расчёт.", MIND_ICON_TEXTURE),
        SPIRIT("Дух", 7, "Воля, внутренняя энергия и сила пробуждения.", SPIRIT_ICON_TEXTURE),
        BOND("Связь", 1, "Резонанс с путями, сущностями и миром.", BOND_ICON_TEXTURE);

        private final String title;
        private final int points;
        private final String description;
        private final ResourceLocation icon;

        AttributeType(String title, int points, String description, ResourceLocation icon) {
            this.title = title;
            this.points = points;
            this.description = description;
            this.icon = icon;
        }

        public String title() {
            return title;
        }

        public int points() {
            return points;
        }

        public String description() {
            return description;
        }

        public ResourceLocation icon() {
            return icon;
        }

        public AttributeType next() {
            AttributeType[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }

        public AttributeType prev() {
            AttributeType[] values = values();
            return values[(this.ordinal() - 1 + values.length) % values.length];
        }
    }

    private static class AttributeLayout {
        int rowX;
        int rowY;
        int rowWidth;
        int rowHeight;

        int iconX;
        int iconY;
        int iconSize;

        int nameX;
        int nameY;

        int valueX;
        int valueY;

        int hitboxX;
        int hitboxY;
        int hitboxWidth;
        int hitboxHeight;

        AttributeLayout copy() {
            AttributeLayout c = new AttributeLayout();
            c.rowX = rowX;
            c.rowY = rowY;
            c.rowWidth = rowWidth;
            c.rowHeight = rowHeight;
            c.iconX = iconX;
            c.iconY = iconY;
            c.iconSize = iconSize;
            c.nameX = nameX;
            c.nameY = nameY;
            c.valueX = valueX;
            c.valueY = valueY;
            c.hitboxX = hitboxX;
            c.hitboxY = hitboxY;
            c.hitboxWidth = hitboxWidth;
            c.hitboxHeight = hitboxHeight;
            return c;
        }
    }

    private AwakeningPathType hoveredPath = null;
    private AwakeningPathType selectedPath = null;
    private AttributeType hoveredAttribute = null;

    private boolean editMode = false;
    private boolean previewMode = false;
    private EditTarget selectedTarget = EditTarget.CENTER;
    private AttributeType selectedAttribute = AttributeType.BODY;

    private int draftBgX;
    private int draftBgY;
    private int draftBgWidth;
    private int draftBgHeight;

    private int draftCenterFrameX;
    private int draftCenterFrameY;
    private int draftCenterFrameWidth;
    private int draftCenterFrameHeight;

    private int draftPlayerOffsetX;
    private int draftPlayerOffsetY;
    private float draftPlayerScale;

    private int draftPathFrameSize;
    private int draftPathIconSize;

    private int draftFireX;
    private int draftFireY;
    private int draftIceX;
    private int draftIceY;
    private int draftStormX;
    private int draftStormY;
    private int draftVoidX;
    private int draftVoidY;

    private int draftAttributesPanelX;
    private int draftAttributesPanelY;
    private int draftAttributesPanelWidth;
    private int draftAttributesPanelHeight;

    private int draftTooltipWidth;
    private int draftTooltipMinHeight;

    private AttributeLayout draftBodyLayout;
    private AttributeLayout draftMindLayout;
    private AttributeLayout draftSpiritLayout;
    private AttributeLayout draftBondLayout;

    private Button editButton;
    private Button previewButton;
    private Button saveButton;
    private Button cancelButton;
    private Button resetButton;
    private Button targetPrevButton;
    private Button targetNextButton;
    private Button attrPrevButton;
    private Button attrNextButton;
    private Button leftButton;
    private Button rightButton;
    private Button upButton;
    private Button downButton;
    private Button widthPlusButton;
    private Button widthMinusButton;
    private Button heightPlusButton;
    private Button heightMinusButton;
    private Button scalePlusButton;
    private Button scaleMinusButton;
    private Button editorMenuButton;

    public AwakeningMainScreen() {
        super(Component.literal("Круг Пробуждения"));
    }

    @Override
    protected void init() {
        super.init();
        loadDraftFromSaved();
        createEditorButtons();
        refreshEditorButtons();
    }

    private void loadDraftFromSaved() {
        draftBgX = ClientAwakeningScreenState.getBgX();
        draftBgY = ClientAwakeningScreenState.getBgY();
        draftBgWidth = ClientAwakeningScreenState.getBgWidth();
        draftBgHeight = ClientAwakeningScreenState.getBgHeight();

        draftCenterFrameX = ClientAwakeningScreenState.getCenterFrameX();
        draftCenterFrameY = ClientAwakeningScreenState.getCenterFrameY();
        draftCenterFrameWidth = ClientAwakeningScreenState.getCenterFrameWidth();
        draftCenterFrameHeight = ClientAwakeningScreenState.getCenterFrameHeight();

        draftPlayerOffsetX = ClientAwakeningScreenState.getPlayerOffsetX();
        draftPlayerOffsetY = ClientAwakeningScreenState.getPlayerOffsetY();
        draftPlayerScale = ClientAwakeningScreenState.getPlayerScale();

        draftPathFrameSize = ClientAwakeningScreenState.getPathFrameSize();
        draftPathIconSize = ClientAwakeningScreenState.getPathIconSize();

        draftFireX = ClientAwakeningScreenState.getFireX();
        draftFireY = ClientAwakeningScreenState.getFireY();
        draftIceX = ClientAwakeningScreenState.getIceX();
        draftIceY = ClientAwakeningScreenState.getIceY();
        draftStormX = ClientAwakeningScreenState.getStormX();
        draftStormY = ClientAwakeningScreenState.getStormY();
        draftVoidX = ClientAwakeningScreenState.getVoidX();
        draftVoidY = ClientAwakeningScreenState.getVoidY();

        draftAttributesPanelX = ClientAwakeningScreenState.getAttributesPanelX();
        draftAttributesPanelY = ClientAwakeningScreenState.getAttributesPanelY();
        draftAttributesPanelWidth = ClientAwakeningScreenState.getAttributesPanelWidth();
        draftAttributesPanelHeight = ClientAwakeningScreenState.getAttributesPanelHeight();

        draftTooltipWidth = ClientAwakeningScreenState.getTooltipWidth();
        draftTooltipMinHeight = ClientAwakeningScreenState.getTooltipMinHeight();

        draftBodyLayout = toDraft(ClientAwakeningScreenState.getBodyLayout());
        draftMindLayout = toDraft(ClientAwakeningScreenState.getMindLayout());
        draftSpiritLayout = toDraft(ClientAwakeningScreenState.getSpiritLayout());
        draftBondLayout = toDraft(ClientAwakeningScreenState.getBondLayout());
    }

    private AttributeLayout toDraft(ClientAwakeningScreenState.AttributeLayoutData data) {
        AttributeLayout l = new AttributeLayout();
        l.rowX = data.rowX;
        l.rowY = data.rowY;
        l.rowWidth = data.rowWidth;
        l.rowHeight = data.rowHeight;
        l.iconX = data.iconX;
        l.iconY = data.iconY;
        l.iconSize = data.iconSize;
        l.nameX = data.nameX;
        l.nameY = data.nameY;
        l.valueX = data.valueX;
        l.valueY = data.valueY;
        l.hitboxX = data.hitboxX;
        l.hitboxY = data.hitboxY;
        l.hitboxWidth = data.hitboxWidth;
        l.hitboxHeight = data.hitboxHeight;
        return l;
    }

    private ClientAwakeningScreenState.AttributeLayoutData toData(AttributeLayout l) {
        ClientAwakeningScreenState.AttributeLayoutData d = new ClientAwakeningScreenState.AttributeLayoutData();
        d.rowX = l.rowX;
        d.rowY = l.rowY;
        d.rowWidth = l.rowWidth;
        d.rowHeight = l.rowHeight;
        d.iconX = l.iconX;
        d.iconY = l.iconY;
        d.iconSize = l.iconSize;
        d.nameX = l.nameX;
        d.nameY = l.nameY;
        d.valueX = l.valueX;
        d.valueY = l.valueY;
        d.hitboxX = l.hitboxX;
        d.hitboxY = l.hitboxY;
        d.hitboxWidth = l.hitboxWidth;
        d.hitboxHeight = l.hitboxHeight;
        return d;
    }

    private AttributeLayout getDraftLayout(AttributeType type) {
        return switch (type) {
            case BODY -> draftBodyLayout;
            case MIND -> draftMindLayout;
            case SPIRIT -> draftSpiritLayout;
            case BOND -> draftBondLayout;
        };
    }

    private boolean canEdit() {
        return this.minecraft != null
                && this.minecraft.player != null
                && this.minecraft.player.getAbilities().instabuild;
    }

    private void createEditorButtons() {
        int panelX = 8;
        int panelY = 8;
        int bw = 42;
        int bh = 20;

        editorMenuButton = this.addRenderableWidget(
                Button.builder(Component.literal("UI"), b -> {
                    if (this.minecraft != null && canEdit()) {
                        this.minecraft.setScreen(new UiEditorMenuScreen(this));
                    }
                }).bounds(panelX, panelY, 30, bh).build()
        );

        editButton = this.addRenderableWidget(
                Button.builder(Component.literal("Ред"), b -> {
                    if (!canEdit()) return;
                    editMode = !editMode;
                    if (!editMode) {
                        previewMode = false;
                        loadDraftFromSaved();
                    }
                    refreshEditorButtons();
                }).bounds(panelX + 34, panelY, bw, bh).build()
        );

        previewButton = this.addRenderableWidget(
                Button.builder(Component.literal("Прев"), b -> {
                    if (editMode) {
                        previewMode = !previewMode;
                        refreshEditorButtons();
                    }
                }).bounds(panelX + 80, panelY, bw, bh).build()
        );

        saveButton = this.addRenderableWidget(
                Button.builder(Component.literal("Сохр"), b -> {
                    saveDraftToState();
                    loadDraftFromSaved();
                    editMode = false;
                    previewMode = false;
                    refreshEditorButtons();
                }).bounds(panelX + 126, panelY, 52, bh).build()
        );

        cancelButton = this.addRenderableWidget(
                Button.builder(Component.literal("Отм"), b -> {
                    loadDraftFromSaved();
                    editMode = false;
                    previewMode = false;
                    refreshEditorButtons();
                }).bounds(panelX + 182, panelY, 46, bh).build()
        );

        resetButton = this.addRenderableWidget(
                Button.builder(Component.literal("Сброс"), b -> {
                    resetSelectedTargetDraft();
                    refreshEditorButtons();
                }).bounds(panelX, panelY + 26, 52, bh).build()
        );

        targetPrevButton = this.addRenderableWidget(
                Button.builder(Component.literal("<"), b -> {
                    selectedTarget = selectedTarget.prev();
                    refreshEditorButtons();
                }).bounds(panelX + 56, panelY + 26, 20, bh).build()
        );

        targetNextButton = this.addRenderableWidget(
                Button.builder(Component.literal(">"), b -> {
                    selectedTarget = selectedTarget.next();
                    refreshEditorButtons();
                }).bounds(panelX + 208, panelY + 26, 20, bh).build()
        );

        attrPrevButton = this.addRenderableWidget(
                Button.builder(Component.literal("A<"), b -> {
                    selectedAttribute = selectedAttribute.prev();
                    refreshEditorButtons();
                }).bounds(panelX + 56, panelY + 50, 28, bh).build()
        );

        attrNextButton = this.addRenderableWidget(
                Button.builder(Component.literal("A>"), b -> {
                    selectedAttribute = selectedAttribute.next();
                    refreshEditorButtons();
                }).bounds(panelX + 176, panelY + 50, 28, bh).build()
        );

        leftButton = this.addRenderableWidget(
                Button.builder(Component.literal("←"), b -> nudgeSelected(-1, 0)).bounds(panelX, panelY + 82, 20, 20).build()
        );

        rightButton = this.addRenderableWidget(
                Button.builder(Component.literal("→"), b -> nudgeSelected(1, 0)).bounds(panelX + 48, panelY + 82, 20, 20).build()
        );

        upButton = this.addRenderableWidget(
                Button.builder(Component.literal("↑"), b -> nudgeSelected(0, -1)).bounds(panelX + 24, panelY + 60, 20, 20).build()
        );

        downButton = this.addRenderableWidget(
                Button.builder(Component.literal("↓"), b -> nudgeSelected(0, 1)).bounds(panelX + 24, panelY + 82, 20, 20).build()
        );

        widthPlusButton = this.addRenderableWidget(
                Button.builder(Component.literal("W+"), b -> resizeSelected(1, 0)).bounds(panelX + 76, panelY + 60, 30, 20).build()
        );

        widthMinusButton = this.addRenderableWidget(
                Button.builder(Component.literal("W-"), b -> resizeSelected(-1, 0)).bounds(panelX + 108, panelY + 60, 30, 20).build()
        );

        heightPlusButton = this.addRenderableWidget(
                Button.builder(Component.literal("H+"), b -> resizeSelected(0, 1)).bounds(panelX + 76, panelY + 82, 30, 20).build()
        );

        heightMinusButton = this.addRenderableWidget(
                Button.builder(Component.literal("H-"), b -> resizeSelected(0, -1)).bounds(panelX + 108, panelY + 82, 30, 20).build()
        );

        scalePlusButton = this.addRenderableWidget(
                Button.builder(Component.literal("S+"), b -> scalePlayer(1)).bounds(panelX + 76, panelY + 104, 30, 20).build()
        );

        scaleMinusButton = this.addRenderableWidget(
                Button.builder(Component.literal("S-"), b -> scalePlayer(-1)).bounds(panelX + 108, panelY + 104, 30, 20).build()
        );
    }

    private void refreshEditorButtons() {
        boolean creative = canEdit();
        boolean showEditor = creative && editMode && !previewMode;

        editorMenuButton.visible = creative;
        editButton.visible = creative;
        previewButton.visible = creative && editMode;
        saveButton.visible = creative && editMode;
        cancelButton.visible = creative && editMode;
        resetButton.visible = showEditor;
        targetPrevButton.visible = showEditor;
        targetNextButton.visible = showEditor;

        leftButton.visible = showEditor;
        rightButton.visible = showEditor;
        upButton.visible = showEditor;
        downButton.visible = showEditor;
        widthPlusButton.visible = showEditor;
        widthMinusButton.visible = showEditor;
        heightPlusButton.visible = showEditor;
        heightMinusButton.visible = showEditor;

        boolean playerSelected = showEditor && selectedTarget == EditTarget.PLAYER;
        scalePlusButton.visible = playerSelected;
        scaleMinusButton.visible = playerSelected;

        boolean attrMode = showEditor && (
                selectedTarget == EditTarget.ATTRIBUTES_ROW ||
                selectedTarget == EditTarget.ATTRIBUTES_ICON ||
                selectedTarget == EditTarget.ATTRIBUTES_NAME ||
                selectedTarget == EditTarget.ATTRIBUTES_VALUE ||
                selectedTarget == EditTarget.ATTRIBUTES_HITBOX
        );

        attrPrevButton.visible = attrMode;
        attrNextButton.visible = attrMode;
    }

    private void saveDraftToState() {
        ClientAwakeningScreenState.applyBackgroundConfig(
                draftBgX, draftBgY, draftBgWidth, draftBgHeight
        );

        ClientAwakeningScreenState.applyCenterConfig(
                draftCenterFrameX,
                draftCenterFrameY,
                draftCenterFrameWidth,
                draftCenterFrameHeight,
                draftPlayerOffsetX,
                draftPlayerOffsetY,
                draftPlayerScale
        );

        ClientAwakeningScreenState.applyPathsConfig(
                draftPathFrameSize,
                draftPathIconSize,
                draftFireX, draftFireY,
                draftIceX, draftIceY,
                draftStormX, draftStormY,
                draftVoidX, draftVoidY
        );

        ClientAwakeningScreenState.applyAttributesPanelConfig(
                draftAttributesPanelX,
                draftAttributesPanelY,
                draftAttributesPanelWidth,
                draftAttributesPanelHeight
        );

        ClientAwakeningScreenState.applyTooltipConfig(
                draftTooltipWidth,
                draftTooltipMinHeight
        );

        ClientAwakeningScreenState.applyAttributeLayouts(
                toData(draftBodyLayout),
                toData(draftMindLayout),
                toData(draftSpiritLayout),
                toData(draftBondLayout)
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int bgX = draftBgX;
        int bgY = draftBgY;
        int bgWidth = draftBgWidth;
        int bgHeight = draftBgHeight;

        hoveredPath = getPathAt(mouseX, mouseY);
        hoveredAttribute = getHoveredAttribute(mouseX, mouseY, bgX, bgY);

        RenderSystem.enableBlend();
        guiGraphics.blit(BG_TEXTURE, bgX, bgY, 0, 0, bgWidth, bgHeight, bgWidth, bgHeight);

        int frameX = bgX + draftCenterFrameX;
        int frameY = bgY + draftCenterFrameY;
        int frameW = draftCenterFrameWidth;
        int frameH = draftCenterFrameHeight;

        guiGraphics.blit(CENTER_FRAME_TEXTURE, frameX, frameY, 0, 0, frameW, frameH, frameW, frameH);

        renderPlayerInCenter(guiGraphics, frameX, frameY, frameW, frameH, mouseX, mouseY);

        renderPathSeal(guiGraphics,
                bgX + draftFireX,
                bgY + draftFireY,
                FIRE_SEAL_TEXTURE, FIRE_SEAL_ACTIVE_TEXTURE,
                AwakeningPathType.FIRE,
                hoveredPath == AwakeningPathType.FIRE);

        renderPathSeal(guiGraphics,
                bgX + draftIceX,
                bgY + draftIceY,
                ICE_SEAL_TEXTURE, ICE_SEAL_ACTIVE_TEXTURE,
                AwakeningPathType.ICE,
                hoveredPath == AwakeningPathType.ICE);

        renderPathSeal(guiGraphics,
                bgX + draftStormX,
                bgY + draftStormY,
                STORM_SEAL_TEXTURE, STORM_SEAL_ACTIVE_TEXTURE,
                AwakeningPathType.STORM,
                hoveredPath == AwakeningPathType.STORM);

        renderPathSeal(guiGraphics,
                bgX + draftVoidX,
                bgY + draftVoidY,
                VOID_SEAL_TEXTURE, VOID_SEAL_ACTIVE_TEXTURE,
                AwakeningPathType.VOID,
                hoveredPath == AwakeningPathType.VOID);

        renderAttributesPanel(guiGraphics, bgX, bgY);

        AwakeningPathType infoPath = hoveredPath != null ? hoveredPath : selectedPath;
        if (infoPath != null) {
            renderPathInfoPanel(guiGraphics, bgX, bgY, infoPath);
        }

        if (hoveredAttribute != null) {
            renderAttributeTooltip(guiGraphics, mouseX, mouseY, hoveredAttribute);
        }

        AwakeningPathType titlePath = hoveredPath != null ? hoveredPath : selectedPath;
        if (titlePath != null && (!editMode || previewMode)) {
            guiGraphics.drawCenteredString(this.font, titlePath.getTitle(), this.width / 2, bgY + 8, 0xE6D7B5);
        }

        if (editMode && !previewMode) {
            renderEditorOverlay(guiGraphics);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderEditorOverlay(GuiGraphics guiGraphics) {
        int panelX = 8;
        int panelY = 8;

        guiGraphics.fill(panelX - 4, panelY + 22, panelX + 258, panelY + 144, 0x88000000);
        guiGraphics.drawString(this.font, "EDIT MODE", panelX, panelY + 52, 0xFFD98C, false);
        guiGraphics.drawString(this.font, "Target: " + selectedTarget.label(), panelX + 80, panelY + 32, 0xFFFFFF, false);

        if (selectedTarget == EditTarget.ATTRIBUTES_ROW
                || selectedTarget == EditTarget.ATTRIBUTES_ICON
                || selectedTarget == EditTarget.ATTRIBUTES_NAME
                || selectedTarget == EditTarget.ATTRIBUTES_VALUE
                || selectedTarget == EditTarget.ATTRIBUTES_HITBOX) {
            guiGraphics.drawString(this.font, "Attr: " + selectedAttribute.title(), panelX + 90, panelY + 54, 0xFFD98C, false);
        }

        drawSelectionBoxes(guiGraphics);

        String info1 = "";
        String info2 = "";

        AttributeLayout layout = getDraftLayout(selectedAttribute);

        switch (selectedTarget) {
            case BACKGROUND -> {
                info1 = "x=" + draftBgX + " y=" + draftBgY;
                info2 = "w=" + draftBgWidth + " h=" + draftBgHeight;
            }
            case CENTER -> {
                info1 = "x=" + draftCenterFrameX + " y=" + draftCenterFrameY;
                info2 = "w=" + draftCenterFrameWidth + " h=" + draftCenterFrameHeight;
            }
            case PLAYER -> {
                info1 = "ox=" + draftPlayerOffsetX + " oy=" + draftPlayerOffsetY;
                info2 = "scale=" + Math.round(draftPlayerScale);
            }
            case FIRE -> {
                info1 = "x=" + draftFireX + " y=" + draftFireY;
                info2 = "size=" + draftPathFrameSize;
            }
            case ICE -> {
                info1 = "x=" + draftIceX + " y=" + draftIceY;
                info2 = "size=" + draftPathFrameSize;
            }
            case STORM -> {
                info1 = "x=" + draftStormX + " y=" + draftStormY;
                info2 = "size=" + draftPathFrameSize;
            }
            case VOID -> {
                info1 = "x=" + draftVoidX + " y=" + draftVoidY;
                info2 = "size=" + draftPathFrameSize;
            }
            case ATTRIBUTES_PANEL -> {
                info1 = "x=" + draftAttributesPanelX + " y=" + draftAttributesPanelY;
                info2 = "w=" + draftAttributesPanelWidth + " h=" + draftAttributesPanelHeight;
            }
            case ATTRIBUTES_ROW -> {
                info1 = "x=" + layout.rowX + " y=" + layout.rowY;
                info2 = "w=" + layout.rowWidth + " h=" + layout.rowHeight;
            }
            case ATTRIBUTES_ICON -> {
                info1 = "x=" + layout.iconX + " y=" + layout.iconY;
                info2 = "size=" + layout.iconSize;
            }
            case ATTRIBUTES_NAME -> {
                info1 = "x=" + layout.nameX + " y=" + layout.nameY;
                info2 = "name pos";
            }
            case ATTRIBUTES_VALUE -> {
                info1 = "x=" + layout.valueX + " y=" + layout.valueY;
                info2 = "value pos";
            }
            case ATTRIBUTES_HITBOX -> {
                info1 = "x=" + layout.hitboxX + " y=" + layout.hitboxY;
                info2 = "w=" + layout.hitboxWidth + " h=" + layout.hitboxHeight;
            }
            case ATTRIBUTES_TOOLTIP -> {
                info1 = "w=" + draftTooltipWidth;
                info2 = "minH=" + draftTooltipMinHeight;
            }
        }

        guiGraphics.drawString(this.font, info1, panelX, panelY + 116, 0xD8D8D8, false);
        guiGraphics.drawString(this.font, info2, panelX, panelY + 128, 0xD8D8D8, false);
    }

    private void drawSelectionBoxes(GuiGraphics guiGraphics) {
        int bgX = draftBgX;
        int bgY = draftBgY;
        int bgW = draftBgWidth;
        int bgH = draftBgHeight;

        int frameX = bgX + draftCenterFrameX;
        int frameY = bgY + draftCenterFrameY;
        int frameW = draftCenterFrameWidth;
        int frameH = draftCenterFrameHeight;

        int pathSize = draftPathFrameSize;

        drawBox(guiGraphics, bgX, bgY, bgW, bgH, selectedTarget == EditTarget.BACKGROUND ? 0xFFFFAA00 : 0x66FFFFFF);
        drawBox(guiGraphics, frameX, frameY, frameW, frameH, selectedTarget == EditTarget.CENTER ? 0xFFFFAA00 : 0x66FFFFFF);

        int playerX = frameX + (frameW / 2) + draftPlayerOffsetX - 12;
        int playerY = frameY + frameH - 10 + draftPlayerOffsetY - 40;
        drawBox(guiGraphics, playerX, playerY, 24, 48, selectedTarget == EditTarget.PLAYER ? 0xFFFFAA00 : 0x66FFFFFF);

        drawBox(guiGraphics, bgX + draftFireX, bgY + draftFireY, pathSize, pathSize, selectedTarget == EditTarget.FIRE ? 0xFFFFAA00 : 0x66FFFFFF);
        drawBox(guiGraphics, bgX + draftIceX, bgY + draftIceY, pathSize, pathSize, selectedTarget == EditTarget.ICE ? 0xFFFFAA00 : 0x66FFFFFF);
        drawBox(guiGraphics, bgX + draftStormX, bgY + draftStormY, pathSize, pathSize, selectedTarget == EditTarget.STORM ? 0xFFFFAA00 : 0x66FFFFFF);
        drawBox(guiGraphics, bgX + draftVoidX, bgY + draftVoidY, pathSize, pathSize, selectedTarget == EditTarget.VOID ? 0xFFFFAA00 : 0x66FFFFFF);

        int panelX = bgX + draftAttributesPanelX;
        int panelY = bgY + draftAttributesPanelY;
        drawBox(guiGraphics, panelX, panelY, draftAttributesPanelWidth, draftAttributesPanelHeight,
                selectedTarget == EditTarget.ATTRIBUTES_PANEL ? 0xFFFFAA00 : 0x66FFFFFF);

        AttributeLayout layout = getDraftLayout(selectedAttribute);
        int baseX = panelX + layout.rowX;
        int baseY = panelY + layout.rowY;

        if (selectedTarget == EditTarget.ATTRIBUTES_ROW) {
            drawBox(guiGraphics, baseX, baseY, layout.rowWidth, layout.rowHeight, 0xFFFFAA00);
        }
        if (selectedTarget == EditTarget.ATTRIBUTES_ICON) {
            drawBox(guiGraphics, baseX + layout.iconX, baseY + layout.iconY, layout.iconSize, layout.iconSize, 0xFFFFAA00);
        }
        if (selectedTarget == EditTarget.ATTRIBUTES_NAME) {
            drawBox(guiGraphics, baseX + layout.nameX - 1, baseY + layout.nameY - 1, 52, 10, 0xFFFFAA00);
        }
        if (selectedTarget == EditTarget.ATTRIBUTES_VALUE) {
            drawBox(guiGraphics, baseX + layout.valueX - 1, baseY + layout.valueY - 1, 12, 10, 0xFFFFAA00);
        }
        if (selectedTarget == EditTarget.ATTRIBUTES_HITBOX) {
            drawBox(guiGraphics, baseX + layout.hitboxX, baseY + layout.hitboxY, layout.hitboxWidth, layout.hitboxHeight, 0xFFFFAA00);
        }
    }

    private void drawBox(GuiGraphics guiGraphics, int x, int y, int w, int h, int color) {
        guiGraphics.fill(x, y, x + w, y + 1, color);
        guiGraphics.fill(x, y + h - 1, x + w, y + h, color);
        guiGraphics.fill(x, y, x + 1, y + h, color);
        guiGraphics.fill(x + w - 1, y, x + w, y + h, color);
    }

    private void renderPathSeal(
            GuiGraphics guiGraphics,
            int x,
            int y,
            ResourceLocation normalTexture,
            ResourceLocation activeTexture,
            AwakeningPathType pathType,
            boolean hovered
    ) {
        int size = draftPathFrameSize;
        boolean locked = isPathLocked(pathType);
        boolean selected = selectedPath == pathType;

        guiGraphics.blit(
                (hovered || selected) ? activeTexture : normalTexture,
                x, y,
                0, 0,
                size, size,
                size, size
        );

        if (locked) {
            renderLockedSealOverlay(guiGraphics, x, y, size, hovered);
        } else if (selected) {
            drawBox(guiGraphics, x - 2, y - 2, size + 4, size + 4, 0xCCFFD98C);
        }

        if (!locked && (hovered || selected) && (!editMode || previewMode)) {
            renderSimpleHoverAura(guiGraphics, x, y, size, pathType);
        }

        if (locked && hovered && (!editMode || previewMode)) {
            renderLockedHoverAura(guiGraphics, x, y, size);
        }
    }

    private void renderSimpleHoverAura(GuiGraphics guiGraphics, int x, int y, int size, AwakeningPathType pathType) {
        int color = switch (pathType) {
            case FIRE -> 0x66FF9A2E;
            case ICE -> 0x66BFEFFF;
            case STORM -> 0x6697B6FF;
            case VOID -> 0x667A52B8;
        };

        drawBox(guiGraphics, x - 2, y - 2, size + 4, size + 4, color);
    }

    private void renderLockedSealOverlay(GuiGraphics guiGraphics, int x, int y, int size, boolean hovered) {
        int veilColor = hovered ? 0x66000000 : 0x88000000;
        int outerColor = 0xAA6E584A;
        int innerColor = 0x88614B3F;
        int chainColor = 0xCC9A7B61;
        int chainShadow = 0x884E3C31;
        int sigilColor = 0xE2C29D;

        guiGraphics.fill(x, y, x + size, y + size, veilColor);

        drawBox(guiGraphics, x, y, size, size, outerColor);
        drawBox(guiGraphics, x + 1, y + 1, size - 2, size - 2, innerColor);

        drawDiagonalSeal(guiGraphics, x + 7, y + 7, x + size - 8, y + size - 8, chainShadow);
        drawDiagonalSeal(guiGraphics, x + size - 8, y + 7, x + 7, y + size - 8, chainShadow);

        drawDiagonalSeal(guiGraphics, x + 8, y + 7, x + size - 7, y + size - 8, chainColor);
        drawDiagonalSeal(guiGraphics, x + size - 7, y + 7, x + 8, y + size - 8, chainColor);

        drawChainLineHorizontal(guiGraphics, x + 6, y + size / 2 - 1, size - 12, chainColor, chainShadow);
        drawChainLineVertical(guiGraphics, x + size / 2 - 1, y + 6, size - 12, chainColor, chainShadow);

        guiGraphics.drawCenteredString(this.font, "◆", x + size / 2, y + size / 2 - 4, sigilColor);

        guiGraphics.fill(x + 3, y + 3, x + 6, y + 4, outerColor);
        guiGraphics.fill(x + 3, y + 3, x + 4, y + 6, outerColor);

        guiGraphics.fill(x + size - 6, y + 3, x + size - 3, y + 4, outerColor);
        guiGraphics.fill(x + size - 4, y + 3, x + size - 3, y + 6, outerColor);

        guiGraphics.fill(x + 3, y + size - 4, x + 6, y + size - 3, outerColor);
        guiGraphics.fill(x + 3, y + size - 6, x + 4, y + size - 3, outerColor);

        guiGraphics.fill(x + size - 6, y + size - 4, x + size - 3, y + size - 3, outerColor);
        guiGraphics.fill(x + size - 4, y + size - 6, x + size - 3, y + size - 3, outerColor);
    }

    private void renderLockedHoverAura(GuiGraphics guiGraphics, int x, int y, int size) {
        drawBox(guiGraphics, x - 2, y - 2, size + 4, size + 4, 0x88745D4E);
        drawBox(guiGraphics, x - 4, y - 4, size + 8, size + 8, 0x444C3B31);
    }

    private void drawDiagonalSeal(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        for (int i = 0; i <= steps; i++) {
            int x = x1 + (x2 - x1) * i / steps;
            int y = y1 + (y2 - y1) * i / steps;
            guiGraphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    private void drawChainLineHorizontal(GuiGraphics guiGraphics, int x, int y, int width, int color, int shadowColor) {
        for (int i = 0; i < width; i += 6) {
            int segW = Math.min(4, width - i);

            guiGraphics.fill(x + i, y + 1, x + i + segW, y + 2, shadowColor);
            guiGraphics.fill(x + i, y, x + i + segW, y + 1, color);

            if (i + 4 < width) {
                guiGraphics.fill(x + i + 4, y - 1, x + i + 5, y + 2, shadowColor);
                guiGraphics.fill(x + i + 4, y - 1, x + i + 5, y + 1, color);
            }
        }
    }

    private void drawChainLineVertical(GuiGraphics guiGraphics, int x, int y, int height, int color, int shadowColor) {
        for (int i = 0; i < height; i += 6) {
            int segH = Math.min(4, height - i);

            guiGraphics.fill(x + 1, y + i, x + 2, y + i + segH, shadowColor);
            guiGraphics.fill(x, y + i, x + 1, y + i + segH, color);

            if (i + 4 < height) {
                guiGraphics.fill(x - 1, y + i + 4, x + 2, y + i + 5, shadowColor);
                guiGraphics.fill(x - 1, y + i + 4, x + 1, y + i + 5, color);
            }
        }
    }

    private void renderPlayerInCenter(GuiGraphics guiGraphics, int frameX, int frameY, int frameW, int frameH, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        LivingEntity entity = mc.player;

        int modelX = frameX + (frameW / 2) + draftPlayerOffsetX;
        int modelY = frameY + frameH - 10 + draftPlayerOffsetY;
        int scale = Math.round(draftPlayerScale);

        float angleX = (float) Math.atan((modelX - mouseX) / 40.0F);
        float angleY = (float) Math.atan((modelY - mouseY) / 40.0F);

        Quaternionf bodyRotation = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf headRotation = new Quaternionf().rotateX(angleY * 20.0F * ((float) Math.PI / 180.0F));
        bodyRotation.mul(headRotation);

        float oldBodyRot = entity.yBodyRot;
        float oldYRot = entity.getYRot();
        float oldXRot = entity.getXRot();
        float oldYHeadRotO = entity.yHeadRotO;
        float oldYHeadRot = entity.yHeadRot;

        entity.yBodyRot = 180.0F + angleX * 20.0F;
        entity.setYRot(180.0F + angleX * 40.0F);
        entity.setXRot(-angleY * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();

        InventoryScreen.renderEntityInInventory(
                guiGraphics,
                modelX,
                modelY,
                scale,
                bodyRotation,
                headRotation,
                entity
        );

        entity.yBodyRot = oldBodyRot;
        entity.setYRot(oldYRot);
        entity.setXRot(oldXRot);
        entity.yHeadRotO = oldYHeadRotO;
        entity.yHeadRot = oldYHeadRot;
    }

    private void renderAttributesPanel(GuiGraphics guiGraphics, int bgX, int bgY) {
        int panelX = bgX + draftAttributesPanelX;
        int panelY = bgY + draftAttributesPanelY;
        int panelW = draftAttributesPanelWidth;
        int panelH = draftAttributesPanelHeight;

        guiGraphics.blit(
                ATTRIBUTES_PANEL_TEXTURE,
                panelX, panelY,
                0, 0,
                panelW, panelH,
                panelW, panelH
        );

        renderAttributeRow(guiGraphics, panelX, panelY, AttributeType.BODY);
        renderAttributeRow(guiGraphics, panelX, panelY, AttributeType.MIND);
        renderAttributeRow(guiGraphics, panelX, panelY, AttributeType.SPIRIT);
        renderAttributeRow(guiGraphics, panelX, panelY, AttributeType.BOND);
    }

    private void renderAttributeRow(GuiGraphics guiGraphics, int panelX, int panelY, AttributeType type) {
        AttributeLayout layout = getDraftLayout(type);
        int level = getAttributeLevel(type.points());

        int baseX = panelX + layout.rowX;
        int baseY = panelY + layout.rowY;

        guiGraphics.blit(
                type.icon(),
                baseX + layout.iconX,
                baseY + layout.iconY,
                0, 0,
                layout.iconSize,
                layout.iconSize,
                layout.iconSize,
                layout.iconSize
        );

        int nameColor = hoveredAttribute == type ? 0xFFD98C : 0xFFFFFF;
        int maxNameWidth = Math.max(10, layout.rowWidth - layout.nameX - 6);
        String nameText = clipToWidth(type.title(), maxNameWidth);

        guiGraphics.drawString(
                this.font,
                nameText,
                baseX + layout.nameX,
                baseY + layout.nameY,
                nameColor,
                false
        );

        guiGraphics.drawString(
                this.font,
                String.valueOf(level),
                baseX + layout.valueX,
                baseY + layout.valueY,
                0xE6D7B5,
                false
        );

        if (editMode && !previewMode && selectedAttribute == type) {
            drawBox(guiGraphics, baseX, baseY, layout.rowWidth, layout.rowHeight, 0x44FFD98C);
        }
    }

    private String clipToWidth(String text, int maxWidth) {
        if (this.font.width(text) <= maxWidth) return text;

        String ellipsis = "...";
        int ellipsisWidth = this.font.width(ellipsis);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            String next = result.toString() + text.charAt(i);
            if (this.font.width(next) + ellipsisWidth > maxWidth) {
                return result + ellipsis;
            }
            result.append(text.charAt(i));
        }

        return result.toString();
    }

    private int getAttributeLevel(int points) {
        return Math.max(0, points / 3);
    }

    private AttributeType getHoveredAttribute(double mouseX, double mouseY, int bgX, int bgY) {
        int panelX = bgX + draftAttributesPanelX;
        int panelY = bgY + draftAttributesPanelY;

        for (AttributeType type : AttributeType.values()) {
            AttributeLayout layout = getDraftLayout(type);
            int baseX = panelX + layout.rowX;
            int baseY = panelY + layout.rowY;

            if (isInside(
                    mouseX,
                    mouseY,
                    baseX + layout.hitboxX,
                    baseY + layout.hitboxY,
                    layout.hitboxWidth,
                    layout.hitboxHeight
            )) {
                return type;
            }
        }

        return null;
    }

    private void renderAttributeTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, AttributeType type) {
        int tooltipX = mouseX + 10;
        int tooltipY = mouseY - 4;
        int padding = 6;
        int textWidth = Math.max(20, draftTooltipWidth - padding * 2);

        List<FormattedCharSequence> wrapped = this.font.split(Component.literal(type.description()), textWidth);

        int titleHeight = 10;
        int lineHeight = 9;
        int textBlockHeight = wrapped.size() * lineHeight;
        int tooltipH = Math.max(draftTooltipMinHeight, padding + titleHeight + 4 + textBlockHeight + padding);

        guiGraphics.fill(tooltipX, tooltipY, tooltipX + draftTooltipWidth, tooltipY + tooltipH, 0xDD111111);
        drawBox(guiGraphics, tooltipX, tooltipY, draftTooltipWidth, tooltipH, 0x99E6D7B5);

        guiGraphics.drawString(this.font, type.title(), tooltipX + padding, tooltipY + padding, 0xE6D7B5, false);

        int textY = tooltipY + padding + 12;
        for (int i = 0; i < wrapped.size(); i++) {
            guiGraphics.drawString(this.font, wrapped.get(i), tooltipX + padding, textY + i * lineHeight, 0xFFFFFF, false);
        }
    }

    private void renderPathInfoPanel(GuiGraphics guiGraphics, int bgX, int bgY, AwakeningPathType pathType) {
        int panelX = bgX + 192;
        int panelY = bgY + 144;
        int panelW = 118;
        int panelH = 62;

        guiGraphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xCC111111);
        drawBox(guiGraphics, panelX, panelY, panelW, panelH, 0x99E6D7B5);

        String title = pathType.getTitle();
        String status = getPathStatusText(pathType);
        String desc = getPathShortDescription(pathType);

        guiGraphics.drawString(this.font, title, panelX + 6, panelY + 6, 0xE6D7B5, false);
        guiGraphics.drawString(this.font, status, panelX + 6, panelY + 18, getPathStatusColor(pathType), false);

        int textWidth = panelW - 12;
        List<FormattedCharSequence> lines = this.font.split(Component.literal(desc), textWidth);

        int textY = panelY + 32;
        for (int i = 0; i < lines.size() && i < 3; i++) {
            guiGraphics.drawString(this.font, lines.get(i), panelX + 6, textY + i * 9, 0xFFFFFF, false);
        }
    }

    private String getPathShortDescription(AwakeningPathType pathType) {
        if (isPathLocked(pathType)) {
            return switch (pathType) {
                case FIRE -> "Печать пламени сокрыта. Этот путь ещё не пробуждён.";
                case ICE -> "Холодная печать молчит. Путь остаётся закрытым.";
                case STORM -> "Грозовой знак запечатан. Сила ещё не откликнулась.";
                case VOID -> "Печать пустоты скрыта глубокой завесой.";
            };
        }

        return switch (pathType) {
            case FIRE -> "Сила пламени, напор, урон и агрессия.";
            case ICE -> "Контроль, холод, замедление и стойкость.";
            case STORM -> "Скорость, разряды, рывки и давление.";
            case VOID -> "Искажение, тайна, нестабильная сила пустоты.";
        };
    }

    private boolean isPathLocked(AwakeningPathType pathType) {
        return switch (pathType) {
            case FIRE -> false;
            case ICE -> false;
            case STORM -> true;
            case VOID -> true;
        };
    }

    private String getPathStatusText(AwakeningPathType pathType) {
        return isPathLocked(pathType) ? "Запечатан" : "Открыт";
    }

    private int getPathStatusColor(AwakeningPathType pathType) {
        return isPathLocked(pathType) ? 0xB8876B : 0x8FD98C;
    }

    private AwakeningPathType getPathAt(double mouseX, double mouseY) {
        int bgX = draftBgX;
        int bgY = draftBgY;
        int size = draftPathFrameSize;

        if (isInside(mouseX, mouseY, bgX + draftFireX, bgY + draftFireY, size, size)) return AwakeningPathType.FIRE;
        if (isInside(mouseX, mouseY, bgX + draftIceX, bgY + draftIceY, size, size)) return AwakeningPathType.ICE;
        if (isInside(mouseX, mouseY, bgX + draftStormX, bgY + draftStormY, size, size)) return AwakeningPathType.STORM;
        if (isInside(mouseX, mouseY, bgX + draftVoidX, bgY + draftVoidY, size, size)) return AwakeningPathType.VOID;

        return null;
    }

    private boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!editMode) {
            AwakeningPathType clickedPath = getPathAt(mouseX, mouseY);
            if (clickedPath != null) {
                if (isPathLocked(clickedPath)) {
                    selectedPath = clickedPath;
                    return true;
                }

                if (selectedPath == clickedPath) {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new AwakeningPathDetailScreen(this, clickedPath));
                    }
                } else {
                    selectedPath = clickedPath;
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void nudgeSelected(int dx, int dy) {
        AttributeLayout layout = getDraftLayout(selectedAttribute);

        switch (selectedTarget) {
            case BACKGROUND -> {
                draftBgX += dx;
                draftBgY += dy;
            }
            case CENTER -> {
                draftCenterFrameX += dx;
                draftCenterFrameY += dy;
            }
            case PLAYER -> {
                draftPlayerOffsetX += dx;
                draftPlayerOffsetY += dy;
            }
            case FIRE -> {
                draftFireX += dx;
                draftFireY += dy;
            }
            case ICE -> {
                draftIceX += dx;
                draftIceY += dy;
            }
            case STORM -> {
                draftStormX += dx;
                draftStormY += dy;
            }
            case VOID -> {
                draftVoidX += dx;
                draftVoidY += dy;
            }
            case ATTRIBUTES_PANEL -> {
                draftAttributesPanelX += dx;
                draftAttributesPanelY += dy;
            }
            case ATTRIBUTES_ROW -> {
                layout.rowX += dx;
                layout.rowY += dy;
            }
            case ATTRIBUTES_ICON -> {
                layout.iconX += dx;
                layout.iconY += dy;
            }
            case ATTRIBUTES_NAME -> {
                layout.nameX += dx;
                layout.nameY += dy;
            }
            case ATTRIBUTES_VALUE -> {
                layout.valueX += dx;
                layout.valueY += dy;
            }
            case ATTRIBUTES_HITBOX -> {
                layout.hitboxX += dx;
                layout.hitboxY += dy;
            }
            case ATTRIBUTES_TOOLTIP -> {
            }
        }
    }

    private void resizeSelected(int dw, int dh) {
        AttributeLayout layout = getDraftLayout(selectedAttribute);

        switch (selectedTarget) {
            case BACKGROUND -> {
                draftBgWidth = Math.max(1, draftBgWidth + dw);
                draftBgHeight = Math.max(1, draftBgHeight + dh);
            }
            case CENTER -> {
                draftCenterFrameWidth = Math.max(1, draftCenterFrameWidth + dw);
                draftCenterFrameHeight = Math.max(1, draftCenterFrameHeight + dh);
            }
            case FIRE, ICE, STORM, VOID -> {
                draftPathFrameSize = Math.max(1, draftPathFrameSize + dw);
                draftPathIconSize = Math.max(1, draftPathIconSize + dw);
            }
            case ATTRIBUTES_PANEL -> {
                draftAttributesPanelWidth = Math.max(1, draftAttributesPanelWidth + dw);
                draftAttributesPanelHeight = Math.max(1, draftAttributesPanelHeight + dh);
            }
            case ATTRIBUTES_ROW -> {
                layout.rowWidth = Math.max(1, layout.rowWidth + dw);
                layout.rowHeight = Math.max(1, layout.rowHeight + dh);
            }
            case ATTRIBUTES_ICON -> {
                layout.iconSize = Math.max(1, layout.iconSize + dw);
            }
            case ATTRIBUTES_NAME -> {
            }
            case ATTRIBUTES_VALUE -> {
            }
            case ATTRIBUTES_HITBOX -> {
                layout.hitboxWidth = Math.max(1, layout.hitboxWidth + dw);
                layout.hitboxHeight = Math.max(1, layout.hitboxHeight + dh);
            }
            case ATTRIBUTES_TOOLTIP -> {
                draftTooltipWidth = Math.max(1, draftTooltipWidth + dw);
                draftTooltipMinHeight = Math.max(1, draftTooltipMinHeight + dh);
            }
            case PLAYER -> {
            }
        }
    }

    private void scalePlayer(int delta) {
        draftPlayerScale = Math.max(1.0F, draftPlayerScale + delta);
    }

    private void resetSelectedTargetDraft() {
        AttributeLayout layout = getDraftLayout(selectedAttribute);

        switch (selectedTarget) {
            case BACKGROUND -> {
                draftBgX = 0;
                draftBgY = 0;
                draftBgWidth = 320;
                draftBgHeight = 220;
            }
            case CENTER -> {
                draftCenterFrameX = 112;
                draftCenterFrameY = 44;
                draftCenterFrameWidth = 96;
                draftCenterFrameHeight = 96;
            }
            case PLAYER -> {
                draftPlayerOffsetX = 0;
                draftPlayerOffsetY = 8;
                draftPlayerScale = 38.0F;
            }
            case FIRE, ICE, STORM, VOID -> {
                draftPathFrameSize = 48;
                draftPathIconSize = 32;
                draftFireX = 136;
                draftFireY = 12;
                draftIceX = 56;
                draftIceY = 68;
                draftStormX = 216;
                draftStormY = 68;
                draftVoidX = 136;
                draftVoidY = 148;
            }
            case ATTRIBUTES_PANEL -> {
                draftAttributesPanelX = 8;
                draftAttributesPanelY = 132;
                draftAttributesPanelWidth = 120;
                draftAttributesPanelHeight = 80;
            }
            case ATTRIBUTES_ROW -> {
                ClientAwakeningScreenState.AttributeLayoutData def = ClientAwakeningScreenState.defaultLayoutFor(selectedAttribute);
                layout.rowX = def.rowX;
                layout.rowY = def.rowY;
                layout.rowWidth = def.rowWidth;
                layout.rowHeight = def.rowHeight;
            }
            case ATTRIBUTES_ICON -> {
                ClientAwakeningScreenState.AttributeLayoutData def = ClientAwakeningScreenState.defaultLayoutFor(selectedAttribute);
                layout.iconX = def.iconX;
                layout.iconY = def.iconY;
                layout.iconSize = def.iconSize;
            }
            case ATTRIBUTES_NAME -> {
                ClientAwakeningScreenState.AttributeLayoutData def = ClientAwakeningScreenState.defaultLayoutFor(selectedAttribute);
                layout.nameX = def.nameX;
                layout.nameY = def.nameY;
            }
            case ATTRIBUTES_VALUE -> {
                ClientAwakeningScreenState.AttributeLayoutData def = ClientAwakeningScreenState.defaultLayoutFor(selectedAttribute);
                layout.valueX = def.valueX;
                layout.valueY = def.valueY;
            }
            case ATTRIBUTES_HITBOX -> {
                ClientAwakeningScreenState.AttributeLayoutData def = ClientAwakeningScreenState.defaultLayoutFor(selectedAttribute);
                layout.hitboxX = def.hitboxX;
                layout.hitboxY = def.hitboxY;
                layout.hitboxWidth = def.hitboxWidth;
                layout.hitboxHeight = def.hitboxHeight;
            }
            case ATTRIBUTES_TOOLTIP -> {
                draftTooltipWidth = 168;
                draftTooltipMinHeight = 34;
            }
        }
    }
}

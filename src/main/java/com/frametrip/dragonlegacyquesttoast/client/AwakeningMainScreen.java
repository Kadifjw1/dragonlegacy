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
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;

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
        ATTRIBUTES("ATTR");

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

    private enum AttributeType {
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
    }

    private AwakeningPathType hoveredPath = null;
    private AttributeType hoveredAttribute = null;

    private boolean editMode = false;
    private boolean previewMode = false;
    private EditTarget selectedTarget = EditTarget.CENTER;

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

    private int draftAttributesContentOffsetX;
    private int draftAttributesContentOffsetY;
    private int draftAttributesRowSpacing;

    private int draftAttributesIconSize;
    private int draftAttributesTextOffsetX;
    private int draftAttributesValueOffsetX;

    private int draftAttributesHoverWidth;
    private int draftAttributesHoverHeight;

    private Button editButton;
    private Button previewButton;
    private Button saveButton;
    private Button cancelButton;
    private Button resetButton;
    private Button targetPrevButton;
    private Button targetNextButton;
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

        draftAttributesContentOffsetX = ClientAwakeningScreenState.getAttributesContentOffsetX();
        draftAttributesContentOffsetY = ClientAwakeningScreenState.getAttributesContentOffsetY();
        draftAttributesRowSpacing = ClientAwakeningScreenState.getAttributesRowSpacing();

        draftAttributesIconSize = ClientAwakeningScreenState.getAttributesIconSize();
        draftAttributesTextOffsetX = ClientAwakeningScreenState.getAttributesTextOffsetX();
        draftAttributesValueOffsetX = ClientAwakeningScreenState.getAttributesValueOffsetX();

        draftAttributesHoverWidth = ClientAwakeningScreenState.getAttributesHoverWidth();
        draftAttributesHoverHeight = ClientAwakeningScreenState.getAttributesHoverHeight();
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

        leftButton = this.addRenderableWidget(
                Button.builder(Component.literal("←"), b -> nudgeSelected(-1, 0)).bounds(panelX, panelY + 76, 20, 20).build()
        );

        rightButton = this.addRenderableWidget(
                Button.builder(Component.literal("→"), b -> nudgeSelected(1, 0)).bounds(panelX + 48, panelY + 76, 20, 20).build()
        );

        upButton = this.addRenderableWidget(
                Button.builder(Component.literal("↑"), b -> nudgeSelected(0, -1)).bounds(panelX + 24, panelY + 54, 20, 20).build()
        );

        downButton = this.addRenderableWidget(
                Button.builder(Component.literal("↓"), b -> nudgeSelected(0, 1)).bounds(panelX + 24, panelY + 76, 20, 20).build()
        );

        widthPlusButton = this.addRenderableWidget(
                Button.builder(Component.literal("W+"), b -> resizeSelected(1, 0)).bounds(panelX + 76, panelY + 54, 30, 20).build()
        );

        widthMinusButton = this.addRenderableWidget(
                Button.builder(Component.literal("W-"), b -> resizeSelected(-1, 0)).bounds(panelX + 108, panelY + 54, 30, 20).build()
        );

        heightPlusButton = this.addRenderableWidget(
                Button.builder(Component.literal("H+"), b -> resizeSelected(0, 1)).bounds(panelX + 76, panelY + 76, 30, 20).build()
        );

        heightMinusButton = this.addRenderableWidget(
                Button.builder(Component.literal("H-"), b -> resizeSelected(0, -1)).bounds(panelX + 108, panelY + 76, 30, 20).build()
        );

        scalePlusButton = this.addRenderableWidget(
                Button.builder(Component.literal("S+"), b -> scalePlayer(1)).bounds(panelX + 76, panelY + 102, 30, 20).build()
        );

        scaleMinusButton = this.addRenderableWidget(
                Button.builder(Component.literal("S-"), b -> scalePlayer(-1)).bounds(panelX + 108, panelY + 102, 30, 20).build()
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

        ClientAwakeningScreenState.applyAttributesConfig(
                draftAttributesPanelX,
                draftAttributesPanelY,
                draftAttributesPanelWidth,
                draftAttributesPanelHeight,
                draftAttributesContentOffsetX,
                draftAttributesContentOffsetY,
                draftAttributesRowSpacing,
                draftAttributesIconSize,
                draftAttributesTextOffsetX,
                draftAttributesValueOffsetX,
                draftAttributesHoverWidth,
                draftAttributesHoverHeight
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

        if (hoveredAttribute != null) {
            renderAttributeTooltip(guiGraphics, mouseX, mouseY, hoveredAttribute);
        }

        if (hoveredPath != null && (!editMode || previewMode)) {
            guiGraphics.drawCenteredString(this.font, hoveredPath.getTitle(), this.width / 2, bgY + 8, 0xE6D7B5);
        }

        if (editMode && !previewMode) {
            renderEditorOverlay(guiGraphics);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderEditorOverlay(GuiGraphics guiGraphics) {
        int panelX = 8;
        int panelY = 8;

        guiGraphics.fill(panelX - 4, panelY + 22, panelX + 232, panelY + 128, 0x88000000);
        guiGraphics.drawString(this.font, "EDIT MODE", panelX, panelY + 52, 0xFFD98C, false);
        guiGraphics.drawString(this.font, "Target: " + selectedTarget.label(), panelX + 80, panelY + 32, 0xFFFFFF, false);

        drawSelectionBoxes(guiGraphics);

        String info1 = "";
        String info2 = "";

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
            case ATTRIBUTES -> {
                info1 = "x=" + draftAttributesPanelX + " y=" + draftAttributesPanelY;
                info2 = "w=" + draftAttributesPanelWidth + " h=" + draftAttributesPanelHeight;
            }
        }

        guiGraphics.drawString(this.font, info1, panelX, panelY + 104, 0xD8D8D8, false);
        guiGraphics.drawString(this.font, info2, panelX, panelY + 116, 0xD8D8D8, false);
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

        drawBox(
                guiGraphics,
                bgX + draftAttributesPanelX,
                bgY + draftAttributesPanelY,
                draftAttributesPanelWidth,
                draftAttributesPanelHeight,
                selectedTarget == EditTarget.ATTRIBUTES ? 0xFFFFAA00 : 0x66FFFFFF
        );
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

        guiGraphics.blit(
                hovered ? activeTexture : normalTexture,
                x, y,
                0, 0,
                size, size,
                size, size
        );

        if (hovered && (!editMode || previewMode)) {
            renderSimpleHoverAura(guiGraphics, x, y, size, pathType);
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

        renderAttributeRow(guiGraphics, panelX + draftAttributesContentOffsetX, panelY + draftAttributesContentOffsetY, AttributeType.BODY);
        renderAttributeRow(guiGraphics, panelX + draftAttributesContentOffsetX, panelY + draftAttributesContentOffsetY + draftAttributesRowSpacing, AttributeType.MIND);
        renderAttributeRow(guiGraphics, panelX + draftAttributesContentOffsetX, panelY + draftAttributesContentOffsetY + draftAttributesRowSpacing * 2, AttributeType.SPIRIT);
        renderAttributeRow(guiGraphics, panelX + draftAttributesContentOffsetX, panelY + draftAttributesContentOffsetY + draftAttributesRowSpacing * 3, AttributeType.BOND);
    }

    private void renderAttributeRow(GuiGraphics guiGraphics, int x, int y, AttributeType type) {
        int level = getAttributeLevel(type.points());

        guiGraphics.blit(
                type.icon(),
                x,
                y - 2,
                0, 0,
                draftAttributesIconSize,
                draftAttributesIconSize,
                draftAttributesIconSize,
                draftAttributesIconSize
        );

        int nameColor = hoveredAttribute == type ? 0xFFD98C : 0xFFFFFF;
        guiGraphics.drawString(this.font, type.title(), x + draftAttributesTextOffsetX, y + 2, nameColor, false);
        guiGraphics.drawString(this.font, "" + level, x + draftAttributesValueOffsetX, y + 2, 0xE6D7B5, false);
    }

    private int getAttributeLevel(int points) {
        return Math.max(0, points / 3);
    }

    private AttributeType getHoveredAttribute(double mouseX, double mouseY, int bgX, int bgY) {
        int panelX = bgX + draftAttributesPanelX;
        int panelY = bgY + draftAttributesPanelY;

        int rowX = panelX + draftAttributesContentOffsetX;
        int rowY = panelY + draftAttributesContentOffsetY;
        int rowW = Math.max(100, draftAttributesPanelWidth - draftAttributesContentOffsetX * 2);
        int rowH = Math.max(12, draftAttributesRowSpacing);

        if (isInside(mouseX, mouseY, rowX, rowY - 2, rowW, rowH)) return AttributeType.BODY;
        if (isInside(mouseX, mouseY, rowX, rowY + draftAttributesRowSpacing - 2, rowW, rowH)) return AttributeType.MIND;
        if (isInside(mouseX, mouseY, rowX, rowY + draftAttributesRowSpacing * 2 - 2, rowW, rowH)) return AttributeType.SPIRIT;
        if (isInside(mouseX, mouseY, rowX, rowY + draftAttributesRowSpacing * 3 - 2, rowW, rowH)) return AttributeType.BOND;

        return null;
    }

    private void renderAttributeTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, AttributeType type) {
        int tooltipX = mouseX + 10;
        int tooltipY = mouseY - 4;
        int tooltipW = draftAttributesHoverWidth;
        int tooltipH = draftAttributesHoverHeight;

        guiGraphics.fill(tooltipX, tooltipY, tooltipX + tooltipW, tooltipY + tooltipH, 0xDD111111);
        drawBox(guiGraphics, tooltipX, tooltipY, tooltipW, tooltipH, 0x99E6D7B5);

        guiGraphics.drawString(this.font, type.title(), tooltipX + 6, tooltipY + 6, 0xE6D7B5, false);
        guiGraphics.drawString(this.font, type.description(), tooltipX + 6, tooltipY + 18, 0xFFFFFF, false);
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
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new AwakeningPathDetailScreen(this, clickedPath));
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void nudgeSelected(int dx, int dy) {
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
            case ATTRIBUTES -> {
                draftAttributesPanelX += dx;
                draftAttributesPanelY += dy;
            }
        }
    }

    private void resizeSelected(int dw, int dh) {
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
            case ATTRIBUTES -> {
                draftAttributesPanelWidth = Math.max(1, draftAttributesPanelWidth + dw);
                draftAttributesPanelHeight = Math.max(1, draftAttributesPanelHeight + dh);
            }
            case PLAYER -> {
            }
        }
    }

    private void scalePlayer(int delta) {
        draftPlayerScale = Math.max(1.0F, draftPlayerScale + delta);
    }

    private void resetSelectedTargetDraft() {
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
            case ATTRIBUTES -> {
                draftAttributesPanelX = 8;
                draftAttributesPanelY = 132;
                draftAttributesPanelWidth = 120;
                draftAttributesPanelHeight = 80;

                draftAttributesContentOffsetX = 8;
                draftAttributesContentOffsetY = 14;
                draftAttributesRowSpacing = 14;

                draftAttributesIconSize = 16;
                draftAttributesTextOffsetX = 20;
                draftAttributesValueOffsetX = 92;

                draftAttributesHoverWidth = 168;
                draftAttributesHoverHeight = 34;
            }
        }
    }
}

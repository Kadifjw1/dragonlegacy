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

    private enum EditTarget {
        BACKGROUND("BG"),
        CENTER("CENTER"),
        PLAYER("PLAYER"),
        FIRE("FIRE"),
        ICE("ICE"),
        STORM("STORM"),
        VOID("VOID");

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

    private AwakeningPathType hoveredPath = null;

    private boolean editMode = false;
    private boolean previewMode = false;
    private EditTarget selectedTarget = EditTarget.CENTER;

    private Button editButton;
    private Button previewButton;
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

    public AwakeningMainScreen() {
        super(Component.literal("Круг Пробуждения"));
    }

    @Override
    protected void init() {
        super.init();
        createEditorButtons();
        refreshEditorButtons();
    }

    private void createEditorButtons() {
        int panelX = 8;
        int panelY = 8;
        int bw = 42;
        int bh = 20;

        editButton = this.addRenderableWidget(
                Button.builder(Component.literal("Ред"), b -> {
                    editMode = !editMode;
                    if (!editMode) {
                        previewMode = false;
                    }
                    refreshEditorButtons();
                }).bounds(panelX, panelY, bw, bh).build()
        );

        previewButton = this.addRenderableWidget(
                Button.builder(Component.literal("Прев"), b -> {
                    if (editMode) {
                        previewMode = !previewMode;
                        refreshEditorButtons();
                    }
                }).bounds(panelX + 46, panelY, bw, bh).build()
        );

        resetButton = this.addRenderableWidget(
                Button.builder(Component.literal("Сброс"), b -> {
                    resetSelectedTarget();
                    refreshEditorButtons();
                }).bounds(panelX + 92, panelY, 52, bh).build()
        );

        targetPrevButton = this.addRenderableWidget(
                Button.builder(Component.literal("<"), b -> {
                    selectedTarget = selectedTarget.prev();
                    refreshEditorButtons();
                }).bounds(panelX, panelY + 26, 20, bh).build()
        );

        targetNextButton = this.addRenderableWidget(
                Button.builder(Component.literal(">"), b -> {
                    selectedTarget = selectedTarget.next();
                    refreshEditorButtons();
                }).bounds(panelX + 124, panelY + 26, 20, bh).build()
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
        boolean showEditor = editMode && !previewMode;

        previewButton.active = editMode;
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int bgX = ClientAwakeningScreenState.getBgX();
        int bgY = ClientAwakeningScreenState.getBgY();
        int bgWidth = ClientAwakeningScreenState.getBgWidth();
        int bgHeight = ClientAwakeningScreenState.getBgHeight();

        hoveredPath = getPathAt(mouseX, mouseY);

        RenderSystem.enableBlend();
        guiGraphics.blit(BG_TEXTURE, bgX, bgY, 0, 0, bgWidth, bgHeight, bgWidth, bgHeight);

        int frameX = bgX + ClientAwakeningScreenState.getCenterFrameX();
        int frameY = bgY + ClientAwakeningScreenState.getCenterFrameY();
        int frameW = ClientAwakeningScreenState.getCenterFrameWidth();
        int frameH = ClientAwakeningScreenState.getCenterFrameHeight();

        guiGraphics.blit(CENTER_FRAME_TEXTURE, frameX, frameY, 0, 0, frameW, frameH, frameW, frameH);

        renderPlayerInCenter(guiGraphics, frameX, frameY, frameW, frameH, mouseX, mouseY);

        renderPathSeal(guiGraphics,
                bgX + ClientAwakeningScreenState.getFireX(),
                bgY + ClientAwakeningScreenState.getFireY(),
                FIRE_SEAL_TEXTURE, FIRE_SEAL_ACTIVE_TEXTURE,
                AwakeningPathType.FIRE,
                hoveredPath == AwakeningPathType.FIRE);

        renderPathSeal(guiGraphics,
                bgX + ClientAwakeningScreenState.getIceX(),
                bgY + ClientAwakeningScreenState.getIceY(),
                ICE_SEAL_TEXTURE, ICE_SEAL_ACTIVE_TEXTURE,
                AwakeningPathType.ICE,
                hoveredPath == AwakeningPathType.ICE);

        renderPathSeal(guiGraphics,
                bgX + ClientAwakeningScreenState.getStormX(),
                bgY + ClientAwakeningScreenState.getStormY(),
                STORM_SEAL_TEXTURE, STORM_SEAL_ACTIVE_TEXTURE,
                AwakeningPathType.STORM,
                hoveredPath == AwakeningPathType.STORM);

        renderPathSeal(guiGraphics,
                bgX + ClientAwakeningScreenState.getVoidX(),
                bgY + ClientAwakeningScreenState.getVoidY(),
                VOID_SEAL_TEXTURE, VOID_SEAL_ACTIVE_TEXTURE,
                AwakeningPathType.VOID,
                hoveredPath == AwakeningPathType.VOID);

        if (hoveredPath != null) {
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

        guiGraphics.fill(panelX - 4, panelY + 22, panelX + 144, panelY + 128, 0x88000000);
        guiGraphics.drawString(this.font, "EDIT MODE", panelX, panelY + 30, 0xFFD98C, false);
        guiGraphics.drawString(this.font, "Target: " + selectedTarget.label(), panelX + 24, panelY + 32, 0xFFFFFF, false);

        drawSelectionBoxes(guiGraphics);

        String info1 = "";
        String info2 = "";

        switch (selectedTarget) {
            case BACKGROUND -> {
                info1 = "x=" + ClientAwakeningScreenState.getBgX() + " y=" + ClientAwakeningScreenState.getBgY();
                info2 = "w=" + ClientAwakeningScreenState.getBgWidth() + " h=" + ClientAwakeningScreenState.getBgHeight();
            }
            case CENTER -> {
                info1 = "x=" + ClientAwakeningScreenState.getCenterFrameX() + " y=" + ClientAwakeningScreenState.getCenterFrameY();
                info2 = "w=" + ClientAwakeningScreenState.getCenterFrameWidth() + " h=" + ClientAwakeningScreenState.getCenterFrameHeight();
            }
            case PLAYER -> {
                info1 = "ox=" + ClientAwakeningScreenState.getPlayerOffsetX() + " oy=" + ClientAwakeningScreenState.getPlayerOffsetY();
                info2 = "scale=" + Math.round(ClientAwakeningScreenState.getPlayerScale());
            }
            case FIRE -> {
                info1 = "x=" + ClientAwakeningScreenState.getFireX() + " y=" + ClientAwakeningScreenState.getFireY();
                info2 = "size=" + ClientAwakeningScreenState.getPathFrameSize();
            }
            case ICE -> {
                info1 = "x=" + ClientAwakeningScreenState.getIceX() + " y=" + ClientAwakeningScreenState.getIceY();
                info2 = "size=" + ClientAwakeningScreenState.getPathFrameSize();
            }
            case STORM -> {
                info1 = "x=" + ClientAwakeningScreenState.getStormX() + " y=" + ClientAwakeningScreenState.getStormY();
                info2 = "size=" + ClientAwakeningScreenState.getPathFrameSize();
            }
            case VOID -> {
                info1 = "x=" + ClientAwakeningScreenState.getVoidX() + " y=" + ClientAwakeningScreenState.getVoidY();
                info2 = "size=" + ClientAwakeningScreenState.getPathFrameSize();
            }
        }

        guiGraphics.drawString(this.font, info1, panelX, panelY + 104, 0xD8D8D8, false);
        guiGraphics.drawString(this.font, info2, panelX, panelY + 116, 0xD8D8D8, false);
    }

    private void drawSelectionBoxes(GuiGraphics guiGraphics) {
        int bgX = ClientAwakeningScreenState.getBgX();
        int bgY = ClientAwakeningScreenState.getBgY();
        int bgW = ClientAwakeningScreenState.getBgWidth();
        int bgH = ClientAwakeningScreenState.getBgHeight();

        int frameX = bgX + ClientAwakeningScreenState.getCenterFrameX();
        int frameY = bgY + ClientAwakeningScreenState.getCenterFrameY();
        int frameW = ClientAwakeningScreenState.getCenterFrameWidth();
        int frameH = ClientAwakeningScreenState.getCenterFrameHeight();

        int pathSize = ClientAwakeningScreenState.getPathFrameSize();

        drawBox(guiGraphics, bgX, bgY, bgW, bgH, selectedTarget == EditTarget.BACKGROUND ? 0xFFFFAA00 : 0x66FFFFFF);
        drawBox(guiGraphics, frameX, frameY, frameW, frameH, selectedTarget == EditTarget.CENTER ? 0xFFFFAA00 : 0x66FFFFFF);

        int playerX = frameX + (frameW / 2) + ClientAwakeningScreenState.getPlayerOffsetX() - 12;
        int playerY = frameY + frameH - 10 + ClientAwakeningScreenState.getPlayerOffsetY() - 40;
        drawBox(guiGraphics, playerX, playerY, 24, 48, selectedTarget == EditTarget.PLAYER ? 0xFFFFAA00 : 0x66FFFFFF);

        drawBox(guiGraphics,
                bgX + ClientAwakeningScreenState.getFireX(),
                bgY + ClientAwakeningScreenState.getFireY(),
                pathSize, pathSize,
                selectedTarget == EditTarget.FIRE ? 0xFFFFAA00 : 0x66FFFFFF);

        drawBox(guiGraphics,
                bgX + ClientAwakeningScreenState.getIceX(),
                bgY + ClientAwakeningScreenState.getIceY(),
                pathSize, pathSize,
                selectedTarget == EditTarget.ICE ? 0xFFFFAA00 : 0x66FFFFFF);

        drawBox(guiGraphics,
                bgX + ClientAwakeningScreenState.getStormX(),
                bgY + ClientAwakeningScreenState.getStormY(),
                pathSize, pathSize,
                selectedTarget == EditTarget.STORM ? 0xFFFFAA00 : 0x66FFFFFF);

        drawBox(guiGraphics,
                bgX + ClientAwakeningScreenState.getVoidX(),
                bgY + ClientAwakeningScreenState.getVoidY(),
                pathSize, pathSize,
                selectedTarget == EditTarget.VOID ? 0xFFFFAA00 : 0x66FFFFFF);
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
        int size = ClientAwakeningScreenState.getPathFrameSize();

        guiGraphics.blit(
                hovered ? activeTexture : normalTexture,
                x, y,
                0, 0,
                size, size,
                size, size
        );

        if (hovered) {
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

        int modelX = frameX + (frameW / 2) + ClientAwakeningScreenState.getPlayerOffsetX();
        int modelY = frameY + frameH - 10 + ClientAwakeningScreenState.getPlayerOffsetY();
        int scale = Math.round(ClientAwakeningScreenState.getPlayerScale());

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

    private AwakeningPathType getPathAt(double mouseX, double mouseY) {
        int bgX = ClientAwakeningScreenState.getBgX();
        int bgY = ClientAwakeningScreenState.getBgY();
        int size = ClientAwakeningScreenState.getPathFrameSize();

        if (isInside(mouseX, mouseY, bgX + ClientAwakeningScreenState.getFireX(), bgY + ClientAwakeningScreenState.getFireY(), size, size)) {
            return AwakeningPathType.FIRE;
        }
        if (isInside(mouseX, mouseY, bgX + ClientAwakeningScreenState.getIceX(), bgY + ClientAwakeningScreenState.getIceY(), size, size)) {
            return AwakeningPathType.ICE;
        }
        if (isInside(mouseX, mouseY, bgX + ClientAwakeningScreenState.getStormX(), bgY + ClientAwakeningScreenState.getStormY(), size, size)) {
            return AwakeningPathType.STORM;
        }
        if (isInside(mouseX, mouseY, bgX + ClientAwakeningScreenState.getVoidX(), bgY + ClientAwakeningScreenState.getVoidY(), size, size)) {
            return AwakeningPathType.VOID;
        }

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
            case BACKGROUND -> ClientAwakeningScreenState.applyBackgroundConfig(
                    ClientAwakeningScreenState.getBgX() + dx,
                    ClientAwakeningScreenState.getBgY() + dy,
                    ClientAwakeningScreenState.getBgWidth(),
                    ClientAwakeningScreenState.getBgHeight()
            );
            case CENTER -> ClientAwakeningScreenState.applyCenterConfig(
                    ClientAwakeningScreenState.getCenterFrameX() + dx,
                    ClientAwakeningScreenState.getCenterFrameY() + dy,
                    ClientAwakeningScreenState.getCenterFrameWidth(),
                    ClientAwakeningScreenState.getCenterFrameHeight(),
                    ClientAwakeningScreenState.getPlayerOffsetX(),
                    ClientAwakeningScreenState.getPlayerOffsetY(),
                    ClientAwakeningScreenState.getPlayerScale()
            );
            case PLAYER -> ClientAwakeningScreenState.applyCenterConfig(
                    ClientAwakeningScreenState.getCenterFrameX(),
                    ClientAwakeningScreenState.getCenterFrameY(),
                    ClientAwakeningScreenState.getCenterFrameWidth(),
                    ClientAwakeningScreenState.getCenterFrameHeight(),
                    ClientAwakeningScreenState.getPlayerOffsetX() + dx,
                    ClientAwakeningScreenState.getPlayerOffsetY() + dy,
                    ClientAwakeningScreenState.getPlayerScale()
            );
            case FIRE -> applySinglePathOffset(EditTarget.FIRE,
                    ClientAwakeningScreenState.getFireX() + dx,
                    ClientAwakeningScreenState.getFireY() + dy);
            case ICE -> applySinglePathOffset(EditTarget.ICE,
                    ClientAwakeningScreenState.getIceX() + dx,
                    ClientAwakeningScreenState.getIceY() + dy);
            case STORM -> applySinglePathOffset(EditTarget.STORM,
                    ClientAwakeningScreenState.getStormX() + dx,
                    ClientAwakeningScreenState.getStormY() + dy);
            case VOID -> applySinglePathOffset(EditTarget.VOID,
                    ClientAwakeningScreenState.getVoidX() + dx,
                    ClientAwakeningScreenState.getVoidY() + dy);
        }
    }

    private void resizeSelected(int dw, int dh) {
        switch (selectedTarget) {
            case BACKGROUND -> ClientAwakeningScreenState.applyBackgroundConfig(
                    ClientAwakeningScreenState.getBgX(),
                    ClientAwakeningScreenState.getBgY(),
                    ClientAwakeningScreenState.getBgWidth() + dw,
                    ClientAwakeningScreenState.getBgHeight() + dh
            );
            case CENTER -> ClientAwakeningScreenState.applyCenterConfig(
                    ClientAwakeningScreenState.getCenterFrameX(),
                    ClientAwakeningScreenState.getCenterFrameY(),
                    ClientAwakeningScreenState.getCenterFrameWidth() + dw,
                    ClientAwakeningScreenState.getCenterFrameHeight() + dh,
                    ClientAwakeningScreenState.getPlayerOffsetX(),
                    ClientAwakeningScreenState.getPlayerOffsetY(),
                    ClientAwakeningScreenState.getPlayerScale()
            );
            case FIRE, ICE, STORM, VOID -> ClientAwakeningScreenState.applyPathsConfig(
                    ClientAwakeningScreenState.getPathFrameSize() + dw,
                    ClientAwakeningScreenState.getPathIconSize() + dw,
                    ClientAwakeningScreenState.getFireX(),
                    ClientAwakeningScreenState.getFireY(),
                    ClientAwakeningScreenState.getIceX(),
                    ClientAwakeningScreenState.getIceY(),
                    ClientAwakeningScreenState.getStormX(),
                    ClientAwakeningScreenState.getStormY(),
                    ClientAwakeningScreenState.getVoidX(),
                    ClientAwakeningScreenState.getVoidY()
            );
            case PLAYER -> {
            }
        }
    }

    private void scalePlayer(int delta) {
        ClientAwakeningScreenState.applyCenterConfig(
                ClientAwakeningScreenState.getCenterFrameX(),
                ClientAwakeningScreenState.getCenterFrameY(),
                ClientAwakeningScreenState.getCenterFrameWidth(),
                ClientAwakeningScreenState.getCenterFrameHeight(),
                ClientAwakeningScreenState.getPlayerOffsetX(),
                ClientAwakeningScreenState.getPlayerOffsetY(),
                ClientAwakeningScreenState.getPlayerScale() + delta
        );
    }

    private void applySinglePathOffset(EditTarget target, int x, int y) {
        int fireX = ClientAwakeningScreenState.getFireX();
        int fireY = ClientAwakeningScreenState.getFireY();
        int iceX = ClientAwakeningScreenState.getIceX();
        int iceY = ClientAwakeningScreenState.getIceY();
        int stormX = ClientAwakeningScreenState.getStormX();
        int stormY = ClientAwakeningScreenState.getStormY();
        int voidX = ClientAwakeningScreenState.getVoidX();
        int voidY = ClientAwakeningScreenState.getVoidY();

        switch (target) {
            case FIRE -> {
                fireX = x;
                fireY = y;
            }
            case ICE -> {
                iceX = x;
                iceY = y;
            }
            case STORM -> {
                stormX = x;
                stormY = y;
            }
            case VOID -> {
                voidX = x;
                voidY = y;
            }
            default -> {
                return;
            }
        }

        ClientAwakeningScreenState.applyPathsConfig(
                ClientAwakeningScreenState.getPathFrameSize(),
                ClientAwakeningScreenState.getPathIconSize(),
                fireX, fireY,
                iceX, iceY,
                stormX, stormY,
                voidX, voidY
        );
    }

    private void resetSelectedTarget() {
        switch (selectedTarget) {
            case BACKGROUND -> ClientAwakeningScreenState.resetBackgroundConfig();
            case CENTER, PLAYER -> ClientAwakeningScreenState.resetCenterConfig();
            case FIRE, ICE, STORM, VOID -> ClientAwakeningScreenState.resetPathsConfig();
        }
    }
}

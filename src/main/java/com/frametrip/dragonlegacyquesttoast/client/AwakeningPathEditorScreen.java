package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AwakeningPathEditorScreen extends Screen {
    private static final ResourceLocation FIRE_BG_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_path_fire_bg_320x220.png");

    private static final ResourceLocation ICE_BG_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_path_ice_bg_320x220.png");

    private static final ResourceLocation STORM_BG_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_path_storm_bg_320x220.png");

    private static final ResourceLocation VOID_BG_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_path_void_bg_320x220.png");

    private final Screen parent;
    private AwakeningPathType selectedPath;

    private int draftX;
    private int draftY;
    private int draftWidth;
    private int draftHeight;

    public AwakeningPathEditorScreen(Screen parent, AwakeningPathType selectedPath) {
        super(Component.literal("Редактор экранов путей"));
        this.parent = parent;
        this.selectedPath = selectedPath;
    }

    @Override
    protected void init() {
        super.init();

        loadDraft();

        int x = 8;
        int y = 8;

        this.addRenderableWidget(
                Button.builder(Component.literal("<"), b -> {
                    selectedPath = selectedPath.prev();
                    loadDraft();
                }).bounds(x, y, 20, 20).build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal(">"), b -> {
                    selectedPath = selectedPath.next();
                    loadDraft();
                }).bounds(x + 146, y, 20, 20).build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Сохранить"), b -> saveDraft())
                        .bounds(x, y + 26, 70, 20).build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Сброс"), b -> {
                    draftX = 0;
                    draftY = 0;
                    draftWidth = 320;
                    draftHeight = 220;
                }).bounds(x + 74, y + 26, 50, 20).build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Назад"), b -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(parent);
                    }
                }).bounds(x + 128, y + 26, 50, 20).build()
        );

        this.addRenderableWidget(Button.builder(Component.literal("←"), b -> draftX -= 1).bounds(x, y + 60, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("→"), b -> draftX += 1).bounds(x + 48, y + 60, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("↑"), b -> draftY -= 1).bounds(x + 24, y + 38, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("↓"), b -> draftY += 1).bounds(x + 24, y + 60, 20, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("W+"), b -> draftWidth += 1).bounds(x + 80, y + 38, 30, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("W-"), b -> draftWidth = Math.max(1, draftWidth - 1)).bounds(x + 112, y + 38, 30, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("H+"), b -> draftHeight += 1).bounds(x + 80, y + 60, 30, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("H-"), b -> draftHeight = Math.max(1, draftHeight - 1)).bounds(x + 112, y + 60, 30, 20).build());

        this.addRenderableWidget(
                Button.builder(Component.literal("Center X"), b -> draftX = (this.width - draftWidth) / 2)
                        .bounds(x, y + 88, 70, 20).build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Center Y"), b -> draftY = (this.height - draftHeight) / 2)
                        .bounds(x + 74, y + 88, 70, 20).build()
        );
    }

    private void loadDraft() {
        draftX = ClientAwakeningPathScreenState.getBgX(selectedPath);
        draftY = ClientAwakeningPathScreenState.getBgY(selectedPath);
        draftWidth = ClientAwakeningPathScreenState.getBgWidth(selectedPath);
        draftHeight = ClientAwakeningPathScreenState.getBgHeight(selectedPath);
    }

    private void saveDraft() {
        ClientAwakeningPathScreenState.applyPathBackgroundConfig(
                selectedPath,
                draftX,
                draftY,
                draftWidth,
                draftHeight
        );
    }

    private ResourceLocation getBackgroundTexture() {
        return switch (selectedPath) {
            case FIRE -> FIRE_BG_TEXTURE;
            case ICE -> ICE_BG_TEXTURE;
            case STORM -> STORM_BG_TEXTURE;
            case VOID -> VOID_BG_TEXTURE;
        };
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        guiGraphics.drawCenteredString(this.font, "Редактор экранов путей", this.width / 2, 6, 0xE6D7B5);
        guiGraphics.drawCenteredString(this.font, selectedPath.getTitle(), 91, 14, 0xFFFFFF);

        RenderSystem.enableBlend();
        guiGraphics.blit(getBackgroundTexture(), draftX, draftY, 0, 0, draftWidth, draftHeight, draftWidth, draftHeight);

        drawBox(guiGraphics, draftX, draftY, draftWidth, draftHeight, 0xFFFFAA00);

        guiGraphics.drawString(this.font, "x=" + draftX + " y=" + draftY, 8, 126, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "w=" + draftWidth + " h=" + draftHeight, 8, 138, 0xFFFFFF, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void drawBox(GuiGraphics guiGraphics, int x, int y, int w, int h, int color) {
        guiGraphics.fill(x, y, x + w, y + 1, color);
        guiGraphics.fill(x, y + h - 1, x + w, y + h, color);
        guiGraphics.fill(x, y, x + 1, y + h, color);
        guiGraphics.fill(x + w - 1, y, x + w, y + h, color);
    }
}

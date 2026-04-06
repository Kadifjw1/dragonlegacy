package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AwakeningPathDetailScreen extends Screen {
    private static final ResourceLocation FIRE_BG_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_path_fire_bg_320x220.png");

    private static final ResourceLocation ICE_BG_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_path_ice_bg_320x220.png");

    private static final ResourceLocation STORM_BG_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_path_storm_bg_320x220.png");

    private static final ResourceLocation VOID_BG_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_path_void_bg_320x220.png");

    private final Screen parent;
    private final AwakeningPathType pathType;

    public AwakeningPathDetailScreen(Screen parent, AwakeningPathType pathType) {
        super(Component.literal(pathType.getTitle()));
        this.parent = parent;
        this.pathType = pathType;
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(
                Button.builder(Component.literal("Назад"), button -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(parent);
                    }
                }).bounds(this.width / 2 - 40, this.height - 30, 80, 20).build()
        );

        if (canEdit()) {
            this.addRenderableWidget(
                    Button.builder(Component.literal("Ред"), button -> {
                        if (this.minecraft != null) {
                            this.minecraft.setScreen(new AwakeningPathEditorScreen(parent, pathType));
                        }
                    }).bounds(8, 8, 40, 20).build()
            );
        }
    }

    private boolean canEdit() {
        return this.minecraft != null
                && this.minecraft.player != null
                && this.minecraft.player.getAbilities().instabuild;
    }

    private ResourceLocation getBackgroundTexture() {
        return switch (pathType) {
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
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int bgX = ClientAwakeningPathScreenState.getBgX(pathType);
        int bgY = ClientAwakeningPathScreenState.getBgY(pathType);
        int bgWidth = ClientAwakeningPathScreenState.getBgWidth(pathType);
        int bgHeight = ClientAwakeningPathScreenState.getBgHeight(pathType);

        RenderSystem.enableBlend();
        guiGraphics.blit(getBackgroundTexture(), bgX, bgY, 0, 0, bgWidth, bgHeight, bgWidth, bgHeight);

        guiGraphics.drawCenteredString(this.font, pathType.getTitle(), this.width / 2, bgY + 16, 0xE6D7B5);

        guiGraphics.drawString(this.font, "Это экран пути.", bgX + 20, bgY + 55, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "Здесь позже будет:", bgX + 20, bgY + 70, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "- описание пути", bgX + 30, bgY + 90, 0xCFCFCF, false);
        guiGraphics.drawString(this.font, "- требования", bgX + 30, bgY + 104, 0xCFCFCF, false);
        guiGraphics.drawString(this.font, "- уровни пробуждения", bgX + 30, bgY + 118, 0xCFCFCF, false);
        guiGraphics.drawString(this.font, "- способности и эффекты", bgX + 30, bgY + 132, 0xCFCFCF, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}

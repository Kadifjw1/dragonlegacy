package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AwakeningPathDetailScreen extends Screen {
    private static final ResourceLocation BG_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_main_bg_320x220.png");

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
                Button.builder(Component.literal("Назад"), button -> this.minecraft.setScreen(parent))
                        .bounds(this.width / 2 - 40, this.height - 30, 80, 20)
                        .build()
        );
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

        int bgWidth = 320;
        int bgHeight = 220;
        int bgX = (this.width - bgWidth) / 2;
        int bgY = (this.height - bgHeight) / 2;

        RenderSystem.enableBlend();
        guiGraphics.blit(BG_TEXTURE, bgX, bgY, 0, 0, bgWidth, bgHeight, bgWidth, bgHeight);

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

package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AwakeningMainScreen extends Screen {
    private static final ResourceLocation BG_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_main_bg_320x220.png");

    public AwakeningMainScreen() {
        super(Component.literal("Круг Пробуждения"));
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int x = ClientAwakeningScreenState.getBgX();
        int y = ClientAwakeningScreenState.getBgY();
        int width = ClientAwakeningScreenState.getBgWidth();
        int height = ClientAwakeningScreenState.getBgHeight();

        RenderSystem.enableBlend();
        guiGraphics.blit(BG_TEXTURE, x, y, 0, 0, width, height, width, height);

        guiGraphics.drawString(
                this.font,
                "Круг Пробуждения",
                x + 10,
                y + 10,
                0xE6D7B5,
                false
        );

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}

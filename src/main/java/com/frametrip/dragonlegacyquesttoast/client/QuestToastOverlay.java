package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class QuestToastOverlay {
    private static final ResourceLocation ACCEPTED_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/quest_accepted.png");

    private static final ResourceLocation COMPLETED_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/quest_completed.png");

    private static final ResourceLocation UPDATED_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/quest_updated.png");

    public static final IGuiOverlay OVERLAY = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.options.hideGui) {
            return;
        }

        if (!ClientQuestToastManager.isActive()) {
            return;
        }

        int x = ClientQuestToastManager.getX();
        int y = ClientQuestToastManager.getY();
        int width = ClientQuestToastManager.getWidth();
        int height = ClientQuestToastManager.getHeight();
        float alpha = ClientQuestToastManager.getAlpha();

        ResourceLocation texture;
        if (ClientQuestToastManager.isUpdated()) {
            texture = UPDATED_TEXTURE;
        } else if (ClientQuestToastManager.isCompleted()) {
            texture = COMPLETED_TEXTURE;
        } else {
            texture = ACCEPTED_TEXTURE;
        }

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        guiGraphics.blit(texture, x, y, 0, 0, width, height, width, height);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        ClientQuestToastManager.tick();
    };
}

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
        int width = 128;
        int height = 32;

        ResourceLocation texture = ClientQuestToastManager.isCompleted() ? COMPLETED_TEXTURE : ACCEPTED_TEXTURE;

        RenderSystem.enableBlend();
        guiGraphics.blit(texture, x, y, 0, 0, width, height, width, height);

        ClientQuestToastManager.tick();
    };
}

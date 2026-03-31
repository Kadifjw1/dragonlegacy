package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.List;

public class NpcDialogueOverlay {
    private static final ResourceLocation DIALOGUE_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/npc_dialogue_bar.png");

    public static final IGuiOverlay OVERLAY = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.options.hideGui) {
            return;
        }

        if (!ClientNpcDialogueManager.isActive()) {
            return;
        }

        int width = ClientNpcDialogueManager.getWidth();
        int height = ClientNpcDialogueManager.getHeight();
        int x = ClientNpcDialogueManager.getX(screenWidth);
        int y = ClientNpcDialogueManager.getY(screenHeight);
        float alpha = ClientNpcDialogueManager.getAlpha();

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        guiGraphics.blit(DIALOGUE_TEXTURE, x, y, 0, 0, width, height, width, height);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        guiGraphics.drawString(
                mc.font,
                ClientNpcDialogueManager.getNpcName(),
                x + ClientNpcDialogueManager.getNameX(),
                y + ClientNpcDialogueManager.getNameY(),
                0xCFA8FF,
                false
        );

        List<String> lines = ClientNpcDialogueManager.getWrappedText();

        for (int i = 0; i < lines.size(); i++) {
            guiGraphics.drawString(
                    mc.font,
                    lines.get(i),
                    x + ClientNpcDialogueManager.getTextX(),
                    y + ClientNpcDialogueManager.getTextY() + (i * ClientNpcDialogueManager.getTextLineHeight()),
                    0xFFFFFF,
                    false
            );
        }

        ClientNpcDialogueManager.tick();
    };
}

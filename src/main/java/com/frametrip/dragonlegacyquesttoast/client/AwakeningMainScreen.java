package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class AwakeningMainScreen extends Screen {
    private static final ResourceLocation BG_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_main_bg_320x220.png");

    private static final ResourceLocation CENTER_FRAME_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_center_frame_96x96.png");

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

        int bgX = ClientAwakeningScreenState.getBgX();
        int bgY = ClientAwakeningScreenState.getBgY();
        int bgWidth = ClientAwakeningScreenState.getBgWidth();
        int bgHeight = ClientAwakeningScreenState.getBgHeight();

        RenderSystem.enableBlend();
        guiGraphics.blit(BG_TEXTURE, bgX, bgY, 0, 0, bgWidth, bgHeight, bgWidth, bgHeight);

        int frameX = bgX + ClientAwakeningScreenState.getCenterFrameX();
        int frameY = bgY + ClientAwakeningScreenState.getCenterFrameY();
        int frameW = ClientAwakeningScreenState.getCenterFrameWidth();
        int frameH = ClientAwakeningScreenState.getCenterFrameHeight();

        guiGraphics.blit(CENTER_FRAME_TEXTURE, frameX, frameY, 0, 0, frameW, frameH, frameW, frameH);

        renderPlayerInCenter(guiGraphics, frameX, frameY, frameW, frameH, mouseX, mouseY);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderPlayerInCenter(GuiGraphics guiGraphics, int frameX, int frameY, int frameW, int frameH, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        LivingEntity entity = mc.player;

        int modelX = frameX + (frameW / 2) + ClientAwakeningScreenState.getPlayerOffsetX();
        int modelY = frameY + frameH - 10 + ClientAwakeningScreenState.getPlayerOffsetY();
        float scale = ClientAwakeningScreenState.getPlayerScale();

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
                (float) modelX,
                (float) modelY,
                scale,
                new Vector3f(0.0F, entity.getBbHeight() / 2.0F, 0.0F),
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
}

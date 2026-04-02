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

public class AwakeningMainScreen extends Screen {
    private static final ResourceLocation BG_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_main_bg_320x220.png");

    private static final ResourceLocation CENTER_FRAME_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_center_frame_96x96.png");

    private static final ResourceLocation PATH_FRAME_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_path_frame_48x48.png");

    private static final ResourceLocation PATH_FRAME_ACTIVE_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_path_frame_active_48x48.png");

    private static final ResourceLocation FIRE_ICON_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/path_fire_icon_32x32.png");

    private static final ResourceLocation ICE_ICON_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/path_ice_icon_32x32.png");

    private static final ResourceLocation STORM_ICON_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/path_storm_icon_32x32.png");

    private static final ResourceLocation VOID_ICON_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/path_void_icon_32x32.png");

    private AwakeningPathType hoveredPath = null;

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

        hoveredPath = getPathAt(mouseX, mouseY);

        RenderSystem.enableBlend();
        guiGraphics.blit(BG_TEXTURE, bgX, bgY, 0, 0, bgWidth, bgHeight, bgWidth, bgHeight);

        int frameX = bgX + ClientAwakeningScreenState.getCenterFrameX();
        int frameY = bgY + ClientAwakeningScreenState.getCenterFrameY();
        int frameW = ClientAwakeningScreenState.getCenterFrameWidth();
        int frameH = ClientAwakeningScreenState.getCenterFrameHeight();

        guiGraphics.blit(CENTER_FRAME_TEXTURE, frameX, frameY, 0, 0, frameW, frameH, frameW, frameH);

        renderPlayerInCenter(guiGraphics, frameX, frameY, frameW, frameH, mouseX, mouseY);

        renderPathNode(guiGraphics,
                bgX + ClientAwakeningScreenState.getFireX(),
                bgY + ClientAwakeningScreenState.getFireY(),
                FIRE_ICON_TEXTURE,
                hoveredPath == AwakeningPathType.FIRE);

        renderPathNode(guiGraphics,
                bgX + ClientAwakeningScreenState.getIceX(),
                bgY + ClientAwakeningScreenState.getIceY(),
                ICE_ICON_TEXTURE,
                hoveredPath == AwakeningPathType.ICE);

        renderPathNode(guiGraphics,
                bgX + ClientAwakeningScreenState.getStormX(),
                bgY + ClientAwakeningScreenState.getStormY(),
                STORM_ICON_TEXTURE,
                hoveredPath == AwakeningPathType.STORM);

        renderPathNode(guiGraphics,
                bgX + ClientAwakeningScreenState.getVoidX(),
                bgY + ClientAwakeningScreenState.getVoidY(),
                VOID_ICON_TEXTURE,
                hoveredPath == AwakeningPathType.VOID);

        if (hoveredPath != null) {
            guiGraphics.drawCenteredString(this.font, hoveredPath.getTitle(), this.width / 2, bgY + 8, 0xE6D7B5);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderPathNode(GuiGraphics guiGraphics, int x, int y, ResourceLocation iconTexture, boolean hovered) {
        int frameSize = ClientAwakeningScreenState.getPathFrameSize();
        int iconSize = ClientAwakeningScreenState.getPathIconSize();

        guiGraphics.blit(
                hovered ? PATH_FRAME_ACTIVE_TEXTURE : PATH_FRAME_TEXTURE,
                x, y,
                0, 0,
                frameSize, frameSize,
                frameSize, frameSize
        );

        int iconX = x + (frameSize - iconSize) / 2;
        int iconY = y + (frameSize - iconSize) / 2;

        guiGraphics.blit(iconTexture, iconX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);
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
        AwakeningPathType clickedPath = getPathAt(mouseX, mouseY);
        if (clickedPath != null) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new AwakeningPathDetailScreen(this, clickedPath));
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}

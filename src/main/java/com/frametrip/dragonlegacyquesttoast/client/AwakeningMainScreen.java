package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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

        renderPathSeal(
                guiGraphics,
                bgX + ClientAwakeningScreenState.getFireX(),
                bgY + ClientAwakeningScreenState.getFireY(),
                FIRE_SEAL_TEXTURE,
                FIRE_SEAL_ACTIVE_TEXTURE,
                AwakeningPathType.FIRE,
                hoveredPath == AwakeningPathType.FIRE
        );

        renderPathSeal(
                guiGraphics,
                bgX + ClientAwakeningScreenState.getIceX(),
                bgY + ClientAwakeningScreenState.getIceY(),
                ICE_SEAL_TEXTURE,
                ICE_SEAL_ACTIVE_TEXTURE,
                AwakeningPathType.ICE,
                hoveredPath == AwakeningPathType.ICE
        );

        renderPathSeal(
                guiGraphics,
                bgX + ClientAwakeningScreenState.getStormX(),
                bgY + ClientAwakeningScreenState.getStormY(),
                STORM_SEAL_TEXTURE,
                STORM_SEAL_ACTIVE_TEXTURE,
                AwakeningPathType.STORM,
                hoveredPath == AwakeningPathType.STORM
        );

        renderPathSeal(
                guiGraphics,
                bgX + ClientAwakeningScreenState.getVoidX(),
                bgY + ClientAwakeningScreenState.getVoidY(),
                VOID_SEAL_TEXTURE,
                VOID_SEAL_ACTIVE_TEXTURE,
                AwakeningPathType.VOID,
                hoveredPath == AwakeningPathType.VOID
        );

        if (hoveredPath != null) {
            guiGraphics.drawCenteredString(this.font, hoveredPath.getTitle(), this.width / 2, bgY + 8, 0xE6D7B5);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
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
            renderPathParticles(guiGraphics, x, y, size, pathType);
        }
    }

    private void renderPathParticles(GuiGraphics guiGraphics, int x, int y, int size, AwakeningPathType pathType) {
        float time = (this.minecraft != null && this.minecraft.level != null)
                ? (this.minecraft.level.getGameTime() + this.minecraft.getFrameTime())
                : 0.0F;

        int centerX = x + size / 2;
        int centerY = y + size / 2;
        float radius = size / 2.0F + 4.0F;

        int particleColor = getParticleColor(pathType);

        for (int i = 0; i < 8; i++) {
            float angle = (time * 0.08F) + (i * ((float) Math.PI * 2.0F / 8.0F));

            int px = centerX + Math.round(Mth.cos(angle) * radius);
            int py = centerY + Math.round(Mth.sin(angle) * radius);

            int pSize = (i % 2 == 0) ? 2 : 1;
            guiGraphics.fill(px, py, px + pSize, py + pSize, particleColor);
        }

        switch (pathType) {
            case FIRE -> renderFireParticles(guiGraphics, centerX, centerY, time);
            case ICE -> renderIceParticles(guiGraphics, centerX, centerY, time);
            case STORM -> renderStormParticles(guiGraphics, centerX, centerY, time);
            case VOID -> renderVoidParticles(guiGraphics, centerX, centerY, time);
        }
    }

    private void renderFireParticles(GuiGraphics guiGraphics, int centerX, int centerY, float time) {
        for (int i = 0; i < 5; i++) {
            int px = centerX - 8 + (i * 4);
            int py = centerY + 10 - ((int) ((time + i * 3) % 10));
            guiGraphics.fill(px, py, px + 1, py + 2, 0xFFFFA040);
        }
    }

    private void renderIceParticles(GuiGraphics guiGraphics, int centerX, int centerY, float time) {
        for (int i = 0; i < 6; i++) {
            int px = centerX - 10 + (i * 4);
            int py = centerY - 10 + (int) ((time + i * 2) % 8);
            guiGraphics.fill(px, py, px + 1, py + 1, 0xFFBFEFFF);
        }
    }

    private void renderStormParticles(GuiGraphics guiGraphics, int centerX, int centerY, float time) {
        int blink = ((int) time / 3) % 2;
        if (blink == 0) {
            guiGraphics.fill(centerX - 12, centerY - 6, centerX - 4, centerY - 5, 0xFF8CB8FF);
            guiGraphics.fill(centerX + 4, centerY + 4, centerX + 12, centerY + 5, 0xFFB08CFF);
            guiGraphics.fill(centerX + 8, centerY - 10, centerX + 9, centerY - 2, 0xFFD0D8FF);
        }
    }

    private void renderVoidParticles(GuiGraphics guiGraphics, int centerX, int centerY, float time) {
        for (int i = 0; i < 5; i++) {
            float angle = -(time * 0.05F) - (i * ((float) Math.PI * 2.0F / 5.0F));
            int px = centerX + Math.round(Mth.cos(angle) * 14.0F);
            int py = centerY + Math.round(Mth.sin(angle) * 14.0F);
            guiGraphics.fill(px, py, px + 2, py + 2, 0xCC6E4C9B);
        }
    }

    private int getParticleColor(AwakeningPathType pathType) {
        return switch (pathType) {
            case FIRE -> 0xFFFFA040;
            case ICE -> 0xFFBFEFFF;
            case STORM -> 0xFF9DB8FF;
            case VOID -> 0xCC7B52B3;
        };
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

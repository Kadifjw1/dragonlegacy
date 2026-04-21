package com.frametrip.dragonlegacyquesttoast.client;
 
import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import com.frametrip.dragonlegacyquesttoast.server.FireStrikeHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
 
import java.util.ArrayList;
import java.util.List;
 
public class AwakeningPathDetailScreen extends Screen {
    private static final ResourceLocation FIRE_BG_TEXTURE =
            new ResourceLocation(DragonLegacyQuestToastMod.MODID, "textures/gui/awakening_path_fire_bg_320x220.png");
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
 
        guiGraphics.drawCenteredString(this.font, pathType.getTitle(), this.width / 2, bgY + 16, 0xE6D7B5);
         if (pathType == AwakeningPathType.FIRE) {
            renderFireAbilities(guiGraphics, bgX, bgY, bgWidth);
        } else {
            guiGraphics.drawString(this.font, "Это экран пути.", bgX + 20, bgY + 55, 0xFFFFFF, false);
            guiGraphics.drawString(this.font, "Здесь позже будет:", bgX + 20, bgY + 70, 0xFFFFFF, false);
            guiGraphics.drawString(this.font, "- описание пути", bgX + 30, bgY + 90, 0xCFCFCF, false);
            guiGraphics.drawString(this.font, "- требования", bgX + 30, bgY + 104, 0xCFCFCF, false);
            guiGraphics.drawString(this.font, "- уровни пробуждения", bgX + 30, bgY + 118, 0xCFCFCF, false);
            guiGraphics.drawString(this.font, "- способности и эффекты", bgX + 30, bgY + 132, 0xCFCFCF, false);
        }
 
        super.render(guiGraphics, mouseX, mouseY, partialTick);
 
        if (pathType == AwakeningPathType.FIRE) {
            renderFireAbilityTooltip(guiGraphics, bgX, bgY, bgWidth, mouseX, mouseY);
        }
    }
 
    private void renderFireAbilities(GuiGraphics guiGraphics, int bgX, int bgY, int bgWidth) {
        guiGraphics.drawString(this.font, "Способности:", bgX + 16, bgY + 40, 0xE6D7B5, false);
 
        boolean unlocked = ClientPlayerAbilityState.hasAbility(FireStrikeHandler.ABILITY_ID);
 
        int slotX = bgX + 12;
        int slotY = bgY + 52;
        int slotWidth = bgWidth - 24;
        int slotHeight = 48;
 
        // slot background
        guiGraphics.fill(slotX, slotY, slotX + slotWidth, slotY + slotHeight, 0x55000000);
 
        // slot border
        int borderColor = unlocked ? 0xFFFF6600 : 0xFF555555;
        guiGraphics.fill(slotX, slotY, slotX + slotWidth, slotY + 1, borderColor);
        guiGraphics.fill(slotX, slotY + slotHeight - 1, slotX + slotWidth, slotY + slotHeight, borderColor);
        guiGraphics.fill(slotX, slotY, slotX + 1, slotY + slotHeight, borderColor);
        guiGraphics.fill(slotX + slotWidth - 1, slotY, slotX + slotWidth, slotY + slotHeight, borderColor);
 
        // fire icon (24x24, layered rectangles simulating a flame)
        int iconX = slotX + 8;
        int iconY = slotY + (slotHeight - 24) / 2;
        if (unlocked) {
            guiGraphics.fill(iconX,      iconY,      iconX + 24, iconY + 24, 0xFF882200);
            guiGraphics.fill(iconX +  4, iconY +  6, iconX + 20, iconY + 24, 0xFFFF4400);
            guiGraphics.fill(iconX +  7, iconY + 10, iconX + 17, iconY + 24, 0xFFFFAA00);
            guiGraphics.fill(iconX +  9, iconY + 15, iconX + 15, iconY + 24, 0xFFFFFF44);
        } else {
            guiGraphics.fill(iconX,      iconY,      iconX + 24, iconY + 24, 0xFF333333);
            guiGraphics.fill(iconX +  4, iconY +  6, iconX + 20, iconY + 24, 0xFF554433);
            guiGraphics.fill(iconX +  7, iconY + 10, iconX + 17, iconY + 24, 0xFF776655);
        }
 
        // name
        int nameColor = unlocked ? 0xFFAA44 : 0x888888;
        guiGraphics.drawString(this.font, "Удар Пламени", slotX + 38, slotY + 9, nameColor, false);
 
        // description
        guiGraphics.drawString(this.font, "Область огня после 5 ударов по врагу.", slotX + 38, slotY + 21, 0xAAAAAA, false);
        guiGraphics.drawString(this.font, "Урон: 8  |  Поджигание: 3 сек  |  Радиус: 3", slotX + 38, slotY + 31, 0x888888, false);
 
        // status badge
        String statusText = unlocked ? "Активна" : "Заблокирована";
        int statusColor = unlocked ? 0x44FF44 : 0xFF4444;
        guiGraphics.drawString(this.font, statusText,
                slotX + slotWidth - this.font.width(statusText) - 6, slotY + 9, statusColor, false);
    }
 
    private void renderFireAbilityTooltip(GuiGraphics guiGraphics, int bgX, int bgY, int bgWidth,
                                          int mouseX, int mouseY) {
        int slotX = bgX + 12;
        int slotY = bgY + 52;
        int slotWidth = bgWidth - 24;
        int slotHeight = 48;
 
        if (mouseX < slotX || mouseX >= slotX + slotWidth) return;
        if (mouseY < slotY || mouseY >= slotY + slotHeight) return;
 
        boolean unlocked = ClientPlayerAbilityState.hasAbility(FireStrikeHandler.ABILITY_ID);
 
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal("Удар Пламени").withStyle(s -> s.withColor(0xFFAA44)));
        tooltip.add(Component.literal("После 5 ударов по врагу наносит мощный").withStyle(s -> s.withColor(0xCCCCCC)));
        tooltip.add(Component.literal("удар огнём по всем в радиусе 3 блоков.").withStyle(s -> s.withColor(0xCCCCCC)));
        tooltip.add(Component.literal("Урон: 8 ед.  |  Поджигание: 3 сек.").withStyle(s -> s.withColor(0xFF8844)));
        if (!unlocked) {
            tooltip.add(Component.empty());
            tooltip.add(Component.literal("Не разблокирована").withStyle(s -> s.withColor(0xFF4444)));
        }
        guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
    }
}

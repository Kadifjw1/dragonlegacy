package com.frametrip.dragonlegacyquesttoast.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class QuestToastEditorScreen extends Screen {
    private final Screen parent;

    private int draftX = 0;
    private int draftY = 0;
    private int draftWidth = 64;
    private int draftHeight = 16;
    private int draftFadeIn = 8;
    private int draftStay = 140;
    private int draftFadeOut = 8;
    private int draftStartOffsetX = 18;

    public QuestToastEditorScreen(Screen parent) {
        super(Component.literal("Редактор плашки заданий"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int panelX = 8;
        int panelY = 8;

        this.addRenderableWidget(
                Button.builder(Component.literal("Сохранить"), b -> saveDraft())
                        .bounds(panelX, panelY, 70, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Превью"), b -> {
                    showPreview();
                }).bounds(panelX + 74, panelY, 60, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Сброс"), b -> resetDraft())
                        .bounds(panelX + 138, panelY, 50, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Назад"), b -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(parent == null ? new UiEditorMenuScreen(null) : parent);
                    }
                }).bounds(panelX + 192, panelY, 50, 20)
                        .build()
        );

        this.addRenderableWidget(Button.builder(Component.literal("←"), b -> draftX -= 1).bounds(panelX, panelY + 34, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("→"), b -> draftX += 1).bounds(panelX + 48, panelY + 34, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("↑"), b -> draftY -= 1).bounds(panelX + 24, panelY + 12, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("↓"), b -> draftY += 1).bounds(panelX + 24, panelY + 34, 20, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("W+"), b -> draftWidth += 1).bounds(panelX + 80, panelY + 12, 30, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("W-"), b -> draftWidth = Math.max(1, draftWidth - 1)).bounds(panelX + 112, panelY + 12, 30, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("H+"), b -> draftHeight += 1).bounds(panelX + 80, panelY + 34, 30, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("H-"), b -> draftHeight = Math.max(1, draftHeight - 1)).bounds(panelX + 112, panelY + 34, 30, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("FI+"), b -> draftFadeIn += 1).bounds(panelX + 150, panelY + 12, 34, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("FI-"), b -> draftFadeIn = Math.max(1, draftFadeIn - 1)).bounds(panelX + 186, panelY + 12, 34, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("ST+"), b -> draftStay += 5).bounds(panelX + 150, panelY + 34, 34, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("ST-"), b -> draftStay = Math.max(1, draftStay - 5)).bounds(panelX + 186, panelY + 34, 34, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("FO+"), b -> draftFadeOut += 1).bounds(panelX + 224, panelY + 12, 34, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("FO-"), b -> draftFadeOut = Math.max(1, draftFadeOut - 1)).bounds(panelX + 260, panelY + 12, 34, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("OX+"), b -> draftStartOffsetX += 1).bounds(panelX + 224, panelY + 34, 34, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("OX-"), b -> draftStartOffsetX -= 1).bounds(panelX + 260, panelY + 34, 34, 20).build());

        this.addRenderableWidget(
                Button.builder(Component.literal("Center X"), b -> draftX = (this.width - draftWidth) / 2)
                        .bounds(panelX, panelY + 60, 70, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Top"), b -> draftY = 20)
                        .bounds(panelX + 74, panelY + 60, 40, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Mid"), b -> draftY = (this.height - draftHeight) / 2)
                        .bounds(panelX + 118, panelY + 60, 40, 20)
                        .build()
        );
    }

    private boolean canEdit() {
        return this.minecraft != null
                && this.minecraft.player != null
                && this.minecraft.player.getAbilities().instabuild;
    }

    private void saveDraft() {
        ClientQuestToastManager.applyConfig(
                draftX,
                draftY,
                draftWidth,
                draftHeight,
                draftFadeIn,
                draftStay,
                draftFadeOut,
                draftStartOffsetX
        );
    }

    private void resetDraft() {
        draftX = 0;
        draftY = 0;
        draftWidth = 64;
        draftHeight = 16;
        draftFadeIn = 8;
        draftStay = 140;
        draftFadeOut = 8;
        draftStartOffsetX = 18;
    }

    private void showPreview() {
        ClientQuestToastManager.show("accepted", "");
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        guiGraphics.drawCenteredString(this.font, "Редактор плашки заданий", this.width / 2, 6, 0xE6D7B5);

        guiGraphics.fill(draftX, draftY, draftX + draftWidth, draftY + draftHeight, 0x66FFFFFF);
        guiGraphics.drawString(this.font, "TEST", draftX + 4, draftY + 4, 0xFFFFFF, false);

        guiGraphics.drawString(this.font, "x=" + draftX + " y=" + draftY, 8, 96, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "w=" + draftWidth + " h=" + draftHeight, 8, 108, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "fadeIn=" + draftFadeIn + " stay=" + draftStay, 8, 120, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "fadeOut=" + draftFadeOut + " offsetX=" + draftStartOffsetX, 8, 132, 0xFFFFFF, false);

        if (!canEdit()) {
            guiGraphics.drawCenteredString(this.font, "Редактирование доступно только в Creative", this.width / 2, this.height - 16, 0xFF7777);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}

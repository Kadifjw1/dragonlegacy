package com.frametrip.dragonlegacyquesttoast.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class NpcDialogueEditorScreen extends Screen {
    private final Screen parent;

    private int draftX = 0;
    private int draftYOffsetFromBottom = 40;
    private int draftMinWidth = 180;
    private int draftMaxWidth = 240;
    private int draftMinHeight = 40;
    private int draftFadeIn = 8;
    private int draftStay = 120;
    private int draftFadeOut = 8;
    private int draftTextMaxLines = 3;
    private int draftLeftPadding = 8;
    private int draftRightPadding = 8;
    private int draftTopPadding = 6;
    private int draftBottomPadding = 6;
    private int draftNameYOffset = 4;
    private int draftTextYOffset = 16;
    private int draftTextLineHeight = 10;

    public NpcDialogueEditorScreen(Screen parent) {
        super(Component.literal("Редактор плашки текста"));
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
                Button.builder(Component.literal("Сброс"), b -> resetDraft())
                        .bounds(panelX + 74, panelY, 50, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Назад"), b -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(parent == null ? new UiEditorMenuScreen(null) : parent);
                    }
                }).bounds(panelX + 128, panelY, 50, 20)
                        .build()
        );

        this.addRenderableWidget(Button.builder(Component.literal("←"), b -> draftX -= 1).bounds(panelX, panelY + 34, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("→"), b -> draftX += 1).bounds(panelX + 48, panelY + 34, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("↑"), b -> draftYOffsetFromBottom += 1).bounds(panelX + 24, panelY + 12, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("↓"), b -> draftYOffsetFromBottom = Math.max(0, draftYOffsetFromBottom - 1)).bounds(panelX + 24, panelY + 34, 20, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("MINW+"), b -> draftMinWidth += 2).bounds(panelX + 80, panelY + 12, 46, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("MINW-"), b -> draftMinWidth = Math.max(1, draftMinWidth - 2)).bounds(panelX + 128, panelY + 12, 46, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("MAXW+"), b -> draftMaxWidth += 2).bounds(panelX + 176, panelY + 12, 46, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("MAXW-"), b -> draftMaxWidth = Math.max(draftMinWidth, draftMaxWidth - 2)).bounds(panelX + 224, panelY + 12, 46, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("MINH+"), b -> draftMinHeight += 1).bounds(panelX + 80, panelY + 34, 46, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("MINH-"), b -> draftMinHeight = Math.max(1, draftMinHeight - 1)).bounds(panelX + 128, panelY + 34, 46, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("PAD+"), b -> {
            draftLeftPadding += 1;
            draftRightPadding += 1;
        }).bounds(panelX + 176, panelY + 34, 46, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("PAD-"), b -> {
            draftLeftPadding = Math.max(0, draftLeftPadding - 1);
            draftRightPadding = Math.max(0, draftRightPadding - 1);
        }).bounds(panelX + 224, panelY + 34, 46, 20).build());

        this.addRenderableWidget(
                Button.builder(Component.literal("Center X"), b -> draftX = (this.width - draftMinWidth) / 2)
                        .bounds(panelX, panelY + 60, 70, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Bottom"), b -> draftYOffsetFromBottom = 40)
                        .bounds(panelX + 74, panelY + 60, 50, 20)
                        .build()
        );

        this.addRenderableWidget(Button.builder(Component.literal("Name+"), b -> draftNameYOffset += 1).bounds(panelX + 128, panelY + 60, 46, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Name-"), b -> draftNameYOffset -= 1).bounds(panelX + 176, panelY + 60, 46, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Text+"), b -> draftTextYOffset += 1).bounds(panelX + 224, panelY + 60, 46, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Text-"), b -> draftTextYOffset -= 1).bounds(panelX + 272, panelY + 60, 46, 20).build());
    }

    private boolean canEdit() {
        return this.minecraft != null
                && this.minecraft.player != null
                && this.minecraft.player.getAbilities().instabuild;
    }

    private void saveDraft() {
        ClientNpcDialogueManager.applyConfig(
                draftX,
                draftYOffsetFromBottom,
                draftMinWidth,
                draftMaxWidth,
                draftMinHeight,
                draftFadeIn,
                draftStay,
                draftFadeOut
        );

        ClientNpcDialogueManager.applyTextLayoutConfig(
                draftTextMaxLines,
                draftLeftPadding,
                draftRightPadding,
                draftTopPadding,
                draftBottomPadding,
                draftNameYOffset,
                draftTextYOffset,
                draftTextLineHeight
        );
    }

    private void resetDraft() {
        draftX = 0;
        draftYOffsetFromBottom = 40;
        draftMinWidth = 180;
        draftMaxWidth = 240;
        draftMinHeight = 40;
        draftFadeIn = 8;
        draftStay = 120;
        draftFadeOut = 8;
        draftTextMaxLines = 3;
        draftLeftPadding = 8;
        draftRightPadding = 8;
        draftTopPadding = 6;
        draftBottomPadding = 6;
        draftNameYOffset = 4;
        draftTextYOffset = 16;
        draftTextLineHeight = 10;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        guiGraphics.drawCenteredString(this.font, "Редактор плашки текста NPC", this.width / 2, 6, 0xE6D7B5);

        int boxX = draftX;
        int boxY = this.height - draftYOffsetFromBottom - draftMinHeight;
        int boxW = draftMinWidth;
        int boxH = draftMinHeight;

        guiGraphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0x88000000);
        guiGraphics.drawString(this.font, "[Эльдринн]", boxX + draftLeftPadding, boxY + draftNameYOffset, 0xE6D7B5, false);
        guiGraphics.drawString(this.font, "Ты ещё не готов войти в круг.", boxX + draftLeftPadding, boxY + draftTextYOffset, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "Но уже слышишь его зов.", boxX + draftLeftPadding, boxY + draftTextYOffset + draftTextLineHeight, 0xFFFFFF, false);

        guiGraphics.drawString(this.font, "x=" + draftX + " yBottom=" + draftYOffsetFromBottom, 8, 96, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "minW=" + draftMinWidth + " maxW=" + draftMaxWidth + " minH=" + draftMinHeight, 8, 108, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "padL=" + draftLeftPadding + " padR=" + draftRightPadding, 8, 120, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, "nameY=" + draftNameYOffset + " textY=" + draftTextYOffset + " line=" + draftTextLineHeight, 8, 132, 0xFFFFFF, false);

        if (!canEdit()) {
            guiGraphics.drawCenteredString(this.font, "Редактирование доступно только в Creative", this.width / 2, this.height - 16, 0xFF7777);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}

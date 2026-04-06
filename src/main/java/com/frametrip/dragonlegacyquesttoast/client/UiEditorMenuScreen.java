package com.frametrip.dragonlegacyquesttoast.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class UiEditorMenuScreen extends Screen {
    private final Screen parent;

    public UiEditorMenuScreen(Screen parent) {
        super(Component.literal("UI Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int cx = this.width / 2;
        int y = this.height / 2 - 62;

        this.addRenderableWidget(
                Button.builder(Component.literal("Пробуждение"), b -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new AwakeningMainScreen());
                    }
                }).bounds(cx - 70, y, 140, 20).build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Экраны путей"), b -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new AwakeningPathEditorScreen(this, AwakeningPathType.FIRE));
                    }
                }).bounds(cx - 70, y + 24, 140, 20).build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Плашка заданий"), b -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new QuestToastEditorScreen(this));
                    }
                }).bounds(cx - 70, y + 48, 140, 20).build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Плашка текста"), b -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new NpcDialogueEditorScreen(this));
                    }
                }).bounds(cx - 70, y + 72, 140, 20).build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Выход"), b -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(parent);
                    }
                }).bounds(cx - 70, y + 106, 140, 20).build()
        );
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

        guiGraphics.drawCenteredString(this.font, "Редактор UI", this.width / 2, this.height / 2 - 84, 0xE6D7B5);
        guiGraphics.drawCenteredString(this.font, "Выбери, что редактировать", this.width / 2, this.height / 2 - 72, 0xCFCFCF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}

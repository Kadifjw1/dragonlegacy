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
        int y = this.height / 2 - 50;

        this.addRenderableWidget(
                Button.builder(Component.literal("Пробуждение"), b -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new AwakeningMainScreen());
                    }
                }).bounds(cx - 60, y, 120, 20).build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Плашка заданий"), b -> {
                    // Заглушка — следующим шагом сделаем отдельный экран редактора
                }).bounds(cx - 60, y + 24, 120, 20).build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Плашка текста"), b -> {
                    // Заглушка — следующим шагом сделаем отдельный экран редактора
                }).bounds(cx - 60, y + 48, 120, 20).build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Выход"), b -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(parent);
                    }
                }).bounds(cx - 60, y + 82, 120, 20).build()
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

        guiGraphics.drawCenteredString(this.font, "Редактор UI", this.width / 2, this.height / 2 - 72, 0xE6D7B5);
        guiGraphics.drawCenteredString(this.font, "Выбери, что редактировать", this.width / 2, this.height / 2 - 60, 0xCFCFCF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}

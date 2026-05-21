package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SpawnVillagePacket;
import com.frametrip.dragonlegacyquesttoast.server.world.VillagePreset;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

// [WLD-4]: Simple village creator screen — pick type + size, then spawn.
public class VillageCreatorScreen extends Screen {

    private static final int W = 300;
    private static final int H = 160;

    private VillagePreset.VillageType   type = VillagePreset.VillageType.MEDIEVAL;
    private VillagePreset.VillageSize   size = VillagePreset.VillageSize.SMALL;

    public VillageCreatorScreen() {
        super(Component.literal("Создать деревню"));
    }

    @Override
    protected void init() {
        int cx = (width  - W) / 2;
        int cy = (height - H) / 2;

        // Village type cycle
        addRenderableWidget(Button.builder(Component.literal("◄"), b -> {
            VillagePreset.VillageType[] vals = VillagePreset.VillageType.values();
            type = vals[(type.ordinal() + vals.length - 1) % vals.length];
        }).bounds(cx + 20, cy + 40, 20, 18).build());

        addRenderableWidget(Button.builder(Component.literal("►"), b -> {
            VillagePreset.VillageType[] vals = VillagePreset.VillageType.values();
            type = vals[(type.ordinal() + 1) % vals.length];
        }).bounds(cx + W - 40, cy + 40, 20, 18).build());

        // Village size cycle
        addRenderableWidget(Button.builder(Component.literal("◄"), b -> {
            VillagePreset.VillageSize[] vals = VillagePreset.VillageSize.values();
            size = vals[(size.ordinal() + vals.length - 1) % vals.length];
        }).bounds(cx + 20, cy + 70, 20, 18).build());

        addRenderableWidget(Button.builder(Component.literal("►"), b -> {
            VillagePreset.VillageSize[] vals = VillagePreset.VillageSize.values();
            size = vals[(size.ordinal() + 1) % vals.length];
        }).bounds(cx + W - 40, cy + 70, 20, 18).build());

        // Create button
        addRenderableWidget(Button.builder(
            Component.literal("§a[ Создать вокруг игрока ]"),
            b -> {
                ModNetwork.CHANNEL.sendToServer(new SpawnVillagePacket(type, size));
                onClose();
            }
        ).bounds(cx + 40, cy + 110, W - 80, 20).build());

        // Cancel
        addRenderableWidget(Button.builder(
            Component.literal("Отмена"),
            b -> onClose()
        ).bounds(cx + W / 2 - 30, cy + H - 24, 60, 16).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int cx = (width  - W) / 2;
        int cy = (height - H) / 2;

        g.fill(cx, cy, cx + W, cy + H, 0xCC222233);
        g.fill(cx, cy, cx + W, cy + 20, 0xFF333355);
        g.drawCenteredString(font, "§l🏘 Пресет «Деревня»", cx + W / 2, cy + 6, 0xFFEECC66);

        g.drawString(font, "§7Тип деревни:", cx + 20, cy + 28, 0xFF888899, false);
        String typeLabel = switch (type) {
            case MEDIEVAL -> "Средневековая";
            case FOREST   -> "Лесная";
            case DESERT   -> "Пустынная";
        };
        g.drawCenteredString(font, "§f" + typeLabel, cx + W / 2, cy + 44, 0xFFFFFFFF);

        g.drawString(font, "§7Размер:", cx + 20, cy + 58, 0xFF888899, false);
        String sizeLabel = switch (size) {
            case SMALL  -> "Малая (" + size.npcCount + " NPC)";
            case MEDIUM -> "Средняя (" + size.npcCount + " NPC)";
            case LARGE  -> "Большая (" + size.npcCount + " NPC)";
        };
        g.drawCenteredString(font, "§f" + sizeLabel, cx + W / 2, cy + 74, 0xFFFFFFFF);

        g.drawCenteredString(font, "§8NPC появятся вокруг вашей позиции",
                cx + W / 2, cy + 96, 0xFF666688);

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}

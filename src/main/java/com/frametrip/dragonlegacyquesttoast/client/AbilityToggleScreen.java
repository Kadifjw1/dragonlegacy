package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.ToggleAbilityPacket;
import com.frametrip.dragonlegacyquesttoast.server.AbilityDefinition;
import com.frametrip.dragonlegacyquesttoast.server.AbilityRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class AbilityToggleScreen extends Screen {
    private AwakeningPathType selectedPath;
    private List<AbilityDefinition> visibleAbilities;

    public AbilityToggleScreen() {
        this(AwakeningPathType.FIRE);
    }

    public AbilityToggleScreen(AwakeningPathType selectedPath) {
        super(Component.literal("Способности: вкл/выкл"));
        this.selectedPath = selectedPath;
    }

    @Override
    protected void init() {
        super.init();
        visibleAbilities = AbilityRegistry.getForPath(selectedPath);

        int tabY = 24;
        int tabW = 72;
        int totalW = tabW * AwakeningPathType.values().length;
        int startX = width / 2 - totalW / 2;

        for (int i = 0; i < AwakeningPathType.values().length; i++) {
            AwakeningPathType path = AwakeningPathType.values()[i];
            Button tab = Button.builder(Component.literal(tabLabel(path)), b -> minecraft.setScreen(new AbilityToggleScreen(path)))
                    .bounds(startX + i * tabW, tabY, tabW - 2, 20)
                    .build();
            tab.active = path != selectedPath;
            addRenderableWidget(tab);
        }

        int listX = width / 2 - 145;
        int listY = 56;
        for (int i = 0; i < visibleAbilities.size(); i++) {
            AbilityDefinition def = visibleAbilities.get(i);
            int rowY = listY + i * 16;
            Button toggleBtn = Button.builder(Component.literal(toggleLabel(def)), b -> {
                        boolean nextEnabled = !ClientPlayerAbilityState.isEnabled(def.id);
                        ModNetwork.CHANNEL.sendToServer(new ToggleAbilityPacket(def.id, nextEnabled));
                    })
                    .bounds(listX + 240, rowY - 2, 50, 14)
                    .build();
            toggleBtn.active = canToggle(def);
            addRenderableWidget(toggleBtn);
        }

        addRenderableWidget(Button.builder(Component.literal("Закрыть"), b -> onClose())
                .bounds(width / 2 - 40, height - 28, 80, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTick);

        int listX = width / 2 - 145;
        int listY = 56;

        g.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);
        g.drawString(font, "Путь: " + selectedPath.getTitle(), listX, 42, 0xFFE6D7B5, false);

        for (int i = 0; i < visibleAbilities.size(); i++) {
            AbilityDefinition def = visibleAbilities.get(i);
            int rowY = listY + i * 16;
            boolean unlocked = isUnlocked(def);
            boolean enabled = ClientPlayerAbilityState.isEnabled(def.id);
            int color = unlocked ? 0xFFFFFF : 0x777777;
            g.drawString(font, (i + 1) + ". " + def.name, listX, rowY, color, false);
            String status = unlocked ? (enabled ? "ВКЛ" : "ВЫКЛ") : "НЕ ОТКРЫТО";
            int statusColor = unlocked ? (enabled ? 0x55FF55 : 0xFFAA55) : 0x777777;
            g.drawString(font, status, listX + 170, rowY, statusColor, false);
        }

        g.drawString(font, "* Переключаются только открытые способности", listX, height - 42, 0xAAAAAA, false);
    }

    private boolean isUnlocked(AbilityDefinition def) {
        return isCreative() || ClientPlayerAbilityState.hasAbility(def.id);
    }

    private boolean canToggle(AbilityDefinition def) {
    return isCreative() || ClientPlayerAbilityState.hasAbility(def.id);
    }

    private boolean isCreative() {
        return minecraft != null && minecraft.player != null && minecraft.player.getAbilities().instabuild;
    }

    private String toggleLabel(AbilityDefinition def) {
        if (!isUnlocked(def)) return "---";
        return ClientPlayerAbilityState.isEnabled(def.id) ? "ВЫКЛ" : "ВКЛ";
    }

    private String tabLabel(AwakeningPathType path) {
        return switch (path) {
            case FIRE -> "Пламя";
            case ICE -> "Лёд";
            case STORM -> "Гроза";
            case VOID -> "Пустота";
        };
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

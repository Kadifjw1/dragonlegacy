package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

// [APP-1..3]: Appearance+ tab — particles, glow, accessories.
public class NpcAppearancePlusTab implements NpcEditorTab {

    public static final int ACCENT = 0xFFCC55FF;

    private static final String[] PARTICLE_LABELS =
            { "Нет", "Огонь", "Вода", "Магия", "Дым", "Звёзды" };

    private static final String[] SLOTS =
            { "HEAD", "BACK", "BELT", "LEFT_HAND", "RIGHT_HAND" };
    private static final String[] SLOT_LABELS =
            { "Голова", "Спина", "Пояс", "Лев.рука", "Прав.рука" };

    // APP-2 glow colour fields
    private EditBox glowRBox, glowGBox, glowBBox;

    // APP-3 accessory fields
    private final EditBox[] accBoxes = new EditBox[SLOTS.length];

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        var font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();

        int y = oy + 18;

        // ── [APP-1] Particle selector ─────────────────────────────────────────
        // ◄ / ► cycle buttons
        add.accept(Button.builder(Component.literal("◄"), b -> {
            d.particleEffect = (byte) Math.max(0, d.particleEffect - 1);
        }).bounds(rx + 90, y, 16, 14).build());

        add.accept(Button.builder(Component.literal("►"), b -> {
            d.particleEffect = (byte) Math.min(PARTICLE_LABELS.length - 1, d.particleEffect + 1);
        }).bounds(rx + 170, y, 16, 14).build());

        y += 20;

        // ── [APP-2] Glow colour RGB ────────────────────────────────────────────
        int r = (d.glowColor >> 16) & 0xFF;
        int g = (d.glowColor >>  8) & 0xFF;
        int b = (d.glowColor)       & 0xFF;

        glowRBox = new EditBox(font, rx + 60, y, 40, 14, Component.literal("R"));
        glowRBox.setValue(String.valueOf(r)); glowRBox.setMaxLength(3); add.accept(glowRBox);

        glowGBox = new EditBox(font, rx + 110, y, 40, 14, Component.literal("G"));
        glowGBox.setValue(String.valueOf(g)); glowGBox.setMaxLength(3); add.accept(glowGBox);

        glowBBox = new EditBox(font, rx + 160, y, 40, 14, Component.literal("B"));
        glowBBox.setValue(String.valueOf(b)); glowBBox.setMaxLength(3); add.accept(glowBBox);

        add.accept(Button.builder(Component.literal("✕ Откл."), btn -> {
            d.glowColor = 0;
            glowRBox.setValue("0"); glowGBox.setValue("0"); glowBBox.setValue("0");
        }).bounds(rx + 210, y, 50, 14).build());

        y += 20;

        // ── [APP-3] Accessory item IDs ────────────────────────────────────────
        if (d.accessories == null) d.accessories = new java.util.LinkedHashMap<>();
        for (int i = 0; i < SLOTS.length; i++) {
            accBoxes[i] = new EditBox(font, rx + 90, y, rw - 94, 14,
                    Component.literal(SLOT_LABELS[i]));
            accBoxes[i].setValue(d.accessories.getOrDefault(SLOTS[i], ""));
            accBoxes[i].setMaxLength(64);
            add.accept(accBoxes[i]);
            y += 18;
        }
    }

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();
        int y = oy + 2;

        // Section header
        g.fill(rx, y, rx + rw, y + 12, 0x33AAFFCC);
        g.drawString(font, "§d§lВНЕШНИЙ ВИД+", rx + 4, y + 2, 0xFFDDAAFF, false);
        y += 18;

        // APP-1
        String particleName = d.particleEffect >= 0 && d.particleEffect < PARTICLE_LABELS.length
                ? PARTICLE_LABELS[d.particleEffect] : "?";
        g.drawString(font, "§7Частицы:", rx + 4, y + 3, 0xFFCCCCCC, false);
        g.drawString(font, "§f" + particleName, rx + 110, y + 3, 0xFFFFFFFF, false);
        y += 20;

        // APP-2 glow preview swatch
        g.drawString(font, "§7Свечение R G B:", rx + 4, y + 3, 0xFFCCCCCC, false);
        int glowPreview = 0xFF000000 | (d.glowColor & 0xFFFFFF);
        if (d.glowColor != 0) {
            g.fill(rx + 205, y, rx + 218, y + 14, 0xFF333333);
            g.fill(rx + 206, y + 1, rx + 217, y + 13, glowPreview);
        }
        y += 20;

        // APP-3 labels
        for (int i = 0; i < SLOTS.length; i++) {
            g.drawString(font, "§7" + SLOT_LABELS[i] + ":", rx + 4, y + 3, 0xFFCCCCCC, false);
            y += 18;
        }
    }

    @Override
    public void pullFields(NpcEditorState state) {
        NpcEntityData d = state.getDraft();

        // APP-2: Parse RGB → glowColor
        try {
            int r = Math.min(255, Math.max(0, Integer.parseInt(glowRBox.getValue().trim())));
            int g = Math.min(255, Math.max(0, Integer.parseInt(glowGBox.getValue().trim())));
            int b = Math.min(255, Math.max(0, Integer.parseInt(glowBBox.getValue().trim())));
            d.glowColor = (r == 0 && g == 0 && b == 0) ? 0 : ((r << 16) | (g << 8) | b);
        } catch (NumberFormatException ignored) {}

        // APP-3: Accessory item IDs
        if (d.accessories == null) d.accessories = new java.util.LinkedHashMap<>();
        for (int i = 0; i < SLOTS.length; i++) {
            String val = accBoxes[i].getValue().trim();
            if (val.isEmpty()) d.accessories.remove(SLOTS[i]);
            else d.accessories.put(SLOTS[i], val);
        }
    }
}

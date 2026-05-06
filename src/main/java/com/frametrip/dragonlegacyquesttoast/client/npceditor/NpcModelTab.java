package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.server.model.NpcModelConfig;
import com.frametrip.dragonlegacyquesttoast.server.model.NpcModelProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/** Вкладка "Модель NPC" — выбор профиля и настройки визуала. */
public class NpcModelTab implements NpcEditorTab {

    public static final int ACCENT = 0xFF44AAFF;

    private static final String[] CAT_IDS    = { "all", "humanoid", "animal", "golem" };
    private static final String[] CAT_LABELS = { "Все", "Гуманоиды", "Животные", "Големы" };

    private String categoryFilter = "all";
    private int profileScroll = 0;
    private EditBox scaleBox, offsetYBox;

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        NpcEntityData d = state.getDraft();
        NpcModelConfig mc = ensureModel(d);
        var font = Minecraft.getInstance().font;
        int y = oy + 26;

        // ── Category filter ───────────────────────────────────────────────────
        int bw = rw / CAT_IDS.length;
        for (int i = 0; i < CAT_IDS.length; i++) {
            final String cat = CAT_IDS[i];
            boolean sel = cat.equals(categoryFilter);
            add.accept(Button.builder(
                    Component.literal(sel ? "§e§l" + CAT_LABELS[i] : CAT_LABELS[i]),
                    b -> { categoryFilter = cat; profileScroll = 0; rebuild.run(); }
            ).bounds(rx + i * bw, y, bw - 2, 16).build());
        }
        y += 20;

        // ── Profile list ──────────────────────────────────────────────────────
        List<NpcModelProfile> filtered = filteredProfiles();
        int visibleRows = 5;
        int maxScroll   = Math.max(0, filtered.size() - visibleRows);
        profileScroll   = Math.max(0, Math.min(profileScroll, maxScroll));
        int listTop     = y;

        for (int i = profileScroll; i < Math.min(filtered.size(), profileScroll + visibleRows); i++) {
            NpcModelProfile profile = filtered.get(i);
            boolean selected = mc.profile == profile;
            add.accept(Button.builder(
                    Component.literal(selected ? "§e§l▶ " + profile.label : profile.label),
                    b -> {
                        mc.profile = profile;
                        mc.scale   = profile.baseScale;
                        state.markDirty();
                        rebuild.run();
                    }
            ).bounds(rx, y + (i - profileScroll) * 18, rw - 24, 16).build());
        }

        add.accept(Button.builder(Component.literal("▲"),
                b -> { profileScroll = Math.max(0, profileScroll - 1); rebuild.run(); }
        ).bounds(rx + rw - 22, listTop, 20, 16).build());
        add.accept(Button.builder(Component.literal("▼"),
                b -> { profileScroll = Math.min(maxScroll, profileScroll + 1); rebuild.run(); }
        ).bounds(rx + rw - 22, listTop + 18, 20, 16).build());

        y += visibleRows * 18 + 8;

        // ── Scale ─────────────────────────────────────────────────────────────
        add.accept(Button.builder(Component.literal("◀"), b -> {
            mc.scale = Math.max(0.1f, Math.round((mc.scale - 0.1f) * 10) / 10f);
            state.markDirty(); rebuild.run();
        }).bounds(rx, y, 18, 16).build());
        add.accept(Button.builder(Component.literal("▶"), b -> {
            mc.scale = Math.min(4f, Math.round((mc.scale + 0.1f) * 10) / 10f);
            state.markDirty(); rebuild.run();
        }).bounds(rx + 54, y, 18, 16).build());

        // ── Offset Y ─────────────────────────────────────────────────────────
        offsetYBox = new EditBox(font, rx + 100, y, 50, 16, Component.literal("Y"));
        offsetYBox.setValue(String.format("%.2f", mc.offsetY));
        add.accept(offsetYBox);

        // ── Eye height override ───────────────────────────────────────────────
        add.accept(Button.builder(Component.literal("◀"), b -> {
            mc.eyeHeightOverride = Math.max(-1f,
                    Math.round((mc.eyeHeightOverride < 0 ? mc.profile.eyeHeight : mc.eyeHeightOverride) - 0.1f) / 10f * 10f);
            state.markDirty(); rebuild.run();
        }).bounds(rx, y + 22, 18, 16).build());
        add.accept(Button.builder(Component.literal("▶"), b -> {
            float base = mc.eyeHeightOverride < 0 ? mc.profile.eyeHeight : mc.eyeHeightOverride;
            mc.eyeHeightOverride = Math.round((base + 0.1f) * 10) / 10f;
            state.markDirty(); rebuild.run();
        }).bounds(rx + 54, y + 22, 18, 16).build());
        add.accept(Button.builder(Component.literal("↺"), b -> {
            mc.eyeHeightOverride = -1f;
            state.markDirty(); rebuild.run();
        }).bounds(rx + 76, y + 22, 18, 16).build());

        y += 48;

        // ── Options ───────────────────────────────────────────────────────────
        add.accept(Button.builder(
                Component.literal("Звуки существа: " + (mc.useCreatureSounds ? "§aВКЛ" : "§cВЫКЛ")),
                b -> { mc.useCreatureSounds = !mc.useCreatureSounds; state.markDirty(); rebuild.run(); }
        ).bounds(rx, y, rw / 2 - 2, 16).build());

        add.accept(Button.builder(
                Component.literal("Анимации: " + (mc.useCreatureAnimations ? "§aВКЛ" : "§cВЫКЛ")),
                b -> { mc.useCreatureAnimations = !mc.useCreatureAnimations; state.markDirty(); rebuild.run(); }
        ).bounds(rx + rw / 2, y, rw / 2 - 2, 16).build());
    }

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();
        NpcModelConfig mc = ensureModel(d);

        NpcEditorUtils.sectionCard(g, rx, oy, rw, 18, "МОДЕЛЬ / ПРОФИЛЬ", ACCENT);

        // Current selection
        g.drawString(font, "§7Выбрано: §f" + mc.profile.label + " §8(" + mc.profile.category() + ")",
                rx + 4, oy + 4, 0xFFCCCCCC, false);

        int y = oy + 26 + 20 + 5 * 18 + 8;

// Scale display
        NpcEditorUtils.sectionCard(g, rx, y - 4, rw, 64, "ПАРАМЕТРЫ", ACCENT);
        g.drawString(font, "§7Масштаб:", rx + 4, y + 2, 0xFF888877, false);
        g.drawString(font, "§f" + String.format("%.1f", mc.scale),
                rx + 22, y + 2, 0xFFFFCC44, false);

        g.drawString(font, "§7Смещ. Y:", rx + 4, y + 18, 0xFF888877, false);

        float eh = mc.effectiveEyeHeight();
        g.drawString(font, "§7Высота глаз: §f" + String.format("%.2f", eh),
                rx + 4, y + 30, 0xFF888877, false);

        // Visual profile info
        float np = mc.effectiveNameplateOffset();
        float dlg = mc.effectiveDialogueOffset();
        g.drawString(font, "§7Имя: §8+" + String.format("%.1f", np) + "  §7Диалог: §8+" + String.format("%.1f", dlg),
                rx + 4, y + 42, 0xFF888877, false);
    }

    @Override
    public void pullFields(NpcEditorState state) {
        NpcModelConfig mc = ensureModel(state.getDraft());
        if (offsetYBox != null) {
            try { mc.offsetY = Float.parseFloat(offsetYBox.getValue()); } catch (Exception ignored) {}
        }
    }

    @Override
    public boolean onMouseScrolled(double mx, double my, double delta,
                                   NpcEditorState state, int rx, int oy, int rw) {
        int maxScroll = Math.max(0, filteredProfiles().size() - 5);
        profileScroll = Math.max(0, Math.min(maxScroll, profileScroll - (int) Math.signum(delta)));
        return true;
    }

    private List<NpcModelProfile> filteredProfiles() {
        if ("all".equals(categoryFilter)) return Arrays.asList(NpcModelProfile.VALUES);
        final String cat = categoryFilter;
        return Arrays.stream(NpcModelProfile.VALUES)
                .filter(p -> p.category().equals(cat))
                .toList();
    }

    private static NpcModelConfig ensureModel(NpcEntityData d) {
        if (d.modelConfig == null) d.modelConfig = new NpcModelConfig();
        return d.modelConfig;
    }
}

package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.client.NpcGeckoPresetManager;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.server.model.NpcGeckoPreset;
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

    // Grid constants (MOD-2)
    private static final int GRID_COLS    = 3;
    private static final int GRID_ROW_H   = 20;
    private static final int GRID_VISIBLE = 5; // visible rows

    private String  categoryFilter = "all";
    private int     profileScroll  = 0;

    // MOD-1: search state
    private String  searchQuery = "";
    private EditBox searchBox;

    // GeckoLib fields
    private EditBox offsetYBox;
    private EditBox geckoModelBox, geckoAnimBox, geckoTexBox;

    // MOD-3: gecko preset fields
    private int     presetScroll   = 0;
    private EditBox presetNameBox;

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        NpcEntityData d = state.getDraft();
        NpcModelConfig mc = ensureModel(d);
        var font = Minecraft.getInstance().font;

        int y = oy + 18;

        // ── [MOD-1] Search box ────────────────────────────────────────────────
        searchBox = new EditBox(font, rx, y, rw, 16, Component.literal("Поиск"));
        searchBox.setHint(Component.literal("Поиск модели...").withStyle(s -> s.withColor(0xFF555566)));
        searchBox.setValue(searchQuery);
        searchBox.setResponder(val -> { searchQuery = val; profileScroll = 0; rebuild.run(); });
        add.accept(searchBox);
        y += 20;

        // ── Category filter row ───────────────────────────────────────────────
        int bw = rw / CAT_IDS.length;
        for (int i = 0; i < CAT_IDS.length; i++) {
            final String cat = CAT_IDS[i];
            boolean sel = cat.equals(categoryFilter);
            add.accept(Button.builder(
                    Component.literal(sel ? "§e§l" + CAT_LABELS[i] : CAT_LABELS[i]),
                    b -> { categoryFilter = cat; profileScroll = 0; searchQuery = ""; rebuild.run(); }
            ).bounds(rx + i * bw, y, bw - 2, 16).build());
        }
        y += 22;

        // ── [MOD-2] Profile grid (3 columns) ─────────────────────────────────
        List<NpcModelProfile> filtered = filteredProfiles();
        int totalRows  = (filtered.size() + GRID_COLS - 1) / GRID_COLS;
        int maxScroll  = Math.max(0, totalRows - GRID_VISIBLE);
        profileScroll  = Math.max(0, Math.min(profileScroll, maxScroll));
        int listTop    = y;
        int cellW      = (rw - (GRID_COLS - 1) * 2) / GRID_COLS;

        for (int row = 0; row < GRID_VISIBLE; row++) {
            int dataRow = row + profileScroll;
            for (int col = 0; col < GRID_COLS; col++) {
                int idx = dataRow * GRID_COLS + col;
                if (idx >= filtered.size()) break;
                NpcModelProfile profile = filtered.get(idx);
                boolean selected = mc.profile == profile;
                int bx = rx + col * (cellW + 2);
                int by = y + row * GRID_ROW_H;
                String prefix  = selected ? "§e▶" : " ";
                String lbl     = NpcEditorUtils.fitText(profile.label, cellW - 16);
                add.accept(Button.builder(
                        Component.literal(prefix + lbl),
                        b -> {
                            mc.profile = profile;
                            mc.scale   = profile.baseScale;
                            state.markDirty();
                            rebuild.run();
                        }
                ).bounds(bx, by, cellW, GRID_ROW_H - 2).build());
            }
        }

        // Scroll arrows
        add.accept(Button.builder(Component.literal("▲"),
                b -> { profileScroll = Math.max(0, profileScroll - 1); rebuild.run(); }
        ).bounds(rx + rw - 18, listTop, 16, 9).build());
        add.accept(Button.builder(Component.literal("▼"),
                b -> { profileScroll = Math.min(maxScroll, profileScroll + 1); rebuild.run(); }
        ).bounds(rx + rw - 18, listTop + 10, 16, 9).build());

        y += GRID_VISIBLE * GRID_ROW_H + 10;

        // ── Parameters section ────────────────────────────────────────────────
        // Scale row
        add.accept(Button.builder(Component.literal("◀"), b -> {
            mc.scale = Math.max(0.1f, Math.round((mc.scale - 0.1f) * 10) / 10f);
            state.markDirty(); rebuild.run();
        }).bounds(rx + 60, y, 20, 16).build());
        add.accept(Button.builder(Component.literal("▶"), b -> {
            mc.scale = Math.min(4f, Math.round((mc.scale + 0.1f) * 10) / 10f);
            state.markDirty(); rebuild.run();
        }).bounds(rx + 116, y, 20, 16).build());
        y += 20;

        // Offset Y
        offsetYBox = new EditBox(font, rx + 60, y, 54, 16, Component.literal("Y"));
        offsetYBox.setValue(String.format("%.2f", mc.offsetY));
        add.accept(offsetYBox);
        y += 20;

        // Eye height
        add.accept(Button.builder(Component.literal("◀"), b -> {
            mc.eyeHeightOverride = Math.max(-1f,
                    Math.round((mc.eyeHeightOverride < 0 ? mc.profile.eyeHeight : mc.eyeHeightOverride) - 0.1f) / 10f * 10f);
            state.markDirty(); rebuild.run();
        }).bounds(rx + 60, y, 20, 16).build());
        add.accept(Button.builder(Component.literal("▶"), b -> {
            float base = mc.eyeHeightOverride < 0 ? mc.profile.eyeHeight : mc.eyeHeightOverride;
            mc.eyeHeightOverride = Math.round((base + 0.1f) * 10) / 10f;
            state.markDirty(); rebuild.run();
        }).bounds(rx + 116, y, 20, 16).build());
        add.accept(Button.builder(Component.literal("↺"), b -> {
            mc.eyeHeightOverride = -1f;
            state.markDirty(); rebuild.run();
        }).bounds(rx + 140, y, 20, 16).build());
        y += 26;

        // Toggle options
        int hw = rw / 2 - 2;
        add.accept(Button.builder(
                Component.literal("Звуки: " + (mc.useCreatureSounds ? "§aВКЛ" : "§cВЫКЛ")),
                b -> { mc.useCreatureSounds = !mc.useCreatureSounds; state.markDirty(); rebuild.run(); }
        ).bounds(rx, y, hw, 16).build());
        add.accept(Button.builder(
                Component.literal("Анимации: " + (mc.useCreatureAnimations ? "§aВКЛ" : "§cВЫКЛ")),
                b -> { mc.useCreatureAnimations = !mc.useCreatureAnimations; state.markDirty(); rebuild.run(); }
        ).bounds(rx + hw + 2, y, hw, 16).build());
        y += 24;

        // ── GeckoLib resource fields ──────────────────────────────────────────
        int fieldW = rw - 4;
        geckoModelBox = new EditBox(font, rx + 2, y, fieldW, 14, Component.literal("Модель"));
        geckoModelBox.setMaxLength(256);
        geckoModelBox.setValue(d.geckoModel);
        geckoModelBox.setHint(Component.literal("modid:geo/npc.geo.json").withStyle(s -> s.withColor(0xFF444455)));
        add.accept(geckoModelBox);
        y += 18;

        geckoAnimBox = new EditBox(font, rx + 2, y, fieldW, 14, Component.literal("Анимация"));
        geckoAnimBox.setMaxLength(256);
        geckoAnimBox.setValue(d.geckoAnimation);
        geckoAnimBox.setHint(Component.literal("modid:animations/npc.animation.json").withStyle(s -> s.withColor(0xFF444455)));
        add.accept(geckoAnimBox);
        y += 18;

        geckoTexBox = new EditBox(font, rx + 2, y, fieldW, 14, Component.literal("Текстура"));
        geckoTexBox.setMaxLength(256);
        geckoTexBox.setValue(d.geckoTexture);
        geckoTexBox.setHint(Component.literal("modid:textures/entity/npc.png").withStyle(s -> s.withColor(0xFF444455)));
        add.accept(geckoTexBox);

        // Reset GeckoLib button
        add.accept(Button.builder(Component.literal("↺ Сброс GeckoLib"), b -> {
            d.geckoModel = ""; d.geckoAnimation = ""; d.geckoTexture = "";
            state.markDirty(); rebuild.run();
        }).bounds(rx + fieldW - 100, y + 18, 102, 14).build());
        y += 34;

        // ── [MOD-3] GeckoLib presets ──────────────────────────────────────────
        List<NpcGeckoPreset> presets = NpcGeckoPresetManager.getAll();
        int maxPresetVisible = 3;
        int maxPresetScroll = Math.max(0, presets.size() - maxPresetVisible);
        presetScroll = Math.max(0, Math.min(presetScroll, maxPresetScroll));

        for (int i = presetScroll; i < Math.min(presets.size(), presetScroll + maxPresetVisible); i++) {
            final int idx = i;
            NpcGeckoPreset preset = presets.get(i);
            int rowY = y + (i - presetScroll) * 18;
            // Load button
            add.accept(Button.builder(
                    Component.literal("▶ " + NpcEditorUtils.fitText(preset.name, fieldW - 60)),
                    b -> {
                        d.geckoModel     = preset.geckoModel;
                        d.geckoAnimation = preset.geckoAnimation;
                        d.geckoTexture   = preset.geckoTexture;
                        state.markDirty(); rebuild.run();
                    }
            ).bounds(rx, rowY, fieldW - 20, 16).build());
            // Delete button
            add.accept(Button.builder(Component.literal("✕"), b -> {
                NpcGeckoPresetManager.remove(idx);
                rebuild.run();
            }).bounds(rx + fieldW - 18, rowY, 18, 16).build());
        }

        // Scroll arrows for presets
        if (presets.size() > maxPresetVisible) {
            int arrowY = y + maxPresetVisible * 18;
            add.accept(Button.builder(Component.literal("▲"),
                    b -> { if (presetScroll > 0) { presetScroll--; rebuild.run(); } }
            ).bounds(rx + rw - 18, y, 16, 9).build());
            add.accept(Button.builder(Component.literal("▼"),
                    b -> { if (presetScroll + maxPresetVisible < presets.size()) { presetScroll++; rebuild.run(); } }
            ).bounds(rx + rw - 18, y + 10, 16, 9).build());
        }

        y += Math.min(presets.size(), maxPresetVisible) * 18 + 4;

        // Save preset row: name box + save button
        presetNameBox = new EditBox(font, rx, y, fieldW - 60, 16, Component.literal("Имя пресета"));
        presetNameBox.setHint(Component.literal("Имя пресета...").withStyle(s -> s.withColor(0xFF444455)));
        presetNameBox.setMaxLength(40);
        add.accept(presetNameBox);

        final EditBox capturedNameBox = presetNameBox;
        add.accept(Button.builder(Component.literal("💾 Сохранить"), b -> {
            pullFields(state); // flush editboxes first
            String name = capturedNameBox.getValue().trim();
            if (name.isEmpty()) name = "Пресет";
            NpcGeckoPresetManager.add(new NpcGeckoPreset(name, d.geckoModel, d.geckoAnimation, d.geckoTexture));
            rebuild.run();
        }).bounds(rx + fieldW - 58, y, 60, 16).build());
    }

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;
        NpcEntityData d  = state.getDraft();
        NpcModelConfig mc = ensureModel(d);

        // Current model subtitle
        g.drawString(font, "§b" + mc.profile.label
                + " §8(" + mc.profile.category() + ")",
                rx + 4, oy + 6, ACCENT, false);

        // MOD-2 grid: category-color left strip per cell
        List<NpcModelProfile> filtered = filteredProfiles();
        int cellW    = (rw - (GRID_COLS - 1) * 2) / GRID_COLS;
        int gridTopY = oy + 18 + 20 + 22; // searchBox(20) + categories(22) = 42 + 18
        for (int row = 0; row < GRID_VISIBLE; row++) {
            int dataRow = row + profileScroll;
            for (int col = 0; col < GRID_COLS; col++) {
                int idx = dataRow * GRID_COLS + col;
                if (idx >= filtered.size()) break;
                NpcModelProfile profile = filtered.get(idx);
                int bx = rx + col * (cellW + 2);
                int by = gridTopY + row * GRID_ROW_H;
                int catColor = switch (profile.category()) {
                    case "humanoid" -> 0xFF4466AA;
                    case "animal"   -> 0xFF44AA55;
                    case "golem"    -> 0xFFAA6633;
                    default         -> 0xFF888888;
                };
                g.fill(bx, by, bx + 3, by + GRID_ROW_H - 2, catColor);
                if (mc.profile == profile)
                    g.fill(bx, by + GRID_ROW_H - 2, bx + cellW, by + GRID_ROW_H - 1, ACCENT);
            }
        }

        // Parameters section
        int paramY = gridTopY + GRID_VISIBLE * GRID_ROW_H + 10;
        NpcEditorUtils.sectionCard(g, rx, paramY - 6, rw, 84, "ПАРАМЕТРЫ", ACCENT);

        g.drawString(font, "§7Масштаб:", rx + 4, paramY + 2, 0xFF888877, false);
        g.drawString(font, "§f" + String.format("%.1f", mc.scale), rx + 84, paramY + 2, 0xFFFFCC44, false);
        g.drawString(font, "§7Смещ. Y:", rx + 4, paramY + 22, 0xFF888877, false);
        float eh = mc.effectiveEyeHeight();
        g.drawString(font, "§7Высота глаз:", rx + 4, paramY + 42, 0xFF888877, false);
        g.drawString(font, "§f" + String.format("%.2f", eh), rx + 84, paramY + 42, 0xFFFFCC44, false);
        float np  = mc.effectiveNameplateOffset();
        float dlg = mc.effectiveDialogueOffset();
        g.drawString(font, "§8Имя: §7+" + String.format("%.1f", np)
                + "  Диалог: §7+" + String.format("%.1f", dlg),
                rx + 4, paramY + 58, 0xFF666677, false);

        // GeckoLib section
        int geckoY = paramY - 6 + 84 + 26;
        NpcEditorUtils.sectionCard(g, rx, geckoY, rw, 14, "GECKOLIB РЕСУРСЫ", ACCENT);
        g.drawString(font, "§8Модель:", rx + 4, geckoY + 17, 0xFF555566, false);
        g.drawString(font, "§8Анимация:", rx + 4, geckoY + 35, 0xFF555566, false);
        g.drawString(font, "§8Текстура:", rx + 4, geckoY + 53, 0xFF555566, false);

        // [MOD-3] Gecko presets section
        int presetHeaderY = geckoY + 14 + 3 * 18 + 34;
        NpcEditorUtils.sectionCard(g, rx, presetHeaderY, rw, 14, "GECKO ПРЕСЕТЫ", 0xFF66AAFF);
        int presetsListY = presetHeaderY + 18;
        List<NpcGeckoPreset> presets = NpcGeckoPresetManager.getAll();
        if (presets.isEmpty()) {
            g.drawString(font, "§8Нет сохранённых пресетов",
                    rx + 4, presetsListY + 2, 0xFF555566, false);
        }
        g.drawString(font, "§8Имя:", rx + 4, presetsListY + Math.min(presets.size(), 3) * 18 + 6,
                0xFF555566, false);
    }

    @Override
    public void pullFields(NpcEditorState state) {
        NpcEntityData d = state.getDraft();
        NpcModelConfig mc = ensureModel(d);

        if (offsetYBox != null) {
            try { mc.offsetY = Float.parseFloat(offsetYBox.getValue()); } catch (Exception ignored) {}
        }
        if (geckoModelBox != null) d.geckoModel     = geckoModelBox.getValue().trim();
        if (geckoAnimBox  != null) d.geckoAnimation = geckoAnimBox.getValue().trim();
        if (geckoTexBox   != null) d.geckoTexture   = geckoTexBox.getValue().trim();
        if (searchBox     != null) searchQuery       = searchBox.getValue();
    }

    @Override
    public boolean onMouseScrolled(double mx, double my, double delta,
                                   NpcEditorState state, int rx, int oy, int rw) {
        List<NpcModelProfile> filtered = filteredProfiles();
        int totalRows = (filtered.size() + GRID_COLS - 1) / GRID_COLS;
        int maxScroll = Math.max(0, totalRows - GRID_VISIBLE);
        profileScroll = Math.max(0, Math.min(maxScroll, profileScroll - (int) Math.signum(delta)));
        return true;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<NpcModelProfile> filteredProfiles() {
        String query = searchQuery == null ? "" : searchQuery.toLowerCase();
        return Arrays.stream(NpcModelProfile.VALUES)
                .filter(p -> ("all".equals(categoryFilter) || p.category().equals(categoryFilter))
                        && (query.isEmpty() || p.label.toLowerCase().contains(query)
                                           || p.category().contains(query)))
                .toList();
    }

    private static NpcModelConfig ensureModel(NpcEntityData d) {
        if (d.modelConfig == null) d.modelConfig = new NpcModelConfig();
        return d.modelConfig;
    }
}

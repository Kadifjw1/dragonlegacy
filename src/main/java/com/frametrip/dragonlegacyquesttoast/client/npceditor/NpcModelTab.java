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
    private int    profileScroll  = 0;
    private EditBox offsetYBox;
    private EditBox geckoModelBox, geckoAnimBox, geckoTexBox;

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        NpcEntityData d = state.getDraft();
        NpcModelConfig mc = ensureModel(d);
        var font = Minecraft.getInstance().font;
        
        int y = oy + 18; // below model subtitle (subtitle at oy+6, height ~8px)

        // ── Category filter row ───────────────────────────────────────────────
        int bw = rw / CAT_IDS.length;
        for (int i = 0; i < CAT_IDS.length; i++) {
            final String cat = CAT_IDS[i];
            boolean sel = cat.equals(categoryFilter);
            add.accept(Button.builder(
                    Component.literal(sel ? "§e§l" + CAT_LABELS[i] : CAT_LABELS[i]),
                    b -> { categoryFilter = cat; profileScroll = 0; rebuild.run(); }
            ).bounds(rx + i * bw, y, bw - 2, 16).build());
        }
        y += 22;

        // ── Profile list ──────────────────────────────────────────────────────
        List<NpcModelProfile> filtered = filteredProfiles();
        int visibleRows = 5;
        int maxScroll   = Math.max(0, filtered.size() - visibleRows);
        profileScroll   = Math.max(0, Math.min(profileScroll, maxScroll));
        int listTop     = y;

        for (int i = profileScroll; i < Math.min(filtered.size(), profileScroll + visibleRows); i++) {
            NpcModelProfile profile = filtered.get(i);
            boolean selected = mc.profile == profile;
            int btnW = rw - 26;
            String prefix = selected ? "§e§l▶ " : "   ";
            String label  = NpcEditorUtils.fitText(profile.label, btnW - 14);
            add.accept(Button.builder(
                    Component.literal(prefix + label),
                    b -> {
                        mc.profile = profile;
                        mc.scale   = profile.baseScale;
                        state.markDirty();
                        rebuild.run();
                    }
            ).bounds(rx, y + (i - profileScroll) * 18, btnW, 16).build());
        }

        // Scroll arrows (right of list)
        add.accept(Button.builder(Component.literal("▲"),
                b -> { profileScroll = Math.max(0, profileScroll - 1); rebuild.run(); }
        ).bounds(rx + rw - 24, listTop, 22, 16).build());
        add.accept(Button.builder(Component.literal("▼"),
                b -> { profileScroll = Math.min(maxScroll, profileScroll + 1); rebuild.run(); }
        ).bounds(rx + rw - 24, listTop + 20, 22, 16).build());
        
        y += visibleRows * 18 + 10;

        // ── Parameters section ────────────────────────────────────────────────
        // Scale row:  ◀  [value]  ▶   label on right
        add.accept(Button.builder(Component.literal("◀"), b -> {
            mc.scale = Math.max(0.1f, Math.round((mc.scale - 0.1f) * 10) / 10f);
            state.markDirty(); rebuild.run();
         }).bounds(rx + 60, y, 20, 16).build());
        add.accept(Button.builder(Component.literal("▶"), b -> {
            mc.scale = Math.min(4f, Math.round((mc.scale + 0.1f) * 10) / 10f);
            state.markDirty(); rebuild.run();
        }).bounds(rx + 116, y, 20, 16).build());
        y += 20;

       // Offset Y row: label + editbox
        offsetYBox = new EditBox(font, rx + 60, y, 54, 16, Component.literal("Y"));
        offsetYBox.setValue(String.format("%.2f", mc.offsetY));
        add.accept(offsetYBox);
        y += 20;

        // Eye height row: ◀  [value]  ▶  ↺
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

       // ── Toggle options ────────────────────────────────────────────────────
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

        // ── GeckoLib binding fields ───────────────────────────────────────────
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

        // Clear-all button
        add.accept(Button.builder(Component.literal("↺ Сброс GeckoLib"), b -> {
            d.geckoModel     = "";
            d.geckoAnimation = "";
            d.geckoTexture   = "";
            state.markDirty();
            rebuild.run();
        }).bounds(rx + fieldW - 100, y + 18, 102, 14).build());
    }

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;
        NpcEntityData d  = state.getDraft();
        NpcModelConfig mc = ensureModel(d);

        // Selected model subtitle (y=6 relative to tab content, no card — avoids
        // overlapping with the tab label drawn by NpcCreatorScreen at oy-10)
        g.drawString(font, "§b" + mc.profile.label
                + " §8(" + mc.profile.category() + ")",
                rx + 4, oy + 6, ACCENT, false);

        // Parameters section (below the list)
        int paramY = oy + 18 + 22 + 5 * 18 + 10;
        NpcEditorUtils.sectionCard(g, rx, paramY - 6, rw, 84, "ПАРАМЕТРЫ", ACCENT);
        
        // Scale label + current value (left of buttons)
        g.drawString(font, "§7Масштаб:", rx + 4, paramY + 2, 0xFF888877, false);
        g.drawString(font, "§f" + String.format("%.1f", mc.scale), rx + 84, paramY + 2, 0xFFFFCC44, false);

         // Offset Y label
        g.drawString(font, "§7Смещ. Y:", rx + 4, paramY + 22, 0xFF888877, false);

        // Eye height label + value (right of ↺)
        float eh = mc.effectiveEyeHeight();
        g.drawString(font, "§7Высота глаз:", rx + 4, paramY + 42, 0xFF888877, false);
        g.drawString(font, "§f" + String.format("%.2f", eh), rx + 84, paramY + 42, 0xFFFFCC44, false);

        // Nameplate / dialogue offsets
        float np  = mc.effectiveNameplateOffset();
        float dlg = mc.effectiveDialogueOffset();
       g.drawString(font, "§8Имя: §7+" + String.format("%.1f", np)
                + "  Диалог: §7+" + String.format("%.1f", dlg),
                rx + 4, paramY + 58, 0xFF666677, false);

        // GeckoLib section header (below parameters card)
        int geckoY = paramY - 6 + 84 + 26;
        NpcEditorUtils.sectionCard(g, rx, geckoY, rw, 14, "GECKOLIB РЕСУРСЫ", ACCENT);
        g.drawString(font, "§8Модель:", rx + 4, geckoY + 17, 0xFF555566, false);
        g.drawString(font, "§8Анимация:", rx + 4, geckoY + 35, 0xFF555566, false);
        g.drawString(font, "§8Текстура:", rx + 4, geckoY + 53, 0xFF555566, false);
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

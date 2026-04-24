package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.client.NpcLayeredSkinManager;
import com.frametrip.dragonlegacyquesttoast.client.NpcSkinManager;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.server.NpcProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Tab 0 — Информация: имя, скин, слои текстур, части тела. */
public class NpcInfoTab implements NpcEditorTab {

    static final int ACCENT = 0xFF4488EE;

    // sub-page: 0=skin 1=layers 2=bodyparts
    private int subPage = 0;
    private EditBox nameField;

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        NpcEntityData d = state.getDraft();

        // Name field
        nameField = new EditBox(Minecraft.getInstance().font,
                rx, oy + 18, rw, 18, Component.literal("Имя NPC"));
        nameField.setMaxLength(64);
        nameField.setValue(d.displayName);
        nameField.setHint(Component.literal("Имя персонажа...").withStyle(s -> s.withColor(0xFF666677)));
        nameField.setResponder(v -> state.markDirty());
        add.accept(nameField);

        // Sub-page toggle buttons
        String[] labels = {"▸ Скин", "▸ Слои", "▸ Части тела"};
        int btnW = rw / 3 - 2;
        for (int i = 0; i < labels.length; i++) {
            final int pg = i;
            add.accept(Button.builder(Component.literal(labels[i]), b -> {
                pullFields(state);
                subPage = pg;
                rebuild.run();
            }).bounds(rx + i * (btnW + 3), oy + 44, btnW, 14).build());
        }

        switch (subPage) {
            case 0 -> initSkin(add, rebuild, state, d, rx, oy, rw);
            case 1 -> initLayers(add, rebuild, state, d, rx, oy, rw);
            case 2 -> initParts(add, rebuild, state, d, rx, oy, rw);
        }
    }

    // ── Sub-page: Skin ────────────────────────────────────────────────────────

    private void initSkin(Consumer<AbstractWidget> add, Runnable rb,
                          NpcEditorState state, NpcEntityData d, int rx, int oy, int rw) {
        add.accept(Button.builder(Component.literal("◀"), b -> {
            pullFields(state);
            List<String> ss = NpcSkinManager.getAvailableSkins();
            d.skinId = ss.get(Math.floorMod(ss.indexOf(d.skinId) - 1, ss.size()));
            state.markDirty();
            rb.run();
        }).bounds(rx, oy + 64, 20, 16).build());

        add.accept(Button.builder(Component.literal("▶"), b -> {
            pullFields(state);
            List<String> ss = NpcSkinManager.getAvailableSkins();
            d.skinId = ss.get(Math.floorMod(ss.indexOf(d.skinId) + 1, ss.size()));
            state.markDirty();
            rb.run();
        }).bounds(rx + 22 + 96, oy + 64, 20, 16).build());

        add.accept(Button.builder(Component.literal("Открыть папку"),
                b -> NpcSkinManager.openSkinsFolder())
                .bounds(rx, oy + 84, 100, 14).build());

        add.accept(Button.builder(Component.literal("Обновить"), b -> {
            NpcSkinManager.refresh();
            rb.run();
        }).bounds(rx + 104, oy + 84, 72, 14).build());
    }

    // ── Sub-page: Layers ──────────────────────────────────────────────────────

    private void initLayers(Consumer<AbstractWidget> add, Runnable rb,
                            NpcEditorState state, NpcEntityData d, int rx, int oy, int rw) {
        int y = oy + 64;
        for (String cat : NpcEntityData.TEXTURE_LAYERS) {
            List<String> opts = NpcLayeredSkinManager.getAvailable(cat);

            add.accept(Button.builder(Component.literal("◀"), b -> {
                pullFields(state);
                int i = opts.indexOf(d.textureLayers.getOrDefault(cat, "none"));
                d.textureLayers.put(cat, opts.get(Math.floorMod(i - 1, opts.size())));
                state.markDirty();
                rb.run();
            }).bounds(rx + 72, y, 18, 13).build());

            add.accept(Button.builder(Component.literal("▶"), b -> {
                pullFields(state);
                int i = opts.indexOf(d.textureLayers.getOrDefault(cat, "none"));
                d.textureLayers.put(cat, opts.get(Math.floorMod(i + 1, opts.size())));
                state.markDirty();
                rb.run();
            }).bounds(rx + 72 + 20 + 72, y, 18, 13).build());

            y += 16;
        }

        add.accept(Button.builder(Component.literal("Папка слоёв"),
                b -> NpcLayeredSkinManager.openLayersRootFolder())
                .bounds(rx, y + 4, 100, 14).build());

        add.accept(Button.builder(Component.literal("Обновить"), b -> {
            NpcLayeredSkinManager.refresh();
            rb.run();
        }).bounds(rx + 104, y + 4, 72, 14).build());
    }

    // ── Sub-page: Body Parts ──────────────────────────────────────────────────

    private void initParts(Consumer<AbstractWidget> add, Runnable rb,
                           NpcEditorState state, NpcEntityData d, int rx, int oy, int rw) {
        int y = oy + 64;
        for (Map.Entry<String, String[]> entry : NpcProfile.PART_OPTIONS.entrySet()) {
            String key = entry.getKey();
            String[] opts = entry.getValue();

            add.accept(Button.builder(Component.literal("◀"), b -> {
                pullFields(state);
                int cur = d.bodyParts.getOrDefault(key, 0);
                d.bodyParts.put(key, Math.floorMod(cur - 1, opts.length));
                state.markDirty();
                rb.run();
            }).bounds(rx + 72, y, 18, 14).build());

            add.accept(Button.builder(Component.literal("▶"), b -> {
                pullFields(state);
                int cur = d.bodyParts.getOrDefault(key, 0);
                d.bodyParts.put(key, Math.floorMod(cur + 1, opts.length));
                state.markDirty();
                rb.run();
            }).bounds(rx + 72 + 20 + 72, y, 18, 14).build());

            y += 18;
        }
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();

        // Name section
        sectionCard(g, rx, oy, rw, 38, "ИМЯ ПЕРСОНАЖА");
        g.drawString(font, "§7Имя:", rx + 4, oy + 10, 0xFF888877, false);

        // Sub-page indicator underline
        int btnW = rw / 3 - 2;
        for (int i = 0; i < 3; i++) {
            if (i == subPage)
                g.fill(rx + i * (btnW + 3), oy + 57, rx + i * (btnW + 3) + btnW, oy + 58, ACCENT);
        }

        // Sub-page label and content
        String[] subLabels = {"СКИН", "СЛОИ ТЕКСТУР", "ЧАСТИ ТЕЛА"};
        sectionCard(g, rx, oy + 60, rw,
                subPage == 1 ? NpcEntityData.TEXTURE_LAYERS.length * 16 + 28 :
                subPage == 2 ? NpcProfile.PART_OPTIONS.size() * 18 + 16 : 52,
                subLabels[subPage]);

        switch (subPage) {
            case 0 -> renderSkin(g, d, rx, oy);
            case 1 -> renderLayers(g, d, rx, oy);
            case 2 -> renderParts(g, d, rx, oy);
        }
    }

    private void renderSkin(GuiGraphics g, NpcEntityData d, int rx, int oy) {
        var font = Minecraft.getInstance().font;
        String name = "default".equals(d.skinId) ? "§7По умолчанию" : "§f" + d.skinId;
        g.drawCenteredString(font, name, rx + 22 + 48, oy + 67, 0xFFCCCCCC);
    }

    private void renderLayers(GuiGraphics g, NpcEntityData d, int rx, int oy) {
        var font = Minecraft.getInstance().font;
        int y = oy + 65;
        for (String cat : NpcEntityData.TEXTURE_LAYERS) {
            String label = NpcEntityData.textureLayerLabel(cat);
            String val   = d.textureLayers.getOrDefault(cat, "none");
            g.drawString(font, "§8" + label + ":", rx + 4, y + 1, 0xFF777788, false);
            g.drawCenteredString(font, "§f" + val, rx + 72 + 10 + 36, y + 2, 0xFFCCCCCC);
            y += 16;
        }
    }

    private void renderParts(GuiGraphics g, NpcEntityData d, int rx, int oy) {
        var font = Minecraft.getInstance().font;
        int y = oy + 65;
        for (Map.Entry<String, String[]> entry : NpcProfile.PART_OPTIONS.entrySet()) {
            String key   = entry.getKey();
            String label = NpcProfile.PART_LABELS.getOrDefault(key, key);
            String value = entry.getValue()[d.bodyParts.getOrDefault(key, 0)];
            g.drawString(font, "§8" + label + ":", rx + 4, y + 1, 0xFF777788, false);
            g.drawCenteredString(font, "§f" + value, rx + 72 + 10 + 36, y + 2, 0xFFCCCCCC);
            y += 18;
        }
    }

    @Override
    public void pullFields(NpcEditorState state) {
        if (nameField != null && !nameField.getValue().isBlank()) {
            state.getDraft().displayName = nameField.getValue();
            state.markDirty();
        }
    }

    // ── Static helpers (delegates to NpcEditorUtils with tab's accent) ─────────

    static void sectionCard(GuiGraphics g, int x, int y, int w, int h, String title) {
        NpcEditorUtils.sectionCard(g, x, y, w, h, title, ACCENT);
    }
}

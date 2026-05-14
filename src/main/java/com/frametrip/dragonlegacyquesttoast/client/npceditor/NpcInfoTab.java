package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.client.NpcAppearancePresetManager;
import com.frametrip.dragonlegacyquesttoast.client.NpcLayeredSkinManager;
import com.frametrip.dragonlegacyquesttoast.client.NpcSkinManager;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.server.NpcProfile;
import com.frametrip.dragonlegacyquesttoast.util.NpcTextureImporter;
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

    public static final int ACCENT = 0xFF4488EE;

    // sub-page: 0=skin 1=layers 2=bodyparts 3=import
    private int subPage = 0;
    private int presetIndex = -1;
    private int roleStyleIndex = 0;
    private EditBox nameField;
    private static final String[] ROLE_STYLES = {"Торговец", "Страж", "Кузнец", "Маг", "Житель", "Босс"};

    // import sub-page state
    private List<String> importFiles = List.of();
    private int importScroll = 0;
    private String importSelected = null;
    private String importTarget = NpcTextureImporter.TARGET_SKIN;
    private String importStatus = "";

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

        // Sub-page toggle buttons (4 tabs, compressed width)
        String[] labels = {"▸ Скин", "▸ Слои", "▸ Части тела", "▸ Импорт"};
        int btnW = rw / 4 - 2;
        for (int i = 0; i < labels.length; i++) {
            final int pg = i;
            add.accept(Button.builder(Component.literal(labels[i]), b -> {
                pullFields(state);
                subPage = pg;
                if (pg == 3) { importFiles = NpcTextureImporter.scanImportDir(); importScroll = 0; importSelected = null; importStatus = ""; }
                rebuild.run();
            }).bounds(rx + i * (btnW + 2), oy + 44, btnW, 14).build());
        }

        switch (subPage) {
            case 0 -> initSkin(add, rebuild, state, d, rx, oy, rw);
            case 1 -> initLayers(add, rebuild, state, d, rx, oy, rw);
            case 2 -> initParts(add, rebuild, state, d, rx, oy, rw);
            case 3 -> initImport(add, rebuild, rx, oy, rw);
        }
        
        initPresets(add, rebuild, state, d, rx, oy, rw);
        initRoleStyles(add, rebuild, state, d, rx, oy, rw);
    }

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

    private void initImport(Consumer<AbstractWidget> add, Runnable rb, int rx, int oy, int rw) {
        // "Open import folder" + "Refresh" buttons
        add.accept(Button.builder(Component.literal("📂 Папка импорта"),
                b -> NpcTextureImporter.openImportFolder())
                .bounds(rx, oy + 64, 120, 14).build());
        add.accept(Button.builder(Component.literal("↺ Обновить"), b -> {
            importFiles = NpcTextureImporter.scanImportDir();
            importScroll = 0;
            importSelected = null;
            importStatus = "";
            rb.run();
        }).bounds(rx + 124, oy + 64, 70, 14).build());

        // Scrollable file list
        int listY = oy + 82;
        int visRows = 7;
        int maxScroll = Math.max(0, importFiles.size() - visRows);
        importScroll = Math.max(0, Math.min(importScroll, maxScroll));

        for (int i = importScroll; i < Math.min(importFiles.size(), importScroll + visRows); i++) {
            final String fname = importFiles.get(i);
            boolean sel = fname.equals(importSelected);
            String label = (sel ? "§e▶ " : "   ") + NpcEditorUtils.fitText(fname, rw - 44);
            add.accept(Button.builder(Component.literal(label), b -> {
                importSelected = fname;
                rb.run();
            }).bounds(rx, listY + (i - importScroll) * 13, rw - 26, 12).build());
        }
        add.accept(Button.builder(Component.literal("▲"),
                b -> { importScroll = Math.max(0, importScroll - 1); rb.run(); }
        ).bounds(rx + rw - 24, listY, 22, 12).build());
        add.accept(Button.builder(Component.literal("▼"),
                b -> { importScroll = Math.min(maxScroll, importScroll + 1); rb.run(); }
        ).bounds(rx + rw - 24, listY + 14, 22, 12).build());

        // Target selector
        int targetY = oy + 82 + visRows * 13 + 6;
        String[] targets = buildTargetList();
        int curIdx = java.util.Arrays.asList(targets).indexOf(importTarget);
        if (curIdx < 0) { curIdx = 0; importTarget = targets[0]; }
        add.accept(Button.builder(Component.literal("◀"), b -> {
            int idx = java.util.Arrays.asList(buildTargetList()).indexOf(importTarget);
            String[] t = buildTargetList();
            importTarget = t[Math.floorMod(idx - 1, t.length)];
            rb.run();
        }).bounds(rx, targetY, 18, 14).build());
        add.accept(Button.builder(Component.literal("▶"), b -> {
            int idx = java.util.Arrays.asList(buildTargetList()).indexOf(importTarget);
            String[] t = buildTargetList();
            importTarget = t[Math.floorMod(idx + 1, t.length)];
            rb.run();
        }).bounds(rx + 20 + 120, targetY, 18, 14).build());

        // Import button
        boolean canImport = importSelected != null;
        add.accept(Button.builder(
                Component.literal(canImport ? "§a✔ Импортировать" : "§8✔ Импортировать"),
                b -> {
                    if (importSelected == null) return;
                    boolean ok = NpcTextureImporter.importTexture(importSelected, importTarget);
                    importStatus = ok ? "§aОК: " + importSelected + " → " + importTarget
                                      : "§cОшибка копирования";
                    if (ok) importSelected = null;
                    rb.run();
                }
        ).bounds(rx + 40 + 120, targetY, 120, 14).build());
    }

    private static String[] buildTargetList() {
        String[] layers = NpcEntityData.TEXTURE_LAYERS;
        String[] result = new String[1 + layers.length];
        result[0] = NpcTextureImporter.TARGET_SKIN;
        System.arraycopy(layers, 0, result, 1, layers.length);
        return result;
    }

    private void initPresets(Consumer<AbstractWidget> add, Runnable rb,
                             NpcEditorState state, NpcEntityData d, int rx, int oy, int rw) {
        int y = oy + 220;
        var presets = NpcAppearancePresetManager.getAll();
        if (!presets.isEmpty()) {
            if (presetIndex < 0 || presetIndex >= presets.size()) presetIndex = 0;
            add.accept(Button.builder(Component.literal("◀ Пресет"), b -> {
                presetIndex = Math.floorMod(presetIndex - 1, presets.size());
                rb.run();
            }).bounds(rx, y, 80, 14).build());
            add.accept(Button.builder(Component.literal("Применить"), b -> {
                pullFields(state);
                NpcAppearancePresetManager.applyPreset(presets.get(presetIndex), d);
                state.markDirty();
                rb.run();
            }).bounds(rx + 84, y, 76, 14).build());
            add.accept(Button.builder(Component.literal("Пресет ▶"), b -> {
                presetIndex = Math.floorMod(presetIndex + 1, presets.size());
                rb.run();
            }).bounds(rx + 164, y, 80, 14).build());
        }
        add.accept(Button.builder(Component.literal("+ Сохранить как пресет"), b -> {
            pullFields(state);
            NpcAppearancePresetManager.savePreset(d.displayName + " look", d);
            presetIndex = Math.max(0, NpcAppearancePresetManager.getAll().size() - 1);
            rb.run();
        }).bounds(rx + 248, y, rw - 248, 14).build());
    }

    private void initRoleStyles(Consumer<AbstractWidget> add, Runnable rb,
                                NpcEditorState state, NpcEntityData d, int rx, int oy, int rw) {
        int y = oy + 256;
        add.accept(Button.builder(Component.literal("◀ Роль"), b -> {
            roleStyleIndex = Math.floorMod(roleStyleIndex - 1, ROLE_STYLES.length);
            rb.run();
        }).bounds(rx, y, 72, 14).build());
        add.accept(Button.builder(Component.literal("Применить: " + ROLE_STYLES[roleStyleIndex]), b -> {
            pullFields(state);
            applyRoleStyle(d, ROLE_STYLES[roleStyleIndex]);
            state.markDirty();
            rb.run();
        }).bounds(rx + 76, y, 140, 14).build());
        add.accept(Button.builder(Component.literal("Роль ▶"), b -> {
            roleStyleIndex = Math.floorMod(roleStyleIndex + 1, ROLE_STYLES.length);
            rb.run();
        }).bounds(rx + 220, y, 72, 14).build());
        add.accept(Button.builder(Component.literal("🎲 Случайно"), b -> {
            pullFields(state);
            randomizeLook(d);
            state.markDirty();
            rb.run();
        }).bounds(rx + 296, y, rw - 296, 14).build());
    }

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();

        sectionCard(g, rx, oy, rw, 38, "ИМЯ ПЕРСОНАЖА");
        g.drawString(font, "§7Имя:", rx + 4, oy + 10, 0xFF888877, false);

        int btnW = rw / 4 - 2;
        for (int i = 0; i < 4; i++) {
            if (i == subPage)
                g.fill(rx + i * (btnW + 2), oy + 57, rx + i * (btnW + 2) + btnW, oy + 58, ACCENT);
        }

        String[] subLabels = {"СКИН", "СЛОИ ТЕКСТУР", "ЧАСТИ ТЕЛА", "ИМПОРТ ТЕКСТУР"};
        int cardH = switch (subPage) {
            case 1 -> NpcEntityData.TEXTURE_LAYERS.length * 16 + 28;
            case 2 -> NpcProfile.PART_OPTIONS.size() * 18 + 16;
            case 3 -> 7 * 13 + 78;
            default -> 52;
        };
        sectionCard(g, rx, oy + 60, rw, cardH, subLabels[subPage]);

        switch (subPage) {
            case 0 -> renderSkin(g, d, rx, oy);
            case 1 -> renderLayers(g, d, rx, oy);
            case 2 -> renderParts(g, d, rx, oy);
            case 3 -> renderImport(g, rx, oy, rw);
        }
        
        var presets = NpcAppearancePresetManager.getAll();
        String pLabel = presets.isEmpty() ? "нет пресетов" :
                presets.get(Math.max(0, Math.min(presetIndex, presets.size() - 1))).name;
        g.drawString(font, "§8Пресеты: §f" + pLabel, rx + 4, oy + 238, 0xFF777788, false);
        g.drawString(font, "§8Стиль роли: §f" + ROLE_STYLES[roleStyleIndex], rx + 4, oy + 272, 0xFF777788, false);
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

    private void renderImport(GuiGraphics g, int rx, int oy, int rw) {
        var font = Minecraft.getInstance().font;
        int listY = oy + 82;
        int visRows = 7;

        if (importFiles.isEmpty()) {
            g.drawString(font, "§8Нет файлов в папке импорта", rx + 4, listY + 4, 0xFF555566, false);
        } else {
            for (int i = importScroll; i < Math.min(importFiles.size(), importScroll + visRows); i++) {
                boolean sel = importFiles.get(i).equals(importSelected);
                if (sel) g.fill(rx, listY + (i - importScroll) * 13, rx + rw - 26, listY + (i - importScroll) * 13 + 12, 0x33AADDFF);
            }
        }

        // Target label
        int targetY = oy + 82 + visRows * 13 + 6;
        String[] targets = buildTargetList();
        int curIdx = java.util.Arrays.asList(targets).indexOf(importTarget);
        if (curIdx < 0) curIdx = 0;
        String targetLabel = NpcTextureImporter.TARGET_SKIN.equals(importTarget)
                ? "Скин"
                : NpcEntityData.textureLayerLabel(importTarget);
        g.drawString(font, "§7→ " + targetLabel, rx + 22, targetY + 2, 0xFF88AACC, false);

        // Status line
        if (!importStatus.isEmpty()) {
            g.drawString(font, importStatus, rx + 4, targetY + 20, 0xFFCCCCCC, false);
        }

        // File count
        g.drawString(font, "§8Файлов: §f" + importFiles.size(), rx + 4, oy + 68, 0xFF555566, false);
    }

    @Override
    public boolean onMouseScrolled(double mx, double my, double delta,
                                   NpcEditorState state, int rx, int oy, int rw) {
        if (subPage == 3) {
            int maxScroll = Math.max(0, importFiles.size() - 7);
            importScroll = Math.max(0, Math.min(maxScroll, importScroll - (int) Math.signum(delta)));
            return true;
        }
        return false;
    }

    @Override
    public void pullFields(NpcEditorState state) {
        if (nameField != null && !nameField.getValue().isBlank()) {
            state.getDraft().displayName = nameField.getValue();
            state.markDirty();
        }
    }

    static void sectionCard(GuiGraphics g, int x, int y, int w, int h, String title) {
        NpcEditorUtils.sectionCard(g, x, y, w, h, title, ACCENT);
    }
    
    private static void randomizeLook(NpcEntityData d) {
        java.util.Random r = new java.util.Random();
        for (Map.Entry<String, String[]> e : NpcProfile.PART_OPTIONS.entrySet()) {
            d.bodyParts.put(e.getKey(), r.nextInt(Math.max(1, e.getValue().length)));
        }
        for (String layer : NpcEntityData.TEXTURE_LAYERS) {
            List<String> opts = NpcLayeredSkinManager.getAvailable(layer);
            if (!opts.isEmpty()) d.textureLayers.put(layer, opts.get(r.nextInt(opts.size())));
        }
    }

    private static void applyRoleStyle(NpcEntityData d, String role) {
        switch (role) {
            case "Страж" -> {
                d.bodyParts.put("eyes", 1);
                d.textureLayers.put("top", "none");
                d.textureLayers.put("accessory", "none");
            }
            case "Кузнец" -> {
                d.bodyParts.put("torso", 1);
                d.bodyParts.put("rightArm", 3);
            }
            case "Маг" -> {
                d.bodyParts.put("eyes", 3);
                d.textureLayers.put("overlay", "none");
            }
            case "Босс" -> {
                d.bodyParts.put("mouth", 4);
                d.bodyParts.put("eyes", 1);
            }
            default -> {}
        }
    }
}

package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.client.NpcAppearancePresetManager;
import com.frametrip.dragonlegacyquesttoast.client.NpcCreatorScreen;
import com.frametrip.dragonlegacyquesttoast.client.NpcFileUtils;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Tab 0 — Информация: имя, скин, слои текстур, части тела, свойства персонажа.
 *
 * Sub-pages:
 *   0 = Скин
 *   1 = Слои текстур
 *   2 = Части тела
 *   3 = Импорт
 *   4 = Пресеты (открывается кнопкой из шапки)
 */
public class NpcInfoTab implements NpcEditorTab {

    public static final int ACCENT = 0xFF4488EE;

    private int subPage = 0;
    private int presetIndex = -1;
    private EditBox nameField;

    // [INFO-NEW-1,5,6,7,11]: EditBoxes for numeric/text fields
    private EditBox healthBox, respawnBox, tagBox, groupBox, noteBox;

    // import sub-page state
    private List<String> importFiles = List.of();
    private int importScroll = 0;
    private String importSelected = null;
    private String importTarget = NpcTextureImporter.TARGET_SKIN;
    private String importStatus = "";

    /** Called from NpcCreatorScreen header button to jump directly to presets page. */
    public void setSubPage(int page) { this.subPage = page; }

    // Y offset where character properties section begins (below all sub-page content)
    private static final int PROPS_Y_OFFSET = 235;

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        NpcEntityData d = state.getDraft();

        // ── Name field ────────────────────────────────────────────────────────
        nameField = new EditBox(Minecraft.getInstance().font,
                rx, oy + 18, rw, 18, Component.literal("Имя NPC"));
        nameField.setMaxLength(64);
        nameField.setValue(d.displayName);
        nameField.setHint(Component.literal("Имя персонажа...").withStyle(s -> s.withColor(0xFF666677)));
        nameField.setResponder(v -> state.markDirty());
        add.accept(nameField);

        // ── Sub-page toggle buttons (5 pages) ────────────────────────────────
        // [INFO-FIX-3]: Presets moved here as sub-page 4 (triggered from header btn)
        String[] labels = {"▸ Скин", "▸ Слои", "▸ Части", "▸ Импорт", "▸ Пресеты"};
        int btnW = rw / 5 - 2;
        for (int i = 0; i < labels.length; i++) {
            final int pg = i;
            add.accept(Button.builder(Component.literal(labels[i]), b -> {
                pullFields(state);
                subPage = pg;
                if (pg == 3) {
                    importFiles = NpcTextureImporter.scanImportDir();
                    importScroll = 0; importSelected = null; importStatus = "";
                }
                rebuild.run();
            }).bounds(rx + i * (btnW + 2), oy + 44, btnW, 14).build());
        }

        switch (subPage) {
            case 0 -> initSkin(add, rebuild, state, d, rx, oy, rw);
            case 1 -> initLayers(add, rebuild, state, d, rx, oy, rw);
            case 2 -> initParts(add, rebuild, state, d, rx, oy, rw);
            case 3 -> initImport(add, rebuild, rx, oy, rw);
            case 4 -> initPresets(add, rebuild, state, d, rx, oy, rw);
        }

        // [INFO-FIX-4]: Role styles removed. Properties section always visible.
        initProperties(add, rebuild, state, d, rx, oy, rw);
    }

    // ── Sub-page: Skin ────────────────────────────────────────────────────────

    // [INFO-FIX-2]: New wide skin selector — ◄ [wide name display] ►
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
        }).bounds(rx + rw - 20, oy + 64, 20, 16).build());

        add.accept(Button.builder(Component.literal("Открыть папку"),
                b -> NpcSkinManager.openSkinsFolder())
                .bounds(rx, oy + 84, 100, 14).build());

        add.accept(Button.builder(Component.literal("Обновить"), b -> {
            NpcSkinManager.refresh(); rb.run();
        }).bounds(rx + 104, oy + 84, 72, 14).build());

        add.accept(Button.builder(Component.literal("↑ Импорт PNG"), b -> {
            importTextureFile(state, d, rb);
        }).bounds(rx + 180, oy + 84, 100, 14).build());
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
                state.markDirty(); rb.run();
            }).bounds(rx + 72, y, 18, 13).build());
            add.accept(Button.builder(Component.literal("▶"), b -> {
                pullFields(state);
                int i = opts.indexOf(d.textureLayers.getOrDefault(cat, "none"));
                d.textureLayers.put(cat, opts.get(Math.floorMod(i + 1, opts.size())));
                state.markDirty(); rb.run();
            }).bounds(rx + 72 + 20 + 72, y, 18, 13).build());
            y += 16;
        }
        add.accept(Button.builder(Component.literal("Папка слоёв"),
                b -> NpcLayeredSkinManager.openLayersRootFolder())
                .bounds(rx, y + 4, 100, 14).build());
        add.accept(Button.builder(Component.literal("Обновить"), b -> {
            NpcLayeredSkinManager.refresh(); rb.run();
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
                state.markDirty(); rb.run();
            }).bounds(rx + 72, y, 18, 14).build());
            add.accept(Button.builder(Component.literal("▶"), b -> {
                pullFields(state);
                int cur = d.bodyParts.getOrDefault(key, 0);
                d.bodyParts.put(key, Math.floorMod(cur + 1, opts.length));
                state.markDirty(); rb.run();
            }).bounds(rx + 72 + 20 + 72, y, 18, 14).build());
            y += 18;
        }
    }

    // ── Sub-page: Import ──────────────────────────────────────────────────────

    private void initImport(Consumer<AbstractWidget> add, Runnable rb, int rx, int oy, int rw) {
        add.accept(Button.builder(Component.literal("📂 Папка импорта"),
                b -> NpcTextureImporter.openImportFolder())
                .bounds(rx, oy + 64, 120, 14).build());
        add.accept(Button.builder(Component.literal("↺ Обновить"), b -> {
            importFiles = NpcTextureImporter.scanImportDir();
            importScroll = 0; importSelected = null; importStatus = ""; rb.run();
        }).bounds(rx + 124, oy + 64, 70, 14).build());

        int listY = oy + 82;
        int visRows = 7;
        int maxScroll = Math.max(0, importFiles.size() - visRows);
        importScroll = Math.max(0, Math.min(importScroll, maxScroll));
        for (int i = importScroll; i < Math.min(importFiles.size(), importScroll + visRows); i++) {
            final String fname = importFiles.get(i);
            boolean sel = fname.equals(importSelected);
            String label = (sel ? "§e▶ " : "   ") + NpcEditorUtils.fitText(fname, rw - 44);
            add.accept(Button.builder(Component.literal(label), b -> {
                importSelected = fname; rb.run();
            }).bounds(rx, listY + (i - importScroll) * 13, rw - 26, 12).build());
        }
        add.accept(Button.builder(Component.literal("▲"),
                b -> { importScroll = Math.max(0, importScroll - 1); rb.run(); }
        ).bounds(rx + rw - 24, listY, 22, 12).build());
        add.accept(Button.builder(Component.literal("▼"),
                b -> { importScroll = Math.min(maxScroll, importScroll + 1); rb.run(); }
        ).bounds(rx + rw - 24, listY + 14, 22, 12).build());

        int targetY = oy + 82 + visRows * 13 + 6;
        String[] targets = buildTargetList();
        int curIdx = java.util.Arrays.asList(targets).indexOf(importTarget);
        if (curIdx < 0) { curIdx = 0; importTarget = targets[0]; }
        add.accept(Button.builder(Component.literal("◀"), b -> {
            String[] t = buildTargetList();
            int idx = java.util.Arrays.asList(t).indexOf(importTarget);
            importTarget = t[Math.floorMod(idx - 1, t.length)]; rb.run();
        }).bounds(rx, targetY, 18, 14).build());
        add.accept(Button.builder(Component.literal("▶"), b -> {
            String[] t = buildTargetList();
            int idx = java.util.Arrays.asList(t).indexOf(importTarget);
            importTarget = t[Math.floorMod(idx + 1, t.length)]; rb.run();
        }).bounds(rx + 20 + 120, targetY, 18, 14).build());

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

    // ── Sub-page: Presets ─────────────────────────────────────────────────────
    // [INFO-FIX-3]: Presets moved here from inline bottom-of-tab position

    private void initPresets(Consumer<AbstractWidget> add, Runnable rb,
                             NpcEditorState state, NpcEntityData d, int rx, int oy, int rw) {
        int y = oy + 64;
        var presets = NpcAppearancePresetManager.getAll();
        if (!presets.isEmpty()) {
            if (presetIndex < 0 || presetIndex >= presets.size()) presetIndex = 0;
            add.accept(Button.builder(Component.literal("◀ Пресет"), b -> {
                presetIndex = Math.floorMod(presetIndex - 1, presets.size()); rb.run();
            }).bounds(rx, y, 80, 14).build());
            add.accept(Button.builder(Component.literal("Применить"), b -> {
                pullFields(state);
                NpcAppearancePresetManager.applyPreset(presets.get(presetIndex), d);
                state.markDirty(); rb.run();
            }).bounds(rx + 84, y, 76, 14).build());
            add.accept(Button.builder(Component.literal("Пресет ▶"), b -> {
                presetIndex = Math.floorMod(presetIndex + 1, presets.size()); rb.run();
            }).bounds(rx + 164, y, 80, 14).build());
            y += 20;
        }
        add.accept(Button.builder(Component.literal("+ Сохранить как пресет"), b -> {
            pullFields(state);
            NpcAppearancePresetManager.savePreset(d.displayName + " look", d);
            presetIndex = Math.max(0, NpcAppearancePresetManager.getAll().size() - 1);
            rb.run();
        }).bounds(rx, y, rw / 2 - 2, 14).build());

        y += 20;
        add.accept(Button.builder(Component.literal("🎲 Случайный вид"), b -> {
            pullFields(state); randomizeLook(d); state.markDirty(); rb.run();
        }).bounds(rx, y, 140, 14).build());
    }

    // ── New character properties (NEW-1 … NEW-11) ─────────────────────────────

    private void initProperties(Consumer<AbstractWidget> add, Runnable rb,
                                NpcEditorState state, NpcEntityData d, int rx, int oy, int rw) {
        int y = oy + PROPS_Y_OFFSET;

        // [INFO-NEW-1]: Max health EditBox + [INFO-NEW-4]: ShowName toggle
        healthBox = new EditBox(Minecraft.getInstance().font,
                rx + 80, y, 44, 14, Component.literal("20"));
        healthBox.setMaxLength(4);
        healthBox.setValue(String.valueOf(d.maxHealth));
        add.accept(healthBox);

        add.accept(Button.builder(Component.literal(d.showName ? "§a[✓] Имя" : "§8[_] Имя"), b -> {
            pullFields(state); d.showName = !d.showName; state.markDirty(); rb.run();
        }).bounds(rx + rw - 80, y, 78, 14).build());
        y += 18;

        // [INFO-NEW-2]: Gender — two toggle buttons
        add.accept(Button.builder(
                Component.literal(d.gender == 0 ? "§e♂ Мужской" : "§7♂ Мужской"), b -> {
            pullFields(state); d.gender = 0; state.markDirty(); rb.run();
        }).bounds(rx + 50, y, 80, 14).build());
        add.accept(Button.builder(
                Component.literal(d.gender == 1 ? "§d♀ Женский" : "§7♀ Женский"), b -> {
            pullFields(state); d.gender = 1; state.markDirty(); rb.run();
        }).bounds(rx + 134, y, 80, 14).build());

        // [INFO-NEW-8]: Invulnerable toggle (same row, right side)
        add.accept(Button.builder(
                Component.literal(d.invulnerable ? "§c[✓] Неуязвим" : "§8[_] Неуязвим"), b -> {
            pullFields(state); d.invulnerable = !d.invulnerable; state.markDirty(); rb.run();
        }).bounds(rx + rw - 100, y, 98, 14).build());
        y += 18;

        // [INFO-NEW-3]: Voice type cycle ◄ label ►
        add.accept(Button.builder(Component.literal("◀"), b -> {
            pullFields(state);
            d.voiceType = (byte) Math.floorMod(d.voiceType - 1, NpcEntityData.VOICE_LABELS.length);
            state.markDirty(); rb.run();
        }).bounds(rx + 50, y, 18, 14).build());
        add.accept(Button.builder(Component.literal("▶"), b -> {
            pullFields(state);
            d.voiceType = (byte) Math.floorMod(d.voiceType + 1, NpcEntityData.VOICE_LABELS.length);
            state.markDirty(); rb.run();
        }).bounds(rx + 50 + 18 + 72, y, 18, 14).build());
        y += 18;

        // [INFO-NEW-9]: Rain behavior — two toggle buttons
        add.accept(Button.builder(
                Component.literal(d.rainBehavior == 0 ? "§e☀ Игнорирует" : "§7☀ Игнорирует"), b -> {
            pullFields(state); d.rainBehavior = 0; state.markDirty(); rb.run();
        }).bounds(rx + 80, y, 90, 14).build());
        add.accept(Button.builder(
                Component.literal(d.rainBehavior == 1 ? "§b🏠 Прячется" : "§7🏠 Прячется"), b -> {
            pullFields(state); d.rainBehavior = 1; state.markDirty(); rb.run();
        }).bounds(rx + 174, y, 88, 14).build());
        y += 22; // small separator gap

        // [INFO-NEW-7]: Death behavior — three toggle buttons
        String[] deathLabels = NpcEntityData.DEATH_LABELS;
        String[] deathIcons  = {"🔄", "💨", "💀"};
        int dbW = (rw - 56) / 3;
        for (int i = 0; i < 3; i++) {
            final byte bi = (byte) i;
            boolean active = d.deathBehavior == i;
            add.accept(Button.builder(
                    Component.literal(active ? "§e" + deathIcons[i] + " " + deathLabels[i]
                                             : "§8" + deathIcons[i] + " " + deathLabels[i]),
                    b -> { pullFields(state); d.deathBehavior = bi; state.markDirty(); rb.run(); }
            ).bounds(rx + 56 + i * (dbW + 2), y, dbW, 14).build());
        }
        y += 18;

        // [INFO-NEW-7]: Respawn time (only when behavior == 0)
        if (d.deathBehavior == 0) {
            respawnBox = new EditBox(Minecraft.getInstance().font,
                    rx + 180, y, 40, 14, Component.literal("60"));
            respawnBox.setMaxLength(5);
            respawnBox.setValue(String.valueOf(d.respawnTime));
            add.accept(respawnBox);
            y += 18;
        }
        y += 4; // separator gap

        // [INFO-NEW-10]: Nameplate icon cycle ◄ label ►
        add.accept(Button.builder(Component.literal("◀"), b -> {
            pullFields(state);
            d.nameplateIcon = (byte) Math.floorMod(d.nameplateIcon - 1, NpcEntityData.ICON_LABELS.length);
            state.markDirty(); rb.run();
        }).bounds(rx + 56, y, 18, 14).build());
        add.accept(Button.builder(Component.literal("▶"), b -> {
            pullFields(state);
            d.nameplateIcon = (byte) Math.floorMod(d.nameplateIcon + 1, NpcEntityData.ICON_LABELS.length);
            state.markDirty(); rb.run();
        }).bounds(rx + 56 + 18 + 84, y, 18, 14).build());
        y += 18;

        // [INFO-NEW-5]: NPC Tag
        tagBox = new EditBox(Minecraft.getInstance().font,
                rx + 50, y, rw - 52, 14, Component.literal("тег..."));
        tagBox.setMaxLength(32);
        tagBox.setValue(d.npcTag);
        add.accept(tagBox);
        y += 18;

        // [INFO-NEW-11]: NPC Group
        groupBox = new EditBox(Minecraft.getInstance().font,
                rx + 64, y, rw - 66, 14, Component.literal("группа..."));
        groupBox.setMaxLength(32);
        groupBox.setValue(d.npcGroup);
        add.accept(groupBox);
        y += 18;

        // [INFO-NEW-6]: Editor note (NOT synced to players)
        noteBox = new EditBox(Minecraft.getInstance().font,
                rx + 62, y, rw - 64, 14, Component.literal("заметка..."));
        noteBox.setMaxLength(256);
        noteBox.setValue(d.editorNote);
        add.accept(noteBox);
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();

        // [INFO-FIX-1]: Only section card header — "Имя:" sub-label removed
        sectionCard(g, rx, oy, rw, 38, "ИМЯ ПЕРСОНАЖА");

        // Sub-page underline indicator
        int btnW = rw / 5 - 2;
        for (int i = 0; i < 5; i++) {
            if (i == subPage)
                g.fill(rx + i * (btnW + 2), oy + 57, rx + i * (btnW + 2) + btnW, oy + 58, ACCENT);
        }

        String[] subLabels = {"СКИН", "СЛОИ ТЕКСТУР", "ЧАСТИ ТЕЛА", "ИМПОРТ ТЕКСТУР", "ПРЕСЕТЫ"};
        int cardH = switch (subPage) {
            case 1 -> NpcEntityData.TEXTURE_LAYERS.length * 16 + 28;
            case 2 -> NpcProfile.PART_OPTIONS.size() * 18 + 16;
            case 3 -> 7 * 13 + 78;
            case 4 -> 80;
            default -> 52;
        };
        sectionCard(g, rx, oy + 60, rw, cardH, subLabels[subPage]);

        switch (subPage) {
            case 0 -> renderSkin(g, d, rx, oy, rw);
            case 1 -> renderLayers(g, d, rx, oy);
            case 2 -> renderParts(g, d, rx, oy);
            case 3 -> renderImport(g, rx, oy, rw);
            case 4 -> renderPresets(g, d, rx, oy);
        }

        // [INFO-NEW-*]: Character properties section
        renderProperties(g, d, rx, oy, rw);
    }

    // ── Sub-render methods ────────────────────────────────────────────────────

    // [INFO-FIX-2]: Wide skin display spanning full available width
    private void renderSkin(GuiGraphics g, NpcEntityData d, int rx, int oy, int rw) {
        var font = Minecraft.getInstance().font;
        // Draw wide field background
        g.fill(rx + 22, oy + 64, rx + rw - 22, oy + 80, 0xFF141420);
        NpcCreatorScreen.brd(g, rx + 22, oy + 64, rw - 44, 16, 0xFF334466);
        String name = "default".equals(d.skinId) ? "§7По умолчанию" : "§f" + d.skinId;
        g.drawCenteredString(font, name, rx + rw / 2, oy + 68, 0xFFCCCCCC);
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
                if (sel) g.fill(rx, listY + (i - importScroll) * 13, rx + rw - 26,
                        listY + (i - importScroll) * 13 + 12, 0x33AADDFF);
            }
        }
        int targetY = oy + 82 + visRows * 13 + 6;
        String targetLabel = NpcTextureImporter.TARGET_SKIN.equals(importTarget)
                ? "Скин" : NpcEntityData.textureLayerLabel(importTarget);
        g.drawString(font, "§7→ " + targetLabel, rx + 22, targetY + 2, 0xFF88AACC, false);
        if (!importStatus.isEmpty())
            g.drawString(font, importStatus, rx + 4, targetY + 20, 0xFFCCCCCC, false);
        g.drawString(font, "§8Файлов: §f" + importFiles.size(), rx + 4, oy + 68, 0xFF555566, false);
    }

    private void renderPresets(GuiGraphics g, NpcEntityData d, int rx, int oy) {
        var font = Minecraft.getInstance().font;
        var presets = NpcAppearancePresetManager.getAll();
        String pLabel = presets.isEmpty() ? "нет пресетов" :
                presets.get(Math.max(0, Math.min(presetIndex, presets.size() - 1))).name;
        g.drawString(font, "§8Активный пресет: §f" + pLabel, rx + 4, oy + 65, 0xFF777788, false);
        g.drawString(font, "§8Всего пресетов: §f" + presets.size(), rx + 4, oy + 75, 0xFF555566, false);
    }

    // [INFO-NEW-*]: Render labels for all new character properties
    private void renderProperties(GuiGraphics g, NpcEntityData d, int rx, int oy, int rw) {
        var font = Minecraft.getInstance().font;
        int y = oy + PROPS_Y_OFFSET;

        // Separator line above props section
        g.fill(rx, y - 6, rx + rw, y - 5, 0xFF2A2A44);
        g.drawString(font, "§7§lСВОЙСТВА", rx + 4, y - 14, ACCENT, false);

        // [INFO-NEW-1]: Health
        g.drawString(font, "§c❤ §7Здоровье:", rx + 4, y + 2, 0xFF888877, false);
        y += 18;

        // [INFO-NEW-2]: Gender
        g.drawString(font, "§7Пол:", rx + 4, y + 2, 0xFF888877, false);
        y += 18;

        // [INFO-NEW-3]: Voice
        g.drawString(font, "§7Голос:", rx + 4, y + 2, 0xFF888877, false);
        // voice label centered between arrows
        int vlIdx = Math.max(0, Math.min(d.voiceType, NpcEntityData.VOICE_LABELS.length - 1));
        g.drawCenteredString(font, "§f" + NpcEntityData.VOICE_LABELS[vlIdx],
                rx + 50 + 18 + 36, y + 2, 0xFFCCCCCC);
        y += 18;

        // [INFO-NEW-9]: Rain behavior
        g.drawString(font, "§7При дожде:", rx + 4, y + 2, 0xFF888877, false);
        y += 22;

        // [INFO-NEW-7]: Death separator + label
        g.fill(rx, y - 4, rx + rw, y - 3, 0xFF2A2A44);
        g.drawString(font, "§7При смерти:", rx + 4, y + 2, 0xFF888877, false);
        y += 18;

        // [INFO-NEW-7]: Respawn time label (only if behavior == 0)
        if (d.deathBehavior == 0) {
            g.drawString(font, "§7Время возрождения:", rx + 4, y + 2, 0xFF888877, false);
            g.drawString(font, "§8 сек.", rx + 224, y + 2, 0xFF666677, false);
            y += 18;
        }
        y += 4;

        // [INFO-NEW-10]: Nameplate icon
        g.fill(rx, y - 4, rx + rw, y - 3, 0xFF2A2A44);
        g.drawString(font, "§7Иконка:", rx + 4, y + 2, 0xFF888877, false);
        int niIdx = Math.max(0, Math.min(d.nameplateIcon, NpcEntityData.ICON_LABELS.length - 1));
        g.drawCenteredString(font, "§f" + NpcEntityData.ICON_LABELS[niIdx],
                rx + 56 + 18 + 42, y + 2, 0xFFCCCCCC);
        y += 18;

        // [INFO-NEW-5]: Tag
        g.drawString(font, "§7Тег:", rx + 4, y + 2, 0xFF888877, false);
        y += 18;

        // [INFO-NEW-11]: Group
        g.drawString(font, "§7Группа:", rx + 4, y + 2, 0xFF888877, false);
        y += 18;

        // [INFO-NEW-6]: Editor note
        g.drawString(font, "§7Заметка:", rx + 4, y + 2, 0xFF888877, false);
        g.drawString(font, "§8(не отображается в игре)", rx + 62, y + 16, 0xFF555566, false);
    }

    // ── Input handlers ────────────────────────────────────────────────────────

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
        NpcEntityData d = state.getDraft();
        boolean changed = false;

        if (nameField != null && !nameField.getValue().isBlank()) {
            d.displayName = nameField.getValue(); changed = true;
        }
        // [INFO-NEW-1]:
        if (healthBox != null) {
            try { d.maxHealth = Math.max(1, Math.min(1000, Integer.parseInt(healthBox.getValue()))); changed = true; }
            catch (NumberFormatException ignored) {}
        }
        // [INFO-NEW-7]:
        if (respawnBox != null) {
            try { d.respawnTime = Math.max(1, Integer.parseInt(respawnBox.getValue())); changed = true; }
            catch (NumberFormatException ignored) {}
        }
        // [INFO-NEW-5]:
        if (tagBox != null) { d.npcTag = tagBox.getValue(); changed = true; }
        // [INFO-NEW-11]:
        if (groupBox != null) { d.npcGroup = groupBox.getValue(); changed = true; }
        // [INFO-NEW-6]:
        if (noteBox != null) { d.editorNote = noteBox.getValue(); changed = true; }

        if (changed) state.markDirty();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String[] buildTargetList() {
        String[] layers = NpcEntityData.TEXTURE_LAYERS;
        String[] result = new String[1 + layers.length];
        result[0] = NpcTextureImporter.TARGET_SKIN;
        System.arraycopy(layers, 0, result, 1, layers.length);
        return result;
    }

    private void importTextureFile(NpcEditorState state, NpcEntityData d, Runnable rb) {
        Thread t = new Thread(() -> {
            try {
                java.awt.FileDialog dialog = new java.awt.FileDialog(
                        (java.awt.Frame) null, "Выберите PNG текстуру", java.awt.FileDialog.LOAD);
                dialog.setFilenameFilter((dir, name) -> name.toLowerCase().endsWith(".png"));
                dialog.setVisible(true);
                String dirStr  = dialog.getDirectory();
                String fileStr = dialog.getFile();
                if (dirStr != null && fileStr != null) {
                    Path source = Path.of(dirStr, fileStr);
                    copyTextureToSkins(source, d, state, rb);
                }
            } catch (Exception e) {
                NpcFileUtils.openInExplorer(NpcFileUtils.getImportTexDir());
            }
        }, "npc-tex-import");
        t.setDaemon(true);
        t.start();
    }

    private void copyTextureToSkins(Path source, NpcEntityData d, NpcEditorState state, Runnable rb) {
        try {
            Path target = NpcFileUtils.getSkinsDir().resolve(source.getFileName());
            Files.createDirectories(target.getParent());
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            Minecraft.getInstance().execute(() -> {
                NpcSkinManager.refresh();
                d.skinId = source.getFileName().toString().replaceAll("\\.png$", "");
                state.markDirty();
                rb.run();
            });
        } catch (IOException e) {
            // silently ignored — folder opened by caller
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
}

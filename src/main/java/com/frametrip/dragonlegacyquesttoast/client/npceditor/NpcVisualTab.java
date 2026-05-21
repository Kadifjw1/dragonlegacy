package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.server.vfx.DynamicSkin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/** [VFX-1..4]: Visual effects tab — nameplate, hologram, cutscenes, dynamic skins. */
public class NpcVisualTab implements NpcEditorTab {

    public static final int ACCENT = 0xFFAA55FF;

    private EditBox nameplateColorBox;
    private EditBox nameplateScaleBox;
    private EditBox nameplateBgColorBox;
    private EditBox hologramTextBox;
    private EditBox hologramHeightBox;
    private EditBox hologramScaleBox;
    private EditBox cutsceneIdBox;
    // Dynamic skin entries: each entry has condition + skinName boxes
    private EditBox dynCondBox0, dynSkinBox0;
    private EditBox dynCondBox1, dynSkinBox1;

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        var mc = Minecraft.getInstance();
        NpcEntityData d = state.getDraft();
        int y = oy + 22;
        int hw = (rw - 8) / 2;

        // ── VFX-1: Nameplate ──────────────────────────────────────────────────
        nameplateColorBox = new EditBox(mc.font, rx, y, hw - 2, 14, Component.literal("Цвет"));
        nameplateColorBox.setHint(Component.literal("ARGB hex, напр. FFFFFFFF"));
        nameplateColorBox.setValue(String.format("%08X", d.nameplateColor));
        add.accept(nameplateColorBox);

        nameplateScaleBox = new EditBox(mc.font, rx + hw + 2, y, hw - 2, 14, Component.literal("Масштаб"));
        nameplateScaleBox.setHint(Component.literal("1.0"));
        nameplateScaleBox.setValue(String.valueOf(d.nameplateScale));
        add.accept(nameplateScaleBox); y += 18;

        add.accept(Button.builder(
            Component.literal(d.nameplateBackground ? "§a✔ Фон имени" : "§7☐ Фон имени"),
            b -> { d.nameplateBackground = !d.nameplateBackground; state.markDirty(); rebuild.run(); }
        ).bounds(rx, y, hw - 2, 14).build());

        nameplateBgColorBox = new EditBox(mc.font, rx + hw + 2, y, hw - 2, 14, Component.literal("Цвет фона"));
        nameplateBgColorBox.setHint(Component.literal("ARGB hex"));
        nameplateBgColorBox.setValue(String.format("%08X", d.nameplateBgColor));
        add.accept(nameplateBgColorBox); y += 22;

        // ── VFX-2: Hologram ───────────────────────────────────────────────────
        add.accept(Button.builder(
            Component.literal(d.hologramEnabled ? "§a✔ Голограмма вкл." : "§7☐ Голограмма выкл."),
            b -> { d.hologramEnabled = !d.hologramEnabled; state.markDirty(); rebuild.run(); }
        ).bounds(rx, y, rw - 4, 14).build()); y += 18;

        hologramTextBox = new EditBox(mc.font, rx, y, rw - 4, 14, Component.literal("Текст голограммы"));
        hologramTextBox.setHint(Component.literal("{name}, {hp}, {mood}, {level}"));
        hologramTextBox.setValue(d.hologramText != null ? d.hologramText : "{name}");
        add.accept(hologramTextBox); y += 18;

        hologramHeightBox = new EditBox(mc.font, rx, y, hw - 2, 14, Component.literal("Высота"));
        hologramHeightBox.setHint(Component.literal("2.5"));
        hologramHeightBox.setValue(String.valueOf(d.hologramHeight));
        add.accept(hologramHeightBox);

        hologramScaleBox = new EditBox(mc.font, rx + hw + 2, y, hw - 2, 14, Component.literal("Масштаб"));
        hologramScaleBox.setHint(Component.literal("0.5"));
        hologramScaleBox.setValue(String.valueOf(d.hologramScale));
        add.accept(hologramScaleBox); y += 22;

        // ── VFX-3: Cutscene ───────────────────────────────────────────────────
        cutsceneIdBox = new EditBox(mc.font, rx, y, rw - 4, 14, Component.literal("ID кат-сцены"));
        cutsceneIdBox.setHint(Component.literal("ID из config/cutscenes/<id>.json (пусто = нет)"));
        cutsceneIdBox.setValue(d.cutsceneId != null ? d.cutsceneId : "");
        add.accept(cutsceneIdBox); y += 22;

        // ── VFX-4: Dynamic Skins ──────────────────────────────────────────────
        // Show 2 editable slots
        DynamicSkin ds0 = d.dynamicSkins.size() > 0 ? d.dynamicSkins.get(0) : null;
        DynamicSkin ds1 = d.dynamicSkins.size() > 1 ? d.dynamicSkins.get(1) : null;

        dynCondBox0 = new EditBox(mc.font, rx, y, hw - 2, 14, Component.literal("Условие 1"));
        dynCondBox0.setHint(Component.literal("time:night / weather:rain / mood:<-50"));
        dynCondBox0.setValue(ds0 != null ? ds0.condition : "");
        add.accept(dynCondBox0);

        dynSkinBox0 = new EditBox(mc.font, rx + hw + 2, y, hw - 2, 14, Component.literal("Скин 1"));
        dynSkinBox0.setHint(Component.literal("skin_id"));
        dynSkinBox0.setValue(ds0 != null ? ds0.skinName : "");
        add.accept(dynSkinBox0); y += 18;

        dynCondBox1 = new EditBox(mc.font, rx, y, hw - 2, 14, Component.literal("Условие 2"));
        dynCondBox1.setHint(Component.literal("Условие"));
        dynCondBox1.setValue(ds1 != null ? ds1.condition : "");
        add.accept(dynCondBox1);

        dynSkinBox1 = new EditBox(mc.font, rx + hw + 2, y, hw - 2, 14, Component.literal("Скин 2"));
        dynSkinBox1.setHint(Component.literal("skin_id"));
        dynSkinBox1.setValue(ds1 != null ? ds1.skinName : "");
        add.accept(dynSkinBox1);
    }

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;
        NpcEditorUtils.sectionCard(g, rx, oy, rw, 18, "ВИЗУАЛЬНЫЕ ЭФФЕКТЫ", ACCENT);
        int y = oy + 22;
        g.drawString(font, "§5VFX-1: §fНеймплейт (цвет ARGB  /  масштаб)", rx + 2, y - 10, ACCENT, false);
        g.drawString(font, "§5VFX-2: §fГолограмма", rx + 2, y + 22 + 18 - 2, ACCENT, false);
        g.drawString(font, "§5VFX-3: §fКат-сцена (ID)", rx + 2, y + 22 + 18 + 18 + 18 + 18 - 2, ACCENT, false);
        g.drawString(font, "§5VFX-4: §fДинамические скины (до 2 условий)", rx + 2,
                y + 22 + 18 + 18 + 18 + 18 + 22 - 2, ACCENT, false);
    }

    @Override
    public void pullFields(NpcEditorState state) {
        NpcEntityData d = state.getDraft();
        if (nameplateColorBox != null) {
            try { d.nameplateColor = (int) Long.parseLong(nameplateColorBox.getValue().trim(), 16); }
            catch (NumberFormatException ignored) {}
        }
        if (nameplateScaleBox != null) {
            try { d.nameplateScale = Float.parseFloat(nameplateScaleBox.getValue().trim()); }
            catch (NumberFormatException ignored) {}
        }
        if (nameplateBgColorBox != null) {
            try { d.nameplateBgColor = (int) Long.parseLong(nameplateBgColorBox.getValue().trim(), 16); }
            catch (NumberFormatException ignored) {}
        }
        if (hologramTextBox  != null) d.hologramText   = hologramTextBox.getValue();
        if (hologramHeightBox != null) {
            try { d.hologramHeight = Float.parseFloat(hologramHeightBox.getValue().trim()); }
            catch (NumberFormatException ignored) {}
        }
        if (hologramScaleBox != null) {
            try { d.hologramScale = Float.parseFloat(hologramScaleBox.getValue().trim()); }
            catch (NumberFormatException ignored) {}
        }
        if (cutsceneIdBox != null) d.cutsceneId = cutsceneIdBox.getValue().trim();

        // Dynamic skins
        d.dynamicSkins.clear();
        if (dynCondBox0 != null && dynSkinBox0 != null) {
            String cond = dynCondBox0.getValue().trim();
            String skin = dynSkinBox0.getValue().trim();
            if (!cond.isEmpty() && !skin.isEmpty()) d.dynamicSkins.add(new DynamicSkin(cond, skin));
        }
        if (dynCondBox1 != null && dynSkinBox1 != null) {
            String cond = dynCondBox1.getValue().trim();
            String skin = dynSkinBox1.getValue().trim();
            if (!cond.isEmpty() && !skin.isEmpty()) d.dynamicSkins.add(new DynamicSkin(cond, skin));
        }
    }
}

package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.NpcBuildingActionPacket;
import com.frametrip.dragonlegacyquesttoast.server.building.BuildingTemplate;
import com.frametrip.dragonlegacyquesttoast.server.building.BuildingTemplateManager;
import com.frametrip.dragonlegacyquesttoast.server.building.NpcBuildingData;
import com.frametrip.dragonlegacyquesttoast.client.building.ClientBuildingState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * Вкладка "Строительство" в редакторе NPC.
 * Разделена на 4 секции:
 *   1. Общие параметры строителя
 *   2. Список разрешённых зданий (карточки + чекбоксы)
 *   3. Активная стройка (прогресс + управление)
 *   4. Рабочая зона
 */
public class NpcBuildingTab implements NpcEditorTab {

    public static final int ACCENT = 0xFF55BB44;

    // scroll for building list
    private int buildingScroll = 0;
    // filter: category
    private String categoryFilter = "all";

    // work-zone editboxes
    private EditBox zoneXBox, zoneYBox, zoneZBox, zoneRadiusBox, speedBox;

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        NpcEntityData d = state.getDraft();
        NpcBuildingData bd = ensureBuilding(d);
        int y = oy + 36;

        // ── 1. Общие параметры ───────────────────────────────────────────────
        add.accept(Button.builder(
                Component.literal(bd.isBuilder ? "§a◉ Строитель" : "§8○ Строитель"),
                b -> {
                    bd.isBuilder = !bd.isBuilder;
                    state.markDirty();
                    rebuild.run();
                }
        ).bounds(rx, y, 120, 18).build());

        add.accept(Button.builder(Component.literal("Скорость: §e" + bd.blocksPerTick + " §7бл/тик"),
                b -> {
                    bd.blocksPerTick = (bd.blocksPerTick % 5) + 1;
                    state.markDirty();
                    rebuild.run();
                }
        ).bounds(rx + 128, y, 160, 18).build());
        y += 24;

        if (!bd.isBuilder) return;

        // ── 2. Список зданий ─────────────────────────────────────────────────
        drawCategoryButtons(add, rebuild, state, bd, rx, y, rw);
        y += 22;

        List<BuildingTemplate> templates = BuildingTemplateManager.getAll();
        // filter
        List<BuildingTemplate> filtered = templates.stream().filter(t ->
                "all".equals(categoryFilter) || categoryFilter.equals(t.category)
        ).toList();

        int cardH = 48;
        int visibleCards = 4;
        int listTop = y;
        int maxScroll = Math.max(0, filtered.size() - visibleCards);
        buildingScroll = Math.max(0, Math.min(buildingScroll, maxScroll));

        for (int i = buildingScroll; i < Math.min(filtered.size(), buildingScroll + visibleCards); i++) {
            BuildingTemplate tmpl = filtered.get(i);
            boolean allowed = bd.allowedBuildingIds.contains(tmpl.id);
            int cardY = listTop + (i - buildingScroll) * cardH;

            // Toggle button
            add.accept(Button.builder(
                    Component.literal(allowed ? "§a☑" : "§8☐"),
                    b -> {
                        if (allowed) bd.allowedBuildingIds.remove(tmpl.id);
                        else bd.allowedBuildingIds.add(tmpl.id);
                        state.markDirty();
                        rebuild.run();
                    }
            ).bounds(rx, cardY + cardH / 2 - 9, 18, 18).build());
        }
        y += visibleCards * cardH + 4;

  // Scroll buttons
        add.accept(Button.builder(Component.literal("▲"),
                b -> { buildingScroll = Math.max(0, buildingScroll - 1); rebuild.run(); }
        ).bounds(rx + rw - 20, listTop, 18, 18).build());
        add.accept(Button.builder(Component.literal("▼"),
                b -> { buildingScroll = Math.min(maxScroll, buildingScroll + 1); rebuild.run(); }
        ).bounds(rx + rw - 20, listTop + 20, 18, 18).build());

        // ── 3. Активная стройка ──────────────────────────────────────────────
        ClientBuildingState.BuildingProgress active = ClientBuildingState.getState(state.getEntity().getUUID());
        if (active != null && active.isActive()) {
            add.accept(Button.builder(Component.literal("⏸ Пауза"),
                    b -> {
                        ModNetwork.CHANNEL.sendToServer(
                                new NpcBuildingActionPacket(state.getEntity().getUUID(), "pause", "", 0, 0, 0));
                    }
            ).bounds(rx, y, 70, 18).build());
            add.accept(Button.builder(Component.literal("▶ Продолжить"),
                    b -> ModNetwork.CHANNEL.sendToServer(
                            new NpcBuildingActionPacket(state.getEntity().getUUID(), "resume", "", 0, 0, 0))
            ).bounds(rx + 74, y, 90, 18).build());
            add.accept(Button.builder(Component.literal("✕ Отмена"),
                    b -> ModNetwork.CHANNEL.sendToServer(
                            new NpcBuildingActionPacket(state.getEntity().getUUID(), "cancel", "", 0, 0, 0))
            ).bounds(rx + 168, y, 70, 18).build());
            y += 22;
        } else if (!bd.allowedBuildingIds.isEmpty()) {
            // Start building selector
            String firstId = bd.allowedBuildingIds.get(0);
            add.accept(Button.builder(Component.literal("🏗 Начать стройку: §e" + firstName(firstId)),
                    b -> {
                        int[] pos = getPlayerPos();
                        ModNetwork.CHANNEL.sendToServer(
                                new NpcBuildingActionPacket(state.getEntity().getUUID(), "start", firstId, pos[0], pos[1], pos[2]));
                    }
            ).bounds(rx, y, rw, 18).build());
            y += 22;
        }

        // ── 4. Рабочая зона ──────────────────────────────────────────────────
        var font = Minecraft.getInstance().font;

        zoneXBox = new EditBox(font, rx, y, 55, 16, Component.literal("X"));
        zoneXBox.setValue(String.valueOf(bd.workZoneX));
        add.accept(zoneXBox);

        zoneYBox = new EditBox(font, rx + 58, y, 55, 16, Component.literal("Y"));
        zoneYBox.setValue(String.valueOf(bd.workZoneY));
        add.accept(zoneYBox);

        zoneZBox = new EditBox(font, rx + 116, y, 55, 16, Component.literal("Z"));
        zoneZBox.setValue(String.valueOf(bd.workZoneZ));
        add.accept(zoneZBox);

        zoneRadiusBox = new EditBox(font, rx + 174, y, 55, 16, Component.literal("Радиус"));
        zoneRadiusBox.setValue(String.valueOf(bd.workZoneRadius));
        add.accept(zoneRadiusBox);

        add.accept(Button.builder(Component.literal("📍 Моя позиция"), b -> {
            var mc = Minecraft.getInstance();
            if (mc.player != null) {
                bd.workZoneX = (int) mc.player.getX();
                bd.workZoneY = (int) mc.player.getY();
                bd.workZoneZ = (int) mc.player.getZ();
                state.markDirty();
                rebuild.run();
            }
        }).bounds(rx + rw - 110, y, 108, 16).build());
    }

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();
        NpcBuildingData bd = ensureBuilding(d);

        g.drawString(font, "§l⚒ Строительство", rx, oy + 14, ACCENT, false);

        if (!bd.isBuilder) {
            g.drawString(font, "§8NPC не является строителем.", rx, oy + 60, 0xFF666677, false);
            return;
        }

        int y = oy + 60;

        // Category filter labels
        g.drawString(font, "§7Категория:", rx, y, 0xFF888899, false);
        y += 22;

        // Building list cards
        List<BuildingTemplate> templates = BuildingTemplateManager.getAll().stream().filter(t ->
                "all".equals(categoryFilter) || categoryFilter.equals(t.category)
        ).toList();

        int cardH = 48;
        int visibleCards = 4;
        int listTop = y;

        for (int i = buildingScroll; i < Math.min(templates.size(), buildingScroll + visibleCards); i++) {
            BuildingTemplate tmpl = templates.get(i);
            boolean allowed = bd.allowedBuildingIds.contains(tmpl.id);
            int cardY = listTop + (i - buildingScroll) * cardH;

            // Card background
            int bg = allowed ? 0x3344AA44 : 0x22FFFFFF;
            g.fill(rx, cardY, rx + rw - 22, cardY + cardH - 2, bg);
            NpcEditorUtils.brd(g, rx, cardY, rw - 22, cardH - 2, allowed ? 0xFF44AA44 : 0xFF444466);

            // Card content
            g.drawString(font, "§f§l" + tmpl.name, rx + 22, cardY + 4, 0xFFFFFFFF, false);
            g.drawString(font, "§7" + tmpl.categoryLabel() + " §8| §7" + tmpl.sizeLabel()
                    + " §8| §7" + tmpl.totalBlocks() + " блоков",
                    rx + 22, cardY + 16, 0xFFCCCCCC, false);
            if (!tmpl.description.isEmpty()) {
                String desc = tmpl.description.length() > 55
                        ? tmpl.description.substring(0, 52) + "…" : tmpl.description;
                g.drawString(font, "§8" + desc, rx + 22, cardY + 28, 0xFF888899, false);
            }
            if (!tmpl.professionLink.isEmpty()) {
                g.drawString(font, "§6⚙ §8" + tmpl.professionLink, rx + rw - 100, cardY + 4, 0xFFBB8833, false);
            }
        }

  if (templates.isEmpty()) {
            g.drawString(font, "§8Нет доступных шаблонов.", rx, listTop + 20, 0xFF555566, false);
        }

        y = listTop + visibleCards * cardH + 8;

        // Active build status
        ClientBuildingState.BuildingProgress active = ClientBuildingState.getState(state.getEntity().getUUID());
        g.drawString(font, "§7— Активная стройка —", rx, y, 0xFF888899, false);
        y += 10;
        if (active != null) {
            String statusColor = switch (active.status()) {
                case "BUILDING"  -> "§a";
                case "PAUSED"    -> "§e";
                case "DONE"      -> "§2";
                case "CANCELLED" -> "§c";
                default          -> "§7";
            };
            BuildingTemplate tmpl = BuildingTemplateManager.get(active.templateId());
            String name = tmpl != null ? tmpl.name : active.templateId();
            g.drawString(font, "§f" + name + " " + statusColor + active.status(), rx, y, 0xFFFFFFFF, false);
            y += 10;
            // Progress bar
            int barW = rw - 22;
            g.fill(rx, y, rx + barW, y + 8, 0xFF333344);
            int filled = (int) (barW * active.fraction());
            if (filled > 0) g.fill(rx, y, rx + filled, y + 8, 0xFF44AA44);
            g.drawString(font, active.label(), rx + barW / 2 - 12, y + 1, 0xFFFFFFFF, false);
            y += 12;
        } else {
            g.drawString(font, "§8Нет активного строительства.", rx, y, 0xFF666677, false);
            y += 12;
        }

        // Work zone label
        g.drawString(font, "§7— Рабочая зона —", rx, y + 20, 0xFF888899, false);
        g.drawString(font, "§8X: §7" + bd.workZoneX + "  Y: §7" + bd.workZoneY
                + "  Z: §7" + bd.workZoneZ + "  R: §7" + bd.workZoneRadius,
                rx, y + 30, 0xFFCCCCCC, false);
    }

    @Override
    public void pullFields(NpcEditorState state) {
        NpcEntityData d = state.getDraft();
        NpcBuildingData bd = ensureBuilding(d);
        if (zoneXBox != null)      try { bd.workZoneX = Integer.parseInt(zoneXBox.getValue()); } catch (Exception ignored) {}
        if (zoneYBox != null)      try { bd.workZoneY = Integer.parseInt(zoneYBox.getValue()); } catch (Exception ignored) {}
        if (zoneZBox != null)      try { bd.workZoneZ = Integer.parseInt(zoneZBox.getValue()); } catch (Exception ignored) {}
        if (zoneRadiusBox != null) try { bd.workZoneRadius = Integer.parseInt(zoneRadiusBox.getValue()); } catch (Exception ignored) {}
    }

    @Override
    public boolean onMouseScrolled(double mx, double my, double delta,
                                   NpcEditorState state, int rx, int oy, int rw) {
        buildingScroll = Math.max(0, buildingScroll - (int) Math.signum(delta));
        return true;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static NpcBuildingData ensureBuilding(NpcEntityData d) {
        if (d.buildingData == null) d.buildingData = new NpcBuildingData();
        return d.buildingData;
    }

    private void drawCategoryButtons(Consumer<AbstractWidget> add, Runnable rebuild,
                                      NpcEditorState state, NpcBuildingData bd,
                                      int rx, int y, int rw) {
        String[] cats  = { "all", "residential", "military", "farm", "civic" };
        String[] clabs = { "Все", "Жильё", "Военное", "Ферма", "Гражд." };
        int bw = (rw) / cats.length;
        for (int i = 0; i < cats.length; i++) {
            final String cat = cats[i];
            boolean sel = cat.equals(categoryFilter);
            add.accept(Button.builder(
                    Component.literal(sel ? "§e§l" + clabs[i] : clabs[i]),
                    b -> { categoryFilter = cat; rebuild.run(); }
            ).bounds(rx + i * bw, y, bw - 2, 18).build());
        }
    }

    private static String firstName(String id) {
        BuildingTemplate t = BuildingTemplateManager.get(id);
        return t != null ? t.name : id;
    }

    private static int[] getPlayerPos() {
        var mc = Minecraft.getInstance();
        if (mc.player != null)
            return new int[]{ (int) mc.player.getX(), (int) mc.player.getY(), (int) mc.player.getZ() };
        return new int[]{ 0, 64, 0 };
    }
}

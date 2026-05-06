package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.client.dialogue.ClientNpcSceneState;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcScene;
import com.frametrip.dragonlegacyquesttoast.server.stealth.GuardState;
import com.frametrip.dragonlegacyquesttoast.server.stealth.PatrolPoint;
import com.frametrip.dragonlegacyquesttoast.server.stealth.StealthConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

/** Вкладка "Стелс / Охрана" — настройка поведения стража. */
public class NpcStealthTab implements NpcEditorTab {

    public static final int ACCENT = 0xFFAA4488;

    private int patrolScroll = 0;
    private EditBox visionRadiusBox, visionAngleBox, hearingBox, detectTicksBox, sensitivityBox;
    private int selectedPatrolPoint = -1;

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        NpcEntityData d = state.getDraft();
        StealthConfig sc = ensureStealth(d);
        var mc = Minecraft.getInstance();
        int y = oy + 26;

        // ── Enable toggle ─────────────────────────────────────────────────────
        add.accept(Button.builder(
                Component.literal(sc.guardEnabled ? "§a◉ Режим стража активен" : "§8○ Режим стража"),
                b -> { sc.guardEnabled = !sc.guardEnabled; state.markDirty(); rebuild.run(); }
        ).bounds(rx, y, rw, 18).build());
        y += 22;

        if (!sc.guardEnabled) return;

        // ── Guard type ────────────────────────────────────────────────────────
        add.accept(Button.builder(
                Component.literal("§7Тип: §f" + guardTypeLabel(sc.guardType)),
                b -> {
                    int i = indexOf(StealthConfig.GUARD_TYPES, sc.guardType);
                    sc.guardType = StealthConfig.GUARD_TYPES[(i + 1) % StealthConfig.GUARD_TYPES.length];
                    state.markDirty(); rebuild.run();
                }
        ).bounds(rx, y, 140, 16).build());
        y += 22;

        // ── Detection parameters ──────────────────────────────────────────────
        visionRadiusBox = new EditBox(mc.font, rx, y, 50, 16, Component.literal("Радиус зрения"));
        visionRadiusBox.setValue(String.format("%.1f", sc.visionRadius));
        add.accept(visionRadiusBox);

        visionAngleBox = new EditBox(mc.font, rx + 56, y, 50, 16, Component.literal("Угол обзора"));
        visionAngleBox.setValue(String.format("%.0f", sc.visionAngle));
        add.accept(visionAngleBox);

        hearingBox = new EditBox(mc.font, rx + 112, y, 50, 16, Component.literal("Радиус слуха"));
        hearingBox.setValue(String.format("%.1f", sc.hearingRadius));
        add.accept(hearingBox);
        y += 22;

        detectTicksBox = new EditBox(mc.font, rx, y, 50, 16, Component.literal("Тики обнаружения"));
        detectTicksBox.setValue(String.valueOf(sc.detectionTicks));
        add.accept(detectTicksBox);

        sensitivityBox = new EditBox(mc.font, rx + 56, y, 50, 16, Component.literal("Чувствительность"));
        sensitivityBox.setValue(String.format("%.2f", sc.sensitivity));
        add.accept(sensitivityBox);
        y += 22;

  // ── Scene selectors ───────────────────────────────────────────────────
        List<NpcScene> scenes = ClientNpcSceneState.getAll();

        add.accept(Button.builder(
                Component.literal("§7Сцена тревоги: §e" + truncateSceneName(sc.alarmSceneId, scenes)),
                b -> {
                    sc.alarmSceneId = cycleScene(sc.alarmSceneId, scenes);
                    state.markDirty(); rebuild.run();
                }
        ).bounds(rx, y, rw, 16).build());
        y += 20;

        add.accept(Button.builder(
                Component.literal("§7Сцена обнаружения: §e" + truncateSceneName(sc.detectSceneId, scenes)),
                b -> {
                    sc.detectSceneId = cycleScene(sc.detectSceneId, scenes);
                    state.markDirty(); rebuild.run();
                }
        ).bounds(rx, y, rw, 16).build());
        y += 22;

        // ── Patrol ────────────────────────────────────────────────────────────
        add.accept(Button.builder(
                Component.literal(sc.loopPatrol ? "§aЦикл патрулирования: ВКЛ" : "§8Цикл патрулирования: ВЫКЛ"),
                b -> { sc.loopPatrol = !sc.loopPatrol; state.markDirty(); rebuild.run(); }
        ).bounds(rx, y, rw / 2 - 2, 16).build());

        add.accept(Button.builder(Component.literal("+ Точка"), b -> {
            PatrolPoint pt = new PatrolPoint();
            if (mc.player != null) {
                pt.x = (int) mc.player.getX();
                pt.y = (int) mc.player.getY();
                pt.z = (int) mc.player.getZ();
            }
            sc.patrolRoute.add(pt);
            selectedPatrolPoint = sc.patrolRoute.size() - 1;
            state.markDirty(); rebuild.run();
        }).bounds(rx + rw / 2, y, 60, 16).build());

        add.accept(Button.builder(Component.literal("✕ Удалить"), b -> {
            if (selectedPatrolPoint >= 0 && selectedPatrolPoint < sc.patrolRoute.size()) {
                sc.patrolRoute.remove(selectedPatrolPoint);
                selectedPatrolPoint = Math.max(-1, selectedPatrolPoint - 1);
                state.markDirty(); rebuild.run();
            }
        }).bounds(rx + rw / 2 + 64, y, 60, 16).build());
        y += 22;

        // Patrol point list
        int visiblePts = 4;
        int maxScroll  = Math.max(0, sc.patrolRoute.size() - visiblePts);
        patrolScroll   = Math.max(0, Math.min(patrolScroll, maxScroll));

        for (int i = patrolScroll; i < Math.min(sc.patrolRoute.size(), patrolScroll + visiblePts); i++) {
            PatrolPoint pt = sc.patrolRoute.get(i);
            boolean sel    = i == selectedPatrolPoint;
            final int idx  = i;
            add.accept(Button.builder(
                    Component.literal((sel ? "§e→ " : "§8  ") + "P" + (i + 1) +
                            " §7(" + (int)pt.x + "," + (int)pt.y + "," + (int)pt.z + ")"),
                    b -> { selectedPatrolPoint = idx; rebuild.run(); }
            ).bounds(rx, y + (i - patrolScroll) * 18, rw, 16).build());
        }
    }

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();
        StealthConfig sc = ensureStealth(d);

        NpcEditorUtils.sectionCard(g, rx, oy, rw, 18, "СТЕЛС / ОХРАНА", ACCENT);
        if (!sc.guardEnabled) {
            g.drawString(font, "§8NPC не является стражем.", rx + 4, oy + 40, 0xFF555566, false);
            return;
        }

        int y = oy + 26;
        g.drawString(font, "§7Тип: §f" + guardTypeLabel(sc.guardType) + "  §7Точек патруля: §f" + sc.patrolRoute.size(),
                rx + 4, y + 22, 0xFF888877, false);

        g.drawString(font, "§7Зрение: §f" + sc.visionRadius + "б §8/ §7Угол: §f" + (int)sc.visionAngle + "°",
                rx + 4, y + 44, 0xFF888877, false);
        g.drawString(font, "§7Слух: §f" + sc.hearingRadius + "б §8| §7Чувств.: §f" + sc.sensitivity,
                rx + 4, y + 54, 0xFF888877, false);

        // Draw detection cone visualizer (simple 2D)
        int cx = rx + rw - 32, cy = oy + 80;
        g.fill(cx - 20, cy - 20, cx + 20, cy + 20, 0xFF111120);
        NpcEditorUtils.brd(g, cx - 20, cy - 20, 40, 40, 0xFF333344);
        g.drawString(font, "§8⚠", cx - 3, cy - 4, GuardState.CALM.color(), false);
    }

@Override
    public void pullFields(NpcEditorState state) {
        StealthConfig sc = ensureStealth(state.getDraft());
        if (visionRadiusBox != null)
            try { sc.visionRadius = Float.parseFloat(visionRadiusBox.getValue()); } catch (Exception ignored) {}
        if (visionAngleBox != null)
            try { sc.visionAngle = Float.parseFloat(visionAngleBox.getValue()); } catch (Exception ignored) {}
        if (hearingBox != null)
            try { sc.hearingRadius = Float.parseFloat(hearingBox.getValue()); } catch (Exception ignored) {}
        if (detectTicksBox != null)
            try { sc.detectionTicks = Integer.parseInt(detectTicksBox.getValue()); } catch (Exception ignored) {}
        if (sensitivityBox != null)
            try { sc.sensitivity = Float.parseFloat(sensitivityBox.getValue()); } catch (Exception ignored) {}
    }

    @Override
    public boolean onMouseScrolled(double mx, double my, double delta,
                                   NpcEditorState state, int rx, int oy, int rw) {
        int total = ensureStealth(state.getDraft()).patrolRoute.size();
        patrolScroll = Math.max(0, Math.min(Math.max(0, total - 4), patrolScroll - (int) Math.signum(delta)));
        return true;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static StealthConfig ensureStealth(NpcEntityData d) {
        if (d.stealthConfig == null) d.stealthConfig = new StealthConfig();
        return d.stealthConfig;
    }

    private static String guardTypeLabel(String type) {
        for (int i = 0; i < StealthConfig.GUARD_TYPES.length; i++)
            if (StealthConfig.GUARD_TYPES[i].equals(type)) return StealthConfig.GUARD_TYPE_LABELS[i];
        return type;
    }

    private static int indexOf(String[] arr, String val) {
        for (int i = 0; i < arr.length; i++) if (arr[i].equals(val)) return i;
        return 0;
    }

    private static String cycleScene(String current, List<NpcScene> scenes) {
        if (scenes.isEmpty()) return "";
        int i = -1;
        for (int j = 0; j < scenes.size(); j++) if (scenes.get(j).id.equals(current)) { i = j; break; }
        return (i < 0 || i >= scenes.size() - 1) ? "" : scenes.get(i + 1).id;
    }

    private static String truncateSceneName(String id, List<NpcScene> scenes) {
        if (id == null || id.isBlank()) return "§8— нет —";
        return scenes.stream().filter(s -> s.id.equals(id))
                .map(s -> s.name != null ? s.name : s.id)
                .findFirst().orElse("§c" + id);
    }
}

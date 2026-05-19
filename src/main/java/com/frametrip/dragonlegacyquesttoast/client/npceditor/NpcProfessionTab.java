package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.profession.JobConditions;
import com.frametrip.dragonlegacyquesttoast.profession.NpcProfessionData;
import com.frametrip.dragonlegacyquesttoast.profession.NpcProfessionType;
import com.frametrip.dragonlegacyquesttoast.profession.WorkSchedule;
import com.frametrip.dragonlegacyquesttoast.profession.trader.TraderMode;
import com.frametrip.dragonlegacyquesttoast.server.stealth.PatrolPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public class NpcProfessionTab implements NpcEditorTab {

    public static final int ACCENT = 0xFFFFAA33;

    // Scroll state for the work-route list
    private int routeScroll = 0;

    // EditBoxes managed per-rebuild
    private EditBox schedStartBox;
    private EditBox schedEndBox;
    private EditBox minHpBox;

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        NpcEntityData d = state.getDraft();
        NpcProfessionData pd = ensureProfession(d);

        // ── Profession selector (2-column grid) ─────────────────────────────
        NpcProfessionType[] types = NpcProfessionType.values();
        int colW = (rw - 4) / 2;
        for (int i = 0; i < types.length; i++) {
            NpcProfessionType t = types[i];
            boolean sel = pd.type == t;
            int col = i % 2;
            int row = i / 2;
            int bx = rx + col * (colW + 4);
            int by = oy + 38 + row * 22;
            String profLabel = NpcEditorUtils.fitText(t.label(), colW - 16);
            add.accept(Button.builder(
                    Component.literal((sel ? "§e◉ §r" : "○ ") + profLabel),
                    b -> {
                        pd.type = t;
                        if (t == NpcProfessionType.TRADER) pd.ensureTraderData();
                        state.markDirty();
                        rebuild.run();
                    }
            ).bounds(bx, by, colW, 18).build());
        }

        int profRows = (types.length + 1) / 2;
        int afterGrid = oy + 38 + profRows * 22 + 8;

        // ── Trader sub-section ───────────────────────────────────────────────
        if (pd.type == NpcProfessionType.TRADER) {
            pd.ensureTraderData();
            int traderBase = afterGrid + 8;

            TraderMode[] modes = TraderMode.values();
            int mx = rx;
            for (TraderMode m : modes) {
                boolean sel = pd.traderData.mode == m;
                add.accept(Button.builder(
                        Component.literal((sel ? "§e◉ §r" : "○ ") + m.label()),
                        b -> {
                            pd.traderData.mode = m;
                            state.markDirty();
                            rebuild.run();
                        }
                ).bounds(mx, traderBase + 24, 120, 18).build());
                mx += 124;
            }

            add.accept(Button.builder(
                    Component.literal("⚙ Настроить магазин"),
                    b -> {
                        state.markDirty();
                        Minecraft.getInstance().setScreen(
                                new com.frametrip.dragonlegacyquesttoast.client.TraderEditorScreen(
                                        state, Minecraft.getInstance().screen));
                    }
            ).bounds(rx, traderBase + 56, 160, 20).build());

            afterGrid = traderBase + 84;
        }

        // ── [JOB-1] Work schedule ────────────────────────────────────────────
        int schedBase = afterGrid + 8;
        WorkSchedule ws = pd.workSchedule;

        boolean schedOn = ws.enabled;
        add.accept(Button.builder(
                Component.literal(schedOn ? "§aРасписание: ВКЛ" : "§7Расписание: ВЫКЛ"),
                b -> {
                    ws.enabled = !ws.enabled;
                    state.markDirty();
                    rebuild.run();
                }
        ).bounds(rx, schedBase + 20, 140, 18).build());

        var mc = Minecraft.getInstance();
        schedStartBox = new EditBox(mc.font, rx + 148, schedBase + 20, 36, 18,
                Component.literal("С"));
        schedStartBox.setValue(String.valueOf(ws.startHour));
        schedStartBox.setMaxLength(2);
        add.accept(schedStartBox);

        schedEndBox = new EditBox(mc.font, rx + 196, schedBase + 20, 36, 18,
                Component.literal("По"));
        schedEndBox.setValue(String.valueOf(ws.endHour));
        schedEndBox.setMaxLength(2);
        add.accept(schedEndBox);

        // ── [JOB-2] Work patrol route ────────────────────────────────────────
        int routeBase = schedBase + 48;
        List<PatrolPoint> route = pd.workRoute;

        // Add current-position button
        add.accept(Button.builder(
                Component.literal("+ Добавить точку"),
                b -> {
                    var player = Minecraft.getInstance().player;
                    if (player == null) return;
                    PatrolPoint pp = new PatrolPoint(
                            (int) player.getX(), (int) player.getY(), (int) player.getZ());
                    route.add(pp);
                    state.markDirty();
                    rebuild.run();
                }
        ).bounds(rx, routeBase + 20, 120, 18).build());

        // Loop toggle
        add.accept(Button.builder(
                Component.literal(pd.loopWorkRoute ? "§aЦикл: ВКЛ" : "§7Цикл: ВЫКЛ"),
                b -> {
                    pd.loopWorkRoute = !pd.loopWorkRoute;
                    state.markDirty();
                    rebuild.run();
                }
        ).bounds(rx + 124, routeBase + 20, 80, 18).build());

        // Scrollable list — show up to 4 points
        int maxVisible = 4;
        int totalPoints = route.size();
        int startIdx = Math.min(routeScroll, Math.max(0, totalPoints - maxVisible));
        routeScroll = startIdx;

        for (int i = startIdx; i < Math.min(startIdx + maxVisible, totalPoints); i++) {
            final int idx = i;
            PatrolPoint pp = route.get(i);
            int rowY = routeBase + 42 + (i - startIdx) * 20;

            // Delete button
            add.accept(Button.builder(
                    Component.literal("✕"),
                    b -> {
                        route.remove(idx);
                        if (routeScroll > 0 && routeScroll >= route.size()) routeScroll--;
                        state.markDirty();
                        rebuild.run();
                    }
            ).bounds(rx + rw - 20, rowY, 18, 18).build());

            // Pause ticks toggle (short cycle: 20/40/60/100)
            add.accept(Button.builder(
                    Component.literal("⏱" + pp.pauseTicks + "t"),
                    b -> {
                        int[] opts = {20, 40, 60, 100};
                        int cur = pp.pauseTicks;
                        int next = opts[0];
                        for (int k = 0; k < opts.length; k++) {
                            if (opts[k] == cur) { next = opts[(k + 1) % opts.length]; break; }
                        }
                        pp.pauseTicks = next;
                        state.markDirty();
                        rebuild.run();
                    }
            ).bounds(rx + rw - 70, rowY, 46, 18).build());
        }

        // Scroll arrows
        if (totalPoints > maxVisible) {
            add.accept(Button.builder(Component.literal("▲"),
                    b -> { if (routeScroll > 0) { routeScroll--; rebuild.run(); } }
            ).bounds(rx + rw - 92, routeBase + 42, 18, 9).build());
            add.accept(Button.builder(Component.literal("▼"),
                    b -> { if (routeScroll + maxVisible < totalPoints) { routeScroll++; rebuild.run(); } }
            ).bounds(rx + rw - 92, routeBase + 52, 18, 9).build());
        }

        // ── [JOB-3] Job conditions ───────────────────────────────────────────
        int condBase = routeBase + 48 + Math.min(totalPoints, maxVisible) * 20 + 8;
        JobConditions cond = pd.jobConditions;

        add.accept(Button.builder(
                Component.literal(cond.requireWorkSchedule ? "§a☑§r Расписание" : "§7☐ Расписание"),
                b -> { cond.requireWorkSchedule = !cond.requireWorkSchedule; state.markDirty(); rebuild.run(); }
        ).bounds(rx, condBase + 20, 110, 18).build());

        add.accept(Button.builder(
                Component.literal(cond.requireFairWeather ? "§a☑§r Ясная погода" : "§7☐ Ясная погода"),
                b -> { cond.requireFairWeather = !cond.requireFairWeather; state.markDirty(); rebuild.run(); }
        ).bounds(rx + 114, condBase + 20, 120, 18).build());

        add.accept(Button.builder(
                Component.literal(cond.requireDaytime ? "§a☑§r Только день" : "§7☐ Только день"),
                b -> { cond.requireDaytime = !cond.requireDaytime; state.markDirty(); rebuild.run(); }
        ).bounds(rx, condBase + 42, 110, 18).build());

        minHpBox = new EditBox(mc.font, rx + 240, condBase + 42, 40, 18,
                Component.literal("%"));
        minHpBox.setValue(String.valueOf((int) (cond.minHealthPercent * 100)));
        minHpBox.setMaxLength(3);
        add.accept(minHpBox);
    }

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();
        NpcProfessionData pd = ensureProfession(d);
        NpcProfessionType[] types = NpcProfessionType.values();
        int profRows = (types.length + 1) / 2;

        // Profession card
        int cardH = 36 + profRows * 22 + 4;
        NpcEditorUtils.sectionCard(g, rx, oy, rw, cardH, "ПРОФЕССИЯ NPC", ACCENT);
        g.drawString(font, "§7Выберите профессию NPC:", rx + 4, oy + 12, 0xFF888877, false);

        int colW = (rw - 4) / 2;
        for (int i = 0; i < types.length; i++) {
            NpcProfessionType t = types[i];
            if (pd.type == t) {
                int col = i % 2;
                int row = i / 2;
                int bx = rx + col * (colW + 4);
                int by = oy + 38 + row * 22;
                g.fill(bx, by + 18, bx + colW, by + 19, ACCENT);
            }
        }

        int afterGrid = oy + 38 + profRows * 22 + 8;

        // Trader card
        if (pd.type == NpcProfessionType.TRADER && pd.traderData != null) {
            int traderBase = afterGrid + 8;
            NpcEditorUtils.sectionCard(g, rx, traderBase, rw, 84, "НАСТРОЙКИ ТОРГОВЦА", ACCENT);
            g.drawString(font, "§7Режим торговли:", rx + 4, traderBase + 12, 0xFF888877, false);
            TraderMode[] modes = TraderMode.values();
            int tmx = rx;
            for (TraderMode m : modes) {
                if (pd.traderData.mode == m)
                    g.fill(tmx, traderBase + 42, tmx + 120, traderBase + 43, ACCENT);
                tmx += 124;
            }
            g.drawString(font, "§7Редактор магазина:", rx + 4, traderBase + 58, 0xFF888877, false);
            afterGrid = traderBase + 84;
        }

        // [JOB-1] Schedule card
        int schedBase = afterGrid + 8;
        NpcEditorUtils.sectionCard(g, rx, schedBase, rw, 46, "РАБОЧЕЕ РАСПИСАНИЕ", ACCENT);
        g.drawString(font, "§7Часы (0-23):", rx + 4, schedBase + 8, 0xFF888877, false);
        g.drawString(font, "с", rx + 144, schedBase + 24, 0xFFAAAAAA, false);
        g.drawString(font, "по", rx + 188, schedBase + 24, 0xFFAAAAAA, false);

        // [JOB-2] Route card
        List<PatrolPoint> route = pd.workRoute;
        int maxVisible = 4;
        int listH = 44 + Math.min(route.size(), maxVisible) * 20;
        int routeBase = schedBase + 48;
        NpcEditorUtils.sectionCard(g, rx, routeBase, rw, listH, "РАБОЧИЙ МАРШРУТ", ACCENT);
        g.drawString(font, "§7Точек: " + route.size(), rx + 4, routeBase + 8, 0xFF888877, false);

        int startIdx = Math.min(routeScroll, Math.max(0, route.size() - maxVisible));
        for (int i = startIdx; i < Math.min(startIdx + maxVisible, route.size()); i++) {
            PatrolPoint pp = route.get(i);
            int rowY = routeBase + 42 + (i - startIdx) * 20;
            g.drawString(font,
                    "§7" + (i + 1) + ". X" + (int) pp.x + " Y" + (int) pp.y + " Z" + (int) pp.z,
                    rx + 4, rowY + 4, 0xFFCCCCCC, false);
        }

        // [JOB-3] Conditions card
        int condBase = routeBase + listH + 8;
        NpcEditorUtils.sectionCard(g, rx, condBase, rw, 68, "УСЛОВИЯ РАБОТЫ", ACCENT);
        g.drawString(font, "§7Ограничения активности NPC:", rx + 4, condBase + 8, 0xFF888877, false);
        g.drawString(font, "§7Мин.HP%:", rx + 198, condBase + 46, 0xFF888877, false);
    }

    @Override
    public void pullFields(NpcEditorState state) {
        NpcEntityData d = state.getDraft();
        NpcProfessionData pd = ensureProfession(d);
        WorkSchedule ws = pd.workSchedule;

        if (schedStartBox != null) {
            try { ws.startHour = Math.max(0, Math.min(23, Integer.parseInt(schedStartBox.getValue().trim()))); }
            catch (NumberFormatException ignored) {}
        }
        if (schedEndBox != null) {
            try { ws.endHour = Math.max(0, Math.min(23, Integer.parseInt(schedEndBox.getValue().trim()))); }
            catch (NumberFormatException ignored) {}
        }
        if (minHpBox != null) {
            try {
                int pct = Math.max(0, Math.min(100, Integer.parseInt(minHpBox.getValue().trim())));
                pd.jobConditions.minHealthPercent = pct / 100f;
            } catch (NumberFormatException ignored) {}
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static NpcProfessionData ensureProfession(NpcEntityData d) {
        if (d.professionData == null) d.professionData = new NpcProfessionData();
        if (d.professionData.workSchedule == null)  d.professionData.workSchedule  = new WorkSchedule();
        if (d.professionData.workRoute == null)      d.professionData.workRoute      = new java.util.ArrayList<>();
        if (d.professionData.jobConditions == null)  d.professionData.jobConditions  = new JobConditions();
        return d.professionData;
    }
}

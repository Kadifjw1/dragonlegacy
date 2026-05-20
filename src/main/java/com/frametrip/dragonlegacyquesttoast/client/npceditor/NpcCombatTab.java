package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.server.combat.BossPhase;
import com.frametrip.dragonlegacyquesttoast.server.combat.FormationType;
import com.frametrip.dragonlegacyquesttoast.server.combat.NpcAbility;
import com.frametrip.dragonlegacyquesttoast.server.combat.NpcAbilityType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

// [CMB-1..5]: Combat System tab.
public class NpcCombatTab implements NpcEditorTab {

    public static final int ACCENT = 0xFFDD4444;

    private int scrollOffset = 0;

    // CMB-1 fields
    private EditBox formationIdBox, formationSlotBox;
    // CMB-3 fields
    private EditBox reinfTypeBox, reinfCountBox, reinfHpBox, reinfCooldownBox;
    // CMB-5 fields
    private EditBox arenaRadiusBox;
    // Boss phase add fields
    private EditBox phaseHpBox, phaseSpeedBox, phaseDamBox, phaseSummonBox, phaseCountBox, phaseDialogBox;
    // Ability add fields
    private EditBox abilTypeBox, abilParamBox, abilCooldownBox, abilTriggerBox, abilSummonBox;

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        Font font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();

        int y = oy + 20 - scrollOffset;
        int bw = rw - 4;

        // ── [CMB-1] Formation ─────────────────────────────────────────────────
        label(font, add, rx, y, "§7── CMB-1: Формация ──────────────────────"); y += 11;
        formationIdBox = box(font, rx, y, bw / 2 - 2, d.formationId, "ID формации", add);
        formationSlotBox = box(font, rx + bw / 2 + 2, y, 40, String.valueOf(d.formationSlot), "Слот", add); y += 16;

        add.accept(Button.builder(
            Component.literal("§7Тип: §f" + d.formationType),
            b -> {
                FormationType[] vals = FormationType.values();
                FormationType cur = FormationType.fromName(d.formationType);
                d.formationType = vals[(cur.ordinal() + 1) % vals.length].name();
                rebuild.run();
            }
        ).bounds(rx, y, 120, 14).build()); y += 18;

        // ── [CMB-2] Boss phases ───────────────────────────────────────────────
        label(font, add, rx, y, "§7── CMB-2: Фазы босса (" + d.bossPhases.size() + ") ──────────────"); y += 11;
        phaseHpBox     = box(font, rx,       y, 30,       "50", "HP%",    add);
        phaseSpeedBox  = box(font, rx + 34,  y, 32,       "1.0","Скор.",  add);
        phaseDamBox    = box(font, rx + 70,  y, 32,       "1.0","Урон",   add);
        phaseSummonBox = box(font, rx + 106, y, bw - 190, "",   "Тип моба (RL)", add);
        phaseCountBox  = box(font, rx + bw - 80, y, 28,   "0",  "Кол.",  add);
        phaseDialogBox = box(font, rx, y + 16, bw - 20, "", "Фраза при переходе", add);
        add.accept(Button.builder(Component.literal("+"), b -> {
            BossPhase p = new BossPhase();
            p.phaseIndex = d.bossPhases.size();
            try { p.hpThreshold     = Float.parseFloat(phaseHpBox.getValue().trim()); } catch (NumberFormatException ignored) {}
            try { p.speedMultiplier = Float.parseFloat(phaseSpeedBox.getValue().trim()); } catch (NumberFormatException ignored) {}
            try { p.damageMultiplier= Float.parseFloat(phaseDamBox.getValue().trim()); } catch (NumberFormatException ignored) {}
            p.summonType  = phaseSummonBox.getValue().trim();
            try { p.summonCount = Integer.parseInt(phaseCountBox.getValue().trim()); } catch (NumberFormatException ignored) {}
            p.phaseDialog = phaseDialogBox.getValue().trim();
            d.bossPhases.add(p);
            state.markDirty();
            rebuild.run();
        }).bounds(rx + bw - 16, y, 14, 14).build());
        y += 34;

        // Show up to 3 phases.
        for (int i = 0; i < Math.min(d.bossPhases.size(), 3); i++) {
            BossPhase p = d.bossPhases.get(i);
            int fi = i;
            String row = String.format("§8P%d §7HP<§f%.0f%% §7x§fspd%.1f §7x§fdam%.1f %s",
                i, p.hpThreshold, p.speedMultiplier, p.damageMultiplier,
                p.summonType.isEmpty() ? "" : "§8[+" + p.summonCount + " " + p.summonType + "]");
            add.accept(Button.builder(Component.literal("§c✕"), b2 -> {
                d.bossPhases.remove(fi); state.markDirty(); rebuild.run();
            }).bounds(rx, y, 12, 10).build());
            // Just a label rendered in render()
            y += 12;
        }

        // ── [CMB-3] Reinforcement ──────────────────────────────────────────────
        label(font, add, rx, y, "§7── CMB-3: Подкрепление ─────────────────"); y += 11;
        add.accept(Button.builder(
            Component.literal(d.reinforcementEnabled ? "§a✔ Активно" : "§7☐ Выключено"),
            b -> { d.reinforcementEnabled = !d.reinforcementEnabled; rebuild.run(); }
        ).bounds(rx, y, 90, 14).build()); y += 16;
        reinfHpBox      = box(font, rx,       y, 30, String.valueOf(d.reinforcementHpThreshold), "HP%",  add);
        reinfCountBox   = box(font, rx + 34,  y, 28, String.valueOf(d.reinforcementCount),        "Кол.", add);
        reinfCooldownBox= box(font, rx + 66,  y, 36, String.valueOf(d.reinforcementCooldownSec),  "Сек.", add);
        reinfTypeBox    = box(font, rx + 106, y, bw - 108, d.reinforcementType, "Entity ID (RL)", add); y += 18;

        // ── [CMB-4] Abilities ─────────────────────────────────────────────────
        label(font, add, rx, y, "§7── CMB-4: Способности (" + d.combatAbilities.size() + ") ─────────────"); y += 11;
        abilTypeBox    = box(font, rx,       y, 70,      "HEAL_SELF", "Тип",    add);
        abilParamBox   = box(font, rx + 74,  y, 34,      "5",         "Парам.", add);
        abilCooldownBox= box(font, rx + 112, y, 30,      "15",        "Сек.",   add);
        abilTriggerBox = box(font, rx + 146, y, bw - 202,"HP_BELOW_50","Триггер",add);
        abilSummonBox  = box(font, rx + bw - 52, y, 50,  "",          "Тип призыва", add);
        add.accept(Button.builder(Component.literal("+"), b -> {
            NpcAbility ab = new NpcAbility();
            ab.abilityType      = abilTypeBox.getValue().trim().toUpperCase();
            ab.triggerCondition = abilTriggerBox.getValue().trim().toUpperCase();
            try { ab.param      = Float.parseFloat(abilParamBox.getValue().trim()); } catch (NumberFormatException ignored) {}
            try { ab.cooldownSec= Integer.parseInt(abilCooldownBox.getValue().trim()); } catch (NumberFormatException ignored) {}
            ab.summonType = abilSummonBox.getValue().trim();
            d.combatAbilities.add(ab);
            state.markDirty();
            rebuild.run();
        }).bounds(rx + bw - 14, y, 14, 14).build());
        y += 18;

        // Ability delete buttons for listed abilities.
        for (int i = 0; i < Math.min(d.combatAbilities.size(), 4); i++) {
            int fi = i;
            add.accept(Button.builder(Component.literal("§c✕"), b2 -> {
                d.combatAbilities.remove(fi); state.markDirty(); rebuild.run();
            }).bounds(rx, y, 12, 10).build());
            y += 12;
        }

        // ── [CMB-5] Arena ─────────────────────────────────────────────────────
        label(font, add, rx, y, "§7── CMB-5: Арена ────────────────────────"); y += 11;
        add.accept(Button.builder(
            Component.literal(d.arenaEnabled ? "§a✔ Арена вкл." : "§7☐ Арена выкл."),
            b -> { d.arenaEnabled = !d.arenaEnabled; rebuild.run(); }
        ).bounds(rx, y, 110, 14).build());
        arenaRadiusBox = box(font, rx + 114, y, 50, String.valueOf(d.arenaRadius), "Радиус", add);
        add.accept(Button.builder(Component.literal("📍 Центр"), b -> {
            // Player is in the editor screen — use the NPC's current position as the center.
            var npc = state.getEntity();
            if (npc != null) {
                d.arenaCenter = npc.getX() + "," + npc.getY() + "," + npc.getZ();
                state.markDirty();
            }
        }).bounds(rx + 168, y, 70, 14).build()); y += 16;
    }

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        Font font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();
        int so = scrollOffset;

        // Fixed top banner.
        g.fill(rx, oy + 2, rx + rw, oy + 14, 0x33DD4444);
        g.drawString(font, "§4§lБОЕВАЯ СИСТЕМА", rx + 4, oy + 4, ACCENT, false);

        int y = oy + 20 - so;
        // CMB-1 header
        g.drawString(font, "§7── CMB-1: Формация ──────────────────────", rx, y, 0xFF666666, false);
        y += 11 + 16 + 18; // after formationId/slot + type button

        // CMB-2 header
        g.drawString(font, "§7── CMB-2: Фазы босса (" + d.bossPhases.size() + ") ──────────────", rx, y, 0xFF666666, false);
        y += 11; // past header
        // Phase rows drawn over the delete buttons area.
        int phY = y + 34; // past add-form rows
        for (int i = 0; i < Math.min(d.bossPhases.size(), 3); i++) {
            BossPhase p = d.bossPhases.get(i);
            String row = String.format("  §8P%d §7HP<§f%.0f%% §8spd§fx%.1f §8dam§fx%.1f %s",
                i, p.hpThreshold, p.speedMultiplier, p.damageMultiplier,
                p.summonType.isEmpty() ? "" : "§8[+" + p.summonCount + " mobs]");
            g.drawString(font, row, rx + 14, phY + i * 12, 0xFFCCCCCC, false);
        }
        y += 34 + Math.min(d.bossPhases.size(), 3) * 12;

        // CMB-3 header
        g.drawString(font, "§7── CMB-3: Подкрепление ─────────────────", rx, y, 0xFF666666, false);
        y += 11 + 16 + 18;

        // CMB-4 header
        g.drawString(font, "§7── CMB-4: Способности (" + d.combatAbilities.size() + ") ─────────────", rx, y, 0xFF666666, false);
        y += 11 + 18;
        // Ability rows
        for (int i = 0; i < Math.min(d.combatAbilities.size(), 4); i++) {
            NpcAbility ab = d.combatAbilities.get(i);
            NpcAbilityType type = NpcAbilityType.fromName(ab.abilityType);
            String row = String.format("  §f%s §7%.0f §8cd%ds §7%s",
                type.label, ab.param, ab.cooldownSec, ab.triggerCondition);
            g.drawString(font, row, rx + 14, y + i * 12, 0xFFCCCCCC, false);
        }
        y += Math.min(d.combatAbilities.size(), 4) * 12;

        // CMB-5 header
        g.drawString(font, "§7── CMB-5: Арена ────────────────────────", rx, y, 0xFF666666, false);
        y += 11 + 16;
        if (d.arenaEnabled && !d.arenaCenter.isEmpty())
            g.drawString(font, "§7Центр: §8" + d.arenaCenter, rx + 4, y, 0xFFAAAAAA, false);
    }

    @Override
    public boolean onMouseScrolled(double mx, double my, double delta,
                                   NpcEditorState state, int rx, int oy, int rw) {
        scrollOffset = Math.max(0, scrollOffset - (int)(delta * 10));
        return true;
    }

    @Override
    public void pullFields(NpcEditorState state) {
        NpcEntityData d = state.getDraft();
        if (formationIdBox   != null) d.formationId   = formationIdBox.getValue().trim();
        if (formationSlotBox != null) { try { d.formationSlot = Integer.parseInt(formationSlotBox.getValue().trim()); } catch (NumberFormatException ignored) {} }
        if (reinfTypeBox     != null) d.reinforcementType        = reinfTypeBox.getValue().trim();
        if (reinfHpBox       != null) { try { d.reinforcementHpThreshold  = Integer.parseInt(reinfHpBox.getValue().trim()); } catch (NumberFormatException ignored) {} }
        if (reinfCountBox    != null) { try { d.reinforcementCount         = Integer.parseInt(reinfCountBox.getValue().trim()); } catch (NumberFormatException ignored) {} }
        if (reinfCooldownBox != null) { try { d.reinforcementCooldownSec   = Integer.parseInt(reinfCooldownBox.getValue().trim()); } catch (NumberFormatException ignored) {} }
        if (arenaRadiusBox   != null) { try { d.arenaRadius = Float.parseFloat(arenaRadiusBox.getValue().trim()); } catch (NumberFormatException ignored) {} }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static EditBox box(Font font, int x, int y, int w, String val, String hint,
                               Consumer<AbstractWidget> add) {
        EditBox b = new EditBox(font, x, y, w, 12, Component.literal(hint));
        b.setValue(val); b.setMaxLength(128); add.accept(b); return b;
    }

    private static void label(Font font, Consumer<AbstractWidget> add, int x, int y, String text) {
        // Labels are rendered directly; not added as widgets.
    }
}

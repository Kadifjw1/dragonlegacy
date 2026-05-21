package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.server.immersion.NpcImmersionData;
import com.frametrip.dragonlegacyquesttoast.server.immersion.NpcItemReaction;
import com.frametrip.dragonlegacyquesttoast.server.immersion.NpcScheduleEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

// [IMM-1..6]: Immersion & Living World tab.
public class NpcImmersionTab implements NpcEditorTab {

    public static final int ACCENT = 0xFF44CC88;

    // IMM-1
    private EditBox firstVisitBox, returningBox, regularBox;
    // IMM-2
    private EditBox moodGiftBox;
    // IMM-3
    private EditBox convPhraseBox;
    // IMM-4
    private EditBox schedHourBox, schedActionBox, schedTargetBox, schedDialogBox;
    // IMM-5
    private EditBox reactItemBox, reactTypeBox, reactDialogBox, reactMoodBox;
    // IMM-6
    private EditBox deathDialogBox;

    private int scrollOffset = 0;

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        Font font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();
        NpcImmersionData imm = ensureImm(d);

        int y = oy + 16 - scrollOffset;
        int bw = rw - 4;

        // ── [IMM-1] Player memory ─────────────────────────────────────────────
        Button remBtn = Button.builder(
                Component.literal(imm.rememberPlayers ? "§a✔ Помнить игроков" : "§7☐ Помнить игроков"),
                b -> { imm.rememberPlayers = !imm.rememberPlayers; rebuild.run(); }
        ).bounds(rx, y, 180, 14).build(); add.accept(remBtn); y += 16;

        firstVisitBox = box(font, rx, y, bw, imm.firstVisitDialog, "Диалог первого визита", add); y += 16;
        returningBox  = box(font, rx, y, bw, imm.returningDialog,   "Диалог возвращения",    add); y += 16;
        regularBox    = box(font, rx, y, bw, imm.regularDialog,      "Обычный диалог",        add); y += 18;

        // ── [IMM-2] Mood ──────────────────────────────────────────────────────
        Button moodBtn = Button.builder(
                Component.literal(imm.moodEnabled ? "§a✔ Настроение" : "§7☐ Настроение"),
                b -> { imm.moodEnabled = !imm.moodEnabled; rebuild.run(); }
        ).bounds(rx, y, 130, 14).build(); add.accept(moodBtn);

        Button iconBtn = Button.builder(
                Component.literal(imm.showMoodIcon ? "§a✔ Иконка" : "§7☐ Иконка"),
                b -> { imm.showMoodIcon = !imm.showMoodIcon; rebuild.run(); }
        ).bounds(rx + 135, y, 100, 14).build(); add.accept(iconBtn); y += 16;

        moodGiftBox = box(font, rx, y, bw, "", "ID предмета-подарка (Enter=добавить)", add);
        moodGiftBox.setResponder(v -> {});
        add.accept(Button.builder(Component.literal("+"), b -> {
            String v = moodGiftBox.getValue().trim();
            if (!v.isEmpty() && !imm.moodGiftItems.contains(v)) { imm.moodGiftItems.add(v); moodGiftBox.setValue(""); }
        }).bounds(rx + bw + 2, y, 14, 14).build());
        y += 18;

        // ── [IMM-3] Conversations ─────────────────────────────────────────────
        Button convBtn = Button.builder(
                Component.literal(imm.selfConvEnabled ? "§a✔ Разговоры NPC" : "§7☐ Разговоры NPC"),
                b -> { imm.selfConvEnabled = !imm.selfConvEnabled; rebuild.run(); }
        ).bounds(rx, y, 180, 14).build(); add.accept(convBtn); y += 16;

        convPhraseBox = box(font, rx, y, bw, "", "Фраза (Enter=добавить)", add);
        add.accept(Button.builder(Component.literal("+"), b -> {
            String v = convPhraseBox.getValue().trim();
            if (!v.isEmpty()) { imm.selfConvPhrases.add(v); convPhraseBox.setValue(""); }
        }).bounds(rx + bw + 2, y, 14, 14).build());
        y += 18;

        // ── [IMM-4] Schedule ──────────────────────────────────────────────────
        schedHourBox   = box(font, rx,       y, 30,  "8",      "Час",    add);
        schedActionBox = box(font, rx + 34,  y, 60,  "IDLE",   "Действие", add);
        schedTargetBox = box(font, rx + 98,  y, 80,  "",       "Цель",   add);
        schedDialogBox = box(font, rx + 182, y, bw - 182, "", "Диалог", add);
        add.accept(Button.builder(Component.literal("+"), b -> {
            NpcScheduleEvent ev = new NpcScheduleEvent();
            try { ev.hour = Integer.parseInt(schedHourBox.getValue().trim()); } catch (NumberFormatException e) {}
            ev.action = schedActionBox.getValue().trim().toUpperCase();
            ev.target = schedTargetBox.getValue().trim();
            ev.dialog = schedDialogBox.getValue().trim();
            imm.dailySchedule.add(ev);
        }).bounds(rx + bw + 2, y, 14, 14).build());
        y += 18;

        // ── [IMM-5] Item reactions ────────────────────────────────────────────
        reactItemBox   = box(font, rx,      y, 80, "", "ID предмета", add);
        reactTypeBox   = box(font, rx + 84, y, 60, "DIALOG", "FEAR/INTEREST/AGGRO/DIALOG", add);
        reactDialogBox = box(font, rx + 148, y, bw - 178, "", "Диалог", add);
        reactMoodBox   = box(font, rx + bw - 26, y, 28, "0", "Настр.", add);
        add.accept(Button.builder(Component.literal("+"), b -> {
            NpcItemReaction r = new NpcItemReaction();
            r.itemId       = reactItemBox.getValue().trim();
            r.reactionType = reactTypeBox.getValue().trim().toUpperCase();
            r.dialog       = reactDialogBox.getValue().trim();
            try { r.moodChange = Integer.parseInt(reactMoodBox.getValue().trim()); } catch (NumberFormatException e) {}
            if (!r.itemId.isEmpty()) imm.itemReactions.add(r);
        }).bounds(rx + bw + 2, y, 14, 14).build());
        y += 18;

        // ── [IMM-6] Death memory ──────────────────────────────────────────────
        Button deathBtn = Button.builder(
                Component.literal(imm.rememberDeath ? "§a✔ Помнить убийство" : "§7☐ Помнить убийство"),
                b -> { imm.rememberDeath = !imm.rememberDeath; rebuild.run(); }
        ).bounds(rx, y, 180, 14).build(); add.accept(deathBtn); y += 16;

        deathDialogBox = box(font, rx, y, bw, imm.deathReactionDialog, "Диалог при виде убийцы", add);
    }

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        Font font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();
        NpcImmersionData imm = ensureImm(d);

        int y = oy + 2;
        g.fill(rx, y, rx + rw, y + 12, 0x3344CC88);
        g.drawString(font, "§2§lИММЕРСИЯ И ЖИВОЙ МИР", rx + 4, y + 2, 0xFF44CC88, false);
        y += 14;

        g.drawString(font, "§7— Память, настроение, расписание, реакции —", rx + 4, y, 0xFF888888, false);

        // Mood value indicator
        if (imm.moodEnabled) {
            int m = imm.clampedMood();
            int moodColor = m > 0 ? 0xFF44EE55 : m < 0 ? 0xFFEE4444 : 0xFFAAAAAA;
            String icon = m > 30 ? "😊" : m < -30 ? "😠" : "😐";
            g.drawString(font, icon + " " + m, rx + rw - 60, oy + 32, moodColor, false);
        }

        // Gift item list
        int listY = oy + 100 - scrollOffset;
        if (!imm.moodGiftItems.isEmpty()) {
            g.drawString(font, "§7Подарки:", rx + 4, listY, 0xFFAAAAAA, false);
            for (int i = 0; i < imm.moodGiftItems.size(); i++) {
                g.drawString(font, "§8• " + imm.moodGiftItems.get(i), rx + 4, listY + 10 + i * 10, 0xFFCCCCCC, false);
            }
        }

        // Conversation phrase list
        int convY = oy + 140 - scrollOffset;
        if (!imm.selfConvPhrases.isEmpty()) {
            g.drawString(font, "§7Фразы разговоров:", rx + 4, convY, 0xFFAAAAAA, false);
            for (int i = 0; i < Math.min(imm.selfConvPhrases.size(), 3); i++) {
                String phrase = imm.selfConvPhrases.get(i);
                if (phrase.length() > 40) phrase = phrase.substring(0, 39) + "…";
                g.drawString(font, "§8• " + phrase, rx + 4, convY + 10 + i * 10, 0xFFCCCCCC, false);
            }
        }

        // Schedule count
        g.drawString(font, "§7Расписание: §f" + imm.dailySchedule.size() + " событий",
                rx + 4, oy + 200 - scrollOffset, 0xFFCCCCCC, false);
        // Reactions count
        g.drawString(font, "§7Реакции: §f" + imm.itemReactions.size(),
                rx + rw / 2, oy + 200 - scrollOffset, 0xFFCCCCCC, false);
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
        NpcImmersionData imm = ensureImm(d);
        if (firstVisitBox != null) imm.firstVisitDialog  = firstVisitBox.getValue();
        if (returningBox  != null) imm.returningDialog   = returningBox.getValue();
        if (regularBox    != null) imm.regularDialog     = regularBox.getValue();
        if (deathDialogBox != null) imm.deathReactionDialog = deathDialogBox.getValue();
    }

    private NpcImmersionData ensureImm(NpcEntityData d) {
        if (d.immersionData == null) d.immersionData = new NpcImmersionData();
        return d.immersionData;
    }

    private EditBox box(Font font, int x, int y, int w, String val, String hint,
                        Consumer<AbstractWidget> add) {
        EditBox b = new EditBox(font, x, y, w, 14, Component.literal(hint));
        b.setValue(val); b.setMaxLength(128); add.accept(b); return b;
    }
}

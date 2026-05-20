package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/** [SRV-1..4]: Server-side settings tab. */
public class NpcServerTab implements NpcEditorTab {

    public static final int ACCENT = 0xFF5599DD;

    private static final String[] PERM_LABELS = { "Все OPы", "Только создатель", "Группа" };

    private EditBox editGroupBox;
    private EditBox regionLockBox;
    private EditBox minRepBox;

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        var mc = Minecraft.getInstance();
        NpcEntityData d = state.getDraft();
        int y = oy + 22;

        // ── SRV-1: Разрешения ─────────────────────────────────────────────────
        add.accept(Button.builder(
            Component.literal("§7Доступ: §f" + PERM_LABELS[d.editPermission & 0xFF]),
            b -> {
                d.editPermission = (byte) ((d.editPermission + 1) % PERM_LABELS.length);
                state.markDirty();
                rebuild.run();
            }
        ).bounds(rx, y, rw - 4, 14).build()); y += 18;

        editGroupBox = new EditBox(mc.font, rx, y, rw - 4, 14, Component.literal("Группа"));
        editGroupBox.setHint(Component.literal("Имена через запятую (или ops)"));
        editGroupBox.setValue(d.editGroup != null ? d.editGroup : "");
        add.accept(editGroupBox); y += 18;

        add.accept(Button.builder(
            Component.literal("👤 Установить меня создателем"),
            b -> {
                if (mc.player != null) {
                    d.creatorUUID = mc.player.getStringUUID();
                    state.markDirty();
                }
            }
        ).bounds(rx, y, rw - 4, 14).build()); y += 22;

        // ── SRV-2: Регион ─────────────────────────────────────────────────────
        regionLockBox = new EditBox(mc.font, rx, y, rw - 4, 14, Component.literal("Регион"));
        regionLockBox.setHint(Component.literal("WorldGuard регион (пусто = нет)"));
        regionLockBox.setValue(d.regionLock != null ? d.regionLock : "");
        add.accept(regionLockBox); y += 22;

        // ── SRV-3: Взаимодействие ─────────────────────────────────────────────
        add.accept(Button.builder(
            Component.literal(d.ignoreBannedPlayers ? "§a✔ Игнор. забан. игроков" : "§7☐ Разр. забан. игроков"),
            b -> { d.ignoreBannedPlayers = !d.ignoreBannedPlayers; state.markDirty(); rebuild.run(); }
        ).bounds(rx, y, rw - 4, 14).build()); y += 18;

        minRepBox = new EditBox(mc.font, rx, y, 80, 14, Component.literal("Мин. репутация"));
        minRepBox.setHint(Component.literal("-1000 = выкл."));
        minRepBox.setValue(String.valueOf(d.minReputationToInteract));
        add.accept(minRepBox);
    }

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();

        NpcEditorUtils.sectionCard(g, rx, oy, rw, 18, "СЕРВЕРНЫЕ НАСТРОЙКИ", ACCENT);

        int y = oy + 22;
        // SRV-1 labels
        g.drawString(font, "§7SRV-1: §fРазрешения на редактирование", rx + 2, y - 10, 0xFF5599DD, false);
        g.drawString(font, "§8UUID создателя: §7" + truncate(d.creatorUUID, 20), rx + 2, y + 35, 0xFF555566, false);

        int y2 = oy + 22 + 18 + 18 + 22;
        g.drawString(font, "§7SRV-2: §fРегион WorldGuard", rx + 2, y2 - 10, 0xFF5599DD, false);

        int y3 = y2 + 22;
        g.drawString(font, "§7SRV-3: §fАнти-спам взаимодействий", rx + 2, y3 - 10, 0xFF5599DD, false);
        g.drawString(font, "§8Мин. репутация (-1000 = выкл.):", rx + 84, y3 + 35, 0xFF555566, false);

        int y4 = y3 + 18 + 18 + 14;
        g.drawString(font, "§7SRV-4: §fЛоги", rx + 2, y4 - 2, 0xFF5599DD, false);
        g.drawString(font, "§8Логи: /npclogs <имя> [N]", rx + 2, y4 + 10, 0xFF777777, false);
    }

    @Override
    public void pullFields(NpcEditorState state) {
        NpcEntityData d = state.getDraft();
        if (editGroupBox != null) d.editGroup = editGroupBox.getValue();
        if (regionLockBox != null) d.regionLock = regionLockBox.getValue();
        if (minRepBox != null) {
            try { d.minReputationToInteract = Integer.parseInt(minRepBox.getValue().trim()); }
            catch (NumberFormatException ignored) {}
        }
    }

    private static String truncate(String s, int max) {
        if (s == null || s.isEmpty()) return "§8—";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}

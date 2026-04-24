package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/** Tab 2 — Анимация: поза, скорость ходьбы, позы рук, поворот тела. */
public class NpcAnimationTab implements NpcEditorTab {

    public static final int ACCENT = 0xFFFF8844;
    
    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        NpcEntityData d = state.getDraft();

        // ── Pose ─────────────────────────────────────────────────────────────
        add.accept(btn("◀", rx, oy + 30, 20, 16, b -> {
            int i = indexOf(NpcEntityData.IDLE_POSES, d.idlePose);
            d.idlePose = NpcEntityData.IDLE_POSES[Math.floorMod(i - 1, NpcEntityData.IDLE_POSES.length)];
            state.markDirty(); rebuild.run();
        }));
        add.accept(btn("▶", rx + 106, oy + 30, 20, 16, b -> {
            int i = indexOf(NpcEntityData.IDLE_POSES, d.idlePose);
            d.idlePose = NpcEntityData.IDLE_POSES[Math.floorMod(i + 1, NpcEntityData.IDLE_POSES.length)];
            state.markDirty(); rebuild.run();
        }));

        // ── Walk speed ────────────────────────────────────────────────────────
        add.accept(btn("◀", rx, oy + 56, 20, 16, b -> {
            d.walkSpeed = Math.max(0.0f, Math.round((d.walkSpeed - 0.1f) * 10) / 10.0f);
            state.markDirty(); rebuild.run();
        }));
        add.accept(btn("▶", rx + 106, oy + 56, 20, 16, b -> {
            d.walkSpeed = Math.min(1.0f, Math.round((d.walkSpeed + 0.1f) * 10) / 10.0f);
            state.markDirty(); rebuild.run();
        }));

        // ── Look at player ────────────────────────────────────────────────────
        add.accept(btn("Смотреть на игрока: " + (d.lookAtPlayer ? "§aВКЛ§r" : "§cВЫКЛ§r"),
                rx, oy + 82, rw, 18, b -> {
            d.lookAtPlayer = !d.lookAtPlayer;
            state.markDirty(); rebuild.run();
        }));

        // ── Right arm ────────────────────────────────────────────────────────
        add.accept(btn("◀", rx, oy + 128, 20, 16, b -> {
            int i = indexOf(NpcEntityData.ARM_POSES, d.rightArmPose);
            d.rightArmPose = NpcEntityData.ARM_POSES[Math.floorMod(i - 1, NpcEntityData.ARM_POSES.length)];
            state.markDirty(); rebuild.run();
        }));
        add.accept(btn("▶", rx + 136, oy + 128, 20, 16, b -> {
            int i = indexOf(NpcEntityData.ARM_POSES, d.rightArmPose);
            d.rightArmPose = NpcEntityData.ARM_POSES[Math.floorMod(i + 1, NpcEntityData.ARM_POSES.length)];
            state.markDirty(); rebuild.run();
        }));

        // ── Left arm ─────────────────────────────────────────────────────────
        add.accept(btn("◀", rx, oy + 158, 20, 16, b -> {
            int i = indexOf(NpcEntityData.ARM_POSES, d.leftArmPose);
            d.leftArmPose = NpcEntityData.ARM_POSES[Math.floorMod(i - 1, NpcEntityData.ARM_POSES.length)];
            state.markDirty(); rebuild.run();
        }));
        add.accept(btn("▶", rx + 136, oy + 158, 20, 16, b -> {
            int i = indexOf(NpcEntityData.ARM_POSES, d.leftArmPose);
            d.leftArmPose = NpcEntityData.ARM_POSES[Math.floorMod(i + 1, NpcEntityData.ARM_POSES.length)];
            state.markDirty(); rebuild.run();
        }));

        // ── Lock body rotation ────────────────────────────────────────────────
        add.accept(btn("Фикс. поворот тела: " + (d.lockBodyRotation ? "§aВКЛ§r" : "§cВЫКЛ§r"),
                rx, oy + 192, rw, 18, b -> {
            d.lockBodyRotation = !d.lockBodyRotation;
            state.markDirty(); rebuild.run();
        }));

        if (d.lockBodyRotation) {
            add.accept(btn("◀ -15°", rx, oy + 216, 48, 16, b -> {
                d.bodyYaw = (d.bodyYaw - 15 + 360) % 360;
                state.markDirty(); rebuild.run();
            }));
            add.accept(btn("+15° ▶", rx + 52, oy + 216, 48, 16, b -> {
                d.bodyYaw = (d.bodyYaw + 15) % 360;
                state.markDirty(); rebuild.run();
            }));
        }
    }

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();

        // Pose + movement card
        NpcInfoTab.sectionCard(g, rx, oy, rw, 100, "ПОЗА И ДВИЖЕНИЕ");
        g.drawString(font, "§7Поза:", rx + 4, oy + 14, 0xFF888877, false);
        g.drawCenteredString(font, "§f" + NpcEntityData.idlePoseLabel(d.idlePose),
                rx + 22 + 40, oy + 33, 0xFFCCCCCC);

        g.drawString(font, "§7Скорость ходьбы:", rx + 4, oy + 42, 0xFF888877, false);
        g.drawCenteredString(font, "§f" + String.format("%.1f", d.walkSpeed),
                rx + 22 + 40, oy + 59, 0xFFCCCCCC);

        // Visual speed bar
        int barX = rx + 4, barY = oy + 72, barW = rw - 8, barH = 4;
        g.fill(barX, barY, barX + barW, barY + barH, 0xFF222233);
        g.fill(barX, barY, barX + (int)(barW * d.walkSpeed), barY + barH, ACCENT);

        // Arms card
        NpcInfoTab.sectionCard(g, rx, oy + 110, rw, 78, "ПОЗЫ РУК");
        g.drawString(font, "§7Правая рука:", rx + 4, oy + 122, 0xFF888877, false);
        g.drawCenteredString(font, "§f" + NpcEntityData.armPoseLabel(d.rightArmPose),
                rx + 22 + 56, oy + 131, 0xFFCCCCCC);

        g.drawString(font, "§7Левая рука:", rx + 4, oy + 152, 0xFF888877, false);
        g.drawCenteredString(font, "§f" + NpcEntityData.armPoseLabel(d.leftArmPose),
                rx + 22 + 56, oy + 161, 0xFFCCCCCC);

        // Body rotation card
        int rotH = d.lockBodyRotation ? 58 : 32;
        NpcInfoTab.sectionCard(g, rx, oy + 190, rw, rotH, "ПОВОРОТ ТЕЛА");
        if (d.lockBodyRotation) {
            g.drawString(font, "§7Угол: §f" + (int) d.bodyYaw + "°", rx + 4, oy + 204, 0xFF888877, false);
            // Compass widget
            int cx = rx + rw - 28, cy = oy + 218;
            g.fill(cx - 14, cy - 14, cx + 14, cy + 14, 0xFF111120);
            NpcEditorUtils.brd(g, cx - 14, cy - 14, 28, 28, 0xFF333344);
            double rad = Math.toRadians(d.bodyYaw);
            int ex = cx + (int)(Math.sin(rad) * 10);
            int ey = cy - (int)(Math.cos(rad) * 10);
            g.fill(cx - 1, cy - 1, cx + 1, cy + 1, 0xFF666677);
            g.fill(ex - 1, ey - 1, ex + 1, ey + 1, 0xFFFFCC44);
        }
    }

    private static Button btn(String label, int x, int y, int w, int h, Button.OnPress press) {
        return Button.builder(Component.literal(label), press).bounds(x, y, w, h).build();
    }

    private static int indexOf(String[] arr, String val) {
        for (int i = 0; i < arr.length; i++) if (arr[i].equals(val)) return i;
        return 0;
    }
}

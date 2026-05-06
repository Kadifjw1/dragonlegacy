package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.server.animation.AnimationBone;
import com.frametrip.dragonlegacyquesttoast.server.animation.AnimationKeyframe;
import com.frametrip.dragonlegacyquesttoast.server.animation.AnimationState;
import com.frametrip.dragonlegacyquesttoast.server.animation.NpcAnimationData;
import com.frametrip.dragonlegacyquesttoast.server.animation.NpcAnimationLibrary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * Полноценная вкладка редактора анимаций NPC.
 * Layout:
 *   [Слева: список анимаций]  [Центр: timeline]  [Справа: bone + keyframe свойства]
 */
public class NpcAnimationEditorTab implements NpcEditorTab {

    public static final int ACCENT = 0xFFFF6644;

    // ── State ─────────────────────────────────────────────────────────────────
    private NpcAnimationData selectedAnim   = null;
    private String selectedBone             = AnimationBone.HEAD;
    private int    selectedKeyframeIdx      = -1;
    private int    animScroll               = 0;
    private float  playheadTick             = 0f;
    private boolean playing                 = false;
    private long   playStartMs              = 0;
    private float  timelineScale            = 10f; // pixels per tick
    private String channelMode              = "rotation"; // rotation / position

    private EditBox nameBox, durationBox, kfTickBox, kfXBox, kfYBox, kfZBox;

    // ── NpcEditorTab ─────────────────────────────────────────────────────────

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        var mc = Minecraft.getInstance();
        int y = oy + 12;

        // ── Animation list (left ~100px wide column) ──────────────────────────
        int listW  = 100;
        int listX  = rx;
        int centerX = rx + listW + 4;
        int centerW = rw - listW - 4 - 120;
        int rightX  = centerX + centerW + 4;

        // New animation button
        add.accept(Button.builder(Component.literal("+ Новая"), b -> {
            NpcAnimationData anim = new NpcAnimationData();
            anim.name = "Анимация " + (NpcAnimationLibrary.size() + 1);
            anim.ensureBones();
            NpcAnimationLibrary.register(anim);
            selectedAnim = anim;
            selectedKeyframeIdx = -1;
            rebuild.run();
        }).bounds(listX, y, listW, 16).build());
        y += 20;

        List<NpcAnimationData> allAnims = NpcAnimationLibrary.getAll();
        int visAnims  = 8;
        int maxScroll = Math.max(0, allAnims.size() - visAnims);
        animScroll    = Math.max(0, Math.min(animScroll, maxScroll));

        for (int i = animScroll; i < Math.min(allAnims.size(), animScroll + visAnims); i++) {
            NpcAnimationData anim = allAnims.get(i);
            boolean sel = anim == selectedAnim;
            add.accept(Button.builder(
                    Component.literal(sel ? "§e▶ " + truncate(anim.name, 9) : "  " + truncate(anim.name, 10)),
                    b -> { selectedAnim = anim; selectedKeyframeIdx = -1; rebuild.run(); }
            ).bounds(listX, y + (i - animScroll) * 18, listW, 16).build());
        }

        if (selectedAnim == null) return;

  // ── Top controls ──────────────────────────────────────────────────────
        int ctrlY = oy + 12;

        nameBox = new EditBox(mc.font, centerX, ctrlY, 90, 16, Component.literal("Имя"));
        nameBox.setValue(selectedAnim.name);
        add.accept(nameBox);

        durationBox = new EditBox(mc.font, centerX + 96, ctrlY, 40, 16, Component.literal("Длит."));
        durationBox.setValue(String.format("%.0f", selectedAnim.durationTicks));
        add.accept(durationBox);

        // Loop toggle
        add.accept(Button.builder(
                Component.literal(selectedAnim.loop ? "§aЦикл" : "§8Цикл"),
                b -> { selectedAnim.loop = !selectedAnim.loop; rebuild.run(); }
        ).bounds(centerX + 142, ctrlY, 40, 16).build());

        // State binding
        AnimationState[] states = AnimationState.values();
        add.accept(Button.builder(
                Component.literal("§7" + selectedAnim.stateBinding.label()),
                b -> {
                    int si = selectedAnim.stateBinding.ordinal();
                    selectedAnim.stateBinding = states[(si + 1) % states.length];
                    rebuild.run();
                }
        ).bounds(centerX + 186, ctrlY, 70, 16).build());

        ctrlY += 22;

        // ── Playback controls ─────────────────────────────────────────────────
        add.accept(Button.builder(Component.literal("▶"), b -> {
            playing = true; playStartMs = System.currentTimeMillis();
            playheadTick = 0; rebuild.run();
        }).bounds(centerX, ctrlY, 22, 16).build());

        add.accept(Button.builder(Component.literal("⏸"), b -> {
            playing = false; rebuild.run();
        }).bounds(centerX + 26, ctrlY, 22, 16).build());

        add.accept(Button.builder(Component.literal("■"), b -> {
            playing = false; playheadTick = 0; rebuild.run();
        }).bounds(centerX + 52, ctrlY, 22, 16).build());

        // Timeline zoom
        add.accept(Button.builder(Component.literal("−"), b -> {
            timelineScale = Math.max(2f, timelineScale - 2);
        }).bounds(centerX + 82, ctrlY, 18, 16).build());
        add.accept(Button.builder(Component.literal("+"), b -> {
            timelineScale = Math.min(30f, timelineScale + 2);
        }).bounds(centerX + 104, ctrlY, 18, 16).build());

        // Channel mode toggle
        add.accept(Button.builder(
                Component.literal("§7" + ("rotation".equals(channelMode) ? "§eВращение" : "Позиция")),
                b -> { channelMode = "rotation".equals(channelMode) ? "position" : "rotation"; rebuild.run(); }
        ).bounds(centerX + 128, ctrlY, 80, 16).build());

        ctrlY += 22;

        // ── Bone selector ─────────────────────────────────────────────────────
        int bw = centerW / AnimationBone.BONE_IDS.length;
        for (int i = 0; i < AnimationBone.BONE_IDS.length; i++) {
            final String boneId = AnimationBone.BONE_IDS[i];
            boolean sel = boneId.equals(selectedBone);
            add.accept(Button.builder(
                    Component.literal(sel ? "§e§l" + AnimationBone.BONE_LABELS[i] : AnimationBone.BONE_LABELS[i]),
                    b -> { selectedBone = boneId; selectedKeyframeIdx = -1; rebuild.run(); }
            ).bounds(centerX + i * bw, ctrlY, bw - 2, 14).build());
        }
        ctrlY += 20;

        // ── Add/remove keyframe buttons ───────────────────────────────────────
        add.accept(Button.builder(Component.literal("+ Кадр"), b -> {
            AnimationBone bone = selectedAnim.getBone(selectedBone);
            List<AnimationKeyframe> frames = "rotation".equals(channelMode)
                    ? bone.rotationFrames : bone.positionFrames;
            AnimationKeyframe kf = new AnimationKeyframe(playheadTick, 0, 0, 0);
            frames.add(kf);
            frames.sort((a, bk) -> Float.compare(a.tick, bk.tick));
            selectedKeyframeIdx = frames.indexOf(kf);
            rebuild.run();
        }).bounds(centerX, ctrlY, 55, 14).build());

        add.accept(Button.builder(Component.literal("✕ Удалить"), b -> {
            if (selectedKeyframeIdx >= 0) {
                AnimationBone bone = selectedAnim.getBone(selectedBone);
                List<AnimationKeyframe> frames = "rotation".equals(channelMode)
                        ? bone.rotationFrames : bone.positionFrames;
                if (selectedKeyframeIdx < frames.size()) {
                    frames.remove(selectedKeyframeIdx);
                    selectedKeyframeIdx = Math.max(-1, selectedKeyframeIdx - 1);
                    rebuild.run();
                }
            }
        }).bounds(centerX + 60, ctrlY, 70, 14).build());
        ctrlY += 20;

  // ── Right panel: keyframe properties ─────────────────────────────────
        if (selectedKeyframeIdx >= 0) {
            AnimationBone bone = selectedAnim.getBone(selectedBone);
            List<AnimationKeyframe> frames = "rotation".equals(channelMode)
                    ? bone.rotationFrames : bone.positionFrames;
            if (selectedKeyframeIdx < frames.size()) {
                AnimationKeyframe kf = frames.get(selectedKeyframeIdx);

                kfTickBox = new EditBox(mc.font, rightX, oy + 36, 50, 14, Component.literal("Тик"));
                kfTickBox.setValue(String.format("%.1f", kf.tick));
                add.accept(kfTickBox);

                kfXBox = new EditBox(mc.font, rightX, oy + 56, 50, 14, Component.literal("X"));
                kfXBox.setValue(String.format("%.2f", kf.x));
                add.accept(kfXBox);

                kfYBox = new EditBox(mc.font, rightX, oy + 76, 50, 14, Component.literal("Y"));
                kfYBox.setValue(String.format("%.2f", kf.y));
                add.accept(kfYBox);

                kfZBox = new EditBox(mc.font, rightX, oy + 96, 50, 14, Component.literal("Z"));
                kfZBox.setValue(String.format("%.2f", kf.z));
                add.accept(kfZBox);

                // Easing selector
                String[] easings = AnimationKeyframe.EASING_IDS;
                add.accept(Button.builder(
                        Component.literal("§7" + kf.easing),
                        b -> {
                            int ei = 0;
                            for (int ii = 0; ii < easings.length; ii++) if (easings[ii].equals(kf.easing)) ei = ii;
                            kf.easing = easings[(ei + 1) % easings.length];
                            rebuild.run();
                        }
                ).bounds(rightX, oy + 116, 80, 14).build());
            }
        }

        // Export button
        add.accept(Button.builder(Component.literal("↓ GeckoLib JSON"), b -> {
            // In a real implementation, this would open a file dialog or clipboard
            String json = selectedAnim.toGeckoLibJson();
            Minecraft.getInstance().keyboardHandler.setClipboard(json);
        }).bounds(rightX, oy + 145, 112, 14).build());

        // Import button
        add.accept(Button.builder(Component.literal("↑ Импорт JSON"), b -> {
            // Stub: user would paste JSON
        }).bounds(rightX, oy + 163, 112, 14).build());
    }

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;

        g.fill(rx, oy, rx + rw, oy + 18, 0x55000000);
        g.drawString(font, "§l⏵ РЕДАКТОР АНИМАЦИЙ", rx + 4, oy + 4, ACCENT, false);

        if (selectedAnim == null) {
            g.drawString(font, "§8Выберите или создайте анимацию слева.", rx + 4, oy + 30, 0xFF555566, false);
            return;
        }

        int listW   = 100;
        int centerX = rx + listW + 4;
        int centerW = rw - listW - 4 - 120;
        int ctrlAreaH = 78; // height used by controls above timeline

        // ── Timeline area ─────────────────────────────────────────────────────
        int tlX = centerX;
        int tlY = oy + ctrlAreaH;
        int tlW = centerW;
        int tlH = 80;

        g.fill(tlX, tlY, tlX + tlW, tlY + tlH, 0xFF0A0A18);
        NpcEditorUtils.brd(g, tlX, tlY, tlW, tlH, 0xFF222233);

        // Timeline ruler
        float dur = selectedAnim.durationTicks;
        int tickMarkEvery = 5; // draw a mark every 5 ticks
        for (int t = 0; t <= (int) dur; t += tickMarkEvery) {
            int tx = tlX + (int)(t * timelineScale);
            if (tx > tlX + tlW) break;
            g.fill(tx, tlY, tx + 1, tlY + 6, 0xFF333355);
            if (t % 20 == 0) {
                g.drawString(font, String.valueOf(t), tx + 1, tlY + 1, 0xFF666677, false);
            }
        }

  // Keyframes for current bone/channel
        if (selectedAnim != null) {
            AnimationBone bone = selectedAnim.getBone(selectedBone);
            List<AnimationKeyframe> frames = "rotation".equals(channelMode)
                    ? bone.rotationFrames : bone.positionFrames;
            int boneRow = tlY + 14;
            for (int i = 0; i < frames.size(); i++) {
                AnimationKeyframe kf = frames.get(i);
                int kx = tlX + (int)(kf.tick * timelineScale);
                boolean sel = i == selectedKeyframeIdx;
                g.fill(kx - 3, boneRow, kx + 3, boneRow + 12, sel ? 0xFFFFCC44 : ACCENT);
            }
        }

        // Playhead
        if (playing) {
            long elapsed = System.currentTimeMillis() - playStartMs;
            playheadTick = (elapsed / 50f); // 50ms per tick
            if (selectedAnim.loop) {
                playheadTick %= selectedAnim.durationTicks;
            } else if (playheadTick > selectedAnim.durationTicks) {
                playheadTick = selectedAnim.durationTicks;
                playing = false;
            }
        }
        int phX = tlX + (int)(playheadTick * timelineScale);
        g.fill(phX, tlY, phX + 1, tlY + tlH, 0xFFFF4444);

        // ── Labels ────────────────────────────────────────────────────────────
        g.drawString(font, "§7Анимация: §f" + selectedAnim.name
                + "  §7Длит: §f" + (int)selectedAnim.durationTicks + " тиков"
                + "  §7" + (selectedAnim.loop ? "§aЦикл" : "§8Не цикл"),
                centerX, oy + 2, 0xFFCCCCCC, false);

        g.drawString(font, "§7Кость: §e" + AnimationBone.boneLabel(selectedBone)
                + "  §7Канал: §f" + ("rotation".equals(channelMode) ? "Вращение" : "Позиция"),
                centerX, tlY - 12, 0xFF888899, false);

        // Keyframe property labels (right panel)
        int rightX = centerX + centerW + 4;
        if (selectedKeyframeIdx >= 0) {
            g.drawString(font, "§7Кадр §e#" + (selectedKeyframeIdx + 1), rightX, oy + 24, 0xFF888899, false);
            g.drawString(font, "§7Тик:", rightX, oy + 38, 0xFF888877, false);
            g.drawString(font, "§7X:", rightX, oy + 58, 0xFF888877, false);
            g.drawString(font, "§7Y:", rightX, oy + 78, 0xFF888877, false);
            g.drawString(font, "§7Z:", rightX, oy + 98, 0xFF888877, false);
            g.drawString(font, "§7Интерп.:", rightX, oy + 108, 0xFF888877, false);
        } else {
            g.drawString(font, "§8Выберите кадр", rightX, oy + 32, 0xFF444455, false);
        }
        g.drawString(font, "§8── Экспорт ──", rightX, oy + 137, 0xFF444455, false);
    }

    @Override
    public void pullFields(NpcEditorState state) {
        if (selectedAnim == null) return;
        if (nameBox != null)     selectedAnim.name = nameBox.getValue();
        if (durationBox != null) {
            try { selectedAnim.durationTicks = Float.parseFloat(durationBox.getValue()); }
            catch (Exception ignored) {}
        }

        if (selectedKeyframeIdx >= 0) {
            AnimationBone bone = selectedAnim.getBone(selectedBone);
            List<AnimationKeyframe> frames = "rotation".equals(channelMode)
                    ? bone.rotationFrames : bone.positionFrames;
            if (selectedKeyframeIdx < frames.size()) {
                AnimationKeyframe kf = frames.get(selectedKeyframeIdx);
                if (kfTickBox != null) try { kf.tick = Float.parseFloat(kfTickBox.getValue()); } catch (Exception ignored) {}
                if (kfXBox != null)    try { kf.x    = Float.parseFloat(kfXBox.getValue()); }    catch (Exception ignored) {}
                if (kfYBox != null)    try { kf.y    = Float.parseFloat(kfYBox.getValue()); }    catch (Exception ignored) {}
                if (kfZBox != null)    try { kf.z    = Float.parseFloat(kfZBox.getValue()); }    catch (Exception ignored) {}
            }
        }
    }

    @Override
    public boolean onMouseScrolled(double mx, double my, double delta,
                                   NpcEditorState state, int rx, int oy, int rw) {
        int maxScroll = Math.max(0, NpcAnimationLibrary.size() - 8);
        animScroll = Math.max(0, Math.min(maxScroll, animScroll - (int) Math.signum(delta)));
        return true;
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}

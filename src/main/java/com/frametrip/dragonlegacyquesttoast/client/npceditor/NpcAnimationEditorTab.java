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
 * Animation editor tab.
 *
 * Layout (left→right):
 *   [90px list] | [timeline + controls] | [110px keyframe props]
 *
 * All controls are assigned non-overlapping Y coordinates.
 */
public class NpcAnimationEditorTab implements NpcEditorTab {

    public static final int ACCENT = 0xFFFF6644;

    // ── State ─────────────────────────────────────────────────────────────────
    private NpcAnimationData selectedAnim = null;
    private String selectedBone = AnimationBone.HEAD;
    private int selectedKeyframeIdx = -1;
    private int animScroll = 0;
    private float playheadTick = 0f;
    private boolean playing = false;
    private long playStartMs = 0;
    private float timelineScale = 10f;
    private String channelMode = "rotation";

    private EditBox nameBox, durationBox, kfTickBox, kfXBox, kfYBox, kfZBox;

    // ── Fixed column widths ───────────────────────────────────────────────────
    private static final int LIST_W = 92;
    private static final int RIGHT_W = 110;

    // ── NpcEditorTab ─────────────────────────────────────────────────────────

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        int listX = rx;
        int centerX = rx + LIST_W + 4;
        int centerW = rw - LIST_W - 4 - RIGHT_W - 4;
        int rightX = centerX + centerW + 4;

        // ── Animation list ────────────────────────────────────────────────────
        int ly = oy + 4;
        add.accept(Button.builder(Component.literal("+ Новая"), b -> {
            NpcAnimationData anim = new NpcAnimationData();
            anim.name = "Анимация " + (NpcAnimationLibrary.size() + 1);
            anim.ensureBones();
            NpcAnimationLibrary.register(anim);
            selectedAnim = anim;
            selectedKeyframeIdx = -1;
            rebuild.run();
        }).bounds(listX, ly, LIST_W, 16).build());
        ly += 20;

        List<NpcAnimationData> allAnims = NpcAnimationLibrary.getAll();
        int visAnims = 9;
        int maxScroll = Math.max(0, allAnims.size() - visAnims);
        animScroll = Math.max(0, Math.min(animScroll, maxScroll));

        for (int i = animScroll; i < Math.min(allAnims.size(), animScroll + visAnims); i++) {
            NpcAnimationData anim = allAnims.get(i);
            boolean sel = anim == selectedAnim;
            add.accept(Button.builder(
                    Component.literal(sel ? "§e▶ " + truncate(anim.name, 8) : "  " + truncate(anim.name, 9)),
                    b -> {
                        selectedAnim = anim;
                        selectedKeyframeIdx = -1;
                        rebuild.run();
                    }
            ).bounds(listX, ly + (i - animScroll) * 18, LIST_W, 16).build());
        }

        // Delete animation button (below list)
        int delY = ly + visAnims * 18 + 4;
        if (selectedAnim != null) {
            add.accept(Button.builder(Component.literal("§c✕ Удалить"), b -> {
                NpcAnimationLibrary.remove(selectedAnim.id);
                selectedAnim = NpcAnimationLibrary.getAll().isEmpty() ? null
                        : NpcAnimationLibrary.getAll().get(0);
                selectedKeyframeIdx = -1;
                rebuild.run();
            }).bounds(listX, delY, LIST_W, 16).build());
        }

        if (selectedAnim == null) return;

        // ── Row 1 (y=oy+4): name + duration + loop + state ───────────────────
        int cy = oy + 4;
        nameBox = new EditBox(Minecraft.getInstance().font, centerX, cy, Math.min(centerW - 120, 100), 16,
                Component.literal("Имя"));
        nameBox.setValue(selectedAnim.name);
        add.accept(nameBox);

        int afterName = centerX + Math.min(centerW - 120, 100) + 4;
        durationBox = new EditBox(Minecraft.getInstance().font, afterName, cy, 40, 16, Component.literal("Длит."));
        durationBox.setValue(String.format("%.0f", selectedAnim.durationTicks));
        add.accept(durationBox);

        add.accept(Button.builder(
                Component.literal(selectedAnim.loop ? "§aЦикл" : "§8Цикл"),
                b -> {
                    selectedAnim.loop = !selectedAnim.loop;
                    rebuild.run();
                }
        ).bounds(afterName + 44, cy, 40, 16).build());

        AnimationState[] states = AnimationState.values();
        add.accept(Button.builder(
                Component.literal("§7" + selectedAnim.stateBinding.label()),
                b -> {
                    int si = selectedAnim.stateBinding.ordinal();
                    selectedAnim.stateBinding = states[(si + 1) % states.length];
                    rebuild.run();
                }
        ).bounds(afterName + 88, cy, 70, 16).build());
        cy += 20;

        // ── Row 2 (y=oy+24): playback + zoom + channel ───────────────────────
        add.accept(Button.builder(Component.literal("▶"), b -> {
            playing = true;
            playStartMs = System.currentTimeMillis();
            playheadTick = 0;
        }).bounds(centerX, cy, 22, 16).build());
        add.accept(Button.builder(Component.literal("⏸"), b -> {
            playing = false;
        }).bounds(centerX + 24, cy, 22, 16).build());
        add.accept(Button.builder(Component.literal("■"), b -> {
            playing = false;
            playheadTick = 0;
        }).bounds(centerX + 48, cy, 22, 16).build());

        add.accept(Button.builder(Component.literal("−"), b -> {
            timelineScale = Math.max(2f, timelineScale - 2);
        }).bounds(centerX + 76, cy, 18, 16).build());
        add.accept(Button.builder(Component.literal("+"), b -> {
            timelineScale = Math.min(30f, timelineScale + 2);
        }).bounds(centerX + 96, cy, 18, 16).build());

        add.accept(Button.builder(
                Component.literal("rotation".equals(channelMode) ? "§eВращение" : "§7Позиция"),
                b -> {
                    channelMode = "rotation".equals(channelMode) ? "position" : "rotation";
                    rebuild.run();
                }
        ).bounds(centerX + 118, cy, 72, 16).build());
        cy += 20;

        // ── Row 3 (y=oy+44): bone selector ───────────────────────────────────
        int boneW = centerW / AnimationBone.BONE_IDS.length;
        for (int i = 0; i < AnimationBone.BONE_IDS.length; i++) {
            final String boneId = AnimationBone.BONE_IDS[i];
            boolean sel = boneId.equals(selectedBone);
            add.accept(Button.builder(
                    Component.literal(sel ? "§e§l" + AnimationBone.BONE_LABELS[i] : AnimationBone.BONE_LABELS[i]),
                    b -> {
                        selectedBone = boneId;
                        selectedKeyframeIdx = -1;
                        rebuild.run();
                    }
            ).bounds(centerX + i * boneW, cy, boneW - 2, 14).build());
        }
        cy += 18;

        // ── Row 4 (y=oy+62): add/remove keyframe ─────────────────────────────
        add.accept(Button.builder(Component.literal("+ Кадр"), b -> {
            AnimationBone bone = selectedAnim.getBone(selectedBone);
            List<AnimationKeyframe> frames = getFrames(bone);
            AnimationKeyframe kf = new AnimationKeyframe(playheadTick, 0, 0, 0);
            frames.add(kf);
            frames.sort((a, bk) -> Float.compare(a.tick, bk.tick));
            selectedKeyframeIdx = frames.indexOf(kf);
            rebuild.run();
        }).bounds(centerX, cy, 55, 14).build());

        add.accept(Button.builder(Component.literal("✕ Удалить кадр"), b -> {
            if (selectedKeyframeIdx >= 0) {
                List<AnimationKeyframe> frames = getFrames(selectedAnim.getBone(selectedBone));
                if (selectedKeyframeIdx < frames.size()) {
                    frames.remove(selectedKeyframeIdx);
                    selectedKeyframeIdx = Math.max(-1, selectedKeyframeIdx - 1);
                    rebuild.run();
                }
            }
        }).bounds(centerX + 58, cy, 90, 14).build());

        // ── Right panel: keyframe properties ─────────────────────────────────
        int ry = oy + 4;
        if (selectedKeyframeIdx >= 0) {
            AnimationBone bone = selectedAnim.getBone(selectedBone);
            List<AnimationKeyframe> frames = getFrames(bone);
            if (selectedKeyframeIdx < frames.size()) {
                AnimationKeyframe kf = frames.get(selectedKeyframeIdx);

                kfTickBox = new EditBox(Minecraft.getInstance().font, rightX + 20, ry, 56, 14, Component.literal("Тик"));
                kfTickBox.setValue(String.format("%.1f", kf.tick));
                add.accept(kfTickBox);
                ry += 18;

                kfXBox = new EditBox(Minecraft.getInstance().font, rightX + 20, ry, 56, 14, Component.literal("X"));
                kfXBox.setValue(String.format("%.2f", kf.x));
                add.accept(kfXBox);
                ry += 18;

                kfYBox = new EditBox(Minecraft.getInstance().font, rightX + 20, ry, 56, 14, Component.literal("Y"));
                kfYBox.setValue(String.format("%.2f", kf.y));
                add.accept(kfYBox);
                ry += 18;

                kfZBox = new EditBox(Minecraft.getInstance().font, rightX + 20, ry, 56, 14, Component.literal("Z"));
                kfZBox.setValue(String.format("%.2f", kf.z));
                add.accept(kfZBox);
                ry += 18;

                String[] easings = AnimationKeyframe.EASING_IDS;
                add.accept(Button.builder(
                        Component.literal("§7" + kf.easing),
                        b -> {
                            int ei = 0;
                            for (int ii = 0; ii < easings.length; ii++) {
                                if (easings[ii].equals(kf.easing)) ei = ii;
                            }
                            kf.easing = easings[(ei + 1) % easings.length];
                            rebuild.run();
                        }
                ).bounds(rightX, ry, RIGHT_W - 2, 14).build());
                ry += 20;
            }
        }

        // Export / Import (always visible when anim selected)
        int exportY = oy + 150;
        add.accept(Button.builder(Component.literal("↓ GeckoLib JSON"), b -> {
            String json = selectedAnim.toGeckoLibJson();
            Minecraft.getInstance().keyboardHandler.setClipboard(json);
        }).bounds(rightX, exportY, RIGHT_W - 2, 14).build());

        add.accept(Button.builder(Component.literal("↑ Импорт JSON"), b -> {
            String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
            if (clip != null && !clip.isBlank()) {
                try {
                    NpcAnimationData imported = NpcAnimationData.fromGeckoLibJson(clip);
                    if (imported != null) {
                        NpcAnimationLibrary.register(imported);
                        selectedAnim = imported;
                        selectedKeyframeIdx = -1;
                        rebuild.run();
                    }
                } catch (Exception ignored) {
                }
            }
        }).bounds(rightX, exportY + 18, RIGHT_W - 2, 14).build());
    }

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;

        g.fill(rx, oy, rx + rw, oy + 18, 0x55000000);
        g.drawString(font, "§l⏵ РЕДАКТОР АНИМАЦИЙ", rx + 4, oy + 4, ACCENT, false);

        if (selectedAnim == null) {
            g.drawString(font, "§8Выберите или создайте анимацию.", rx + LIST_W + 8, oy + 30, 0xFF555566, false);
            return;
        }

        int centerX = rx + LIST_W + 4;
        int centerW = rw - LIST_W - 4 - RIGHT_W - 4;
        int rightX = centerX + centerW + 4;

        // Info line
        g.drawString(font, "§7" + selectedAnim.name
                + "  §8" + (int) selectedAnim.durationTicks + " тик"
                + "  " + (selectedAnim.loop ? "§aЦикл" : "§8Не цикл"),
                centerX, oy + 2, 0xFFCCCCCC, false);

        // Timeline area — below 4 rows of controls
        int tlY = oy + 84;
        int tlH = 70;
        g.fill(centerX, tlY, centerX + centerW, tlY + tlH, 0xFF0A0A18);
        NpcEditorUtils.brd(g, centerX, tlY, centerW, tlH, 0xFF222233);

        // Ruler
        float dur = selectedAnim.durationTicks;
        for (int t = 0; t <= (int) dur; t += 5) {
            int tx = centerX + (int) (t * timelineScale);
            if (tx > centerX + centerW) break;
            g.fill(tx, tlY, tx + 1, tlY + 6, 0xFF333355);
            if (t % 20 == 0) {
                g.drawString(font, String.valueOf(t), tx + 1, tlY + 1, 0xFF555566, false);
            }
        }

        // Keyframes
        AnimationBone bone = selectedAnim.getBone(selectedBone);
        List<AnimationKeyframe> frames = getFrames(bone);
        int boneRow = tlY + 18;
        for (int i = 0; i < frames.size(); i++) {
            AnimationKeyframe kf = frames.get(i);
            int kx = centerX + (int) (kf.tick * timelineScale);
            boolean sel = i == selectedKeyframeIdx;
            g.fill(kx - 3, boneRow, kx + 3, boneRow + 12, sel ? 0xFFFFCC44 : ACCENT);
        }

        // Playhead
        if (playing) {
            long elapsed = System.currentTimeMillis() - playStartMs;
            playheadTick = elapsed / 50f;
            if (selectedAnim.loop) {
                playheadTick %= selectedAnim.durationTicks;
            } else if (playheadTick > selectedAnim.durationTicks) {
                playheadTick = selectedAnim.durationTicks;
                playing = false;
            }
        }
        int phX = centerX + (int) (playheadTick * timelineScale);
        g.fill(phX, tlY, phX + 1, tlY + tlH, 0xFFFF4444);

        // Channel/bone label above timeline
        g.drawString(font, "§7Кость: §e" + AnimationBone.boneLabel(selectedBone)
                + "  §7" + ("rotation".equals(channelMode) ? "Вращение" : "Позиция"),
                centerX, tlY - 11, 0xFF888899, false);

        // Right panel labels
        int ry = oy + 4;
        if (selectedKeyframeIdx >= 0 && selectedKeyframeIdx < frames.size()) {
            g.drawString(font, "§7Кадр §e#" + (selectedKeyframeIdx + 1), rightX, ry - 2, 0xFF888899, false);
            ry += 2;
            String[] lbls = {"§8Тик:", "§8X:", "§8Y:", "§8Z:"};
            for (String lbl : lbls) {
                g.drawString(font, lbl, rightX, ry + 2, 0xFF777788, false);
                ry += 18;
            }
            g.drawString(font, "§8Интерп.:", rightX, ry + 2, 0xFF777788, false);
        } else {
            g.drawString(font, "§8Выберите кадр", rightX, ry + 30, 0xFF444455, false);
        }
        g.drawString(font, "§8── Экспорт ──", rightX, oy + 142, 0xFF444455, false);
    }

    @Override
    public void pullFields(NpcEditorState state) {
        if (selectedAnim == null) return;
        if (nameBox != null) selectedAnim.name = nameBox.getValue();
        if (durationBox != null) {
            try {
                selectedAnim.durationTicks = Float.parseFloat(durationBox.getValue());
            } catch (Exception ignored) {
            }
        }
        if (selectedKeyframeIdx >= 0) {
            List<AnimationKeyframe> frames = getFrames(selectedAnim.getBone(selectedBone));
            if (selectedKeyframeIdx < frames.size()) {
                AnimationKeyframe kf = frames.get(selectedKeyframeIdx);
                if (kfTickBox != null) try {
                    kf.tick = Float.parseFloat(kfTickBox.getValue());
                } catch (Exception ignored) {
                }
                if (kfXBox != null) try {
                    kf.x = Float.parseFloat(kfXBox.getValue());
                } catch (Exception ignored) {
                }
                if (kfYBox != null) try {
                    kf.y = Float.parseFloat(kfYBox.getValue());
                } catch (Exception ignored) {
                }
                if (kfZBox != null) try {
                    kf.z = Float.parseFloat(kfZBox.getValue());
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public boolean onMouseScrolled(double mx, double my, double delta,
                                   NpcEditorState state, int rx, int oy, int rw) {
        int maxScroll = Math.max(0, NpcAnimationLibrary.size() - 9);
        animScroll = Math.max(0, Math.min(maxScroll, animScroll - (int) Math.signum(delta)));
        return true;
    }

    @Override
    public boolean onMouseClicked(double mx, double my, int btn,
                                  NpcEditorState state, int rx, int oy, int rw) {
        if (selectedAnim == null) return false;
        int centerX = rx + LIST_W + 4;
        int centerW = rw - LIST_W - 4 - RIGHT_W - 4;
        int tlY = oy + 84;
        int tlH = 70;
        if (mx >= centerX && mx <= centerX + centerW && my >= tlY && my <= tlY + tlH) {
            float tick = (float) ((mx - centerX) / timelineScale);
            List<AnimationKeyframe> frames = getFrames(selectedAnim.getBone(selectedBone));
            int nearest = -1;
            float nearDist = 5f / timelineScale;
            for (int i = 0; i < frames.size(); i++) {
                float d = Math.abs(frames.get(i).tick - tick);
                if (d < nearDist) {
                    nearDist = d;
                    nearest = i;
                }
            }
            selectedKeyframeIdx = nearest;
            playheadTick = tick;
            return true;
        }
        return false;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<AnimationKeyframe> getFrames(AnimationBone bone) {
        return "rotation".equals(channelMode) ? bone.rotationFrames : bone.positionFrames;
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}

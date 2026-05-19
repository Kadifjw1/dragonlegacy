package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.server.animation.AnimationBone;
import com.frametrip.dragonlegacyquesttoast.server.animation.AnimationKeyframe;
import com.frametrip.dragonlegacyquesttoast.server.animation.AnimationState;
import com.frametrip.dragonlegacyquesttoast.server.animation.AnimationTrigger;
import com.frametrip.dragonlegacyquesttoast.server.animation.NpcAnimationData;
import com.frametrip.dragonlegacyquesttoast.server.animation.NpcAnimationLibrary;
import com.frametrip.dragonlegacyquesttoast.client.NpcFileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * Animation editor tab — works directly with NpcEntityData.animations so animations
 * are persisted per NPC and saved to the server when the editor is saved.
 *
 * Layout (left→right):
 *   [90px list] | [timeline + controls] | [110px keyframe props]
 */
public class NpcAnimationEditorTab implements NpcEditorTab {

    public static final int ACCENT = 0xFFFF6644;

    // ── State ─────────────────────────────────────────────────────────────────
    private static final Logger LOGGER = LogManager.getLogger("dragonlegacyquesttoast");
    private NpcAnimationData selectedAnim = null;
    private String selectedBone = AnimationBone.HEAD;
    private int selectedKeyframeIdx = -1;
    private int animScroll = 0;
    private float playheadTick = 0f;
    private boolean playing = false;
    private long playStartMs = 0;
    private float timelineScale = 10f;
    private String channelMode = "rotation";

    // ── Library / Pose / Trigger mode state ──────────────────────────────────
    private boolean showLibrary  = false;
    private boolean showPose     = false;
    private boolean showTriggers = false;
    private AnimationState libStateFilter = null; // null = ALL

    // [ANI-1]: pose editor boxes [bone][axis]
    private final EditBox[][] poseBoxes = new EditBox[NpcEntityData.POSE_BONE_IDS.length][3];

    // [ANI-2]: trigger editor state
    private int     selectedTriggerIdx = -1;
    private EditBox trigParamBox;

    // ── Import/export status ──────────────────────────────────────────────────
    private String ioStatus = "";
    private int libScroll = 0;
    private String libSelectedId = null;

    private EditBox nameBox, durationBox, kfTickBox, kfXBox, kfYBox, kfZBox;

    // ── Fixed column widths ───────────────────────────────────────────────────
    private static final int LIST_W = 92;
    private static final int RIGHT_W = 110;

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        NpcEntityData draft = state.getDraft();
        List<NpcAnimationData> anims = draft.animations;

        // ── Mode toggle ───────────────────────────────────────────────────────
        boolean editorMode = !showLibrary && !showPose && !showTriggers;
        add.accept(Button.builder(
                Component.literal(editorMode ? "§e§lРедактор" : "§7Редактор"),
                b -> { showLibrary = false; showPose = false; showTriggers = false; rebuild.run(); }
        ).bounds(rx, oy, 76, 14).build());
        add.accept(Button.builder(
                Component.literal(showLibrary ? "§e§lБиблиотека" : "§7Библиотека"),
                b -> { showLibrary = true; showPose = false; showTriggers = false; rebuild.run(); }
        ).bounds(rx + 80, oy, 76, 14).build());
        add.accept(Button.builder(
                Component.literal(showPose ? "§e§lПоза" : "§7Поза"),
                b -> { showPose = true; showLibrary = false; showTriggers = false; rebuild.run(); }
        ).bounds(rx + 160, oy, 60, 14).build());
        add.accept(Button.builder(
                Component.literal(showTriggers ? "§e§lТриггеры" : "§7Триггеры"),
                b -> { showTriggers = true; showLibrary = false; showPose = false; rebuild.run(); }
        ).bounds(rx + 224, oy, 76, 14).build());

        if (showLibrary) {
            initLibraryPanel(add, rebuild, state, draft, rx, oy + 18, rw);
            return;
        }
        if (showPose) {
            initPosePanel(add, rebuild, state, draft, rx, oy + 18, rw);
            return;
        }
        if (showTriggers) {
            initTriggersPanel(add, rebuild, state, draft, rx, oy + 18, rw);
            return;
        }

        int listX = rx;
        int centerX = rx + LIST_W + 4;
        int centerW = rw - LIST_W - 4 - RIGHT_W - 4;
        int rightX = centerX + centerW + 4;

        int ly = oy + 4;
        add.accept(Button.builder(Component.literal("+ Новая"), b -> {
            NpcAnimationData anim = new NpcAnimationData();
            anim.name = "Анимация " + (anims.size() + 1);
            anim.ensureBones();
            anims.add(anim);
            selectedAnim = anim;
            selectedKeyframeIdx = -1;
            state.markDirty();
            rebuild.run();
        }).bounds(listX, ly, LIST_W, 16).build());
        ly += 20;

        int visAnims = 9;
        int maxScroll = Math.max(0, anims.size() - visAnims);
        animScroll = Math.max(0, Math.min(animScroll, maxScroll));

        for (int i = animScroll; i < Math.min(anims.size(), animScroll + visAnims); i++) {
            NpcAnimationData anim = anims.get(i);
            boolean sel = anim == selectedAnim;
            add.accept(Button.builder(
                    Component.literal(sel ? "§e▶ " + NpcEditorUtils.fitText(anim.name, LIST_W - 14) : "  " + NpcEditorUtils.fitText(anim.name, LIST_W - 10)),
                    b -> {
                        selectedAnim = anim;
                        selectedKeyframeIdx = -1;
                        rebuild.run();
                    }
            ).bounds(listX, ly + (i - animScroll) * 18, LIST_W, 16).build());
        }

        int delY = ly + visAnims * 18 + 4;
        if (selectedAnim != null) {
            add.accept(Button.builder(Component.literal("§c✕ Удалить"), b -> {
                anims.remove(selectedAnim);
                selectedAnim = anims.isEmpty() ? null : anims.get(0);
                selectedKeyframeIdx = -1;
                state.markDirty();
                rebuild.run();
            }).bounds(listX, delY, LIST_W, 16).build());
        }

        if (selectedAnim == null) return;

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
                    state.markDirty();
                    rebuild.run();
                }
        ).bounds(afterName + 44, cy, 40, 16).build());

        AnimationState[] states = AnimationState.values();
        add.accept(Button.builder(
                Component.literal("§7" + selectedAnim.stateBinding.label()),
                b -> {
                    int si = selectedAnim.stateBinding.ordinal();
                    selectedAnim.stateBinding = states[(si + 1) % states.length];
                    state.markDirty();
                    rebuild.run();
                }
        ).bounds(afterName + 88, cy, 70, 16).build());
        cy += 20;

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

        add.accept(Button.builder(Component.literal("+ Кадр"), b -> {
            AnimationBone bone = selectedAnim.getBone(selectedBone);
            List<AnimationKeyframe> frames = getFrames(bone);
            AnimationKeyframe kf = new AnimationKeyframe(playheadTick, 0, 0, 0);
            frames.add(kf);
            frames.sort((a, bk) -> Float.compare(a.tick, bk.tick));
            selectedKeyframeIdx = frames.indexOf(kf);
            state.markDirty();
            rebuild.run();
        }).bounds(centerX, cy, 55, 14).build());

        add.accept(Button.builder(Component.literal("✕ Удалить кадр"), b -> {
            if (selectedKeyframeIdx >= 0) {
                List<AnimationKeyframe> frames = getFrames(selectedAnim.getBone(selectedBone));
                if (selectedKeyframeIdx < frames.size()) {
                    frames.remove(selectedKeyframeIdx);
                    selectedKeyframeIdx = Math.max(-1, selectedKeyframeIdx - 1);
                    state.markDirty();
                    rebuild.run();
                }
            }
        }).bounds(centerX + 58, cy, 90, 14).build());

        // ── Right panel: keyframe fields ─────────────────────────────────────
        // Layout: header (14px) + 4 fields × (label 12px + box 14px + gap 2px)
        //         + easing button. Total ≈ 130px.
        int ry = oy + 4;
        ry += 14; // reserve space for "Кадр #N" header drawn in render()
        if (selectedKeyframeIdx >= 0) {
            AnimationBone bone = selectedAnim.getBone(selectedBone);
            List<AnimationKeyframe> frames = getFrames(bone);
            if (selectedKeyframeIdx < frames.size()) {
                AnimationKeyframe kf = frames.get(selectedKeyframeIdx);

                ry += 12; // label "Тик:" drawn in render()
                kfTickBox = new EditBox(Minecraft.getInstance().font, rightX, ry, RIGHT_W - 2, 14, Component.literal("Тик"));
                kfTickBox.setValue(String.format("%.1f", kf.tick));
                add.accept(kfTickBox);
                ry += 16;

                ry += 12; // label "X:"
                kfXBox = new EditBox(Minecraft.getInstance().font, rightX, ry, RIGHT_W - 2, 14, Component.literal("X"));
                kfXBox.setValue(String.format("%.2f", kf.x));
                add.accept(kfXBox);
                ry += 16;

                ry += 12; // label "Y:"
                kfYBox = new EditBox(Minecraft.getInstance().font, rightX, ry, RIGHT_W - 2, 14, Component.literal("Y"));
                kfYBox.setValue(String.format("%.2f", kf.y));
                add.accept(kfYBox);
                ry += 16;

                ry += 12; // label "Z:"
                kfZBox = new EditBox(Minecraft.getInstance().font, rightX, ry, RIGHT_W - 2, 14, Component.literal("Z"));
                kfZBox.setValue(String.format("%.2f", kf.z));
                add.accept(kfZBox);
                ry += 16;

                // easing cycle button
                String[] easings = AnimationKeyframe.EASING_IDS;
                add.accept(Button.builder(
                        Component.literal("§7" + kf.easing),
                        b -> {
                            int ei = 0;
                            for (int ii = 0; ii < easings.length; ii++) {
                                if (easings[ii].equals(kf.easing)) ei = ii;
                            }
                            kf.easing = easings[(ei + 1) % easings.length];
                            state.markDirty();
                            rebuild.run();
                        }
                ).bounds(rightX, ry, RIGHT_W - 2, 14).build());
            }
        }

        int exportY = oy + 160;

        // ── Export ───────────────────────────────────────────────────────────
        // Single export — selected animation only
        add.accept(Button.builder(Component.literal("↓ Текущую"), b -> {
            if (selectedAnim == null) return;
            exportSingle(selectedAnim);
            ioStatus = "§aЭкспорт: " + selectedAnim.name;
            rebuild.run();
        }).bounds(rightX, exportY, RIGHT_W - 2, 14).build());

        // Batch export — all NPC animations
        add.accept(Button.builder(Component.literal("↓ Все (" + anims.size() + ")"), b -> {
            if (anims.isEmpty()) return;
            int count = 0;
            for (NpcAnimationData a : anims) { exportSingle(a); count++; }
            Path dir = NpcFileUtils.getExportAnimDir();
            try { Files.createDirectories(dir); NpcFileUtils.openInExplorer(dir); } catch (IOException ignored) {}
            ioStatus = "§aЭкспортировано: " + count;
            rebuild.run();
        }).bounds(rightX, exportY + 16, RIGHT_W - 2, 14).build());

        // ── Import ───────────────────────────────────────────────────────────
        // From clipboard
        add.accept(Button.builder(Component.literal("↑ Буфер"), b -> {
            String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
            if (clip == null || clip.isBlank()) { ioStatus = "§cБуфер пуст"; rebuild.run(); return; }
            NpcAnimationData imported = NpcAnimationData.fromGeckoLibJson(clip);
            if (imported != null) {
                anims.add(imported);
                selectedAnim = imported;
                selectedKeyframeIdx = -1;
                state.markDirty();
                ioStatus = "§aИмпорт: " + imported.name;
            } else {
                ioStatus = "§cНеверный формат";
            }
            rebuild.run();
        }).bounds(rightX, exportY + 34, RIGHT_W - 2, 14).build());

        // From import/animations directory — loads all .animation.json files found
        add.accept(Button.builder(Component.literal("↑ Из папки"), b -> {
            int[] loaded = {0};
            try (java.nio.file.DirectoryStream<Path> ds = Files.newDirectoryStream(
                    NpcFileUtils.getImportAnimDir(), "*.animation.json")) {
                for (Path p : ds) {
                    try {
                        String json = Files.readString(p);
                        NpcAnimationData imported = NpcAnimationData.fromGeckoLibJson(json);
                        if (imported != null) {
                            anims.add(imported);
                            selectedAnim = imported;
                            selectedKeyframeIdx = -1;
                            loaded[0]++;
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Skip {}: {}", p.getFileName(), e.getMessage());
                    }
                }
            } catch (IOException e) {
                ioStatus = "§cПапка недоступна";
                rebuild.run();
                return;
            }
            if (loaded[0] > 0) state.markDirty();
            ioStatus = loaded[0] > 0 ? "§aЗагружено: " + loaded[0] : "§8Файлов не найдено";
            rebuild.run();
        }).bounds(rightX, exportY + 52, RIGHT_W - 2, 14).build());

        // Open import/animations folder
        add.accept(Button.builder(Component.literal("📂 Папка"), b -> {
            NpcFileUtils.openInExplorer(NpcFileUtils.getImportAnimDir());
        }).bounds(rightX, exportY + 72, RIGHT_W - 2, 12).build());
    }

    // ── [ANI-1]: Pose editor panel ────────────────────────────────────────────

    private void initPosePanel(Consumer<AbstractWidget> add, Runnable rebuild,
                               NpcEditorState state, NpcEntityData draft, int rx, int oy, int rw) {
        if (draft.customPose == null) draft.customPose = new java.util.LinkedHashMap<>();
        for (String boneId : NpcEntityData.POSE_BONE_IDS)
            draft.customPose.putIfAbsent(boneId, new float[3]);

        add.accept(Button.builder(
                Component.literal(draft.customPoseEnabled ? "§a✓ Поза" : "§8✗ Поза"),
                b -> { draft.customPoseEnabled = !draft.customPoseEnabled; state.markDirty(); rebuild.run(); }
        ).bounds(rx, oy, 90, 14).build());

        add.accept(Button.builder(Component.literal("Сброс"), b -> {
            draft.customPose.replaceAll((k, v) -> new float[3]);
            draft.customPoseEnabled = false;
            state.markDirty();
            rebuild.run();
        }).bounds(rx + 94, oy, 60, 14).build());

        oy += 18;
        for (int bi = 0; bi < NpcEntityData.POSE_BONE_IDS.length; bi++) {
            final int bIdx = bi;
            final String boneId = NpcEntityData.POSE_BONE_IDS[bi];
            float[] angles = draft.customPose.get(boneId);
            // bone label is drawn in render()
            for (int ai = 0; ai < 3; ai++) {
                final int aIdx = ai;
                int colX = rx + 66 + ai * 90;
                int rowY = oy + bi * 20;

                add.accept(Button.builder(Component.literal("◄"), b -> {
                    float[] a = draft.customPose.get(NpcEntityData.POSE_BONE_IDS[bIdx]);
                    a[aIdx] = Math.round((a[aIdx] - 5) * 10f) / 10f;
                    poseBoxes[bIdx][aIdx].setValue(String.format("%.1f", a[aIdx]));
                    state.markDirty();
                }).bounds(colX, rowY, 14, 14).build());

                poseBoxes[bi][ai] = new EditBox(Minecraft.getInstance().font,
                        colX + 16, rowY, 42, 14, Component.literal("°"));
                poseBoxes[bi][ai].setValue(String.format("%.1f", angles[ai]));
                add.accept(poseBoxes[bi][ai]);

                add.accept(Button.builder(Component.literal("►"), b -> {
                    float[] a = draft.customPose.get(NpcEntityData.POSE_BONE_IDS[bIdx]);
                    a[aIdx] = Math.round((a[aIdx] + 5) * 10f) / 10f;
                    poseBoxes[bIdx][aIdx].setValue(String.format("%.1f", a[aIdx]));
                    state.markDirty();
                }).bounds(colX + 60, rowY, 14, 14).build());
            }
        }
    }

    // ── [ANI-2]: Triggers panel ───────────────────────────────────────────────

    private void initTriggersPanel(Consumer<AbstractWidget> add, Runnable rebuild,
                                   NpcEditorState state, NpcEntityData draft, int rx, int oy, int rw) {
        if (draft.animTriggers == null) draft.animTriggers = new java.util.ArrayList<>();
        List<AnimationTrigger> trigs = draft.animTriggers;

        add.accept(Button.builder(Component.literal("+ Триггер"), b -> {
            trigs.add(new AnimationTrigger());
            selectedTriggerIdx = trigs.size() - 1;
            state.markDirty();
            rebuild.run();
        }).bounds(rx, oy, 90, 14).build());

        if (selectedTriggerIdx >= 0 && selectedTriggerIdx < trigs.size()) {
            add.accept(Button.builder(Component.literal("§c✕ Удалить"), b -> {
                trigs.remove(selectedTriggerIdx);
                selectedTriggerIdx = Math.min(selectedTriggerIdx, trigs.size() - 1);
                state.markDirty();
                rebuild.run();
            }).bounds(rx + 94, oy, 80, 14).build());
        }

        oy += 18;
        int visRows = 7;
        for (int i = 0; i < Math.min(trigs.size(), visRows); i++) {
            AnimationTrigger trig = trigs.get(i);
            boolean sel = i == selectedTriggerIdx;
            final int idx = i;

            NpcAnimationData targetAnim = findAnimById(draft, trig.targetAnimId);
            String animLabel = targetAnim != null ? NpcEditorUtils.fitText(targetAnim.name, 60) : "§8(нет)";
            String rowLabel = (trig.enabled ? "§a✓ " : "§8✗ ") + trig.type.label()
                    + " §8" + String.format("%.1f", trig.param) + " →§7 " + animLabel;

            add.accept(Button.builder(Component.literal(sel ? "§e▶ " + rowLabel : "  " + rowLabel), b -> {
                selectedTriggerIdx = idx;
                rebuild.run();
            }).bounds(rx, oy + i * 18, rw - 2, 16).build());
        }

        int editY = oy + visRows * 18 + 6;
        if (selectedTriggerIdx >= 0 && selectedTriggerIdx < trigs.size()) {
            AnimationTrigger sel = trigs.get(selectedTriggerIdx);

            // Enable toggle
            add.accept(Button.builder(
                    Component.literal(sel.enabled ? "§a✓ Вкл." : "§8✗ Выкл."),
                    b -> { sel.enabled = !sel.enabled; state.markDirty(); rebuild.run(); }
            ).bounds(rx, editY, 70, 14).build());

            // Type cycle
            AnimationTrigger.TriggerType[] types = AnimationTrigger.TriggerType.values();
            add.accept(Button.builder(
                    Component.literal("§7" + sel.type.label()),
                    b -> {
                        int next = (sel.type.ordinal() + 1) % types.length;
                        sel.type = types[next];
                        state.markDirty();
                        rebuild.run();
                    }
            ).bounds(rx + 74, editY, 110, 14).build());

            // Param box
            trigParamBox = new EditBox(Minecraft.getInstance().font,
                    rx + 188, editY, 50, 14, Component.literal("param"));
            trigParamBox.setValue(String.format("%.1f", sel.param));
            add.accept(trigParamBox);

            editY += 18;
            // Target animation cycle
            List<NpcAnimationData> anims = draft.animations;
            NpcAnimationData targetAnim = findAnimById(draft, sel.targetAnimId);
            String animLabel = targetAnim != null ? targetAnim.name : "§8(нет)";
            add.accept(Button.builder(
                    Component.literal("§7Анимация: " + animLabel),
                    b -> {
                        if (anims.isEmpty()) return;
                        int curIdx = -1;
                        for (int j = 0; j < anims.size(); j++)
                            if (anims.get(j).id.equals(sel.targetAnimId)) { curIdx = j; break; }
                        curIdx = (curIdx + 1) % anims.size();
                        sel.targetAnimId = anims.get(curIdx).id;
                        state.markDirty();
                        rebuild.run();
                    }
            ).bounds(rx, editY, 200, 14).build());
        }
    }

    private NpcAnimationData findAnimById(NpcEntityData data, String id) {
        if (id == null || id.isEmpty() || data.animations == null) return null;
        for (NpcAnimationData a : data.animations) if (id.equals(a.id)) return a;
        return null;
    }

    private void exportSingle(NpcAnimationData anim) {
        String json = anim.toGeckoLibJson();
        Minecraft.getInstance().keyboardHandler.setClipboard(json);
        Path dir = NpcFileUtils.getExportAnimDir();
        String safe = anim.name.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        try {
            Files.createDirectories(dir);
            Files.writeString(dir.resolve(safe + ".animation.json"), json);
        } catch (IOException e) {
            LOGGER.error("Failed to export animation: {}", safe, e);
        }
    }

    // ── Library panel ─────────────────────────────────────────────────────────

    private void initLibraryPanel(Consumer<AbstractWidget> add, Runnable rebuild,
                                   NpcEditorState state, NpcEntityData draft, int rx, int oy, int rw) {
        // State filter cycle button
        String filterLabel = libStateFilter == null ? "§7Все состояния" : "§7" + libStateFilter.label();
        add.accept(Button.builder(Component.literal("Фильтр: " + filterLabel), b -> {
            AnimationState[] vals = AnimationState.values();
            if (libStateFilter == null) {
                libStateFilter = vals[0];
            } else {
                int next = libStateFilter.ordinal() + 1;
                libStateFilter = next >= vals.length ? null : vals[next];
            }
            libScroll = 0;
            rebuild.run();
        }).bounds(rx, oy, rw - 100, 14).build());

        // Save selected NPC animation → library
        add.accept(Button.builder(Component.literal("↑ В библиотеку"), b -> {
            if (selectedAnim != null) {
                NpcAnimationData copy = selectedAnim.copy();
                NpcAnimationLibrary.register(copy);
                libSelectedId = copy.id;
                rebuild.run();
            }
        }).bounds(rx + rw - 96, oy, 96, 14).build());

        // Filtered library list
        List<NpcAnimationData> lib = libStateFilter == null
                ? NpcAnimationLibrary.getAll()
                : NpcAnimationLibrary.getByState(libStateFilter);

        int visRows = 10;
        int maxScroll = Math.max(0, lib.size() - visRows);
        libScroll = Math.max(0, Math.min(libScroll, maxScroll));

        int ly = oy + 20;
        for (int i = libScroll; i < Math.min(lib.size(), libScroll + visRows); i++) {
            NpcAnimationData a = lib.get(i);
            boolean sel = a.id.equals(libSelectedId);
            int btnW = rw - 26;
            String prefix = sel ? "§e▶ " : "   ";
            String label = NpcEditorUtils.fitText(
                    a.name + " §8[" + a.stateBinding.label() + "]", btnW - 10);
            final String id = a.id;
            add.accept(Button.builder(Component.literal(prefix + label), b -> {
                libSelectedId = id;
                rebuild.run();
            }).bounds(rx, ly + (i - libScroll) * 18, btnW, 16).build());
        }

        // Scroll arrows
        add.accept(Button.builder(Component.literal("▲"),
                b -> { libScroll = Math.max(0, libScroll - 1); rebuild.run(); }
        ).bounds(rx + rw - 24, ly, 22, 16).build());
        add.accept(Button.builder(Component.literal("▼"),
                b -> { libScroll = Math.min(maxScroll, libScroll + 1); rebuild.run(); }
        ).bounds(rx + rw - 24, ly + 20, 22, 16).build());

        // Action buttons for selected library entry
        if (libSelectedId != null && NpcAnimationLibrary.get(libSelectedId) != null) {
            int actY = ly + visRows * 18 + 6;

            // Apply to NPC
            add.accept(Button.builder(Component.literal("→ Применить к NPC"), b -> {
                NpcAnimationData src = NpcAnimationLibrary.get(libSelectedId);
                if (src == null) return;
                NpcAnimationData copy = src.copy();
                copy.ensureBones();
                draft.animations.add(copy);
                selectedAnim = copy;
                state.markDirty();
                showLibrary = false;
                rebuild.run();
            }).bounds(rx, actY, 130, 16).build());

            // Delete from library
            add.accept(Button.builder(Component.literal("§c✕ Удалить"), b -> {
                NpcAnimationLibrary.remove(libSelectedId);
                libSelectedId = null;
                libScroll = Math.max(0, libScroll - 1);
                rebuild.run();
            }).bounds(rx + 134, actY, 80, 16).build());
        }
    }

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;

        g.fill(rx, oy, rx + rw, oy + 18, 0x55000000);
        g.drawString(font, "§l⏵ РЕДАКТОР АНИМАЦИЙ", rx + 4, oy + 4, ACCENT, false);

        if (showLibrary) {
            renderLibraryPanel(g, rx, oy + 18, rw);
            return;
        }
        if (showPose) {
            renderPosePanel(g, state, rx, oy + 18, rw);
            return;
        }
        if (showTriggers) {
            renderTriggersPanel(g, state, rx, oy + 18, rw);
            return;
        }

        if (selectedAnim == null) {
            g.drawString(font, "§8Выберите или создайте анимацию.", rx + LIST_W + 8, oy + 30, 0xFF555566, false);
            return;
        }

        int centerX = rx + LIST_W + 4;
        int centerW = rw - LIST_W - 4 - RIGHT_W - 4;
        int rightX = centerX + centerW + 4;

        g.drawString(font, "§7" + selectedAnim.name
                + "  §8" + (int) selectedAnim.durationTicks + " тик"
                + "  " + (selectedAnim.loop ? "§aЦикл" : "§8Не цикл"),
                centerX, oy + 2, 0xFFCCCCCC, false);

        int tlY = oy + 84;
        int tlH = 70;
        g.fill(centerX, tlY, centerX + centerW, tlY + tlH, 0xFF0A0A18);
        NpcEditorUtils.brd(g, centerX, tlY, centerW, tlH, 0xFF222233);

        float dur = selectedAnim.durationTicks;
        for (int t = 0; t <= (int) dur; t += 5) {
            int tx = centerX + (int) (t * timelineScale);
            if (tx > centerX + centerW) break;
            g.fill(tx, tlY, tx + 1, tlY + 6, 0xFF333355);
            if (t % 20 == 0) {
                g.drawString(font, String.valueOf(t), tx + 1, tlY + 1, 0xFF555566, false);
            }
        }

        AnimationBone bone = selectedAnim.getBone(selectedBone);
        List<AnimationKeyframe> frames = getFrames(bone);
        int boneRow = tlY + 18;
        for (int i = 0; i < frames.size(); i++) {
            AnimationKeyframe kf = frames.get(i);
            int kx = centerX + (int) (kf.tick * timelineScale);
            boolean sel = i == selectedKeyframeIdx;
            g.fill(kx - 3, boneRow, kx + 3, boneRow + 12, sel ? 0xFFFFCC44 : ACCENT);
        }

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

        g.drawString(font, "§7Кость: §e" + AnimationBone.boneLabel(selectedBone)
                + "  §7" + ("rotation".equals(channelMode) ? "Вращение" : "Позиция"),
                centerX, tlY - 11, 0xFF888899, false);

        int ry = oy + 4;
        if (selectedKeyframeIdx >= 0 && selectedKeyframeIdx < frames.size()) {
            g.drawString(font, "§7Кадр §e#" + (selectedKeyframeIdx + 1), rightX, ry, 0xFF888899, false);
            ry += 14; // header
            // Each field: label (12px) then box (14px) then gap (2px)
            String[] lbls = {"§8Тик:", "§8X:", "§8Y:", "§8Z:"};
            for (String lbl : lbls) {
                g.drawString(font, lbl, rightX, ry, 0xFF777788, false);
                ry += 12; // label
                ry += 16; // box + gap
            }
            g.drawString(font, "§8Интерп.:", rightX, ry, 0xFF777788, false);
        } else {
            g.drawString(font, "§8Выберите кадр", rightX, ry + 30, 0xFF444455, false);
        }
        g.drawString(font, "§8── И/О ──", rightX, oy + 152, 0xFF444455, false);
        if (!ioStatus.isEmpty()) {
            g.drawString(font, ioStatus, rightX, oy + 248, 0xFFCCCCCC, false);
        }
    }

    private void renderPosePanel(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw) {
        var font = Minecraft.getInstance().font;
        NpcEditorUtils.sectionCard(g, rx, oy - 2, rw, 16, "ПОЗА КОСТЕЙ", ACCENT);
        int rowOy = oy + 18;
        String[] axisLabels = {"X°", "Y°", "Z°"};
        for (int bi = 0; bi < NpcEntityData.POSE_BONE_IDS.length; bi++) {
            int rowY = rowOy + bi * 20;
            g.drawString(font, NpcEntityData.POSE_BONE_LABELS[bi], rx, rowY + 3, 0xFFCCCCCC, false);
            for (int ai = 0; ai < 3; ai++) {
                int colX = rx + 66 + ai * 90;
                g.drawString(font, "§8" + axisLabels[ai], colX + 17, rowY - 8, 0xFF777788, false);
            }
        }
    }

    private void renderTriggersPanel(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw) {
        var font = Minecraft.getInstance().font;
        NpcEditorUtils.sectionCard(g, rx, oy - 2, rw, 16, "ТРИГГЕРЫ АНИМАЦИЙ", ACCENT);
        if (state.getDraft().animTriggers == null || state.getDraft().animTriggers.isEmpty()) {
            g.drawString(font, "§8Нет триггеров. Нажмите «+ Триггер».", rx + 4, oy + 30, 0xFF444455, false);
        }
        int editY = oy + 18 + 7 * 18 + 24;
        if (selectedTriggerIdx >= 0) {
            g.drawString(font, "§8Тип / Параметр / Анимация:", rx, editY - 14, 0xFF666677, false);
        }
    }

    private void renderLibraryPanel(GuiGraphics g, int rx, int oy, int rw) {
        var font = Minecraft.getInstance().font;
        List<NpcAnimationData> lib = libStateFilter == null
                ? NpcAnimationLibrary.getAll()
                : NpcAnimationLibrary.getByState(libStateFilter);

        NpcEditorUtils.sectionCard(g, rx, oy - 2, rw, 16,
                "БИБЛИОТЕКА (" + lib.size() + ")", ACCENT);

        int ly = oy + 20;
        for (int i = libScroll; i < Math.min(lib.size(), libScroll + 10); i++) {
            NpcAnimationData a = lib.get(i);
            boolean sel = a.id.equals(libSelectedId);
            int rowY = ly + (i - libScroll) * 18;
            if (sel) g.fill(rx, rowY, rx + rw - 26, rowY + 16, 0x44FFCC44);
            g.drawString(font,
                    "§8" + (int) a.durationTicks + "т  "
                    + (a.loop ? "§aЦ " : "§8. ")
                    + "§7" + a.stateBinding.label(),
                    rx + rw - 140, rowY + 4, 0xFF666677, false);
        }

        if (lib.isEmpty()) {
            g.drawString(font, "§8Библиотека пуста. Создайте анимацию в редакторе",
                    rx + 4, ly + 8, 0xFF444455, false);
            g.drawString(font, "§8и нажмите «↑ В библиотеку».",
                    rx + 4, ly + 20, 0xFF444455, false);
        }
    }

    @Override
    public void pullFields(NpcEditorState state) {
        // [ANI-1]: Pull pose box values into draft
        if (showPose) {
            NpcEntityData d = state.getDraft();
            if (d.customPose == null) d.customPose = new java.util.LinkedHashMap<>();
            for (int bi = 0; bi < NpcEntityData.POSE_BONE_IDS.length; bi++) {
                float[] angles = d.customPose.computeIfAbsent(
                        NpcEntityData.POSE_BONE_IDS[bi], k -> new float[3]);
                for (int ai = 0; ai < 3; ai++) {
                    EditBox box = poseBoxes[bi][ai];
                    if (box != null) {
                        try { angles[ai] = Float.parseFloat(box.getValue()); state.markDirty(); }
                        catch (Exception ignored) {}
                    }
                }
            }
        }
        // [ANI-2]: Pull trigger param box
        if (showTriggers && trigParamBox != null) {
            NpcEntityData d = state.getDraft();
            if (d.animTriggers != null && selectedTriggerIdx >= 0
                    && selectedTriggerIdx < d.animTriggers.size()) {
                try {
                    d.animTriggers.get(selectedTriggerIdx).param =
                            Float.parseFloat(trigParamBox.getValue());
                    state.markDirty();
                } catch (Exception ignored) {}
            }
        }
        if (selectedAnim == null) return;

        if (nameBox != null) {
            selectedAnim.name = nameBox.getValue();
            state.markDirty();
        }

        if (durationBox != null) {
            try {
                selectedAnim.durationTicks = Float.parseFloat(durationBox.getValue());
                state.markDirty();
            } catch (Exception ignored) {
            }
        }

        if (selectedKeyframeIdx >= 0) {
            List<AnimationKeyframe> frames = getFrames(selectedAnim.getBone(selectedBone));
            if (selectedKeyframeIdx < frames.size()) {
                AnimationKeyframe kf = frames.get(selectedKeyframeIdx);
                boolean changed = false;

                if (kfTickBox != null) {
                    try {
                        kf.tick = Float.parseFloat(kfTickBox.getValue());
                        changed = true;
                    } catch (Exception ignored) {
                    }
                }

                if (kfXBox != null) {
                    try {
                        kf.x = Float.parseFloat(kfXBox.getValue());
                        changed = true;
                    } catch (Exception ignored) {
                    }
                }

                if (kfYBox != null) {
                    try {
                        kf.y = Float.parseFloat(kfYBox.getValue());
                        changed = true;
                    } catch (Exception ignored) {
                    }
                }

                if (kfZBox != null) {
                    try {
                        kf.z = Float.parseFloat(kfZBox.getValue());
                        changed = true;
                    } catch (Exception ignored) {
                    }
                }

                if (changed) {
                    state.markDirty();
                }
            }
        }
    }

    @Override
    public boolean onMouseScrolled(double mx, double my, double delta,
                                   NpcEditorState state, int rx, int oy, int rw) {
        // Scroll numeric keyframe fields when cursor hovers over them
        if (!showLibrary && selectedKeyframeIdx >= 0 && selectedAnim != null) {
            List<AnimationKeyframe> frames = getFrames(selectedAnim.getBone(selectedBone));
            if (selectedKeyframeIdx < frames.size()) {
                AnimationKeyframe kf = frames.get(selectedKeyframeIdx);
                if (scrollAdjustBox(kfTickBox, mx, my, delta, 1.0f)) {
                    try { kf.tick = Math.max(0, Float.parseFloat(kfTickBox.getValue())); } catch (Exception ignored) {}
                    state.markDirty();
                    return true;
                }
                if (scrollAdjustBox(kfXBox, mx, my, delta, 1.0f)) {
                    try { kf.x = Float.parseFloat(kfXBox.getValue()); } catch (Exception ignored) {}
                    state.markDirty();
                    return true;
                }
                if (scrollAdjustBox(kfYBox, mx, my, delta, 1.0f)) {
                    try { kf.y = Float.parseFloat(kfYBox.getValue()); } catch (Exception ignored) {}
                    state.markDirty();
                    return true;
                }
                if (scrollAdjustBox(kfZBox, mx, my, delta, 1.0f)) {
                    try { kf.z = Float.parseFloat(kfZBox.getValue()); } catch (Exception ignored) {}
                    state.markDirty();
                    return true;
                }
            }
        }

        if (showLibrary) {
            List<NpcAnimationData> lib = libStateFilter == null
                    ? NpcAnimationLibrary.getAll()
                    : NpcAnimationLibrary.getByState(libStateFilter);
            int maxScroll = Math.max(0, lib.size() - 10);
            libScroll = Math.max(0, Math.min(maxScroll, libScroll - (int) Math.signum(delta)));
        } else {
            int maxScroll = Math.max(0, state.getDraft().animations.size() - 9);
            animScroll = Math.max(0, Math.min(maxScroll, animScroll - (int) Math.signum(delta)));
        }
        return true;
    }

    /** Adjusts an EditBox value by step*delta if the cursor is inside the box. Returns true if adjusted. */
    private static boolean scrollAdjustBox(EditBox box, double mx, double my, double delta, float step) {
        if (box == null) return false;
        if (mx < box.getX() || mx > box.getX() + box.getWidth()) return false;
        if (my < box.getY() || my > box.getY() + box.getHeight()) return false;
        try {
            float val = Float.parseFloat(box.getValue());
            val += (float)(Math.signum(delta) * step);
            box.setValue(String.format("%.2f", val));
            return true;
        } catch (Exception ignored) {
            return false;
        }
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

    private List<AnimationKeyframe> getFrames(AnimationBone bone) {
        return "rotation".equals(channelMode) ? bone.rotationFrames : bone.positionFrames;
    }

}

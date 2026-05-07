package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SaveNpcEntityDataPacket;
import com.frametrip.dragonlegacyquesttoast.server.event.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Visual event-chain editor.
 *
 * Layout (760×500):
 *   Left  200px  — chain list (cards) + Add/Duplicate/Delete
 *   Right 560px  — selected chain editor:
 *       Row 1: chain name + enable toggle + conditionMode + executeAll
 *       Col A 185px  — Trigger section
 *       Col B 185px  — Conditions section
 *       Col C 185px  — Actions section
 */
public class EventChainScreen extends Screen {

    private static final int W = 760, H = 500;
    private static final int LIST_W = 190;
    private static final int COL_W  = 185;
    private static final int PAD    = 5;

    private static final int C_BG     = 0xFF0D0D1A;
    private static final int C_PANEL  = 0xFF14142B;
    private static final int C_BORDER = 0xFF2A2A44;
    private static final int C_ACCENT = 0xFFFF8844;
    private static final int C_TEXT   = 0xFFCCCCDD;
    private static final int C_DIM    = 0xFF666677;

    private final NpcEntity npc;
    private NpcEntityData draft;

    private EventChain selectedChain = null;
    private int chainScroll = 0;

    private int selectedCondIdx = -1;
    private int selectedActionIdx = -1;

    private EditBox chainNameBox;
    private EditBox triggerParamBox;
    private EditBox condParamBox;
    private EditBox actionParamBox;

    private int guiLeft, guiTop;

    public EventChainScreen(NpcEntity npc) {
        super(Component.literal("Редактор событий — " + npc.getNpcData().displayName));
        this.npc = npc;
        this.draft = npc.getNpcData().copy();
    }

    @Override
    protected void init() {
        guiLeft = (width - W) / 2;
        guiTop  = (height - H) / 2;
        rebuild();
    }

    private void rebuild() {
        clearWidgets();
        int lx = guiLeft + PAD;
        int ly = guiTop + PAD;

        addRenderableWidget(Button.builder(Component.literal("+ Добавить событие"), b -> {
            EventChain chain = new EventChain();
            draft.eventChains.add(chain);
            selectedChain = chain;
            selectedCondIdx = -1;
            selectedActionIdx = -1;
            rebuild();
        }).bounds(lx, ly, LIST_W, 16).build());
        ly += 20;

        List<EventChain> chains = draft.eventChains;
        int visChains = 14;
        chainScroll = Math.max(0, Math.min(chainScroll, Math.max(0, chains.size() - visChains)));

        for (int i = chainScroll; i < Math.min(chains.size(), chainScroll + visChains); i++) {
            EventChain chain = chains.get(i);
            boolean sel = chain == selectedChain;
            String label = (chain.enabled ? "§a● §r" : "§8○ §r")
                    + (sel ? "§e" : "") + truncate(chain.name, 18);
            final int fi = i;
            addRenderableWidget(Button.builder(Component.literal(label), b -> {
                selectedChain = draft.eventChains.get(fi);
                selectedCondIdx = -1;
                selectedActionIdx = -1;
                rebuild();
            }).bounds(lx, ly, LIST_W, 14).build());
            ly += 15;
        }

        int btnY = guiTop + H - 22;
        if (selectedChain != null) {
            addRenderableWidget(Button.builder(Component.literal("⧉ Копия"), b -> {
                EventChain copy = selectedChain.copy();
                draft.eventChains.add(copy);
                selectedChain = copy;
                rebuild();
            }).bounds(lx, btnY, 58, 14).build());

            addRenderableWidget(Button.builder(Component.literal("§c✕"), b -> {
                draft.eventChains.remove(selectedChain);
                selectedChain = draft.eventChains.isEmpty() ? null : draft.eventChains.get(0);
                selectedCondIdx = -1;
                selectedActionIdx = -1;
                rebuild();
            }).bounds(lx + 62, btnY, 22, 14).build());
        }

        addRenderableWidget(Button.builder(Component.literal("§aСохранить"), b -> save())
                .bounds(guiLeft + W - 110, guiTop + H - 22, 56, 14).build());
        addRenderableWidget(Button.builder(Component.literal("§cЗакрыть"), b -> onClose())
                .bounds(guiLeft + W - 52, guiTop + H - 22, 50, 14).build());

        if (selectedChain == null) return;

        int rx = guiLeft + LIST_W + PAD * 2;
        int ry = guiTop + PAD;

        chainNameBox = new EditBox(font, rx, ry, 180, 14, Component.literal("Название"));
        chainNameBox.setValue(selectedChain.name);
        addRenderableWidget(chainNameBox);

        addRenderableWidget(Button.builder(
                Component.literal(selectedChain.enabled ? "§aВКЛ" : "§8ВЫКЛ"),
                b -> { selectedChain.enabled = !selectedChain.enabled; rebuild(); }
        ).bounds(rx + 184, ry, 42, 14).build());

        addRenderableWidget(Button.builder(
                Component.literal(selectedChain.conditionMode),
                b -> {
                    selectedChain.conditionMode = "AND".equals(selectedChain.conditionMode) ? "OR" : "AND";
                    rebuild();
                }
        ).bounds(rx + 230, ry, 36, 14).build());

        addRenderableWidget(Button.builder(
                Component.literal(selectedChain.executeAll ? "§aВсе цепи" : "§eПервая"),
                b -> { selectedChain.executeAll = !selectedChain.executeAll; rebuild(); }
        ).bounds(rx + 270, ry, 70, 14).build());
        ry += 20;

        int colAx = rx;
        int colBx = rx + COL_W + PAD;
        int colCx = rx + (COL_W + PAD) * 2;

        buildTriggerColumn(colAx, ry);
        buildConditionColumn(colBx, ry);
        buildActionColumn(colCx, ry);
    }

    private void buildTriggerColumn(int cx, int cy) {
        EventTriggerType[] types = EventTriggerType.values();
        int idx = selectedChain.trigger.ordinal();

        addRenderableWidget(Button.builder(Component.literal("◀"), b -> {
            selectedChain.trigger = types[Math.floorMod(idx - 1, types.length)];
            rebuild();
        }).bounds(cx, cy, 20, 14).build());

        addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
            selectedChain.trigger = types[(idx + 1) % types.length];
            rebuild();
        }).bounds(cx + COL_W - 22, cy, 20, 14).build());
        cy += 18;

        String hint = selectedChain.trigger.paramHint();
        if (!hint.isEmpty()) {
            String paramKey = triggerParamKey(selectedChain.trigger);
            triggerParamBox = new EditBox(font, cx, cy, COL_W, 14, Component.literal(hint));
            triggerParamBox.setValue(selectedChain.triggerParam(paramKey));
            addRenderableWidget(triggerParamBox);
        }
    }

    private void buildConditionColumn(int cx, int cy) {
        addRenderableWidget(Button.builder(Component.literal("+ Условие"), b -> {
            selectedChain.conditions.add(new EventCondition());
            selectedCondIdx = selectedChain.conditions.size() - 1;
            rebuild();
        }).bounds(cx, cy, COL_W, 14).build());
        cy += 18;

        List<EventCondition> conds = selectedChain.conditions;
        for (int i = 0; i < Math.min(conds.size(), 6); i++) {
            EventCondition cond = conds.get(i);
            boolean sel = i == selectedCondIdx;
            final int fi = i;

            addRenderableWidget(Button.builder(
                    Component.literal((sel ? "§e▶ " : "  ") + truncate(cond.type.label(), 16)),
                    b -> { selectedCondIdx = fi; selectedActionIdx = -1; rebuild(); }
            ).bounds(cx, cy, COL_W - 20, 12).build());

            addRenderableWidget(Button.builder(Component.literal("§c✕"), b -> {
                conds.remove(fi);
                selectedCondIdx = -1;
                rebuild();
            }).bounds(cx + COL_W - 18, cy, 16, 12).build());
            cy += 14;
        }

        if (selectedCondIdx >= 0 && selectedCondIdx < conds.size()) {
            EventCondition cond = conds.get(selectedCondIdx);
            EventConditionType[] types = EventConditionType.values();
            int idx = cond.type.ordinal();

            addRenderableWidget(Button.builder(Component.literal("◀"), b -> {
                cond.type = types[Math.floorMod(idx - 1, types.length)];
                rebuild();
            }).bounds(cx, cy, 20, 13).build());
            addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
                cond.type = types[(idx + 1) % types.length];
                rebuild();
            }).bounds(cx + COL_W - 22, cy, 20, 13).build());
            cy += 16;

            String paramKey = condParamKey(cond.type);
            if (!paramKey.isEmpty()) {
                condParamBox = new EditBox(font, cx, cy, COL_W, 13, Component.literal(condParamLabel(cond.type)));
                condParamBox.setValue(cond.param(paramKey));
                addRenderableWidget(condParamBox);
            }
        }
    }

    private void buildActionColumn(int cx, int cy) {
        addRenderableWidget(Button.builder(Component.literal("+ Действие"), b -> {
            selectedChain.actions.add(new EventAction());
            selectedActionIdx = selectedChain.actions.size() - 1;
            rebuild();
        }).bounds(cx, cy, COL_W, 14).build());
        cy += 18;

        List<EventAction> acts = selectedChain.actions;
        for (int i = 0; i < Math.min(acts.size(), 6); i++) {
            EventAction act = acts.get(i);
            boolean sel = i == selectedActionIdx;
            final int fi = i;

            addRenderableWidget(Button.builder(
                    Component.literal((sel ? "§e▶ " : "  ") + truncate(act.type.label(), 16)),
                    b -> { selectedActionIdx = fi; selectedCondIdx = -1; rebuild(); }
            ).bounds(cx, cy, COL_W - 20, 12).build());

            addRenderableWidget(Button.builder(Component.literal("§c✕"), b -> {
                acts.remove(fi);
                selectedActionIdx = -1;
                rebuild();
            }).bounds(cx + COL_W - 18, cy, 16, 12).build());
            cy += 14;
        }

        if (selectedActionIdx >= 0 && selectedActionIdx < acts.size()) {
            EventAction act = acts.get(selectedActionIdx);
            EventActionType[] types = EventActionType.values();
            int idx = act.type.ordinal();

            addRenderableWidget(Button.builder(Component.literal("◀"), b -> {
                act.type = types[Math.floorMod(idx - 1, types.length)];
                rebuild();
            }).bounds(cx, cy, 20, 13).build());
            addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
                act.type = types[(idx + 1) % types.length];
                rebuild();
            }).bounds(cx + COL_W - 22, cy, 20, 13).build());
            cy += 16;

            String paramKey = actionParamKey(act.type);
            if (!paramKey.isEmpty()) {
                actionParamBox = new EditBox(font, cx, cy, COL_W, 13,
                        Component.literal(actionParamLabel(act.type)));
                actionParamBox.setValue(act.param(paramKey));
                addRenderableWidget(actionParamBox);
                cy += 16;
            }

            String param2Key = actionParam2Key(act.type);
            if (!param2Key.isEmpty()) {
                EditBox p2 = new EditBox(font, cx, cy, COL_W, 13,
                        Component.literal(actionParam2Label(act.type)));
                p2.setValue(act.param(param2Key));
                final String pk2 = param2Key;
                p2.setResponder(v -> act.param(pk2, v));
                addRenderableWidget(p2);
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(guiLeft, guiTop, guiLeft + W, guiTop + H, C_BG);
        border(g, guiLeft, guiTop, W, H);

        g.fill(guiLeft + PAD, guiTop + PAD, guiLeft + LIST_W + PAD, guiTop + H - PAD, C_PANEL);
        border(g, guiLeft + PAD, guiTop + PAD, LIST_W, H - PAD * 2);

        if (selectedChain != null) {
            int rx = guiLeft + LIST_W + PAD * 2;
            int ry = guiTop + PAD + 22;
            int colBx = rx + COL_W + PAD;
            int colCx = rx + (COL_W + PAD) * 2;

            columnHeader(g, rx,    ry - 4, COL_W, "СОБЫТИЕ",  0xFFFF6644);
            columnHeader(g, colBx, ry - 4, COL_W, "УСЛОВИЕ",  0xFF4488FF);
            columnHeader(g, colCx, ry - 4, COL_W, "ДЕЙСТВИЕ", 0xFF44CC66);

            g.drawCenteredString(font, "§e" + selectedChain.trigger.label(),
                    rx + COL_W / 2, ry + 5, C_TEXT);

            g.drawCenteredString(font,
                    "§8Режим: §f" + ("AND".equals(selectedChain.conditionMode) ? "ВСЕ (AND)" : "ЛЮБОЕ (OR)"),
                    colBx + COL_W / 2, ry - 16, C_DIM);
        }

        g.drawString(font, "§l⚡ РЕДАКТОР СОБЫТИЙ  §7— " + npc.getNpcData().displayName,
                guiLeft + PAD + 2, guiTop + H - 10, C_ACCENT, false);

        super.render(g, mx, my, pt);
    }

    private void columnHeader(GuiGraphics g, int x, int y, int w, String label, int color) {
        g.fill(x, y, x + w, y + 12, C_PANEL);
        border(g, x, y, w, 12);
        g.drawCenteredString(font, label, x + w / 2, y + 2, color);
    }

    private static void border(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x,         y,         x + w, y + 1,     C_BORDER);
        g.fill(x,         y + h - 1, x + w, y + h,     C_BORDER);
        g.fill(x,         y,         x + 1, y + h,     C_BORDER);
        g.fill(x + w - 1, y,         x + w, y + h,     C_BORDER);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        int lx = guiLeft + PAD;
        if (mx >= lx && mx <= lx + LIST_W) {
            chainScroll = Math.max(0, Math.min(
                    Math.max(0, draft.eventChains.size() - 14),
                    chainScroll - (int) Math.signum(delta)
            ));
            return true;
        }
        return super.mouseScrolled(mx, my, delta);
    }

    @Override
    public void onClose() {
        pullFields();
        save();
        super.onClose();
    }

    private void pullFields() {
        if (selectedChain == null) return;

        if (chainNameBox != null) {
            selectedChain.name = chainNameBox.getValue();
        }
        if (triggerParamBox != null) {
            String key = triggerParamKey(selectedChain.trigger);
            if (!key.isEmpty()) selectedChain.triggerParam(key, triggerParamBox.getValue());
        }
        if (selectedCondIdx >= 0 && selectedCondIdx < selectedChain.conditions.size() && condParamBox != null) {
            EventCondition cond = selectedChain.conditions.get(selectedCondIdx);
            String key = condParamKey(cond.type);
            if (!key.isEmpty()) cond.param(key, condParamBox.getValue());
        }
        if (selectedActionIdx >= 0 && selectedActionIdx < selectedChain.actions.size() && actionParamBox != null) {
            EventAction act = selectedChain.actions.get(selectedActionIdx);
            String key = actionParamKey(act.type);
            if (!key.isEmpty()) act.param(key, actionParamBox.getValue());
        }
    }

    private void save() {
        pullFields();
        ModNetwork.CHANNEL.sendToServer(new SaveNpcEntityDataPacket(npc.getUUID(), draft));
        npc.setNpcData(draft);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private static String triggerParamKey(EventTriggerType type) {
        return switch (type) {
            case CHAT_MESSAGE  -> "phrase";
            case ZONE_ENTER    -> "radius";
            case ITEM_RECEIVED -> "itemId";
            case TIMER         -> "interval";
            case QUEST_COMPLETE, SCENE_START, SCENE_END -> "id";
            case TIME_CHANGE   -> "time";
            default            -> "";
        };
    }

    private static String condParamKey(EventConditionType type) {
        return switch (type) {
            case ITEM_IN_INVENTORY -> "itemId";
            case QUEST_STATUS      -> "questId";
            case TIME_OF_DAY       -> "time";
            case IN_ZONE           -> "radius";
            case NPC_PROFESSION    -> "profession";
            case NPC_STATE         -> "state";
            case REPUTATION        -> "value";
        };
    }

    private static String condParamLabel(EventConditionType type) {
        return switch (type) {
            case ITEM_IN_INVENTORY -> "ID предмета";
            case QUEST_STATUS      -> "ID квеста";
            case TIME_OF_DAY       -> "День / Ночь / Рассвет / Закат";
            case IN_ZONE           -> "Радиус";
            case NPC_PROFESSION    -> "Профессия";
            case NPC_STATE         -> "Состояние";
            case REPUTATION        -> "Значение";
        };
    }

    private static String actionParamKey(EventActionType type) {
        return switch (type) {
            case SAY_PHRASE     -> "phrase";
            case OPEN_DIALOGUE  -> "dialogueId";
            case START_SCENE    -> "sceneId";
            case GIVE_ITEM      -> "itemId";
            case GIVE_QUEST, COMPLETE_QUEST -> "questId";
            case SET_NPC_STATE  -> "state";
            case PLAY_ANIMATION -> "animName";
            case TELEPORT       -> "x";
            default             -> "";
        };
    }

    private static String actionParamLabel(EventActionType type) {
        return switch (type) {
            case SAY_PHRASE     -> "Текст фразы";
            case OPEN_DIALOGUE  -> "ID диалога";
            case START_SCENE    -> "ID сцены";
            case GIVE_ITEM      -> "ID предмета";
            case GIVE_QUEST, COMPLETE_QUEST -> "ID квеста";
            case SET_NPC_STATE  -> "Состояние";
            case PLAY_ANIMATION -> "Имя анимации";
            case TELEPORT       -> "X";
            default             -> "";
        };
    }

    private static String actionParam2Key(EventActionType type) {
        return switch (type) {
            case GIVE_ITEM -> "qty";
            case TELEPORT  -> "y";
            default        -> "";
        };
    }

    private static String actionParam2Label(EventActionType type) {
        return switch (type) {
            case GIVE_ITEM -> "Кол-во";
            case TELEPORT  -> "Y";
            default        -> "";
        };
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}

package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SaveQuestChainPacket;
import com.frametrip.dragonlegacyquesttoast.server.QuestDefinition;
import com.frametrip.dragonlegacyquesttoast.server.quest.QuestChain;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

// [QST-1]: Chain editor — create/edit quest chains with visual link list.
public class QuestChainEditorScreen extends Screen {

    private static final int W = 500, H = 380;
    private static final int LW = 150; // chain list panel width
    private static final int ACCENT = 0xFF88DDFF;

    private final Screen parent;
    private List<QuestChain> chains = new ArrayList<>();
    private int selectedIdx = -1;
    private QuestChain editing = null;
    private int listScroll = 0;

    private EditBox nameBox;
    private final List<EditBox> linkQuestBoxes = new ArrayList<>();
    private final List<EditBox> linkUnlockBoxes = new ArrayList<>();

    public QuestChainEditorScreen(Screen parent) {
        super(Component.literal("Редактор цепочек квестов"));
        this.parent = parent;
    }

    private int ox() { return (width  - W) / 2; }
    private int oy() { return (height - H) / 2; }

    @Override
    protected void init() {
        super.init();
        chains = new ArrayList<>(ClientQuestChainState.getAll());
        linkQuestBoxes.clear();
        linkUnlockBoxes.clear();
        nameBox = null;
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        clearWidgets();
        int ox = ox(), oy = oy();

        // ── Chain list ────────────────────────────────────────────────────────
        int listY = oy + 22;
        for (int i = 0; i < chains.size(); i++) {
            int fi = i;
            QuestChain c = chains.get(i);
            boolean sel = fi == selectedIdx;
            Button btn = Button.builder(
                Component.literal((sel ? "§e▶ " : "§7  ") + c.chainName),
                b -> { selectedIdx = fi; editing = chains.get(fi).copy(); init(); }
            ).bounds(ox + 2, listY + (i - listScroll) * 14, LW - 4, 12).build();
            addRenderableWidget(btn);
        }

        // New chain button.
        addRenderableWidget(Button.builder(Component.literal("§a+ Цепочка"),
            b -> {
                QuestChain nc = new QuestChain();
                chains.add(nc);
                selectedIdx = chains.size() - 1;
                editing = nc.copy();
                init();
            }
        ).bounds(ox + 2, oy + H - 20, LW - 4, 14).build());

        if (editing == null) return;

        // ── Editor panel ──────────────────────────────────────────────────────
        int rx = ox + LW + 4, rw = W - LW - 8;
        int y = oy + 22;

        nameBox = new EditBox(font, rx, y, rw, 14, Component.literal("Название цепочки"));
        nameBox.setValue(editing.chainName);
        nameBox.setMaxLength(64);
        addRenderableWidget(nameBox);
        y += 18;

        // Render up to 8 link slots.
        linkQuestBoxes.clear();
        linkUnlockBoxes.clear();
        int maxLinks = Math.min(editing.links.size() + 1, 8);
        for (int i = 0; i < maxLinks; i++) {
            QuestChain.QuestChainLink link = i < editing.links.size()
                ? editing.links.get(i) : null;

            EditBox qBox = new EditBox(font, rx, y, rw / 2 - 2, 14, Component.literal("ID квеста"));
            qBox.setValue(link != null ? link.questId : "");
            qBox.setMaxLength(64);
            addRenderableWidget(qBox);
            linkQuestBoxes.add(qBox);

            EditBox uBox = new EditBox(font, rx + rw / 2 + 2, y, rw / 2 - 2, 14,
                Component.literal("Разблокирует (через запятую)"));
            uBox.setValue(link != null ? String.join(",", link.unlocksOnComplete) : "");
            uBox.setMaxLength(256);
            addRenderableWidget(uBox);
            linkUnlockBoxes.add(uBox);
            y += 16;
        }

        // Save button.
        addRenderableWidget(Button.builder(Component.literal("§aSохранить"), b -> save())
            .bounds(rx, oy + H - 20, 80, 14).build());
        // Delete button.
        if (selectedIdx >= 0) {
            addRenderableWidget(Button.builder(Component.literal("§cУдалить"), b -> {
                chains.remove(selectedIdx);
                selectedIdx = -1;
                editing = null;
                init();
            }).bounds(rx + 84, oy + H - 20, 60, 14).build());
        }
        // Back button.
        addRenderableWidget(Button.builder(Component.literal("§7← Назад"),
            b -> minecraft.setScreen(parent)
        ).bounds(ox + W - 68, oy + H - 20, 66, 14).build());
    }

    private void save() {
        if (editing == null) return;
        if (nameBox != null) editing.chainName = nameBox.getValue().trim();

        editing.links.clear();
        for (int i = 0; i < linkQuestBoxes.size(); i++) {
            String qid = linkQuestBoxes.get(i).getValue().trim();
            if (qid.isEmpty()) continue;
            QuestChain.QuestChainLink link = new QuestChain.QuestChainLink();
            link.questId = qid;
            if (i < linkUnlockBoxes.size()) {
                String raw = linkUnlockBoxes.get(i).getValue().trim();
                if (!raw.isEmpty()) {
                    for (String part : raw.split(",")) {
                        String p = part.trim();
                        if (!p.isEmpty()) link.unlocksOnComplete.add(p);
                    }
                }
            }
            editing.links.add(link);
        }
        ModNetwork.CHANNEL.sendToServer(new SaveQuestChainPacket(editing));
        if (selectedIdx >= 0 && selectedIdx < chains.size()) {
            chains.set(selectedIdx, editing.copy());
        }
        minecraft.setScreen(this); // refresh
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int ox = ox(), oy = oy();

        // Window frame.
        g.fill(ox, oy, ox + W, oy + H, 0xFF1A1A2E);
        g.fill(ox, oy, ox + W, oy + 18, 0xFF2255AA);
        g.drawString(font, "§b§lЦЕПОЧКИ КВЕСТОВ", ox + 4, oy + 4, ACCENT, false);

        // Divider between list and editor.
        g.fill(ox + LW, oy + 18, ox + LW + 2, oy + H, 0xFF334466);

        // Column labels when editing.
        if (editing != null) {
            int rx = ox + LW + 4, rw = W - LW - 8;
            g.drawString(font, "§7Звено", rx, oy + 20, 0xFFAAAAAA, false);
            g.drawString(font, "§7ID квеста", rx, oy + 40, 0xFF888888, false);
            g.drawString(font, "§7Разблокирует", rx + rw / 2 + 2, oy + 40, 0xFF888888, false);
        }

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}

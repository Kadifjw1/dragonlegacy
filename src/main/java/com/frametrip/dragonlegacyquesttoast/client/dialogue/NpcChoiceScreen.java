package com.frametrip.dragonlegacyquesttoast.client.dialogue;

import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcChoiceOption;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcSceneNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Overlay screen for question nodes — shows NPC question and player's choices. */
public class NpcChoiceScreen extends Screen {

    private static final int W = 420;
    private static final int CHOICE_H = 20;
    private static final int PAD = 12;

    private final String npcName;
    private final NpcSceneNode questionNode;
    /** Invoked with the chosen option's id (preferred) or with nextNodeId for legacy compatibility. */
    private final Consumer<String> onChoice;

    private final List<NpcChoiceOption> visibleChoices = new ArrayList<>();

    public NpcChoiceScreen(String npcName, NpcSceneNode questionNode, Consumer<String> onChoice) {
        super(Component.literal("Диалог"));
        this.npcName      = npcName;
        this.questionNode = questionNode;
        this.onChoice     = onChoice;
        if (questionNode.choices != null) {
            for (NpcChoiceOption o : questionNode.choices) {
                if (NpcSceneChoiceFilter.isAvailable(o)) visibleChoices.add(o);
            }
        }
    }

    @Override
    protected void init() {
        int ox = (width - W) / 2;
        int totalH = computeHeight();
        int oy = (height - totalH) / 2;

        int btnY = oy + PAD + 10 + 16 + PAD + wrapCount(questionNode.text) * 10 + 8;

        for (int i = 0; i < visibleChoices.size(); i++) {
            NpcChoiceOption opt = visibleChoices.get(i);
            final String payload = (opt.id != null && !opt.id.isEmpty()) ? opt.id : opt.nextNodeId;
            addRenderableWidget(Button.builder(
                    Component.literal("§e▶ §f" + opt.text),
                    b -> choose(payload)
            ).bounds(ox + PAD, btnY + i * (CHOICE_H + 4), W - PAD * 2, CHOICE_H).build());
        }
        
        if (visibleChoices.isEmpty()) {
            addRenderableWidget(Button.builder(
                    Component.literal("§8▶ Закрыть"),
                    b -> choose("")
            ).bounds(ox + PAD, btnY, W - PAD * 2, CHOICE_H).build());
        }
    }

    private void choose(String payload) {
        onClose();
        onChoice.accept(payload);
    }

    private int wrapCount(String text) {
        if (text == null || text.isEmpty()) return 1;
        int innerW = W - PAD * 2;
        return Math.max(1, Minecraft.getInstance()
                .font.split(Component.literal(text), innerW).size());
    }

    private int computeHeight() {
        int nameH   = 16;
        int textH   = wrapCount(questionNode.text) * 10 + 8;
        int count   = Math.max(1, visibleChoices.size());
        int choiceH = count * (CHOICE_H + 4);        
        return PAD + nameH + PAD + textH + choiceH + PAD;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0xAA000011);

        int ox = (width - W) / 2;
        int totalH = computeHeight();
        int oy = (height - totalH) / 2;

        g.fill(ox, oy, ox + W, oy + totalH, 0xEE0D0D1A);
        brd(g, ox, oy, W, totalH, 0xFF5544AA);

        g.fill(ox, oy, ox + W, oy + PAD + 10, 0xBB1A1040);
        g.drawCenteredString(font, "§d" + npcName, ox + W / 2, oy + PAD / 2 + 1, 0xFFCFA8FF);

        g.fill(ox + PAD, oy + PAD + 12, ox + W - PAD, oy + PAD + 13, 0xFF5544AA);

        int textY = oy + PAD + 16 + 4;
        int innerW = W - PAD * 2;
        var formattedLines = font.split(Component.literal("§f" + questionNode.text), innerW);
        for (int i = 0; i < formattedLines.size(); i++) {
            g.drawString(font, formattedLines.get(i), ox + PAD, textY + i * 10, 0xFFFFFFFF, false);
        }

        int textH = formattedLines.size() * 10 + 8;
        g.fill(ox + PAD, oy + PAD + 16 + textH, ox + W - PAD, oy + PAD + 16 + textH + 1, 0xFF333355);

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return false; }

    private static void brd(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x,     y,     x + w, y + 1, c);
        g.fill(x,     y+h-1, x + w, y + h, c);
        g.fill(x,     y,     x + 1, y + h, c);
        g.fill(x+w-1, y,     x + w, y + h, c);
    }
}

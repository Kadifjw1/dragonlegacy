package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

// [EDT-2]: NPC template picker — select a built-in template to apply to the current draft.
public class NpcTemplateScreen extends Screen {

    private static final int W = 360;
    private static final int H = 240;

    private final Consumer<NpcEntityData> onApply;

    public NpcTemplateScreen(Consumer<NpcEntityData> onApply) {
        super(Component.literal("Шаблоны NPC"));
        this.onApply = onApply;
    }

    @Override
    protected void init() {
        int cx = (width  - W) / 2;
        int cy = (height - H) / 2;
        int bw = (W - 40) / 3;
        int col = 0, row = 0;

        for (NpcTemplateLibrary.Template tmpl : NpcTemplateLibrary.TEMPLATES) {
            int bx = cx + 20 + col * (bw + 8);
            int by = cy + 40 + row * 58;
            final NpcTemplateLibrary.Template t = tmpl;
            addRenderableWidget(Button.builder(
                    Component.literal(tmpl.icon() + " " + tmpl.label()),
                    b -> {
                        NpcEntityData data = t.factory().get();
                        onApply.accept(data);
                        onClose();
                    }
            ).bounds(bx, by, bw, 40).build());

            col++;
            if (col >= 3) { col = 0; row++; }
        }

        addRenderableWidget(Button.builder(
                Component.literal("Отмена"),
                b -> onClose()
        ).bounds(cx + W / 2 - 30, cy + H - 22, 60, 16).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int cx = (width  - W) / 2;
        int cy = (height - H) / 2;

        g.fill(cx, cy, cx + W, cy + H, 0xCC222233);
        g.fill(cx, cy, cx + W, cy + 20, 0xFF333355);
        g.drawCenteredString(font, "§l📚 Шаблоны NPC", cx + W / 2, cy + 6, 0xFFEECC66);

        // Description of hovered template
        for (NpcTemplateLibrary.Template tmpl : NpcTemplateLibrary.TEMPLATES) {
            int col = java.util.Arrays.asList(NpcTemplateLibrary.TEMPLATES).indexOf(tmpl);
            int bw = (W - 40) / 3;
            int bx = cx + 20 + (col % 3) * (bw + 8);
            int by = cy + 40 + (col / 3) * 58;
            if (mx >= bx && mx < bx + bw && my >= by && my < by + 40) {
                g.drawCenteredString(font, "§8" + tmpl.description(), cx + W / 2, cy + H - 36, 0xFFAAAAAA);
            }
        }

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}

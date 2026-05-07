package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SetCompanionModePacket;
import com.frametrip.dragonlegacyquesttoast.server.companion.CompanionData;
import com.frametrip.dragonlegacyquesttoast.server.companion.CompanionMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import org.joml.Quaternionf;

import java.util.UUID;

/**
 * Companion control screen.
 *
 * Layout (420 × 320):
 *   Left 100px  — 3-D NPC preview
 *   Right 300px — mode buttons + sliders
 *
 * Sends SetCompanionModePacket immediately on each button press.
 */
public class CompanionControlScreen extends Screen {

    private static final int W = 420, H = 320;
    private static final int PREVIEW_W = 100;

    private static final int C_BG     = 0xFF0D0D1A;
    private static final int C_PANEL  = 0xFF14142B;
    private static final int C_BORDER = 0xFF2A2A44;
    private static final int C_ACCENT = 0xFFFF8844;
    private static final int C_TEXT   = 0xFFCCCCDD;
    private static final int C_DIM    = 0xFF555566;

    private static final int SLIDER_STEPS = 10;

    private final NpcEntity npc;
    private final UUID npcUuid;
    private final CompanionData cd;

    private int guiLeft, guiTop;

    public CompanionControlScreen(NpcEntity npc, UUID npcUuid) {
        super(Component.literal("Управление спутником"));
        this.npc = npc;
        this.npcUuid = npcUuid;
        NpcEntityData data = npc.getNpcData();
        this.cd = data.companionData != null ? data.companionData.copy() : new CompanionData();
    }

    @Override
    protected void init() {
        guiLeft = (width - W) / 2;
        guiTop = (height - H) / 2;
        rebuild();
    }

    private void rebuild() {
        clearWidgets();

        int rx = guiLeft + PREVIEW_W + 10;
        int ry = guiTop + 32;

        CompanionMode[] modes = CompanionMode.values();
        int btnW = 135, btnH = 26, gap = 4, cols = 2;

        for (int i = 0; i < modes.length; i++) {
            CompanionMode m = modes[i];
            int col = i % cols;
            int row = i / cols;
            boolean active = m == cd.mode;

            addRenderableWidget(Button.builder(
                    Component.literal((active ? "§e■ " : "§8□ ") + m.label()),
                    b -> {
                        cd.setMode(m);
                        sendPacket();
                        rebuild();
                    }
            ).bounds(rx + col * (btnW + gap), ry + row * (btnH + gap), btnW, btnH).build());
        }

        ry += ((modes.length + 1) / cols) * (btnH + gap) + 10;

        buildSlider(rx, ry, "Дистанция следования", cd.followDistance, 1f, 12f, v -> {
            cd.followDistance = v;
            sendPacket();
        });
        ry += 24;

        buildSlider(rx, ry, "Агрессивность", cd.aggressiveness, 0f, 1f, v -> {
            cd.aggressiveness = v;
            sendPacket();
        });
        ry += 24;

        if (cd.mode == CompanionMode.GUARD) {
            buildSlider(rx, ry, "Радиус охраны", cd.guardRadius, 2f, 20f, v -> {
                cd.guardRadius = v;
                sendPacket();
            });
        }

        addRenderableWidget(Button.builder(Component.literal("Закрыть"), b -> onClose())
                .bounds(guiLeft + W - 74, guiTop + H - 22, 70, 16).build());
    }

    private void buildSlider(int x, int y, String label, float current, float min, float max,
                             java.util.function.Consumer<Float> onChanged) {
        float step = (max - min) / SLIDER_STEPS;

        addRenderableWidget(Button.builder(Component.literal("◀"), b -> {
            onChanged.accept(Math.max(min, round1(current - step)));
            rebuild();
        }).bounds(x + 130, y, 16, 14).build());

        addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
            onChanged.accept(Math.min(max, round1(current + step)));
            rebuild();
        }).bounds(x + 170, y, 16, 14).build());
    }

    private static float round1(float v) {
        return Math.round(v * 10f) / 10f;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(guiLeft, guiTop, guiLeft + W, guiTop + H, C_BG);
        border(g, guiLeft, guiTop, W, H);

        g.fill(guiLeft, guiTop, guiLeft + PREVIEW_W, guiTop + H, C_PANEL);
        border(g, guiLeft, guiTop, PREVIEW_W, H);

        int previewCX = guiLeft + PREVIEW_W / 2;
        int previewCY = guiTop + H / 2 + 20;

        float yaw = (float) Math.atan((previewCX - mx) / 40.0F);
        float pitch = (float) Math.atan((previewCY - 80 - my) / 40.0F);

        Quaternionf bodyRot = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf headRot = new Quaternionf().rotateX(pitch * 0.35F);
        bodyRot.mul(headRot);

        InventoryScreen.renderEntityInInventory(g, previewCX, previewCY, 28, headRot, bodyRot, npc);

        g.drawString(font, "§l" + npc.getNpcData().displayName, guiLeft + PREVIEW_W + 10, guiTop + 6, C_ACCENT, false);
        g.drawString(font, "§7Режим: §e" + cd.mode.label(), guiLeft + PREVIEW_W + 10, guiTop + 18, C_TEXT, false);

        int rx = guiLeft + PREVIEW_W + 10;
        int sliderY0 = modeButtonsBottom();

        renderSliderRow(g, rx, sliderY0, "Дистанция следования", cd.followDistance, 1f, 12f);
        renderSliderRow(g, rx, sliderY0 + 24, "Агрессивность", cd.aggressiveness, 0f, 1f);
        if (cd.mode == CompanionMode.GUARD) {
            renderSliderRow(g, rx, sliderY0 + 48, "Радиус охраны", cd.guardRadius, 2f, 20f);
        }

        g.drawString(font, "§8" + cd.mode.description(),
                guiLeft + PREVIEW_W + 10, guiTop + H - 32, C_DIM, false);

        super.render(g, mx, my, pt);
    }

    private void renderSliderRow(GuiGraphics g, int x, int y, String label, float val, float min, float max) {
        g.drawString(font, "§7" + label + ":", x, y + 2, C_TEXT, false);
        g.drawString(font, "§f" + round1(val), x + 148, y + 2, C_ACCENT, false);

        int barX = x, barY = y + 13, barW = 120, barH = 3;
        g.fill(barX, barY, barX + barW, barY + barH, C_PANEL);
        float pct = (val - min) / (max - min);
        g.fill(barX, barY, barX + (int) (barW * pct), barY + barH, C_ACCENT);
    }

    private int modeButtonsBottom() {
        int rows = (CompanionMode.values().length + 1) / 2;
        return guiTop + 32 + rows * 30 + 10;
    }

    private static void border(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x,         y,         x + w, y + 1,     C_BORDER);
        g.fill(x,         y + h - 1, x + w, y + h,     C_BORDER);
        g.fill(x,         y,         x + 1, y + h,     C_BORDER);
        g.fill(x + w - 1, y,         x + w, y + h,     C_BORDER);
    }

    private void sendPacket() {
        ModNetwork.CHANNEL.sendToServer(new SetCompanionModePacket(
                npcUuid, cd.mode, cd.followDistance, cd.aggressiveness, cd.guardRadius
        ));
    }

    @Override
    public boolean isPauseScreen() { return false; }
}

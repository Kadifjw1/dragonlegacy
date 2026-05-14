package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SetCompanionModePacket;
import com.frametrip.dragonlegacyquesttoast.server.companion.CompanionData;
import com.frametrip.dragonlegacyquesttoast.server.companion.CompanionMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import org.joml.Quaternionf;

import java.util.UUID;

/**
 * Companion control screen.
 *
 * Layout (460 × 360):
 *   Left  110px — 3-D NPC preview
 *   Right 330px — mode buttons, sliders, owner/guard/commands
 */
public class CompanionControlScreen extends Screen {

    private static final int W = 460, H = 360;
    private static final int PREVIEW_W = 110;

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
    private boolean bindOwnerPending = false;
    private boolean setGuardPending  = false;

    // Command word edit boxes — indexed by CompanionMode.ordinal()
    private final EditBox[] cmdBoxes = new EditBox[CompanionMode.values().length];

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
        guiTop  = (height - H) / 2;
        rebuild();
    }

    private void rebuild() {
        clearWidgets();
        java.util.Arrays.fill(cmdBoxes, null);

        int rx = guiLeft + PREVIEW_W + 10;
        int ry = guiTop + 32;

        // ── Mode buttons ──────────────────────────────────────────────────────
        CompanionMode[] modes = CompanionMode.values();
        int btnW = 130, btnH = 22, gap = 4, cols = 2;
        for (int i = 0; i < modes.length; i++) {
            CompanionMode m = modes[i];
            int col = i % cols;
            int row = i / cols;
            boolean active = m == cd.mode;
            addRenderableWidget(Button.builder(
                    Component.literal((active ? "§e■ " : "§8□ ") + m.label()),
                    b -> { cd.setMode(m); sendPacket(false, false); rebuild(); }
            ).bounds(rx + col * (btnW + gap), ry + row * (btnH + gap), btnW, btnH).build());
        }
        ry += ((modes.length + 1) / cols) * (btnH + gap) + 8;

        // ── Sliders ───────────────────────────────────────────────────────────
        buildSlider(rx, ry, cd.followDistance, 1f, 12f, v -> { cd.followDistance = v; sendPacket(false, false); });
        ry += 22;
        buildSlider(rx, ry, cd.aggressiveness, 0f, 1f, v -> { cd.aggressiveness = v; sendPacket(false, false); });
        ry += 22;
        if (cd.mode == CompanionMode.GUARD) {
            buildSlider(rx, ry, cd.guardRadius, 2f, 20f, v -> { cd.guardRadius = v; sendPacket(false, false); });
            ry += 22;
        }
        ry += 4;

        // ── Owner binding ─────────────────────────────────────────────────────
        addRenderableWidget(Button.builder(Component.literal("§a⚓ Привязать к себе"), b -> {
            sendPacket(true, false);
            cd.ownerUUID = Minecraft.getInstance().player != null
                    ? Minecraft.getInstance().player.getUUID().toString() : "";
            rebuild();
        }).bounds(rx, ry, 148, 14).build());
        ry += 18;

        // ── Guard point ───────────────────────────────────────────────────────
        if (cd.mode == CompanionMode.GUARD) {
            addRenderableWidget(Button.builder(Component.literal("§b📍 Точка охраны здесь"), b -> {
                sendPacket(false, true);
                cd.guardPointSet = true;
                rebuild();
            }).bounds(rx, ry, 148, 14).build());
            ry += 18;
        }

        // ── Command words ─────────────────────────────────────────────────────
        for (int i = 0; i < modes.length; i++) {
            CompanionMode m = modes[i];
            cmdBoxes[i] = new EditBox(font, rx + 70, ry, 120, 12,
                    Component.literal(m.label()));
            cmdBoxes[i].setMaxLength(32);
            cmdBoxes[i].setValue(cd.commandFor(m));
            addRenderableWidget(cmdBoxes[i]);
            ry += 14;
        }

        addRenderableWidget(Button.builder(Component.literal("§7Сохранить команды"), b -> {
            pullCommands();
            sendPacket(false, false);
        }).bounds(rx, ry, 130, 13).build());
        ry += 18;

        // ── Close ─────────────────────────────────────────────────────────────
        addRenderableWidget(Button.builder(Component.literal("Закрыть"), b -> onClose())
                .bounds(guiLeft + W - 74, guiTop + H - 22, 70, 16).build());
    }

    private void buildSlider(int x, int y, float current, float min, float max,
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
        float yaw   = (float) Math.atan((previewCX - mx) / 40.0F);
        float pitch = (float) Math.atan((previewCY - 80 - my) / 40.0F);
        Quaternionf bodyRot = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf headRot = new Quaternionf().rotateX(pitch * 0.35F);
        bodyRot.mul(headRot);
        InventoryScreen.renderEntityInInventory(g, previewCX, previewCY, 28, headRot, bodyRot, npc);

        g.drawString(font, "§l" + npc.getNpcData().displayName,
                guiLeft + PREVIEW_W + 10, guiTop + 6, C_ACCENT, false);
        g.drawString(font, "§7Режим: §e" + cd.mode.label(),
                guiLeft + PREVIEW_W + 10, guiTop + 18, C_TEXT, false);

        int rx = guiLeft + PREVIEW_W + 10;
        int sliderY = modeButtonsBottom();

        renderSliderRow(g, rx, sliderY,      "Дистанция следования", cd.followDistance, 1f, 12f);
        renderSliderRow(g, rx, sliderY + 22, "Агрессивность",        cd.aggressiveness, 0f, 1f);
        if (cd.mode == CompanionMode.GUARD) {
            renderSliderRow(g, rx, sliderY + 44, "Радиус охраны", cd.guardRadius, 2f, 20f);
        }

        // Owner line
        int infoY = sliderY + (cd.mode == CompanionMode.GUARD ? 70 : 48);
        String ownerTxt = cd.ownerUUID.isEmpty() ? "§8никто" : "§a" + shortUUID(cd.ownerUUID);
        g.drawString(font, "§7Владелец: " + ownerTxt, rx, infoY, C_DIM, false);

        // Guard point
        if (cd.mode == CompanionMode.GUARD) {
            String gp = cd.guardPointSet
                    ? "§f" + String.format("%.0f %.0f %.0f", cd.guardX, cd.guardY, cd.guardZ)
                    : "§8не задана";
            g.drawString(font, "§7Точка охраны: " + gp, rx, infoY + 14, C_DIM, false);
        }

        // Commands label
        int cmdLabelY = infoY + (cd.mode == CompanionMode.GUARD ? 30 : 18);
        g.drawString(font, "§8Команды (чат):", rx, cmdLabelY, C_DIM, false);
        CompanionMode[] modes = CompanionMode.values();
        for (int i = 0; i < modes.length; i++) {
            g.drawString(font, "§7" + modes[i].label() + ":", rx, cmdLabelY + 14 + i * 14, C_DIM, false);
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
        g.fill(barX, barY, barX + (int)(barW * pct), barY + barH, C_ACCENT);
    }

    private int modeButtonsBottom() {
        int rows = (CompanionMode.values().length + 1) / 2;
        return guiTop + 32 + rows * 26 + 8;
    }

    private static void border(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x,         y,         x + w, y + 1,     C_BORDER);
        g.fill(x,         y + h - 1, x + w, y + h,     C_BORDER);
        g.fill(x,         y,         x + 1, y + h,     C_BORDER);
        g.fill(x + w - 1, y,         x + w, y + h,     C_BORDER);
    }

    private void pullCommands() {
        CompanionMode[] modes = CompanionMode.values();
        for (int i = 0; i < modes.length; i++) {
            if (cmdBoxes[i] != null) {
                cd.setCommand(modes[i], cmdBoxes[i].getValue().trim());
            }
        }
    }

    private void sendPacket(boolean bindOwner, boolean setGuardHere) {
        pullCommands();
        ModNetwork.CHANNEL.sendToServer(new SetCompanionModePacket(
                npcUuid, cd.mode, cd.followDistance, cd.aggressiveness, cd.guardRadius,
                bindOwner, setGuardHere, new java.util.HashMap<>(cd.modeCommands)
        ));
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private static String shortUUID(String uuid) {
        return uuid.length() > 8 ? uuid.substring(0, 8) + "…" : uuid;
    }
}

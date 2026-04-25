package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.currency.ClientCurrencyState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
 
public class MainHubMenuScreen extends Screen {
 
    private static final int W = 420;
    private static final int H = 310;
 
    // Section colors
    private static final int COL_HEADER  = 0xFFE6D7B5;
    private static final int COL_SECTION = 0xFF888877;
    private static final int COL_BG      = 0xDD0E0E18;
    private static final int COL_BORDER  = 0xFF444455;
    private static final int COL_PANEL   = 0xAA1A1A2A;
    private static final int COL_DIVIDER = 0xFF333344;
 
    public MainHubMenuScreen() {
        super(Component.literal("Dragon Legacy"));
    }
 
    @Override
    protected void init() {
        super.init();
        int ox = (width - W) / 2;
        int oy = (height - H) / 2;
 
        boolean creative = isCreative();
 
        // ── Колонка: Способности ──────────────────────────────────────────────
        int col1X = ox + 16;
        int col1Y = oy + 52;
 
        addBtn("Круг Пробуждения", col1X, col1Y, 180, () ->
            mc().setScreen(new AwakeningMainScreen()));
 
        if (creative) {
            addBtn("Управление способностями", col1X, col1Y + 26, 180, () ->
                mc().setScreen(new CreativeAbilityManagerScreen(this)));
        }
 
        // ── Колонка: Контент ──────────────────────────────────────────────────
        int col2X = ox + 220;
        int col2Y = oy + 52;
 
        addBtn("Создать квест", col2X, col2Y, 180, () ->
            mc().setScreen(new QuestCreatorScreen(this)));
 
        addBtn("Диалоги NPC", col2X, col2Y + 26, 180, () ->
            mc().setScreen(new DialogueCreatorScreen(this)));
 
        addBtn("Персонажи NPC", col2X, col2Y + 52, 180, () ->
            mc().setScreen(new NpcCustomizerScreen(this)));
 
        // ── Нижняя полоса: Настройки ──────────────────────────────────────────
        int settY = oy + H - 122;
 
        if (creative) {
            addBtn("Редактор UI", col1X, settY, 180, () ->
                mc().setScreen(new UiEditorMenuScreen(this)));
        }
 
        addBtn("Редактор текста NPC", col2X, settY, 180, () ->
            mc().setScreen(new NpcDialogueEditorScreen(this)));
 
        // ── Закрыть ───────────────────────────────────────────────────────────
        addBtn("Закрыть", ox + W / 2 - 55, oy + H - 30, 110, this::onClose);
    }
 
    private void addBtn(String label, int x, int y, int w, Runnable action) {
        addRenderableWidget(Button.builder(Component.literal(label), b -> {
            if (minecraft != null) action.run();
        }).bounds(x, y, w, 20).build());
    }
 
    private Minecraft mc() {
        return Minecraft.getInstance();
    }
 
    private boolean isCreative() {
        return minecraft != null && minecraft.player != null
               && minecraft.player.getAbilities().instabuild;
    }
 
    @Override
    public boolean isPauseScreen() { return false; }
 
    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
 
        int ox = (width - W) / 2;
        int oy = (height - H) / 2;
 
        // Основная панель
        g.fill(ox, oy, ox + W, oy + H, COL_BG);
        drawBorder(g, ox, oy, W, H, COL_BORDER);
 
        // Заголовок
        g.fill(ox, oy, ox + W, oy + 34, 0xAA161622);
        drawBorder(g, ox, oy, W, 34, COL_DIVIDER);
        g.drawCenteredString(font, "Dragon Legacy", ox + W / 2, oy + 5, COL_HEADER);
        g.drawCenteredString(font, "Главное меню", ox + W / 2, oy + 18, 0xFFAAAAAA);
 
        boolean creative = isCreative();
 
        // Секция: Способности
        int col1X = ox + 16;
        int col1Y = oy + 38;
        g.fill(col1X, col1Y, col1X + 188, col1Y + (creative ? 78 : 52), COL_PANEL);
        drawBorder(g, col1X, col1Y, 188, creative ? 78 : 52, COL_DIVIDER);
        g.drawString(font, "СПОСОБНОСТИ", col1X + 6, col1Y + 5, COL_SECTION, false);
 
        // Секция: Контент
        int col2X = ox + 216;
        g.fill(col2X, col1Y, col2X + 188, col1Y + 104, COL_PANEL);
        drawBorder(g, col2X, col1Y, 188, 104, COL_DIVIDER);
        g.drawString(font, "КОНТЕНТ", col2X + 6, col1Y + 5, COL_SECTION, false);
 
        // Секция: Настройки
        int settY = oy + H - 132;
        g.fill(ox + 16, settY, ox + W - 16, settY + 54, COL_PANEL);
        drawBorder(g, ox + 16, settY, W - 32, 54, COL_DIVIDER);
        g.drawString(font, "НАСТРОЙКИ", ox + 22, settY + 5, COL_SECTION, false);

        // Секция: Банк наследия
        int bankY = oy + H - 72;
        g.fill(ox + 16, bankY, ox + W - 16, bankY + 40, COL_PANEL);
        drawBorder(g, ox + 16, bankY, W - 32, 40, 0xFF554422);
        g.drawString(font, "БАНК НАСЛЕДИЯ", ox + 22, bankY + 5, 0xFFE6A030, false);
        g.drawString(font,
                "§e◆ §fМонеты наследия: §e" + ClientCurrencyState.formatted(),
                ox + 22, bankY + 18, 0xFFCCCCCC, false);
     
        super.render(g, mx, my, pt);
    }
 
    private static void drawBorder(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x,         y,         x + w,     y + 1,     c);
        g.fill(x,         y + h - 1, x + w,     y + h,     c);
        g.fill(x,         y,         x + 1,     y + h,     c);
        g.fill(x + w - 1, y,         x + w,     y + h,     c);
    }
}

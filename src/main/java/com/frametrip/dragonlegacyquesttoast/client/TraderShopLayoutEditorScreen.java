package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.client.npceditor.NpcEditorState;
import com.frametrip.dragonlegacyquesttoast.profession.trader.GuiRect;
import com.frametrip.dragonlegacyquesttoast.profession.trader.TraderShopLayoutData;
import com.frametrip.dragonlegacyquesttoast.profession.trader.TraderShopLayoutPreset;
import com.frametrip.dragonlegacyquesttoast.profession.trader.TraderShopLayoutPresetManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Appearance editor for the trader shop.
 * Sub-tabs: Шаблон | Текстуры | Позиции | Текст | Сетка
 */
public class TraderShopLayoutEditorScreen extends Screen {

    private static final int W = 560;
    private static final int H = 460;

    private static final String[] SUB_TAB_LABELS = {"Шаблон", "Текстуры", "Позиции", "Текст", "Сетка"};

    private final NpcEditorState editorState;
    private final Screen         parent;
    private int subTab = 0;

    // ── Positions sub-tab state ───────────────────────────────────────────────
    private int    posElementIndex = 0;
    private EditBox posX, posY, posW, posH;

    // ── Textures sub-tab ──────────────────────────────────────────────────────
    private EditBox texBg, texLeft, texRight, texSlot, texSlotSel, texSlotDis,
            texPrev, texPrice, texDesc, texAct, texBuy, texSell;

    // ── Text sub-tab ──────────────────────────────────────────────────────────
    private EditBox colTitle, colPrice, colDesc, colBalance, colDisc, colErr;
    private EditBox scaleTitle, scalePrice, scaleDesc, scaleBalance;

    // ── Grid sub-tab ──────────────────────────────────────────────────────────
    private EditBox gridCols, gridRows, gridSlotW, gridSlotH, gridGapX, gridGapY;

    private static final String[] ELEMENT_NAMES = {
        "Основное окно", "Левая панель", "Правая панель",
        "Сетка товаров", "Слот товара", "Иконка в слоте",
        "Цена в слоте", "+N в слоте", "Блок предпросмотра",
        "Блок цены", "Блок описания", "Нижний блок действия",
        "Кнопка Купить", "Кнопка Продать", "Баланс игрока",
        "Вкладки Купить/Продать", "Скроллбар"
    };

    public TraderShopLayoutEditorScreen(NpcEditorState editorState, Screen parent) {
        super(Component.literal("Редактор внешнего вида магазина"));
        this.editorState = editorState;
        this.parent      = parent;
    }

    private TraderShopLayoutData layout() {
        var d = editorState.getDraft();
        if (d.professionData == null) return null;
        d.professionData.ensureTraderData();
        return d.professionData.traderData.getOrCreateLayout();
    }

    // ── Init ─────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();
        int ox = ox(), oy = oy();

        // Header back button
        addRenderableWidget(Button.builder(Component.literal("← Назад"),
                b -> { if (minecraft != null) minecraft.setScreen(parent); })
                .bounds(ox + W - 84, oy + 8, 76, 18).build());

        // Sub-tab buttons
        for (int i = 0; i < SUB_TAB_LABELS.length; i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(
                    Component.literal(subTab == idx ? "§e" + SUB_TAB_LABELS[i] : SUB_TAB_LABELS[i]),
                    b -> { subTab = idx; rebuildWidgets(); })
                    .bounds(ox + 8 + i * 104, oy + 32, 100, 18).build());
        }

        switch (subTab) {
            case 0 -> initPresetTab(ox, oy);
            case 1 -> initTexturesTab(ox, oy);
            case 2 -> initPositionsTab(ox, oy);
            case 3 -> initTextTab(ox, oy);
            case 4 -> initGridTab(ox, oy);
        }
    }

    // ── Sub-tab: Шаблон ───────────────────────────────────────────────────────

private void initPresetTab(int ox, int oy) {
        List<TraderShopLayoutPreset> presets = TraderShopLayoutPresetManager.getPresets();
        int y = oy + 60;
        for (TraderShopLayoutPreset preset : presets) {
            final String pid = preset.id;
            TraderShopLayoutData cur = layout();
            boolean active = cur != null && pid.equals(cur.presetId);
            addRenderableWidget(Button.builder(
                    Component.literal(active ? "§e◉ §r" + preset.displayName : "○ " + preset.displayName),
                    b -> applyPreset(pid))
                    .bounds(ox + 16, y, 340, 20).build());
            y += 24;
        }
    }

    private void applyPreset(String presetId) {
        var d = editorState.getDraft();
        if (d.professionData == null) return;
        d.professionData.ensureTraderData();
        d.professionData.traderData.layoutData = TraderShopLayoutPresetManager.createLayoutFromPreset(presetId);
        editorState.markDirty();
        rebuildWidgets();
    }

    // ── Sub-tab: Текстуры ─────────────────────────────────────────────────────

    private void initTexturesTab(int ox, int oy) {
        TraderShopLayoutData l = layout();
        if (l == null) return;
        int y = oy + 58;
        int fw = W - 170;
        texBg     = addBox(ox + 160, y,      fw, "Фон окна",        l.backgroundTexture,       256); y += 20;
        texLeft   = addBox(ox + 160, y,      fw, "Левая панель",     l.leftPanelTexture,        256); y += 20;
        texRight  = addBox(ox + 160, y,      fw, "Правая панель",    l.rightPanelTexture,       256); y += 20;
        texSlot   = addBox(ox + 160, y,      fw, "Слот",             l.itemSlotTexture,         256); y += 20;
        texSlotSel= addBox(ox + 160, y,      fw, "Слот (выбр.)",     l.selectedItemSlotTexture, 256); y += 20;
        texSlotDis= addBox(ox + 160, y,      fw, "Слот (недост.)",   l.disabledItemSlotTexture, 256); y += 20;
        texPrev   = addBox(ox + 160, y,      fw, "Блок предпросм.",  l.previewBoxTexture,       256); y += 20;
        texPrice  = addBox(ox + 160, y,      fw, "Блок цены",        l.priceBoxTexture,         256); y += 20;
        texDesc   = addBox(ox + 160, y,      fw, "Блок описания",    l.descriptionBoxTexture,   256); y += 20;
        texAct    = addBox(ox + 160, y,      fw, "Блок действия",    l.actionBoxTexture,        256); y += 20;
        texBuy    = addBox(ox + 160, y,      fw, "Кнопка Купить",    l.buyButtonTexture,        256); y += 20;
        texSell   = addBox(ox + 160, y,      fw, "Кнопка Продать",   l.sellButtonTexture,       256);

        addRenderableWidget(Button.builder(Component.literal("§aПрименить текстуры"), b -> applyTextures())
                .bounds(ox + 8, oy + H - 32, 160, 20).build());
    }

    private void applyTextures() {
        TraderShopLayoutData l = layout();
        if (l == null) return;
        l.backgroundTexture        = val(texBg);
        l.leftPanelTexture         = val(texLeft);
        l.rightPanelTexture        = val(texRight);
        l.itemSlotTexture          = val(texSlot);
        l.selectedItemSlotTexture  = val(texSlotSel);
        l.disabledItemSlotTexture  = val(texSlotDis);
        l.previewBoxTexture        = val(texPrev);
        l.priceBoxTexture          = val(texPrice);
        l.descriptionBoxTexture    = val(texDesc);
        l.actionBoxTexture         = val(texAct);
        l.buyButtonTexture         = val(texBuy);
        l.sellButtonTexture        = val(texSell);
        editorState.markDirty();
    }

    // ── Sub-tab: Позиции ──────────────────────────────────────────────────────

    private void initPositionsTab(int ox, int oy) {
        TraderShopLayoutData l = layout();
        if (l == null) return;

        // Cycle element button
        addRenderableWidget(Button.builder(
                Component.literal("◀ " + ELEMENT_NAMES[posElementIndex] + " ▶"),
                b -> { posElementIndex = (posElementIndex + 1) % ELEMENT_NAMES.length; rebuildWidgets(); })
                .bounds(ox + 8, oy + 58, 320, 20).build());

        GuiRect cur = getRect(l, posElementIndex);
        if (cur != null) {
            posX = addBox(ox + 60, oy + 90,  60, "X", String.valueOf(cur.x), 6);
            posY = addBox(ox + 60, oy + 112, 60, "Y", String.valueOf(cur.y), 6);
            posW = addBox(ox + 60, oy + 134, 60, "W", String.valueOf(cur.width), 6);
            posH = addBox(ox + 60, oy + 156, 60, "H", String.valueOf(cur.height), 6);
        }

        addRenderableWidget(Button.builder(Component.literal("§aПрименить"), b -> applyPosition(l))
                .bounds(ox + 8, oy + 184, 100, 20).build());
    }

private GuiRect getRect(TraderShopLayoutData l, int idx) {
        return switch (idx) {
            case 0  -> new GuiRect(0, 0, l.screenWidth, l.screenHeight);
            case 1  -> l.leftPanelRect;
            case 2  -> l.rightPanelRect;
            case 3  -> l.itemGridRect;
            case 4  -> new GuiRect(0, 0, l.itemSlotWidth, l.itemSlotHeight);
            case 5  -> l.itemIconRect;
            case 6  -> l.itemPriceTextRect;
            case 7  -> l.itemAmountTextRect;
            case 8  -> l.previewItemBoxRect;
            case 9  -> l.priceInfoBoxRect;
            case 10 -> l.descriptionBoxRect;
            case 11 -> l.actionBoxRect;
            case 12 -> l.buyButtonRect;
            case 13 -> l.sellButtonRect;
            case 14 -> l.balanceTextRect;
            case 15 -> l.modeTabsRect;
            case 16 -> l.scrollbarRect;
            default -> null;
        };
    }

    private void applyPosition(TraderShopLayoutData l) {
        int x = parseInt(val(posX), 0);
        int y = parseInt(val(posY), 0);
        int w = parseInt(val(posW), 1);
        int h = parseInt(val(posH), 1);
        // Clamp
        x = Math.max(-512, Math.min(512, x));
        y = Math.max(-512, Math.min(512, y));
        w = Math.max(1, Math.min(512, w));
        h = Math.max(1, Math.min(512, h));

        switch (posElementIndex) {
            case 0  -> { l.screenWidth = w; l.screenHeight = h; }
            case 1  -> l.leftPanelRect.set(x, y, w, h);
            case 2  -> l.rightPanelRect.set(x, y, w, h);
            case 3  -> l.itemGridRect.set(x, y, w, h);
            case 4  -> { l.itemSlotWidth = w; l.itemSlotHeight = h; }
            case 5  -> l.itemIconRect.set(x, y, w, h);
            case 6  -> l.itemPriceTextRect.set(x, y, w, h);
            case 7  -> l.itemAmountTextRect.set(x, y, w, h);
            case 8  -> l.previewItemBoxRect.set(x, y, w, h);
            case 9  -> l.priceInfoBoxRect.set(x, y, w, h);
            case 10 -> l.descriptionBoxRect.set(x, y, w, h);
            case 11 -> l.actionBoxRect.set(x, y, w, h);
            case 12 -> l.buyButtonRect.set(x, y, w, h);
            case 13 -> l.sellButtonRect.set(x, y, w, h);
            case 14 -> l.balanceTextRect.set(x, y, w, h);
            case 15 -> l.modeTabsRect.set(x, y, w, h);
            case 16 -> l.scrollbarRect.set(x, y, w, h);
        }
        editorState.markDirty();
    }

    // ── Sub-tab: Текст ────────────────────────────────────────────────────────

    private void initTextTab(int ox, int oy) {
        TraderShopLayoutData l = layout();
        if (l == null) return;
        int y = oy + 58, cx = ox + 160, cw = 90;

        colTitle   = addBox(cx, y, cw, "Цвет названия",  hexColor(l.titleTextColor), 8); y += 20;
        colPrice   = addBox(cx, y, cw, "Цвет цены",      hexColor(l.priceTextColor), 8); y += 20;
        colDesc    = addBox(cx, y, cw, "Цвет описания",  hexColor(l.descriptionTextColor), 8); y += 20;
        colBalance = addBox(cx, y, cw, "Цвет баланса",   hexColor(l.balanceTextColor), 8); y += 20;
        colDisc    = addBox(cx, y, cw, "Цвет скидки",    hexColor(l.discountTextColor), 8); y += 20;
        colErr     = addBox(cx, y, cw, "Цвет ошибки",    hexColor(l.errorTextColor), 8); y += 26;

        scaleTitle   = addBox(cx, y, cw, "Масштаб названия", String.format("%.1f", l.titleTextScale), 4); y += 20;
        scalePrice   = addBox(cx, y, cw, "Масштаб цены",     String.format("%.1f", l.priceTextScale), 4); y += 20;
        scaleDesc    = addBox(cx, y, cw, "Масштаб описания", String.format("%.1f", l.descriptionTextScale), 4); y += 20;
        scaleBalance = addBox(cx, y, cw, "Масштаб баланса",  String.format("%.1f", l.balanceTextScale), 4); y += 26;

        // Visibility toggles
        addToggle(ox + 8, y, "Название товара",  l.showItemName,           v -> { l.showItemName = v;           editorState.markDirty(); rebuildWidgets(); }); y += 22;
        addToggle(ox + 8, y, "Цена в слоте",     l.showItemPrice,          v -> { l.showItemPrice = v;          editorState.markDirty(); rebuildWidgets(); }); y += 22;
        addToggle(ox + 8, y, "Количество",       l.showItemAmountModifier, v -> { l.showItemAmountModifier = v; editorState.markDirty(); rebuildWidgets(); }); y += 22;
        addToggle(ox + 8, y, "Баланс",           l.showBalance,            v -> { l.showBalance = v;            editorState.markDirty(); rebuildWidgets(); }); y += 22;
        addToggle(ox + 8, y, "Скидки",           l.showDiscount,           v -> { l.showDiscount = v;           editorState.markDirty(); rebuildWidgets(); }); y += 22;
        addToggle(ox + 8, y, "Остаток",          l.showStock,              v -> { l.showStock = v;              editorState.markDirty(); rebuildWidgets(); });

        addRenderableWidget(Button.builder(Component.literal("§aПрименить"), b -> applyText(l))
                .bounds(ox + 8, oy + H - 32, 100, 20).build());
    }

  private void applyText(TraderShopLayoutData l) {
        l.titleTextColor       = parseColor(val(colTitle),   l.titleTextColor);
        l.priceTextColor       = parseColor(val(colPrice),   l.priceTextColor);
        l.descriptionTextColor = parseColor(val(colDesc),    l.descriptionTextColor);
        l.balanceTextColor     = parseColor(val(colBalance), l.balanceTextColor);
        l.discountTextColor    = parseColor(val(colDisc),    l.discountTextColor);
        l.errorTextColor       = parseColor(val(colErr),     l.errorTextColor);
        l.titleTextScale       = parseFloat(val(scaleTitle),   l.titleTextScale);
        l.priceTextScale       = parseFloat(val(scalePrice),   l.priceTextScale);
        l.descriptionTextScale = parseFloat(val(scaleDesc),    l.descriptionTextScale);
        l.balanceTextScale     = parseFloat(val(scaleBalance), l.balanceTextScale);
        editorState.markDirty();
    }

    // ── Sub-tab: Сетка ────────────────────────────────────────────────────────

    private void initGridTab(int ox, int oy) {
        TraderShopLayoutData l = layout();
        if (l == null) return;
        int y = oy + 58, cx = ox + 160, cw = 70;

        gridCols  = addBox(cx, y, cw, "Колонок",       String.valueOf(l.itemColumns),   4); y += 22;
        gridRows  = addBox(cx, y, cw, "Видимых строк", String.valueOf(l.visibleRows),   4); y += 22;
        gridSlotW = addBox(cx, y, cw, "Ширина слота",  String.valueOf(l.itemSlotWidth), 4); y += 22;
        gridSlotH = addBox(cx, y, cw, "Высота слота",  String.valueOf(l.itemSlotHeight),4); y += 22;
        gridGapX  = addBox(cx, y, cw, "Отступ X",      String.valueOf(l.itemSlotGapX), 4); y += 22;
        gridGapY  = addBox(cx, y, cw, "Отступ Y",      String.valueOf(l.itemSlotGapY), 4);

        addRenderableWidget(Button.builder(Component.literal("§aПрименить"), b -> applyGrid(l))
                .bounds(ox + 8, oy + H - 32, 100, 20).build());
    }

    private void applyGrid(TraderShopLayoutData l) {
        l.itemColumns    = Math.max(1, Math.min(10, parseInt(val(gridCols),  l.itemColumns)));
        l.visibleRows    = Math.max(1, Math.min(10, parseInt(val(gridRows),  l.visibleRows)));
        l.itemSlotWidth  = Math.max(8, Math.min(256, parseInt(val(gridSlotW), l.itemSlotWidth)));
        l.itemSlotHeight = Math.max(8, Math.min(256, parseInt(val(gridSlotH), l.itemSlotHeight)));
        l.itemSlotGapX   = Math.max(0, Math.min(32, parseInt(val(gridGapX),  l.itemSlotGapX)));
        l.itemSlotGapY   = Math.max(0, Math.min(32, parseInt(val(gridGapY),  l.itemSlotGapY)));
        editorState.markDirty();
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int ox = ox(), oy = oy();
        g.fill(ox, oy, ox + W, oy + H, 0xEE0A0A14);
        NpcCreatorScreen.brd(g, ox, oy, W, H, 0xFF3A3A55);

        g.fill(ox, oy, ox + W, oy + 26, 0xBB12121E);
        NpcCreatorScreen.brd(g, ox, oy, W, 26, 0xFF444466);
        g.drawString(font, "§e⚙ §fРедактор внешнего вида магазина", ox + 8, oy + 8, 0xFFE6D7B5, false);

        g.fill(ox, oy + 26, ox + W, oy + 56, 0xAA101020);
        NpcCreatorScreen.brd(g, ox, oy + 26, W, 30, 0xFF2A2A44);

        renderTabContent(g, ox, oy);
        super.render(g, mx, my, pt);
    }

    private void renderTabContent(GuiGraphics g, int ox, int oy) {
        switch (subTab) {
            case 1 -> renderTextureLabels(g, ox, oy);
            case 2 -> renderPosLabels(g, ox, oy);
            case 3 -> renderTextLabels(g, ox, oy);
            case 4 -> renderGridLabels(g, ox, oy);
        }
    }

    private void renderTextureLabels(GuiGraphics g, int ox, int oy) {
        String[] labels = {"Фон окна:", "Левая панель:", "Правая панель:", "Слот:", "Слот (выбр.):",
                "Слот (недост.):", "Предпросмотр:", "Цена:", "Описание:", "Действие:", "Кнопка Купить:", "Кнопка Продать:"};
        int y = oy + 61;
        for (String l : labels) { g.drawString(font, "§7" + l, ox + 8, y, 0xFF888877, false); y += 20; }
    }

    private void renderPosLabels(GuiGraphics g, int ox, int oy) {
        g.drawString(font, "§7X:", ox + 8, oy + 93, 0xFF888877, false);
        g.drawString(font, "§7Y:", ox + 8, oy + 115, 0xFF888877, false);
        g.drawString(font, "§7Ш:", ox + 8, oy + 137, 0xFF888877, false);
        g.drawString(font, "§7В:", ox + 8, oy + 159, 0xFF888877, false);
    }

    private void renderTextLabels(GuiGraphics g, int ox, int oy) {
        String[] colorLabels = {"Цвет названия:", "Цвет цены:", "Цвет описания:", "Цвет баланса:", "Цвет скидки:", "Цвет ошибки:"};
        String[] scaleLabels = {"Масштаб назв.:", "Масштаб цены:", "Масштаб опис.:", "Масштаб бал.:"};
        int y = oy + 61;
        for (String l : colorLabels) { g.drawString(font, "§7" + l, ox + 8, y, 0xFF888877, false); y += 20; }
        y += 6;
        for (String l : scaleLabels) { g.drawString(font, "§7" + l, ox + 8, y, 0xFF888877, false); y += 20; }
    }

    private void renderGridLabels(GuiGraphics g, int ox, int oy) {
        String[] labels = {"Колонок:", "Видимых строк:", "Ширина слота:", "Высота слота:", "Отступ X:", "Отступ Y:"};
        int y = oy + 61;
        for (String l : labels) { g.drawString(font, "§7" + l, ox + 8, y, 0xFF888877, false); y += 22; }
    }

// ── Widgets ───────────────────────────────────────────────────────────────

    private EditBox addBox(int x, int y, int w, String hint, String value, int maxLen) {
        EditBox box = new EditBox(font, x, y, w, 16, Component.literal(hint));
        box.setMaxLength(maxLen);
        box.setValue(value);
        addRenderableWidget(box);
        return box;
    }

    private void addToggle(int x, int y, String label, boolean current, java.util.function.Consumer<Boolean> setter) {
        addRenderableWidget(Button.builder(
                Component.literal(current ? "§a■ §f" + label : "§7□ §f" + label),
                b -> setter.accept(!current))
                .bounds(x, y, 220, 18).build());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String val(EditBox box) { return box == null ? "" : box.getValue().trim(); }
    private static int parseInt(String s, int def) { try { return Integer.parseInt(s); } catch (Exception e) { return def; } }
    private static float parseFloat(String s, float def) { try { return Float.parseFloat(s); } catch (Exception e) { return def; } }

    private static String hexColor(int color) {
        return String.format("%06X", color & 0xFFFFFF);
    }

    private static int parseColor(String s, int def) {
        try {
            String cleaned = s.startsWith("#") ? s.substring(1) : s;
            if (cleaned.length() == 6) return 0xFF000000 | Integer.parseInt(cleaned, 16);
            if (cleaned.length() == 8) return (int) Long.parseLong(cleaned, 16);
        } catch (Exception ignored) {}
        return def;
    }

    @Override public boolean isPauseScreen() { return false; }
    private int ox() { return (width  - W) / 2; }
    private int oy() { return (height - H) / 2; }
}

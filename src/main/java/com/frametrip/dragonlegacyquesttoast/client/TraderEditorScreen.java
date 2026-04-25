package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.client.npceditor.NpcEditorState;
import com.frametrip.dragonlegacyquesttoast.profession.trader.BuyTradeOffer;
import com.frametrip.dragonlegacyquesttoast.profession.trader.RestockMode;
import com.frametrip.dragonlegacyquesttoast.profession.trader.SellTradeOffer;
import com.frametrip.dragonlegacyquesttoast.profession.trader.TradeRestockSettings;
import com.frametrip.dragonlegacyquesttoast.profession.trader.TraderDiscountData;
import com.frametrip.dragonlegacyquesttoast.profession.trader.TraderProfessionData;
import com.frametrip.dragonlegacyquesttoast.profession.trader.TraderShopLayoutData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class TraderEditorScreen extends Screen {

    private static final int W        = 560;
    private static final int H        = 440;
    private static final int ROW_H    = 32;
    private static final int LIST_TOP = 62;
    private static final int LIST_BOT = H - 36;

    private final NpcEditorState editorState;
    private final Screen         parent;
    private int activeTab = 0; // 0=Продажа,1=Скупка,2=Настройки,3=Скидки,4=Вид,5=Просмотр

    private EditBox timeField;
    private EditBox discGlobalField;
    private EditBox discSellBonusField;

    public TraderEditorScreen(NpcEditorState editorState, Screen parent) {
        super(Component.literal("Настройка магазина NPC"));
        this.editorState = editorState;
        this.parent      = parent;
    }

    private TraderProfessionData trader() {
        var d = editorState.getDraft();
        if (d.professionData == null) return null;
        d.professionData.ensureTraderData();
        return d.professionData.traderData;
    }

    // ── Init ─────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();
        int ox = ox(), oy = oy();

        String[] tabs = {"Продажа", "Скупка", "Настройки", "Скидки", "Вид", "Просмотр"};
        for (int i = 0; i < tabs.length; i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(
                    Component.literal(activeTab == idx ? "§e" + tabs[i] : tabs[i]),
                    b -> { activeTab = idx; rebuildWidgets(); })
                    .bounds(ox + 8 + i * 90, oy + 36, 86, 18).build());
        }

        addRenderableWidget(Button.builder(Component.literal("← Назад"),
                b -> back()).bounds(ox + W - 84, oy + 8, 76, 18).build());

        switch (activeTab) {
            case 0 -> initSellTab(ox, oy);
            case 1 -> initBuyTab(ox, oy);
            case 2 -> initSettingsTab(ox, oy);
            case 3 -> initDiscountsTab(ox, oy);
            case 4 -> initAppearanceTab(ox, oy);
            case 5 -> {} // preview: no widgets
        }
    }

    // ── Sell tab ─────────────────────────────────────────────────────────────

    private void initSellTab(int ox, int oy) {
        TraderProfessionData td = trader();
        if (td == null) return;
        List<SellTradeOffer> offers = td.sellOffers;
        int y = oy + LIST_TOP;
        for (int i = 0; i < offers.size(); i++) {
            if (y + ROW_H > oy + LIST_BOT) break;
            final SellTradeOffer offer = offers.get(i);
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal("✎"),
                    b -> openSellEdit(offer))
                    .bounds(ox + W - 82, y + 6, 34, 18).build());
            addRenderableWidget(Button.builder(Component.literal("✕"),
                    b -> { td.sellOffers.remove(idx); editorState.markDirty(); rebuildWidgets(); })
                    .bounds(ox + W - 44, y + 6, 34, 18).build());
            y += ROW_H + 2;
        }
        addRenderableWidget(Button.builder(Component.literal("+ Добавить товар"),
                b -> openSellEdit(null))
                .bounds(ox + 8, oy + LIST_BOT + 4, 140, 20).build());
        addRenderableWidget(Button.builder(Component.literal("💾 Сохранить"),
                b -> save()).bounds(ox + W - 120, oy + LIST_BOT + 4, 112, 20).build());
    }

// ── Buy tab ──────────────────────────────────────────────────────────────

    private void initBuyTab(int ox, int oy) {
        TraderProfessionData td = trader();
        if (td == null) return;
        List<BuyTradeOffer> offers = td.buyOffers;
        int y = oy + LIST_TOP;
        for (int i = 0; i < offers.size(); i++) {
            if (y + ROW_H > oy + LIST_BOT) break;
            final BuyTradeOffer offer = offers.get(i);
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal("✎"),
                    b -> openBuyEdit(offer))
                    .bounds(ox + W - 82, y + 6, 34, 18).build());
            addRenderableWidget(Button.builder(Component.literal("✕"),
                    b -> { td.buyOffers.remove(idx); editorState.markDirty(); rebuildWidgets(); })
                    .bounds(ox + W - 44, y + 6, 34, 18).build());
            y += ROW_H + 2;
        }
        addRenderableWidget(Button.builder(Component.literal("+ Добавить скупку"),
                b -> openBuyEdit(null))
                .bounds(ox + 8, oy + LIST_BOT + 4, 140, 20).build());
        addRenderableWidget(Button.builder(Component.literal("💾 Сохранить"),
                b -> save()).bounds(ox + W - 120, oy + LIST_BOT + 4, 112, 20).build());
    }

    // ── Settings tab ─────────────────────────────────────────────────────────

    private void initSettingsTab(int ox, int oy) {
        TraderProfessionData td = trader();
        if (td == null) return;
        TradeRestockSettings rs = td.restockSettings;
        int y = oy + LIST_TOP;

        for (RestockMode mode : RestockMode.values()) {
            final RestockMode m = mode;
            addRenderableWidget(Button.builder(
                    Component.literal(rs.mode == m ? "§e◉ §r" + m.label() : "○ " + m.label()),
                    b -> { rs.mode = m; editorState.markDirty(); rebuildWidgets(); }
            ).bounds(ox + 8, y, 210, 18).build());
            y += 22;
        }

        y += 6;
        timeField = new EditBox(font, ox + 140, y, 60, 16, Component.literal("время (0-23999)"));
        timeField.setMaxLength(6);
        timeField.setValue(String.valueOf(rs.timeOfDay));
        addRenderableWidget(timeField);
        y += 24;

        if (rs.mode == RestockMode.EVERY_N_DAYS_AT_TIME) {
            EditBox nDaysField = new EditBox(font, ox + 140, y, 60, 16, Component.literal("кол. дней"));
            nDaysField.setMaxLength(4);
            nDaysField.setValue(String.valueOf(rs.everyNDays));
            addRenderableWidget(nDaysField);
            addRenderableWidget(Button.builder(Component.literal("Применить"),
                    b -> {
                        try { rs.everyNDays = Math.max(1, Integer.parseInt(nDaysField.getValue().trim())); }
                        catch (Exception ignored) {}
                        applyTimeField(rs);
                        editorState.markDirty();
                        rebuildWidgets();
                    }
            ).bounds(ox + 210, y, 80, 18).build());
            y += 24;
        }

        addRenderableWidget(Button.builder(Component.literal("Применить время"),
                b -> { applyTimeField(rs); editorState.markDirty(); rebuildWidgets(); }
        ).bounds(ox + 210, y - (rs.mode == RestockMode.EVERY_N_DAYS_AT_TIME ? 24 : 0) - 24, 110, 18).build());

        addRenderableWidget(Button.builder(Component.literal("💾 Сохранить"),
                b -> save()).bounds(ox + W - 120, oy + LIST_BOT + 4, 112, 20).build());
    }

    private void applyTimeField(TradeRestockSettings rs) {
        if (timeField == null) return;
        try { rs.timeOfDay = Math.max(0, Math.min(23999, Integer.parseInt(timeField.getValue().trim()))); }
        catch (Exception ignored) {}
    }

    // ── Discounts tab ─────────────────────────────────────────────────────────

    private void initDiscountsTab(int ox, int oy) {
        TraderProfessionData td = trader();
        if (td == null) return;
        TraderDiscountData disc = td.getOrCreateDiscounts();
        int y = oy + LIST_TOP;

        addRenderableWidget(Button.builder(
                Component.literal(disc.enabled ? "§a■ §fСкидки включены" : "§7□ §fСкидки включены"),
                b -> { disc.enabled = !disc.enabled; editorState.markDirty(); rebuildWidgets(); }
        ).bounds(ox + 8, y, 180, 18).build());
        y += 28;

        discGlobalField = new EditBox(font, ox + 200, y, 60, 16, Component.literal("0-90"));
        discGlobalField.setMaxLength(3);
        discGlobalField.setValue(String.valueOf(disc.globalBuyDiscountPercent));
        addRenderableWidget(discGlobalField);
        y += 26;

  discSellBonusField = new EditBox(font, ox + 200, y, 60, 16, Component.literal("0-300"));
        discSellBonusField.setMaxLength(4);
        discSellBonusField.setValue(String.valueOf(disc.globalSellBonusPercent));
        addRenderableWidget(discSellBonusField);
        y += 26;

        addRenderableWidget(Button.builder(
                Component.literal(disc.allowPerOfferDiscounts ? "§a■ §fПоофферные скидки" : "§7□ §fПоофферные скидки"),
                b -> { disc.allowPerOfferDiscounts = !disc.allowPerOfferDiscounts; editorState.markDirty(); rebuildWidgets(); }
        ).bounds(ox + 8, y, 200, 18).build());
        y += 28;

        addRenderableWidget(Button.builder(Component.literal("Применить"),
                b -> applyDiscounts(td)).bounds(ox + 8, y, 100, 18).build());
        addRenderableWidget(Button.builder(Component.literal("💾 Сохранить"),
                b -> save()).bounds(ox + W - 120, oy + LIST_BOT + 4, 112, 20).build());
    }

    private void applyDiscounts(TraderProfessionData td) {
        TraderDiscountData disc = td.getOrCreateDiscounts();
        if (discGlobalField != null) {
            try { disc.globalBuyDiscountPercent = Math.max(0, Math.min(90,
                    Integer.parseInt(discGlobalField.getValue().trim()))); }
            catch (Exception ignored) {}
        }
        if (discSellBonusField != null) {
            try { disc.globalSellBonusPercent = Math.max(0, Math.min(300,
                    Integer.parseInt(discSellBonusField.getValue().trim()))); }
            catch (Exception ignored) {}
        }
        editorState.markDirty();
    }

    // ── Appearance tab ────────────────────────────────────────────────────────

    private void initAppearanceTab(int ox, int oy) {
        TraderProfessionData td = trader();
        if (td == null) return;
        int y = oy + LIST_TOP;

        addRenderableWidget(Button.builder(Component.literal("Открыть редактор оформления"),
                b -> { if (minecraft != null)
                    minecraft.setScreen(new TraderShopLayoutEditorScreen(editorState, this)); }
        ).bounds(ox + 8, y, 220, 20).build());
        y += 30;

        addRenderableWidget(Button.builder(Component.literal("Сбросить к стандарту"),
                b -> { td.layoutData = TraderShopLayoutData.createDefault(); editorState.markDirty(); rebuildWidgets(); }
        ).bounds(ox + 8, y, 180, 20).build());

        addRenderableWidget(Button.builder(Component.literal("💾 Сохранить"),
                b -> save()).bounds(ox + W - 120, oy + LIST_BOT + 4, 112, 20).build());
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int ox = ox(), oy = oy();

        g.fill(ox, oy, ox + W, oy + H, 0xEE0A0A14);
        NpcCreatorScreen.brd(g, ox, oy, W, H, 0xFF3A3A55);
        g.fill(ox, oy, ox + W, oy + 28, 0xBB12121E);
        NpcCreatorScreen.brd(g, ox, oy, W, 28, 0xFF444466);
        g.drawString(font, "§e⚙ §fНастройка магазина NPC", ox + 8, oy + 9, 0xFFE6D7B5, false);

        g.fill(ox, oy + 28, ox + W, oy + 58, 0xAA101020);
        NpcCreatorScreen.brd(g, ox, oy + 28, W, 30, 0xFF2A2A44);

        if (activeTab != 5) {
            g.fill(ox + 4, oy + LIST_TOP - 2, ox + W - 4, oy + LIST_BOT, 0x44090910);
            NpcCreatorScreen.brd(g, ox + 4, oy + LIST_TOP - 2, W - 8, LIST_BOT - LIST_TOP + 2, 0xFF2A2A44);
        }

        switch (activeTab) {
            case 0 -> renderSellTab(g, ox, oy);
            case 1 -> renderBuyTab(g, ox, oy);
            case 2 -> renderSettingsTab(g, ox, oy);
            case 3 -> renderDiscountsTab(g, ox, oy);
            case 4 -> renderAppearanceTab(g, ox, oy);
            case 5 -> renderPreviewTab(g, ox, oy);
        }

        super.render(g, mx, my, pt);
    }

    private void renderSellTab(GuiGraphics g, int ox, int oy) {
        TraderProfessionData td = trader();
        if (td == null) return;
        if (td.sellOffers.isEmpty()) {
            g.drawString(font, "§8— нет товаров на продажу —", ox + 16, oy + LIST_TOP + 8, 0xFF555566, false);
            return;
        }
        int y = oy + LIST_TOP;
        for (SellTradeOffer o : td.sellOffers) {
            if (y + ROW_H > oy + LIST_BOT) break;
            renderSellRow(g, ox + 8, y, W - 16, o);
            y += ROW_H + 2;
        }
    }

private void renderSellRow(GuiGraphics g, int x, int y, int w, SellTradeOffer o) {
        g.fill(x, y, x + w - 90, y + ROW_H, 0x55202030);
        NpcCreatorScreen.brd(g, x, y, w - 90, ROW_H, 0xFF333344);
        String name = o.customName.isBlank() ? "§8" + o.itemId : "§f" + o.customName;
        g.drawString(font, name, x + 6, y + 5, 0xFFCCCCCC, false);
        String stock = o.infiniteStock ? "∞" : o.stock + "/" + o.maxStock;
        String discStr = o.discountPercent > 0 ? "  §aСкидка:" + o.discountPercent + "%" : "";
        g.drawString(font, "§7Цена: §e" + o.price + "  §7×" + o.amount + "  §7Остаток: §f" + stock + discStr,
                x + 6, y + 17, 0xFF888877, false);
    }

    private void renderBuyTab(GuiGraphics g, int ox, int oy) {
        TraderProfessionData td = trader();
        if (td == null) return;
        if (td.buyOffers.isEmpty()) {
            g.drawString(font, "§8— нет предложений скупки —", ox + 16, oy + LIST_TOP + 8, 0xFF555566, false);
            return;
        }
        int y = oy + LIST_TOP;
        for (BuyTradeOffer o : td.buyOffers) {
            if (y + ROW_H > oy + LIST_BOT) break;
            renderBuyRow(g, ox + 8, y, W - 16, o);
            y += ROW_H + 2;
        }
    }

    private void renderBuyRow(GuiGraphics g, int x, int y, int w, BuyTradeOffer o) {
        g.fill(x, y, x + w - 90, y + ROW_H, 0x55202030);
        NpcCreatorScreen.brd(g, x, y, w - 90, ROW_H, 0xFF333344);
        String name = o.customName.isBlank() ? "§8" + o.itemId : "§f" + o.customName;
        g.drawString(font, name, x + 6, y + 5, 0xFFCCCCCC, false);
        String demand = o.infiniteDemand ? "∞" : o.demandLeft + "/" + o.maxDemand;
        String bonusStr = o.bonusPercent > 0 ? "  §aБонус:+" + o.bonusPercent + "%" : "";
        g.drawString(font, "§7×" + o.requiredAmount + " → §e" + o.reward + " монет  §7Лимит: §f" + demand + bonusStr,
                x + 6, y + 17, 0xFF888877, false);
    }

    private void renderSettingsTab(GuiGraphics g, int ox, int oy) {
        TraderProfessionData td = trader();
        if (td == null) return;
        TradeRestockSettings rs = td.restockSettings;
        int y = oy + LIST_TOP;
        g.drawString(font, "§7Режим обновления запасов:", ox + 8, y - 12, 0xFF888877, false);
        y += RestockMode.values().length * 22 + 14;
        g.drawString(font, "§7Время обновления (тики, 0–23999):", ox + 8, y - 2, 0xFF888877, false);
        g.drawString(font, "§8≈ " + rs.timeOfDayLabel(), ox + 208, y - 2, 0xFF666655, false);
        if (rs.mode == RestockMode.EVERY_N_DAYS_AT_TIME) {
            y += 24;
            g.drawString(font, "§7Каждые N дней:", ox + 8, y - 2, 0xFF888877, false);
        }
        g.drawString(font, "§7Валюта: §fМонета наследия", ox + 8, oy + LIST_BOT - 20, 0xFF888877, false);
    }

    private void renderDiscountsTab(GuiGraphics g, int ox, int oy) {
        int y = oy + LIST_TOP + 28;
        g.drawString(font, "§7Глобальная скидка покупки (0–90%):", ox + 8, y + 2, 0xFF888877, false);
        y += 26;
        g.drawString(font, "§7Глобальный бонус продажи (0–300%):", ox + 8, y + 2, 0xFF888877, false);
    }

    private void renderAppearanceTab(GuiGraphics g, int ox, int oy) {
        TraderProfessionData td = trader();
        if (td == null) return;
        TraderShopLayoutData l = td.getOrCreateLayout();
        int y = oy + LIST_TOP + 56;
        g.drawString(font, "§7Текущий размер: §f" + l.screenWidth + "×" + l.screenHeight, ox + 8, y, 0xFF888877, false);
        y += 14;
        g.drawString(font, "§7Сетка: §f" + l.itemColumns + " кол. × " + l.visibleRows + " строк", ox + 8, y, 0xFF888877, false);
        y += 14;
        g.drawString(font, "§7Слот: §f" + l.itemSlotWidth + "×" + l.itemSlotHeight, ox + 8, y, 0xFF888877, false);
    }

    private void renderPreviewTab(GuiGraphics g, int ox, int oy) {
        TraderProfessionData td = trader();
        if (td == null) return;
        TraderShopLayoutData l = td.getOrCreateLayout();

        int pw = Math.min(l.screenWidth,  W - 16);
        int ph = Math.min(l.screenHeight, LIST_BOT - LIST_TOP + 34);
        int px = ox + (W - pw) / 2;
        int py = oy + LIST_TOP - 2;

        // Background + border
        g.fill(px, py, px + pw, py + ph, l.bgColor);
        NpcCreatorScreen.brd(g, px, py, pw, ph, l.bgBorderColor);

        // Header bar
        int headerH = l.modeTabsRect.y + l.modeTabsRect.height;
        g.fill(px, py, px + pw, py + headerH, l.headerBgColor);
        NpcCreatorScreen.brd(g, px, py, pw, headerH, l.headerBdrColor);
        g.drawString(font, "§fМагазин NPC", px + 8, py + 6, l.titleTextColor, false);

        // Left panel
        var lp = l.leftPanelRect;
        int lpX = px + lp.x, lpY = py + lp.y;
        int lpR = Math.min(lpX + lp.width,  px + pw);
        int lpB = Math.min(lpY + lp.height, py + ph);
        if (lpR > lpX && lpB > lpY) {
            g.fill(lpX, lpY, lpR, lpB, l.leftPanelBgColor);
            NpcCreatorScreen.brd(g, lpX, lpY, lpR - lpX, lpB - lpY, l.leftPanelBdrColor);
        }

  // Right panel
        var rp = l.rightPanelRect;
        int rpX = px + rp.x, rpY = py + rp.y;
        int rpR = Math.min(rpX + rp.width,  px + pw);
        int rpB = Math.min(rpY + rp.height, py + ph);
        if (rpR > rpX && rpB > rpY) {
            g.fill(rpX, rpY, rpR, rpB, l.rightPanelBgColor);
            NpcCreatorScreen.brd(g, rpX, rpY, rpR - rpX, rpB - rpY, l.rightPanelBdrColor);
        }

        // Sample slot in grid
        var ig = l.itemGridRect;
        int sx = px + ig.x + l.itemSlotGapX;
        int sy = py + ig.y + l.itemSlotGapY;
        int sr = Math.min(sx + l.itemSlotWidth,  px + ig.x + ig.width);
        int sb = Math.min(sy + l.itemSlotHeight, py + ig.y + ig.height);
        if (sr > sx && sb > sy) {
            g.fill(sx, sy, sr, sb, l.slotBgColor);
            NpcCreatorScreen.brd(g, sx, sy, sr - sx, sb - sy, l.slotBdrColor);
            g.drawString(font, "§8Пример товара", sx + 4, sy + 4, l.titleTextColor, false);
            g.drawString(font, "§e100 монет",     sx + 4, sy + 16, l.priceTextColor, false);
        }

        // Bottom balance bar
        var bt = l.balanceTextRect;
        int btY = py + bt.y;
        if (btY + 14 <= py + ph) {
            g.fill(px, btY - 2, px + pw, py + ph, l.bottomBarBgColor);
            g.drawString(font, "§7Баланс: §e999 монет", px + bt.x, btY, l.balanceTextColor, false);
        }

        // Preview label
        g.drawString(font, "§eЭто предпросмотр. §7Покупки не выполняются.",
                px + 8, py + ph + 4, 0xFFAAAAAA, false);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void openSellEdit(SellTradeOffer offer) {
        if (minecraft != null) minecraft.setScreen(new TraderSellOfferEditScreen(editorState, offer, this));
    }

    private void openBuyEdit(BuyTradeOffer offer) {
        if (minecraft != null) minecraft.setScreen(new TraderBuyOfferEditScreen(editorState, offer, this));
    }

    private void save() {
        editorState.save();
        back();
    }

    private void back() {
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private int ox() { return (width  - W) / 2; }
    private int oy() { return (height - H) / 2; }
}

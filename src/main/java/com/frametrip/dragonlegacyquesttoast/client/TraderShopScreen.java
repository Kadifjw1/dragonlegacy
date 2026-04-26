package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.currency.ClientCurrencyState;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.network.BuyTradeOfferPacket;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SellToNpcPacket;
import com.frametrip.dragonlegacyquesttoast.profession.trader.BuyTradeOffer;
import com.frametrip.dragonlegacyquesttoast.profession.trader.GuiRect;
import com.frametrip.dragonlegacyquesttoast.profession.trader.SellTradeOffer;
import com.frametrip.dragonlegacyquesttoast.profession.trader.TradePriceResult;
import com.frametrip.dragonlegacyquesttoast.profession.trader.TradeRewardResult;
import com.frametrip.dragonlegacyquesttoast.profession.trader.TraderDiscountData;
import com.frametrip.dragonlegacyquesttoast.profession.trader.TraderMode;
import com.frametrip.dragonlegacyquesttoast.profession.trader.TraderShopLayoutData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TraderShopScreen extends Screen {

    private final UUID               npcUuid;
    private final NpcEntityData      npcData;
    private final TraderShopLayoutData layout;
    private final TraderDiscountData   discounts;
    private final int W;
    private final int H;

    private int activeTab     = 0;  // 0=buy-from-npc, 1=sell-to-npc
    private int selectedIndex = -1;
    private int scrollOffset  = 0;
    private final Map<String, Integer> buyCart = new HashMap<>();

    public TraderShopScreen(UUID npcUuid, NpcEntityData npcData) {
        super(Component.literal(npcData.displayName));
        this.npcUuid   = npcUuid;
        this.npcData   = npcData;
        var td         = npcData.professionData.traderData;
        this.layout    = td.getOrCreateLayout();
        this.discounts = td.getOrCreateDiscounts();
        this.W         = Math.max(300, Math.min(512, layout.screenWidth));
        this.H         = Math.max(200, Math.min(320, layout.screenHeight));
        if (td.mode == TraderMode.BUYER) activeTab = 1;
    }

    // ── Init ─────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        super.init();
        int ox = ox(), oy = oy();
        TraderMode mode = npcData.professionData.traderData.mode;

        List<?> offers = activeOffers();
        if (selectedIndex < 0 && !offers.isEmpty()) selectedIndex = 0;
        if (!offers.isEmpty() && selectedIndex >= offers.size()) selectedIndex = 0;

        if (mode == TraderMode.BOTH) {
            addTabBtn("⬇ Купить",  ox + 8,      oy + 34, 0);
            addTabBtn("⬆ Продать", ox + 8 + 94, oy + 34, 1);
        }

        addRenderableWidget(Button.builder(Component.literal("✕"),
                b -> { if (minecraft != null) minecraft.setScreen(null); })
                .bounds(ox + W - 24, oy + 6, 18, 18).build());

        // Action buttons in right-panel action box
        GuiRect rp  = layout.rightPanelRect;
        GuiRect act = layout.actionBoxRect;
        int abx = ox + rp.x + act.x;
        int aby = oy + rp.y + act.y;

        if (!offers.isEmpty()) {
            if (activeTab == 0) {
                GuiRect b = layout.buyButtonRect;
                Button btn = Button.builder(Component.literal("§a⬇ Купить корзину"), x -> doBuy())
                        .bounds(abx + b.x, aby + b.y, b.width, b.height).build();
                btn.active = canAffordCart() && totalCartItems() > 0;
                addRenderableWidget(btn);
            } else {
                GuiRect b    = layout.sellButtonRect;
                boolean can  = canSellSelected();
                Button btn1  = Button.builder(Component.literal("§e⬆ Продать"), x -> doSell(false))
                        .bounds(abx + b.x, aby + b.y, b.width, b.height).build();
                btn1.active  = can;
                addRenderableWidget(btn1);
                Button btn2  = Button.builder(Component.literal("§6Всё"), x -> doSell(true))
                        .bounds(abx + b.x + b.width + 4, aby + b.y, 42, b.height).build();
                btn2.active  = can;
                addRenderableWidget(btn2);
            }
        }
    }
  
private void addTabBtn(String label, int x, int y, int idx) {
        addRenderableWidget(Button.builder(
                Component.literal(activeTab == idx ? "§e" + label : "§7" + label),
                b -> { activeTab = idx; scrollOffset = 0; selectedIndex = -1; rebuildWidgets(); })
                .bounds(x, y, 90, 18).build());
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int ox = ox(), oy = oy();

        g.fill(ox, oy, ox + W, oy + H, layout.bgColor);
        NpcCreatorScreen.brd(g, ox, oy, W, H, layout.bgBorderColor);

        g.fill(ox, oy, ox + W, oy + 28, layout.headerBgColor);
        NpcCreatorScreen.brd(g, ox, oy, W, 28, layout.headerBdrColor);
        g.drawString(font, "§e⚒ §f" + npcData.displayName, ox + 8, oy + 9, layout.titleTextColor, false);

        g.fill(ox, oy + 28, ox + W, oy + 58, layout.tabBarBgColor);

        GuiRect lp = layout.leftPanelRect;
        g.fill(ox + lp.x, oy + lp.y, ox + lp.x + lp.width, oy + lp.y + lp.height, layout.leftPanelBgColor);
        NpcCreatorScreen.brd(g, ox + lp.x, oy + lp.y, lp.width, lp.height, layout.leftPanelBdrColor);

        GuiRect rp = layout.rightPanelRect;
        g.fill(ox + rp.x, oy + rp.y, ox + rp.x + rp.width, oy + rp.y + rp.height, layout.rightPanelBgColor);
        NpcCreatorScreen.brd(g, ox + rp.x, oy + rp.y, rp.width, rp.height, layout.rightPanelBdrColor);

        renderItemGrid(g, ox, oy);
        renderRightPanel(g, ox, oy);

        g.fill(ox, oy + H - 28, ox + W, oy + H, layout.bottomBarBgColor);
        if (layout.showBalance)
            g.drawString(font, "§7Банк: §e" + ClientCurrencyState.formatted() + " §7монет",
                    ox + 10, oy + H - 18, layout.balanceTextColor, false);

        super.render(g, mx, my, pt);
    }

    // ── Item grid ─────────────────────────────────────────────────────────────

    private void renderItemGrid(GuiGraphics g, int ox, int oy) {
        List<?> offers = activeOffers();
        GuiRect gr     = layout.itemGridRect;
        int startX = ox + gr.x + 2, startY = oy + gr.y + 2;
        int maxY   = oy + gr.y + gr.height - 2;
        int cols   = 4;
        int sW     = slotSize();
        int sH     = sW;
        int gX     = layout.itemSlotGapX,  gY = layout.itemSlotGapY;

        if (offers.isEmpty()) {
            g.drawString(font, "§8— нет предложений —", startX + 4, startY + 8, 0xFF555566, false);
            return;
        }

        int first = scrollOffset * cols;
        for (int i = first; i < offers.size(); i++) {
            int row = (i - first) / cols, col = (i - first) % cols;
            int sx  = startX + col * (sW + gX);
            int sy  = startY + row * (sH + gY);
            if (sy + sH > maxY) break;
            boolean sel = (i == selectedIndex);
            if (activeTab == 0) renderBuySlot(g, sx, sy, sW, sH, (SellTradeOffer) offers.get(i), sel);
            else                renderSellSlot(g, sx, sy, sW, sH, (BuyTradeOffer)  offers.get(i), sel);
        }
    }

    private void renderBuySlot(GuiGraphics g, int x, int y, int w, int h, SellTradeOffer o, boolean sel) {
        g.fill(x, y, x + w, y + h, sel ? layout.slotSelBgColor : layout.slotBgColor);
        NpcCreatorScreen.brd(g, x, y, w, h, sel ? layout.slotSelBdrColor : layout.slotBdrColor);
        int controlsH = 20;
        int previewSize = Math.max(16, h - controlsH - 8);
        int previewX = x + (w - previewSize) / 2;
        int previewY = y + 4;
        drawBox(g, previewX, previewY, previewSize, previewSize);
        renderOfferIcon(g, o.itemId, previewX + (previewSize - 16) / 2, previewY + (previewSize - 16) / 2);

        int qty = cartCount(o.id);
        if (qty > 0) {
            g.drawString(font, "§e" + qty, x + w - 12, y + 5, 0xFFF9E07F, false);
    }
        
        int cy = y + h - controlsH;
        g.fill(x + 2, cy, x + w - 2, cy + controlsH - 2, 0x33000000);
        g.drawCenteredString(font, Component.literal("§c-1"), x + 12, cy + 6, layout.priceTextColor);
        g.drawCenteredString(font, Component.literal("§e" + pricePerSingle(o)), x + (w / 2), cy + 6, layout.priceTextColor);
        g.drawCenteredString(font, Component.literal("§a+1"), x + w - 12, cy + 6, layout.priceTextColor);
    }
    
private void renderSellSlot(GuiGraphics g, int x, int y, int w, int h, BuyTradeOffer o, boolean sel) {
        g.fill(x, y, x + w, y + h, sel ? layout.slotSelBgColor : layout.slotBgColor);
        NpcCreatorScreen.brd(g, x, y, w, h, sel ? layout.slotSelBdrColor : layout.slotBdrColor);
        if (layout.showItemName)
            g.drawString(font, o.customName.isBlank() ? "§8" + o.itemId : "§f" + o.customName,
                    x + 6, y + 6, layout.titleTextColor, false);
        int has = countInInventory(o.itemId);
        g.drawString(font, "У вас: " + (has >= o.requiredAmount ? "§f" : "§c") + has + " §7/ §f" + o.requiredAmount,
                x + 6, y + 17, 0xFFAAAAAA, false);
        if (layout.showItemPrice) {
            int bon = discounts.effectiveSellBonus(o.bonusPercent);
            long fr = TradeRewardResult.calculate(o.reward, 1, bon).finalReward;
            String bonPart = bon > 0 ? " §a(+" + bon + "%)" : "";
            g.drawString(font, "§7Награда: §e" + fr + " §7монет" + bonPart, x + 6, y + 28, 0xFFAAAAAA, false);
        }
    }

    // ── Right panel detail ────────────────────────────────────────────────────

    private void renderRightPanel(GuiGraphics g, int ox, int oy) {
        GuiRect rp  = layout.rightPanelRect;
        int rpx = ox + rp.x, rpy = oy + rp.y;
        List<?> offers = activeOffers();
        if (selectedIndex < 0 || selectedIndex >= offers.size()) {
            g.drawString(font, "§8← Выберите товар", rpx + 6, rpy + rp.height / 2, 0xFF555566, false);
            return;
        }
        if (activeTab == 0) renderBuyDetail(g, rpx, rpy, (SellTradeOffer) offers.get(selectedIndex));
        else                renderSellDetail(g, rpx, rpy, (BuyTradeOffer)  offers.get(selectedIndex));
    }

    private void renderBuyDetail(GuiGraphics g, int rpx, int rpy, SellTradeOffer o) {
        GuiRect prev = layout.previewItemBoxRect;
        drawBox(g, rpx + prev.x, rpy + prev.y, prev.width, prev.height);
        renderOfferIcon(g, o.itemId, rpx + prev.x + (prev.width - 32) / 2, rpy + prev.y + 10, 2.0f);
        g.drawString(font, "§f§l" + (o.customName.isBlank() ? o.itemId : o.customName),
               rpx + prev.x + 6, rpy + prev.y + prev.height - 28, layout.titleTextColor, false);
        g.drawString(font, "§8" + (o.description.isBlank() ? "Описание отсутствует" : o.description),
                rpx + prev.x + 6, rpy + prev.y + prev.height - 15, layout.descriptionTextColor, false);

        GuiRect pric = layout.priceInfoBoxRect;
        drawBox(g, rpx + pric.x, rpy + pric.y, pric.width, pric.height);
        int disc = discounts.effectiveBuyDiscount(o.discountPercent);
        TradePriceResult pr = TradePriceResult.calculate(o.price, o.amount, disc);
        int py = rpy + pric.y + 6;
        if (disc > 0 && layout.showDiscount) {
            g.drawString(font, "§7Без скидки: §8" + (long) o.price * o.amount, rpx + pric.x + 6, py, 0xFF777766, false);
            g.drawString(font, "§aСкидка: -" + disc + "%", rpx + pric.x + 6, py + 12, layout.discountTextColor, false);
            py += 24;
        }
       boolean canAff = ClientCurrencyState.getBalance() >= totalCartPrice();
        g.drawString(font, "Цена за 1: §e" + pr.finalPrice + " §7монет", rpx + pric.x + 6, py, layout.priceTextColor, false);
        g.drawString(font, "В корзине: §f" + cartCount(o.id), rpx + pric.x + 6, py + 12, 0xFFAAAAAA, false);
        g.drawString(font, "К оплате: " + (canAff ? "§e" : "§c") + totalCartPrice() + " §7монет", rpx + pric.x + 6, py + 24, layout.priceTextColor, false);
        g.drawString(font, "Всего позиций: §f" + totalCartItems(), rpx + pric.x + 6, py + 36, 0xFFAAAAAA, false);

        GuiRect act = layout.actionBoxRect;
        drawBox(g, rpx + act.x, rpy + act.y, act.width, act.height);
        boolean inSt = hasAnyInStockInCart();
        if (totalCartItems() <= 0) g.drawString(font, "§7Добавьте товары кнопкой +1", rpx + act.x + 6, rpy + act.y + 6, 0xFFAAAAAA, false);
        else if (!canAff)  g.drawString(font, "§cНедостаточно монет",   rpx + act.x + 6, rpy + act.y + 6, layout.errorTextColor, false);
        else if (!inSt) g.drawString(font, "§cТовар закончился",   rpx + act.x + 6, rpy + act.y + 6, layout.errorTextColor, false);
        else            g.drawString(font, "§aДоступно к покупке", rpx + act.x + 6, rpy + act.y + 6, layout.discountTextColor, false);
    }

    private void renderSellDetail(GuiGraphics g, int rpx, int rpy, BuyTradeOffer o) {
        GuiRect prev = layout.previewItemBoxRect;
        drawBox(g, rpx + prev.x, rpy + prev.y, prev.width, prev.height);
        g.drawString(font, "§f§l" + (o.customName.isBlank() ? o.itemId : o.customName),
                rpx + prev.x + 6, rpy + prev.y + 8, layout.titleTextColor, false);
        if (!o.description.isBlank())
            g.drawString(font, "§8" + o.description, rpx + prev.x + 6, rpy + prev.y + 22, layout.descriptionTextColor, false);

        GuiRect pric = layout.priceInfoBoxRect;
        drawBox(g, rpx + pric.x, rpy + pric.y, pric.width, pric.height);
        int bon = discounts.effectiveSellBonus(o.bonusPercent);
        TradeRewardResult rw = TradeRewardResult.calculate(o.reward, 1, bon);
        int py = rpy + pric.y + 6;
        if (bon > 0 && layout.showDiscount) {
            g.drawString(font, "§7Базовая: §8" + o.reward, rpx + pric.x + 6, py, 0xFF777766, false);
            g.drawString(font, "§aБонус: +" + bon + "%", rpx + pric.x + 6, py + 12, layout.discountTextColor, false);
            py += 24;
        }
        g.drawString(font, "§7Вы получите: §e" + rw.finalReward + " §7монет", rpx + pric.x + 6, py, layout.priceTextColor, false);
        g.drawString(font, "§7×" + o.requiredAmount + " предм./сделка", rpx + pric.x + 6, py + 12, 0xFFAAAAAA, false);

  GuiRect act = layout.actionBoxRect;
        drawBox(g, rpx + act.x, rpy + act.y, act.width, act.height);
        int has    = countInInventory(o.itemId);
        boolean en = has >= o.requiredAmount;
        boolean hl = o.infiniteDemand || o.demandLeft > 0;
        String dem = o.infiniteDemand ? "§a∞" : (o.demandLeft > 0 ? "§f" + o.demandLeft : "§cИсчерп.");
        g.drawString(font, "§7У вас: " + (en ? "§f" : "§c") + has + " §7/ §f" + o.requiredAmount, rpx + act.x + 6, rpy + act.y + 6, 0xFFAAAAAA, false);
        g.drawString(font, "§7Лимит NPC: " + dem, rpx + act.x + 6, rpy + act.y + 18, 0xFFAAAAAA, false);
        if (!en)       g.drawString(font, "§cНедостаточно предметов", rpx + act.x + 6, rpy + act.y + 30, layout.errorTextColor, false);
        else if (!hl)  g.drawString(font, "§cЛимит исчерпан",         rpx + act.x + 6, rpy + act.y + 30, layout.errorTextColor, false);
        else if (maxSellTimes(has, o) > 1) {
            g.drawString(font, "§7Макс. продаж: §f×" + maxSellTimes(has, o), rpx + act.x + 6, rpy + act.y + 30, 0xFFAAAAAA, false);
        }
    }

    private void drawBox(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, layout.slotBgColor);
        NpcCreatorScreen.brd(g, x, y, w, h, layout.slotBdrColor);
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            if (activeTab == 0 && handleBuySlotControls((int) mx, (int) my)) return true;
            int idx = hitTestGrid((int) mx, (int) my);
            if (idx >= 0) { selectedIndex = idx; rebuildWidgets(); return true; }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        List<?> offers = activeOffers();
        int cols = 4;
        int cols = Math.max(1, layout.itemColumns);
        int visibleRows = visibleRowsForGrid();
        if (total <= visibleRows) return false;
        scrollOffset = Math.max(0, Math.min(scrollOffset - (int) Math.signum(delta), total - visibleRows));
        scrollOffset = Math.max(0, Math.min(scrollOffset - (int) Math.signum(delta), total - layout.visibleRows));
        rebuildWidgets();
        return true;
    }

    private int hitTestGrid(int mx, int my) {
        GuiRect gr = layout.itemGridRect;
        int sx0 = ox() + gr.x + 2, sy0 = oy() + gr.y + 2;
        int cols = 4;
        int sW = slotSize(), sH = slotSize();
        int sW = layout.itemSlotWidth, sH = layout.itemSlotHeight;
        int gX = layout.itemSlotGapX,  gY = layout.itemSlotGapY;
        List<?> offers = activeOffers();
        int first = scrollOffset * cols;
        for (int i = first; i < offers.size(); i++) {
            int row = (i - first) / cols, col = (i - first) % cols;
            int sx  = sx0 + col * (sW + gX);
            int sy  = sy0 + row * (sH + gY);
            if (sy + sH > maxY) break;
            if (mx >= sx && mx < sx + sW && my >= sy && my < sy + sH) return i;
        }
        return -1;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void doBuy() {
        if (sells.isEmpty()) return;
        if (totalCartItems() <= 0 || !canAffordCart()) return;
        for (SellTradeOffer offer : sells) {
            int qty = cartCount(offer.id);
            for (int i = 0; i < qty; i++) {
                ModNetwork.CHANNEL.sendToServer(new BuyTradeOfferPacket(npcUuid, offer.id));
            }
        }
        buyCart.clear();
        ModNetwork.CHANNEL.sendToServer(new BuyTradeOfferPacket(npcUuid, sells.get(selectedIndex).id));
        if (minecraft != null) minecraft.setScreen(null);
    }

    private void doSell(boolean all) {
        var buys = npcData.professionData.traderData.buyOffers;
        if (selectedIndex < 0 || selectedIndex >= buys.size()) return;
        ModNetwork.CHANNEL.sendToServer(new SellToNpcPacket(npcUuid, buys.get(selectedIndex).id, all));
        if (minecraft != null) minecraft.setScreen(null);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private List<?> activeOffers() {
        var td = npcData.professionData.traderData;
        if (td == null) return List.of();
        return activeTab == 0 ? td.sellOffers : td.buyOffers;
    }

    private boolean canAffordSelected() {
        var sells = npcData.professionData.traderData.sellOffers;
        if (selectedIndex < 0 || selectedIndex >= sells.size()) return false;
        SellTradeOffer o = sells.get(selectedIndex);
        if (!o.infiniteStock && o.stock <= 0) return false;
        long fp = TradePriceResult.calculate(o.price, o.amount, discounts.effectiveBuyDiscount(o.discountPercent)).finalPrice;
        return ClientCurrencyState.getBalance() >= fp;
    }

    private int pricePerSingle(SellTradeOffer o) {
        int disc = discounts.effectiveBuyDiscount(o.discountPercent);
        return (int) TradePriceResult.calculate(o.price, o.amount, disc).finalPrice;
    }

    private long totalCartPrice() {
        var sells = npcData.professionData.traderData.sellOffers;
        long total = 0;
        for (SellTradeOffer offer : sells) {
            int qty = cartCount(offer.id);
            if (qty <= 0) continue;
            total += (long) pricePerSingle(offer) * qty;
        }
        return total;
    }

    private int totalCartItems() {
        return buyCart.values().stream().mapToInt(Integer::intValue).sum();
    }

    private int cartCount(String offerId) {
        return Math.max(0, buyCart.getOrDefault(offerId, 0));
    }

    private boolean canAffordCart() {
        return ClientCurrencyState.getBalance() >= totalCartPrice();
    }

    private boolean hasAnyInStockInCart() {
        for (SellTradeOffer offer : npcData.professionData.traderData.sellOffers) {
            int qty = cartCount(offer.id);
            if (qty <= 0) continue;
            if (offer.infiniteStock || offer.stock > 0) return true;
        }
        return false;
    }

    private boolean handleBuySlotControls(int mx, int my) {
        List<?> offers = activeOffers();
        GuiRect gr = layout.itemGridRect;
        int sx0 = ox() + gr.x + 2, sy0 = oy() + gr.y + 2;
        int maxY = oy() + gr.y + gr.height - 2;
        int cols = 4;
        int sW = slotSize(), sH = slotSize();
        int gX = layout.itemSlotGapX, gY = layout.itemSlotGapY;
        int first = scrollOffset * cols;
        for (int i = first; i < offers.size(); i++) {
            int row = (i - first) / cols, col = (i - first) % cols;
            int sx  = sx0 + col * (sW + gX);
            int sy  = sy0 + row * (sH + gY);
            if (sy + sH > maxY) break;
            if (mx < sx || mx >= sx + sW || my < sy || my >= sy + sH) continue;
            SellTradeOffer offer = (SellTradeOffer) offers.get(i);
            selectedIndex = i;
            int controlsTop = sy + sH - 20;
            if (my >= controlsTop) {
                if (mx < sx + 24) adjustCart(offer, -1);
                else if (mx > sx + sW - 24) adjustCart(offer, 1);
            }
            rebuildWidgets();
            return true;
        }
        return false;
    }

    private void adjustCart(SellTradeOffer offer, int delta) {
        int next = Math.max(0, cartCount(offer.id) + delta);
        if (next == 0) buyCart.remove(offer.id);
        else buyCart.put(offer.id, next);
    }

    private int slotSize() {
        GuiRect gr = layout.itemGridRect;
        int cols = 4;
        int totalGap = layout.itemSlotGapX * (cols - 1);
        return Math.max(36, (gr.width - 4 - totalGap) / cols);
    }

    private int visibleRowsForGrid() {
        int s = slotSize();
        return Math.max(1, (layout.itemGridRect.height - 4 + layout.itemSlotGapY) / (s + layout.itemSlotGapY));
    }

    private void renderOfferIcon(GuiGraphics g, String itemId, int x, int y) {
        renderOfferIcon(g, itemId, x, y, 1.0f);
    }

    private void renderOfferIcon(GuiGraphics g, String itemId, int x, int y, float scale) {
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
        if (item == null) return;
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1.0f);
        g.renderItem(new ItemStack(item), 0, 0);
        g.pose().popPose();
    }

private boolean canSellSelected() {
        var buys = npcData.professionData.traderData.buyOffers;
        if (selectedIndex < 0 || selectedIndex >= buys.size()) return false;
        BuyTradeOffer o = buys.get(selectedIndex);
        return countInInventory(o.itemId) >= o.requiredAmount && (o.infiniteDemand || o.demandLeft > 0);
    }

    private int countInInventory(String itemId) {
        if (itemId == null || itemId.isBlank()) return 0;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return 0;
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
        if (item == null) return 0;
        int count = 0;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            var s = mc.player.getInventory().getItem(i);
            if (!s.isEmpty() && s.is(item)) count += s.getCount();
        }
        return count;
    }

    private int maxSellTimes(int has, BuyTradeOffer o) {
        int by = has / Math.max(1, o.requiredAmount);
        return o.infiniteDemand ? by : Math.min(by, o.demandLeft);
    }

    @Override public boolean isPauseScreen() { return false; }
    private int ox() { return (width  - W) / 2; }
    private int oy() { return (height - H) / 2; }
}

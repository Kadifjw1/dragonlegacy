package com.frametrip.dragonlegacyquesttoast.profession.trader;

public class TraderShopLayoutData {

    public String presetId = "default";

    // ── Textures (empty string = use fallback colored fill) ──────────────────
    public String backgroundTexture        = "";
    public String leftPanelTexture         = "";
    public String rightPanelTexture        = "";
    public String itemSlotTexture          = "";
    public String selectedItemSlotTexture  = "";
    public String disabledItemSlotTexture  = "";
    public String previewBoxTexture        = "";
    public String priceBoxTexture          = "";
    public String descriptionBoxTexture    = "";
    public String actionBoxTexture         = "";
    public String buyButtonTexture         = "";
    public String sellButtonTexture        = "";
    public String tabButtonTexture         = "";
    public String tabButtonSelectedTexture = "";
    public String scrollbarTexture         = "";

    // ── Screen size ──────────────────────────────────────────────────────────
    public int screenWidth  = 480;
    public int screenHeight = 400;

    // ── Panel rects (absolute, from screen top-left) ─────────────────────────
    public GuiRect leftPanelRect  = new GuiRect(4,   58, 288, 306);
    public GuiRect rightPanelRect = new GuiRect(296, 58, 176, 306);

    // ── Item grid ────────────────────────────────────────────────────────────
    public GuiRect itemGridRect   = new GuiRect(4, 58, 288, 306);
    public int     itemColumns    = 1;
    public int     visibleRows    = 5;
    public int     itemSlotWidth  = 280;
    public int     itemSlotHeight = 52;
    public int     itemSlotGapX   = 2;
    public int     itemSlotGapY   = 4;

    // ── Slot sub-elements (relative to slot top-left) ────────────────────────
    public GuiRect itemIconRect       = new GuiRect(4,  8,  32, 32);
    public GuiRect itemPriceTextRect  = new GuiRect(42, 30, 200, 12);
    public GuiRect itemAmountTextRect = new GuiRect(42, 6,  200, 12);

    // ── Right panel sub-elements (relative to rightPanelRect) ────────────────
    public GuiRect previewItemBoxRect = new GuiRect(0,   0,  176,  76);
    public GuiRect priceInfoBoxRect   = new GuiRect(0,  78,  176,  66);
    public GuiRect descriptionBoxRect = new GuiRect(0, 146,  176,  54);
    public GuiRect actionBoxRect      = new GuiRect(0, 202,  176, 104);

    // ── Buttons (relative to actionBoxRect) ──────────────────────────────────
    public GuiRect buyButtonRect  = new GuiRect(6,  70, 76, 20);
    public GuiRect sellButtonRect = new GuiRect(86, 70, 76, 20);

    // ── Other UI rects (absolute from screen top-left) ────────────────────────
    public GuiRect balanceTextRect = new GuiRect(10, 374, 460, 16);
    public GuiRect modeTabsRect    = new GuiRect(8,  34,  190, 18);
    public GuiRect scrollbarRect   = new GuiRect(284, 58,  8, 306);

    // ── Text colors ──────────────────────────────────────────────────────────
    public int titleTextColor       = 0xFFCCCCCC;
    public int priceTextColor       = 0xFFFFDD55;
    public int descriptionTextColor = 0xFFAAAAAA;
    public int balanceTextColor     = 0xFFCCCCCC;
    public int discountTextColor    = 0xFF55FF55;
    public int errorTextColor       = 0xFFFF5555;

    // ── Text scales ──────────────────────────────────────────────────────────
    public float titleTextScale       = 1.0f;
    public float priceTextScale       = 1.0f;
    public float descriptionTextScale = 1.0f;
    public float balanceTextScale     = 1.0f;

    // ── Visibility flags ─────────────────────────────────────────────────────
    public boolean showItemName           = true;
    public boolean showItemPrice          = true;
    public boolean showItemAmountModifier = true;
    public boolean showBalance            = true;
    public boolean showDiscount           = true;
    public boolean showStock              = true;
    public boolean showRestockInfo        = false;

    // ── Panel colors (fallback when no texture set) ──────────────────────────
    public int bgColor            = 0xEE0A0A14;
    public int bgBorderColor      = 0xFF3A3A55;
    public int leftPanelBgColor   = 0x44090910;
    public int leftPanelBdrColor  = 0xFF2A2A44;
    public int rightPanelBgColor  = 0x55101020;
    public int rightPanelBdrColor = 0xFF2A2A44;
    public int slotBgColor        = 0x55202030;
    public int slotBdrColor       = 0xFF333344;
    public int slotSelBgColor     = 0x88304050;
    public int slotSelBdrColor    = 0xFF4488AA;
    public int headerBgColor      = 0xBB12121E;
    public int headerBdrColor     = 0xFF444466;
    public int tabBarBgColor      = 0xAA101020;
    public int bottomBarBgColor   = 0xAA101020;

    // ── Factory ──────────────────────────────────────────────────────────────
    public static TraderShopLayoutData createDefault() { return new TraderShopLayoutData(); }

    public TraderShopLayoutData copy() {
        TraderShopLayoutData c = new TraderShopLayoutData();
        c.presetId                  = this.presetId;
        c.backgroundTexture         = this.backgroundTexture;
        c.leftPanelTexture          = this.leftPanelTexture;
        c.rightPanelTexture         = this.rightPanelTexture;
        c.itemSlotTexture           = this.itemSlotTexture;
        c.selectedItemSlotTexture   = this.selectedItemSlotTexture;
        c.disabledItemSlotTexture   = this.disabledItemSlotTexture;
        c.previewBoxTexture         = this.previewBoxTexture;
        c.priceBoxTexture           = this.priceBoxTexture;
        c.descriptionBoxTexture     = this.descriptionBoxTexture;
        c.actionBoxTexture          = this.actionBoxTexture;
        c.buyButtonTexture          = this.buyButtonTexture;
        c.sellButtonTexture         = this.sellButtonTexture;
        c.tabButtonTexture          = this.tabButtonTexture;
        c.tabButtonSelectedTexture  = this.tabButtonSelectedTexture;
        c.scrollbarTexture          = this.scrollbarTexture;
        c.screenWidth               = this.screenWidth;
        c.screenHeight              = this.screenHeight;
        c.leftPanelRect             = this.leftPanelRect.copy();
        c.rightPanelRect            = this.rightPanelRect.copy();
        c.itemGridRect              = this.itemGridRect.copy();
        c.itemColumns               = this.itemColumns;
        c.visibleRows               = this.visibleRows;
        c.itemSlotWidth             = this.itemSlotWidth;
        c.itemSlotHeight            = this.itemSlotHeight;
        c.itemSlotGapX              = this.itemSlotGapX;
        c.itemSlotGapY              = this.itemSlotGapY;
        c.itemIconRect              = this.itemIconRect.copy();
        c.itemPriceTextRect         = this.itemPriceTextRect.copy();
        c.itemAmountTextRect        = this.itemAmountTextRect.copy();
        c.previewItemBoxRect        = this.previewItemBoxRect.copy();
        c.priceInfoBoxRect          = this.priceInfoBoxRect.copy();
        c.descriptionBoxRect        = this.descriptionBoxRect.copy();
        c.actionBoxRect             = this.actionBoxRect.copy();
        c.buyButtonRect             = this.buyButtonRect.copy();
        c.sellButtonRect            = this.sellButtonRect.copy();
        c.balanceTextRect           = this.balanceTextRect.copy();
        c.modeTabsRect              = this.modeTabsRect.copy();
        c.scrollbarRect             = this.scrollbarRect.copy();
        c.titleTextColor            = this.titleTextColor;
        c.priceTextColor            = this.priceTextColor;
        c.descriptionTextColor      = this.descriptionTextColor;
        c.balanceTextColor          = this.balanceTextColor;
        c.discountTextColor         = this.discountTextColor;
        c.errorTextColor            = this.errorTextColor;
        c.titleTextScale            = this.titleTextScale;
        c.priceTextScale            = this.priceTextScale;
        c.descriptionTextScale      = this.descriptionTextScale;
        c.balanceTextScale          = this.balanceTextScale;
        c.showItemName              = this.showItemName;
        c.showItemPrice             = this.showItemPrice;
        c.showItemAmountModifier    = this.showItemAmountModifier;
        c.showBalance               = this.showBalance;
        c.showDiscount              = this.showDiscount;
        c.showStock                 = this.showStock;
        c.showRestockInfo           = this.showRestockInfo;
        c.bgColor                   = this.bgColor;
        c.bgBorderColor             = this.bgBorderColor;
        c.leftPanelBgColor          = this.leftPanelBgColor;
        c.leftPanelBdrColor         = this.leftPanelBdrColor;
        c.rightPanelBgColor         = this.rightPanelBgColor;
        c.rightPanelBdrColor        = this.rightPanelBdrColor;
        c.slotBgColor               = this.slotBgColor;
        c.slotBdrColor              = this.slotBdrColor;
        c.slotSelBgColor            = this.slotSelBgColor;
        c.slotSelBdrColor           = this.slotSelBdrColor;
        c.headerBgColor             = this.headerBgColor;
        c.headerBdrColor            = this.headerBdrColor;
        c.tabBarBgColor             = this.tabBarBgColor;
        c.bottomBarBgColor          = this.bottomBarBgColor;
        return c;
    }
}


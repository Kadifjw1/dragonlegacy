package com.frametrip.dragonlegacyquesttoast.profession.trader;

import java.util.ArrayList;
import java.util.List;

public class TraderShopLayoutPresetManager {

    private static final List<TraderShopLayoutPreset> PRESETS = new ArrayList<>();

    static { buildPresets(); }

    public static List<TraderShopLayoutPreset> getPresets() { return PRESETS; }

    public static TraderShopLayoutPreset getPreset(String id) {
        return PRESETS.stream().filter(p -> p.id.equals(id)).findFirst().orElse(PRESETS.get(0));
    }

    public static TraderShopLayoutData createLayoutFromPreset(String id) {
        return getPreset(id).layoutData.copy();
    }

    private static void buildPresets() {
        PRESETS.add(new TraderShopLayoutPreset("default",          "Стандартный",           buildDefault()));
        PRESETS.add(new TraderShopLayoutPreset("wooden_market",    "Деревянная лавка",      buildWooden()));
        PRESETS.add(new TraderShopLayoutPreset("stone_blacksmith", "Кузница",               buildBlacksmith()));
        PRESETS.add(new TraderShopLayoutPreset("arcane_mage",      "Магический магазин",    buildArcane()));
        PRESETS.add(new TraderShopLayoutPreset("dragon_legacy",    "Наследие Дракона",      buildDragon()));
        PRESETS.add(new TraderShopLayoutPreset("dark_ritual",      "Тёмный ритуал",         buildDark()));
        PRESETS.add(new TraderShopLayoutPreset("village_shop",     "Деревенская лавка",     buildVillage()));
    }

    private static TraderShopLayoutData buildDefault() {
        return TraderShopLayoutData.createDefault();
    }

    private static TraderShopLayoutData buildWooden() {
        TraderShopLayoutData d = TraderShopLayoutData.createDefault();
        d.presetId            = "wooden_market";
        d.bgColor             = 0xEE1A1004;  d.bgBorderColor     = 0xFF8B6530;
        d.leftPanelBgColor    = 0x44251804;  d.leftPanelBdrColor  = 0xFF7A5020;
        d.rightPanelBgColor   = 0x44201508;  d.rightPanelBdrColor = 0xFF8B6530;
        d.slotBgColor         = 0x55301A08;  d.slotBdrColor       = 0xFF6A4018;
        d.slotSelBgColor      = 0x88503018;  d.slotSelBdrColor    = 0xFFCCAA55;
        d.headerBgColor       = 0xBB1A1004;  d.headerBdrColor     = 0xFF8B6530;
        d.tabBarBgColor       = 0xAA1A1208;  d.bottomBarBgColor   = 0xAA1A1208;
        d.titleTextColor      = 0xFFE8CB87;  d.priceTextColor     = 0xFFFFD36A;
        d.descriptionTextColor= 0xFFCCAA77;  d.balanceTextColor   = 0xFFE8CB87;
        d.discountTextColor   = 0xFF88FF44;  d.errorTextColor     = 0xFFFF7755;
        return d;
    }

    private static TraderShopLayoutData buildBlacksmith() {
        TraderShopLayoutData d = TraderShopLayoutData.createDefault();
        d.presetId            = "stone_blacksmith";
        d.bgColor             = 0xEE0C0C0E;  d.bgBorderColor     = 0xFF555555;
        d.leftPanelBgColor    = 0x44141416;  d.leftPanelBdrColor  = 0xFF444446;
        d.rightPanelBgColor   = 0x44101012;  d.rightPanelBdrColor = 0xFF555555;
        d.slotBgColor         = 0x55181818;  d.slotBdrColor       = 0xFF383838;
        d.slotSelBgColor      = 0x88282818;  d.slotSelBdrColor    = 0xFFCC6600;
        d.headerBgColor       = 0xBB0C0C0E;  d.headerBdrColor     = 0xFF666666;
        d.tabBarBgColor       = 0xAA101010;  d.bottomBarBgColor   = 0xAA101010;
        d.titleTextColor      = 0xFFCCCCCC;  d.priceTextColor     = 0xFFFF8833;
        d.descriptionTextColor= 0xFF999999;  d.balanceTextColor   = 0xFFBBBBBB;
        d.discountTextColor   = 0xFFFFAA44;  d.errorTextColor     = 0xFFFF4444;
        return d;
    }

    private static TraderShopLayoutData buildArcane() {
        TraderShopLayoutData d = TraderShopLayoutData.createDefault();
        d.presetId            = "arcane_mage";
        d.bgColor             = 0xEE04040E;  d.bgBorderColor     = 0xFF6644BB;
        d.leftPanelBgColor    = 0x44060414;  d.leftPanelBdrColor  = 0xFF4422AA;
        d.rightPanelBgColor   = 0x44080414;  d.rightPanelBdrColor = 0xFF6644BB;
        d.slotBgColor         = 0x55100830;  d.slotBdrColor       = 0xFF3322AA;
        d.slotSelBgColor      = 0x88201060;  d.slotSelBdrColor    = 0xFFCC88FF;
        d.headerBgColor       = 0xBB040410;  d.headerBdrColor     = 0xFF7755CC;
        d.tabBarBgColor       = 0xAA060410;  d.bottomBarBgColor   = 0xAA060410;
        d.titleTextColor      = 0xFFCC99FF;  d.priceTextColor     = 0xFF88DDFF;
        d.descriptionTextColor= 0xFF9988CC;  d.balanceTextColor   = 0xFFBB99EE;
        d.discountTextColor   = 0xFF44FFCC;  d.errorTextColor     = 0xFFFF44AA;
        return d;
    }

    private static TraderShopLayoutData buildDragon() {
        TraderShopLayoutData d = TraderShopLayoutData.createDefault();
        d.presetId            = "dragon_legacy";
        d.bgColor             = 0xEE080600;  d.bgBorderColor     = 0xFFCC8800;
        d.leftPanelBgColor    = 0x440C0A00;  d.leftPanelBdrColor  = 0xFFAA7700;
        d.rightPanelBgColor   = 0x44100C00;  d.rightPanelBdrColor = 0xFFCC8800;
        d.slotBgColor         = 0x55181000;  d.slotBdrColor       = 0xFF8B6600;
        d.slotSelBgColor      = 0x88302000;  d.slotSelBdrColor    = 0xFFFFCC33;
        d.headerBgColor       = 0xBB080600;  d.headerBdrColor     = 0xFFDD9900;
        d.tabBarBgColor       = 0xAA0A0800;  d.bottomBarBgColor   = 0xAA0A0800;
        d.titleTextColor      = 0xFFFFE066;  d.priceTextColor     = 0xFFFFCC33;
        d.descriptionTextColor= 0xFFCCAA66;  d.balanceTextColor   = 0xFFFFDD77;
        d.discountTextColor   = 0xFF88FF44;  d.errorTextColor     = 0xFFFF5533;
        return d;
    }

    private static TraderShopLayoutData buildDark() {
        TraderShopLayoutData d = TraderShopLayoutData.createDefault();
        d.presetId            = "dark_ritual";
        d.bgColor             = 0xEE020204;  d.bgBorderColor     = 0xFF660011;
        d.leftPanelBgColor    = 0x44040204;  d.leftPanelBdrColor  = 0xFF440011;
        d.rightPanelBgColor   = 0x44040204;  d.rightPanelBdrColor = 0xFF660011;
        d.slotBgColor         = 0x55080208;  d.slotBdrColor       = 0xFF330022;
        d.slotSelBgColor      = 0x88200010;  d.slotSelBdrColor    = 0xFFCC0022;
        d.headerBgColor       = 0xBB020204;  d.headerBdrColor     = 0xFF880022;
        d.tabBarBgColor       = 0xAA040204;  d.bottomBarBgColor   = 0xAA040204;
        d.titleTextColor      = 0xFFCC3344;  d.priceTextColor     = 0xFFFF4455;
        d.descriptionTextColor= 0xFF996677;  d.balanceTextColor   = 0xFFCC3344;
        d.discountTextColor   = 0xFF884444;  d.errorTextColor     = 0xFFFF2222;
        return d;
    }

    private static TraderShopLayoutData buildVillage() {
        TraderShopLayoutData d = TraderShopLayoutData.createDefault();
        d.presetId            = "village_shop";
        d.bgColor             = 0xEE10180C;  d.bgBorderColor     = 0xFF557733;
        d.leftPanelBgColor    = 0x44141C10;  d.leftPanelBdrColor  = 0xFF446633;
        d.rightPanelBgColor   = 0x44101810;  d.rightPanelBdrColor = 0xFF557733;
        d.slotBgColor         = 0x55182010;  d.slotBdrColor       = 0xFF3A5525;
        d.slotSelBgColor      = 0x88203010;  d.slotSelBdrColor    = 0xFF88CC44;
        d.headerBgColor       = 0xBB101810;  d.headerBdrColor     = 0xFF5A7A3A;
        d.tabBarBgColor       = 0xAA121A10;  d.bottomBarBgColor   = 0xAA121A10;
        d.titleTextColor      = 0xFFCCEEAA;  d.priceTextColor     = 0xFFAACC55;
        d.descriptionTextColor= 0xFF99AA77;  d.balanceTextColor   = 0xFFCCDD99;
        d.discountTextColor   = 0xFF66FF55;  d.errorTextColor     = 0xFFFF7755;
        return d;
    }
}


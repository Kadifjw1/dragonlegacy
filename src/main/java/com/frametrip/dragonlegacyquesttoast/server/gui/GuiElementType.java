package com.frametrip.dragonlegacyquesttoast.server.gui;

public enum GuiElementType {
    BACKGROUND,
    PANEL,
    BUTTON,
    TEXT,
    SCROLL_AREA,
    ITEM_GRID;

    public String label() {
        return switch (this) {
            case BACKGROUND -> "Фон окна";
            case PANEL      -> "Панель";
            case BUTTON     -> "Кнопка";
            case TEXT       -> "Текст";
            case SCROLL_AREA -> "Прокрутка";
            case ITEM_GRID  -> "Сетка товаров";
        };
    }

    public String icon() {
        return switch (this) {
            case BACKGROUND -> "▪";
            case PANEL      -> "▬";
            case BUTTON     -> "⬜";
            case TEXT       -> "T";
            case SCROLL_AREA -> "≡";
            case ITEM_GRID  -> "⊞";
        };
    }
}

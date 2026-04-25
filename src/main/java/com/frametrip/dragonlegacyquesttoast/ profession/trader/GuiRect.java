package com.frametrip.dragonlegacyquesttoast.profession.trader;

public class GuiRect {
    public int x, y, width, height;

    public GuiRect() {}

    public GuiRect(int x, int y, int width, int height) {
        this.x = x; this.y = y; this.width = width; this.height = height;
    }

    public GuiRect copy() { return new GuiRect(x, y, width, height); }

    public void set(int x, int y, int width, int height) {
        this.x = x; this.y = y; this.width = width; this.height = height;
    }
}

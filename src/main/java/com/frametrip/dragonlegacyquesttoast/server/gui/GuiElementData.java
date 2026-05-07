package com.frametrip.dragonlegacyquesttoast.server.gui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class GuiElementData {
    public String id = UUID.randomUUID().toString().substring(0, 6);
    public GuiElementType type = GuiElementType.PANEL;

    /** Position and size in canvas units (0–100 relative to template canvas). */
    public int x = 10, y = 10, w = 80, h = 40;

    /**
     * Type-specific properties.
     * Common keys: "color", "text", "texture", "action",
     *              "columns", "alpha", "fontSize", "scrollDir"
     */
    public Map<String, String> props = new LinkedHashMap<>();

    public GuiElementData() {}

    public GuiElementData(GuiElementType type) {
        this.type = type;
        applyDefaults();
    }

    private void applyDefaults() {
        switch (type) {
            case BACKGROUND -> {
                props.put("color", "#0D0D1A");
                props.put("alpha", "220");
                w = 100;
                h = 100;
                x = 0;
                y = 0;
            }
            case PANEL -> {
                props.put("color", "#14142B");
                props.put("alpha", "200");
            }
            case BUTTON -> {
                props.put("text", "Кнопка");
                props.put("color", "#224488");
                props.put("action", "");
                h = 14;
            }
            case TEXT -> {
                props.put("text", "Текст");
                props.put("color", "#CCCCDD");
                props.put("fontSize", "normal");
                h = 10;
            }
            case SCROLL_AREA -> {
                props.put("scrollDir", "vertical");
                h = 60;
            }
            case ITEM_GRID -> {
                props.put("columns", "3");
                props.put("padding", "2");
            }
        }
    }

    public String prop(String key, String def) {
        return props.getOrDefault(key, def);
    }

    public String prop(String key) {
        return props.get(key);
    }

    public void setProp(String key, String value) {
        props.put(key, value);
    }

    public GuiElementData copy() {
        GuiElementData c = new GuiElementData();
        c.id = UUID.randomUUID().toString().substring(0, 6);
        c.type = this.type;
        c.x = this.x;
        c.y = this.y;
        c.w = this.w;
        c.h = this.h;
        c.props = new LinkedHashMap<>(this.props);
        return c;
    }
}

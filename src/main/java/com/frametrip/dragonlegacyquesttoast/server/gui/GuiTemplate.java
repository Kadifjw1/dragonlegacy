package com.frametrip.dragonlegacyquesttoast.server.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GuiTemplate {
    public String id   = UUID.randomUUID().toString().substring(0, 8);
    public String name = "Интерфейс";

    /** "shop", "journal", "custom" */
    public String templateType = "custom";

    public List<GuiElementData> elements = new ArrayList<>();

    public GuiTemplate() {}

    public GuiTemplate(String name, String type) {
        this.name         = name;
        this.templateType = type;
        applyDefaults();
    }

    private void applyDefaults() {
        elements.add(new GuiElementData(GuiElementType.BACKGROUND));
        switch (templateType) {
            case "shop" -> {
                GuiElementData panel = new GuiElementData(GuiElementType.PANEL);
                panel.x = 5; panel.y = 5; panel.w = 90; panel.h = 70;
                elements.add(panel);
                GuiElementData grid = new GuiElementData(GuiElementType.ITEM_GRID);
                grid.x = 8; grid.y = 10; grid.w = 84; grid.h = 55;
                elements.add(grid);
                GuiElementData btn = new GuiElementData(GuiElementType.BUTTON);
                btn.x = 30; btn.y = 80; btn.w = 40; btn.h = 14;
                btn.prop("text", "Купить");
                elements.add(btn);
            }
            case "journal" -> {
                GuiElementData panel = new GuiElementData(GuiElementType.PANEL);
                panel.x = 5; panel.y = 5; panel.w = 90; panel.h = 80;
                elements.add(panel);
                GuiElementData text = new GuiElementData(GuiElementType.TEXT);
                text.x = 8; text.y = 8; text.w = 84; text.h = 70;
                text.prop("text", "Страница журнала...");
                elements.add(text);
            }
        }
    }

    public GuiTemplate copy() {
        GuiTemplate c = new GuiTemplate();
        c.id           = UUID.randomUUID().toString().substring(0, 8);
        c.name         = this.name + " (копия)";
        c.templateType = this.templateType;
        for (GuiElementData e : elements) c.elements.add(e.copy());
        return c;
    }
}

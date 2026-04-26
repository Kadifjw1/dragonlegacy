package com.frametrip.dragonlegacyquesttoast.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

public class TraderItemPickerScreen extends Screen {

    private static final int W = 560;
    private static final int H = 360;
    private static final int COLS = 10;
    private static final int ROWS = 5;
    private static final int SLOT = 20;

    private final Screen parent;
    private final BiConsumer<String, String> onSelect;
    private final String initiallySelectedId;

    private EditBox searchField;
    private final List<Entry> allEntries = new ArrayList<>();
    private final List<Entry> filteredEntries = new ArrayList<>();
    private int page = 0;

    private static class Entry {
        final Item item;
        final String id;
        final String displayName;

        Entry(Item item, String id, String displayName) {
            this.item = item;
            this.id = id;
            this.displayName = displayName;
        }
    }

    public TraderItemPickerScreen(Screen parent, String selectedItemId, BiConsumer<String, String> onSelect) {
        super(Component.literal("Выбор предмета / блока"));
        this.parent = parent;
        this.onSelect = onSelect;
        this.initiallySelectedId = selectedItemId == null ? "" : selectedItemId;
    }

    @Override
    protected void init() {
        super.init();
        loadEntries();

        int ox = ox();
        int oy = oy();

        searchField = new EditBox(font, ox + 10, oy + 30, W - 20, 18, Component.literal("Поиск"));
        searchField.setMaxLength(100);
        searchField.setValue("");
        searchField.setResponder(v -> {
            page = 0;
            refilter();
        });
        addRenderableWidget(searchField);
        setInitialFocus(searchField);

        addRenderableWidget(Button.builder(Component.literal("◀"), b -> {
            if (page > 0) page--;
        }).bounds(ox + 10, oy + H - 28, 24, 18).build());

        addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
            if (page < maxPage()) page++;
        }).bounds(ox + 38, oy + H - 28, 24, 18).build());

        addRenderableWidget(Button.builder(Component.literal("Отмена"), b -> {
            if (minecraft != null) minecraft.setScreen(parent);
        }).bounds(ox + W - 90, oy + H - 28, 80, 18).build());

        refilter();
        jumpToInitiallySelected();
    }

    private void loadEntries() {
        allEntries.clear();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item == null || item == Items.AIR) continue;
            var key = ForgeRegistries.ITEMS.getKey(item);
            if (key == null) continue;
            String id = key.toString();
            String displayName = new ItemStack(item).getHoverName().getString();
            allEntries.add(new Entry(item, id, displayName));
        }
        allEntries.sort(Comparator.comparing((Entry e) -> e.displayName.toLowerCase(Locale.ROOT))
                .thenComparing(e -> e.id));
    }

    private void refilter() {
        filteredEntries.clear();
        String q = searchField == null ? "" : searchField.getValue().trim().toLowerCase(Locale.ROOT);
        for (Entry e : allEntries) {
            if (q.isEmpty()
                    || e.id.toLowerCase(Locale.ROOT).contains(q)
                    || e.displayName.toLowerCase(Locale.ROOT).contains(q)) {
                filteredEntries.add(e);
            }
        }
        if (page > maxPage()) page = maxPage();
    }

    private void jumpToInitiallySelected() {
        if (initiallySelectedId.isBlank()) return;
        for (int i = 0; i < filteredEntries.size(); i++) {
            if (filteredEntries.get(i).id.equalsIgnoreCase(initiallySelectedId)) {
                page = i / pageSize();
                return;
            }
        }
    }

    private int pageSize() {
        return COLS * ROWS;
    }

    private int maxPage() {
        if (filteredEntries.isEmpty()) return 0;
        return (filteredEntries.size() - 1) / pageSize();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (super.mouseClicked(mx, my, btn)) return true;

        int ox = ox();
        int oy = oy();
        int startX = ox + 10;
        int startY = oy + 60;
        int indexStart = page * pageSize();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int x = startX + col * (SLOT + 4);
                int y = startY + row * (SLOT + 24);
                if (mx >= x && mx < x + SLOT && my >= y && my < y + SLOT) {
                    int idx = indexStart + row * COLS + col;
                    if (idx >= 0 && idx < filteredEntries.size()) {
                        Entry e = filteredEntries.get(idx);
                        onSelect.accept(e.id, e.displayName);
                        if (minecraft != null) minecraft.setScreen(parent);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int ox = ox();
        int oy = oy();

        g.fill(ox, oy, ox + W, oy + H, 0xEE0A0A14);
        NpcCreatorScreen.brd(g, ox, oy, W, H, 0xFF3A3A55);
        g.fill(ox, oy, ox + W, oy + 24, 0xBB12121E);
        g.drawString(font, "§f" + getTitle().getString(), ox + 8, oy + 8, 0xFFE6D7B5, false);

        super.render(g, mx, my, pt);

        int startX = ox + 10;
        int startY = oy + 60;
        int idxStart = page * pageSize();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int idx = idxStart + row * COLS + col;
                int x = startX + col * (SLOT + 4);
                int y = startY + row * (SLOT + 24);
                g.fill(x, y, x + SLOT, y + SLOT, 0xCC202030);
                NpcCreatorScreen.brd(g, x, y, SLOT, SLOT, 0xFF444455);

                if (idx < filteredEntries.size()) {
                    Entry e = filteredEntries.get(idx);
                    g.renderItem(new ItemStack(e.item), x + 2, y + 2);
                    String shortName = trim(e.displayName, 11);
                    g.drawString(font, shortName, x, y + SLOT + 2, 0xFFAAAAAA, false);

                    if (mx >= x && mx < x + SLOT && my >= y && my < y + SLOT) {
                        g.fill(x, y, x + SLOT, y + SLOT, 0x66FFFFFF);
                        g.drawString(font, "§f" + e.displayName, ox + 10, oy + H - 48, 0xFFE6D7B5, false);
                        g.drawString(font, "§8" + e.id, ox + 10, oy + H - 38, 0xFF9A9A9A, false);
                    }
                }
            }
        }

        g.drawString(font, "§7Найдено: §f" + filteredEntries.size(), ox + 70, oy + H - 23, 0xFF888877, false);
        g.drawString(font, "§7Стр. §f" + (page + 1) + "§7/§f" + (maxPage() + 1), ox + 170, oy + H - 23, 0xFF888877, false);
        g.drawString(font, "§8Совет: ищи по названию или ID (включая модовые предметы)", ox + 10, oy + H - 12, 0xFF777777, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int ox() {
        return (width - W) / 2;
    }

    private int oy() {
        return (height - H) / 2;
    }

    private static String trim(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}

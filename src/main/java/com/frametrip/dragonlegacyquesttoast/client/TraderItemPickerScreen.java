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
    private static final int SLOT = 20;
    private static final int GAP = 2;

    private final Screen parent;
    private final BiConsumer<String, String> onSelect;
    private final String initiallySelectedId;

    private EditBox searchField;
    private final List<Entry> allEntries = new ArrayList<>();
    private final List<Entry> filteredEntries = new ArrayList<>();
    private int scrollRow = 0;

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
            scrollRow = 0;
            refilter();
        });
        addRenderableWidget(searchField);
        setInitialFocus(searchField);

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
         int maxScroll = Math.max(0, totalRows() - visibleRows());
        if (scrollRow > maxScroll) scrollRow = maxScroll;
    }

    private void jumpToInitiallySelected() {
        if (initiallySelectedId.isBlank()) return;
        for (int i = 0; i < filteredEntries.size(); i++) {
            if (filteredEntries.get(i).id.equalsIgnoreCase(initiallySelectedId)) {
                scrollRow = i / gridCols();
                return;
            }
        }
    }

    private int gridLeft() {
        return ox() + 10;
    }

    private int gridTop() {
        return oy() + 56;
    }

    private int gridWidth() {
        return W - 20;
    }

    private int gridHeight() {
        return H - 92;
    }

    private int gridCols() {
        return Math.max(1, (gridWidth() + GAP) / (SLOT + GAP));
    }

    private int visibleRows() {
        return Math.max(1, (gridHeight() + GAP) / (SLOT + GAP));
    }

    private int totalRows() {
        int cols = gridCols();
        if (filteredEntries.isEmpty()) return 0;
        return (filteredEntries.size() + cols - 1) / cols;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (super.mouseClicked(mx, my, btn)) return true;

        int startX = gridLeft();
        int startY = gridTop();
        int cols = gridCols();
        int rows = visibleRows();
        int indexStart = scrollRow * cols;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = startX + col * (SLOT + GAP);
                int y = startY + row * (SLOT + GAP);
                if (mx >= x && mx < x + SLOT && my >= y && my < y + SLOT) {
                    int idx = indexStart + row * cols + col;
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
    public boolean mouseScrolled(double mx, double my, double delta) {
        int maxScroll = Math.max(0, totalRows() - visibleRows());
        if (maxScroll <= 0) return false;
        int next = scrollRow - (int) Math.signum(delta);
        scrollRow = Math.max(0, Math.min(next, maxScroll));
        return true;
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

        int startX = gridLeft();
        int startY = gridTop();
        int cols = gridCols();
        int rows = visibleRows();
        int idxStart = scrollRow * cols;
        Entry hovered = null;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int idx = idxStart + row * cols + col;
                int x = startX + col * (SLOT + GAP);
                int y = startY + row * (SLOT + GAP);
                g.fill(x, y, x + SLOT, y + SLOT, 0xCC202030);
                NpcCreatorScreen.brd(g, x, y, SLOT, SLOT, 0xFF444455);

                if (idx < filteredEntries.size()) {
                    Entry e = filteredEntries.get(idx);
                    g.renderItem(new ItemStack(e.item), x + 2, y + 2);

                    if (mx >= x && mx < x + SLOT && my >= y && my < y + SLOT) {
                        g.fill(x, y, x + SLOT, y + SLOT, 0x66FFFFFF);
                         hovered = e;
                    }
                }
            }
        }

        if (hovered != null) {
            g.renderTooltip(font, Component.literal(hovered.displayName), mx, my);
        }

        g.drawString(font, "§7Найдено: §f" + filteredEntries.size(), ox + 10, oy + H - 23, 0xFF888877, false);
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
}

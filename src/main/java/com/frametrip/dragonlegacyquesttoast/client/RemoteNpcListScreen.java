package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.network.SyncRemoteNpcListPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// [EDT-4]: Remote NPC editor — lists all loaded NPC entities and opens their editor.
public class RemoteNpcListScreen extends Screen {

    private static final int W        = 400;
    private static final int H        = 280;
    private static final int ROW_H    = 22;
    private static final int VISIBLE  = 9;

    private final List<SyncRemoteNpcListPacket.NpcEntry> allEntries;
    private List<SyncRemoteNpcListPacket.NpcEntry>       filtered;
    private int scrollOffset = 0;

    private EditBox searchBox;

    public RemoteNpcListScreen(List<SyncRemoteNpcListPacket.NpcEntry> entries) {
        super(Component.literal("Удалённый редактор NPC"));
        this.allEntries = entries.stream()
                .sorted(Comparator.comparing(SyncRemoteNpcListPacket.NpcEntry::name))
                .collect(Collectors.toList());
        this.filtered = allEntries;
    }

    @Override
    protected void init() {
        int cx = (width  - W) / 2;
        int cy = (height - H) / 2;

        // Search box
        searchBox = new EditBox(font, cx + 4, cy + 24, W - 28, 16, Component.literal("Поиск…"));
        searchBox.setResponder(text -> {
            String q = text.toLowerCase();
            filtered = allEntries.stream()
                    .filter(e -> e.name().toLowerCase().contains(q))
                    .collect(Collectors.toList());
            scrollOffset = 0;
        });
        addRenderableWidget(searchBox);

        // NPC row buttons
        for (int i = 0; i < VISIBLE; i++) {
            int idx = scrollOffset + i;
            if (idx >= filtered.size()) break;
            SyncRemoteNpcListPacket.NpcEntry entry = filtered.get(idx);
            int rowY = cy + 44 + i * ROW_H;
            addRenderableWidget(Button.builder(
                    Component.literal("§f" + entry.name() + " §8@ " +
                            String.format("%.0f,%.0f,%.0f", entry.x(), entry.y(), entry.z())),
                    b -> openEditor(entry.uuid())
            ).bounds(cx + 4, rowY, W - 30, ROW_H - 2).build());
        }

        // Scroll buttons
        addRenderableWidget(Button.builder(Component.literal("▲"),
                b -> { scrollOffset = Math.max(0, scrollOffset - 1); rebuildList(); }
        ).bounds(cx + W - 22, cy + 44, 18, 18).build());
        addRenderableWidget(Button.builder(Component.literal("▼"),
                b -> { scrollOffset = Math.min(Math.max(0, filtered.size() - VISIBLE), scrollOffset + 1); rebuildList(); }
        ).bounds(cx + W - 22, cy + 64, 18, 18).build());

        // Close
        addRenderableWidget(Button.builder(Component.literal("Закрыть"),
                b -> onClose()
        ).bounds(cx + W / 2 - 30, cy + H - 22, 60, 16).build());
    }

    private void rebuildList() {
        String prev = searchBox != null ? searchBox.getValue() : "";
        clearWidgets();
        init();
        if (searchBox != null) searchBox.setValue(prev);
    }

    private void openEditor(UUID npcUuid) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        NpcEntity entity = mc.level.getEntitiesOfClass(NpcEntity.class,
                mc.player.getBoundingBox().inflate(512),
                e -> e.getUUID().equals(npcUuid)).stream().findFirst().orElse(null);
        if (entity != null) {
            mc.setScreen(new NpcCreatorScreen(entity));
        } else {
            mc.player.sendSystemMessage(
                    Component.literal("§cNPC вне зоны загрузки — подойдите ближе."));
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int cx = (width  - W) / 2;
        int cy = (height - H) / 2;

        g.fill(cx, cy, cx + W, cy + H, 0xCC222233);
        g.fill(cx, cy, cx + W, cy + 20, 0xFF333355);
        g.drawCenteredString(font, "§l📡 Удалённый редактор NPC", cx + W / 2, cy + 5, 0xFF99CCFF);
        g.drawString(font, "§8" + filtered.size() + " / " + allEntries.size() + " NPC",
                cx + 4, cy + H - 12, 0xFF555566, false);

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}

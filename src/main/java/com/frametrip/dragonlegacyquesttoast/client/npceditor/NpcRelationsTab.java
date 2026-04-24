package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.client.ClientFactionState;
import com.frametrip.dragonlegacyquesttoast.entity.FactionData;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SaveFactionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Tab 3 — Отношения: отношение к игроку, фракция NPC, редактор фракций. */
public class NpcRelationsTab implements NpcEditorTab {

public static final int ACCENT = 0xFFCC55EE;
    
    private static final int SWATCH_COUNT = 8;
    private static final int[] SWATCH_COLORS = {
            0xFF4466EE, 0xFFEE4444, 0xFF44EE55, 0xFFEEEE33,
            0xFFEE44EE, 0xFF33EEEE, 0xFFEEAA00, 0xFF999999
    };

    private boolean editingFaction = false;
    private FactionData draftFaction = null;
    private EditBox factionNameField;
    private EditBox factionDescField;

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        if (editingFaction) {
            initFactionEditor(add, rebuild, state, rx, oy, rw);
            return;
        }

        NpcEntityData d = state.getDraft();

        // ── Player relation (radio buttons) ───────────────────────────────────
        int relX = rx;
        for (String rel : NpcEntityData.RELATIONS) {
            boolean sel = d.playerRelation.equals(rel);
            add.accept(Button.builder(
                    Component.literal((sel ? "§e◉ §r" : "○ ")
                            + relColorCode(rel) + NpcEntityData.relationLabel(rel)),
                    b -> {
                        d.playerRelation = rel;
                        state.markDirty();
                        rebuild.run();
                    }
            ).bounds(relX, oy + 38, 110, 18).build());
            relX += 114;
        }

        // ── Faction selector ──────────────────────────────────────────────────
        List<FactionData> factions = ClientFactionState.getAll();

        add.accept(Button.builder(Component.literal("◀"), b -> {
            int i = factionIndex(factions, d.factionId);
            d.factionId = (i <= 0) ? "" : factions.get(i - 1).id;
            state.markDirty(); rebuild.run();
        }).bounds(rx, oy + 88, 20, 16).build());

        add.accept(Button.builder(Component.literal("▶"), b -> {
            int i = factionIndex(factions, d.factionId);
            if (!factions.isEmpty())
                d.factionId = (i < 0 || i >= factions.size() - 1)
                        ? factions.get(0).id : factions.get(i + 1).id;
            state.markDirty(); rebuild.run();
        }).bounds(rx + 22 + 120, oy + 88, 20, 16).build());

        add.accept(Button.builder(Component.literal("✕"), b -> {
            d.factionId = ""; state.markDirty(); rebuild.run();
        }).bounds(rx + 22 + 144, oy + 88, 20, 16).build());

        add.accept(Button.builder(Component.literal("+ Создать фракцию"), b -> {
            editingFaction = true;
            draftFaction = new FactionData();
            rebuild.run();
        }).bounds(rx, oy + 114, 130, 18).build());

        FactionData sel = ClientFactionState.get(d.factionId);
        if (sel != null) {
            add.accept(Button.builder(Component.literal("✎ Редактировать"), b -> {
                editingFaction = true;
                draftFaction = sel.copy();
                rebuild.run();
            }).bounds(rx + 134, oy + 114, 120, 18).build());
        }
    }

    // ── Faction editor ────────────────────────────────────────────────────────

    private void initFactionEditor(Consumer<AbstractWidget> add, Runnable rebuild,
                                   NpcEditorState state, int rx, int oy, int rw) {
        if (draftFaction == null) draftFaction = new FactionData();

        factionNameField = new EditBox(Minecraft.getInstance().font,
                rx, oy + 38, rw, 18, Component.literal("Название фракции"));
        factionNameField.setMaxLength(48);
        factionNameField.setValue(draftFaction.name);
        add.accept(factionNameField);

        factionDescField = new EditBox(Minecraft.getInstance().font,
                rx, oy + 74, rw, 18, Component.literal("Описание"));
        factionDescField.setMaxLength(128);
        factionDescField.setValue(draftFaction.description);
        add.accept(factionDescField);

        // Relation editors with other factions
        List<FactionData> others = ClientFactionState.getAll().stream()
                .filter(f -> !f.id.equals(draftFaction.id)).toList();

        int relY = oy + 154;
        for (FactionData other : others) {
            add.accept(Button.builder(
                    Component.literal("◀▶ " + NpcEntityData.relationLabel(draftFaction.getRelationTo(other.id))),
                    b -> {
                        String[] rels = NpcEntityData.RELATIONS;
                        int i = indexOf(rels, draftFaction.getRelationTo(other.id));
                        draftFaction.setRelationTo(other.id, rels[Math.floorMod(i + 1, rels.length)]);
                        rebuild.run();
                    }
            ).bounds(rx + 108, relY, 120, 14).build());
            relY += 18;
        }

        // Action buttons
        int bY = oy + 300;
        add.accept(Button.builder(Component.literal("Сохранить"), b -> {
            pullFactionFields();
            if (!draftFaction.name.isBlank()) {
                ModNetwork.CHANNEL.sendToServer(new SaveFactionPacket(draftFaction, false));
                ClientFactionState.sync(mergeFaction(ClientFactionState.getAll(), draftFaction));
                NpcEntityData d = state.getDraft();
                if (d.factionId.isEmpty()) d.factionId = draftFaction.id;
                state.markDirty();
            }
            closeFactionEditor(); rebuild.run();
        }).bounds(rx, bY, 110, 18).build());

        add.accept(Button.builder(Component.literal("Удалить"), b -> {
            ModNetwork.CHANNEL.sendToServer(new SaveFactionPacket(draftFaction, true));
            List<FactionData> updated = new ArrayList<>(ClientFactionState.getAll());
            updated.removeIf(f -> f.id.equals(draftFaction.id));
            ClientFactionState.sync(updated);
            NpcEntityData d = state.getDraft();
            if (d.factionId.equals(draftFaction.id)) d.factionId = "";
            state.markDirty();
            closeFactionEditor(); rebuild.run();
        }).bounds(rx + 114, bY, 100, 18).build());

        add.accept(Button.builder(Component.literal("Отмена"), b -> {
            closeFactionEditor(); rebuild.run();
        }).bounds(rx + 218, bY, 70, 18).build());
    }

    // ── Render ────────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();

        if (editingFaction && draftFaction != null) {
            renderFactionEditor(g, rx, oy, rw);
            return;
        }

        // Player relation card
        NpcInfoTab.sectionCard(g, rx, oy, rw, 66, "ОТНОШЕНИЕ К ИГРОКУ");
        g.drawString(font, "§7Как NPC относится к игроку:", rx + 4, oy + 12, 0xFF888877, false);

        // Visual accent under the selected relation
        int relX = rx;
        for (String rel : NpcEntityData.RELATIONS) {
            if (d.playerRelation.equals(rel)) {
                int col = "FRIENDLY".equals(rel) ? 0xFF44EE55 :
                        "HOSTILE".equals(rel)  ? 0xFFEE4444 : 0xFFAAAAAA;
                g.fill(relX, oy + 36, relX + 110, oy + 37, col);
            }
            relX += 114;
        }

        // Faction card
        FactionData fac = ClientFactionState.get(d.factionId);
        NpcInfoTab.sectionCard(g, rx, oy + 70, rw, 52, "ФРАКЦИЯ");
        g.drawString(font, "§7Фракция NPC:", rx + 4, oy + 82, 0xFF888877, false);

        if (fac != null) {
            g.fill(rx + 4, oy + 92, rx + 14, oy + 102, fac.color | 0xFF000000);
            NpcEditorUtils.brd(g, rx + 4, oy + 92, 10, 10, 0xFF555555);
        }
        String facLabel = fac == null ? "§8— без фракции —" : "§f" + fac.name;
        g.drawCenteredString(font, facLabel, rx + 22 + 60, oy + 92, 0xFFCCCCCC);

        // Faction relations overview (if faction is selected)
        if (fac != null) {
            List<FactionData> allFactions = ClientFactionState.getAll();
            long relCount = allFactions.stream().filter(f -> !f.id.equals(fac.id)).count();
            if (relCount > 0) {
                NpcInfoTab.sectionCard(g, rx, oy + 126, rw, (int)(16 + relCount * 14), "ОТНОШЕНИЯ ФРАКЦИИ");
                int ry = oy + 142;
                for (FactionData other : allFactions) {
                    if (other.id.equals(fac.id)) continue;
                    String rel = fac.getRelationTo(other.id);
                    int relCol = "FRIENDLY".equals(rel) ? 0xFF44EE55 :
                            "HOSTILE".equals(rel) ? 0xFFEE4444 : 0xFFAAAAAA;
                    g.fill(rx + 4, ry + 1, rx + 10, ry + 7, other.color | 0xFF000000);
                    g.drawString(font, "§f" + other.name + "§r: ", rx + 16, ry, 0xFFCCCCCC, false);
                    g.drawString(font, NpcEntityData.relationLabel(rel),
                            rx + 16 + font.width(other.name + ": "), ry, relCol, false);
                    ry += 14;
                    if (ry > oy + 320) break;
                }
            }
        }
    }

    private void renderFactionEditor(GuiGraphics g, int rx, int oy, int rw) {
        var font = Minecraft.getInstance().font;
        NpcInfoTab.sectionCard(g, rx, oy, rw, 20, "РЕДАКТОР ФРАКЦИИ");

        g.drawString(font, "§7Название:", rx + 4, oy + 26, 0xFF888877, false);
        g.drawString(font, "§7Описание:", rx + 4, oy + 62, 0xFF888877, false);
        g.drawString(font, "§7Цвет:", rx + 4, oy + 96, 0xFF888877, false);

        // Colour swatches
        int swX = rx + 4, swY = oy + 108;
        for (int i = 0; i < SWATCH_COUNT; i++) {
            int col = SWATCH_COLORS[i];
            g.fill(swX + i * 22, swY, swX + i * 22 + 18, swY + 18, col);
            boolean selected = draftFaction != null && draftFaction.color == col;
            NpcEditorUtils.brd(g, swX + i * 22 - 1, swY - 1, 20, 20,
                    selected ? 0xFFFFFFFF : 0xFF444444);
        }

        List<FactionData> others = ClientFactionState.getAll().stream()
                .filter(f -> draftFaction == null || !f.id.equals(draftFaction.id)).toList();

        if (!others.isEmpty()) {
            g.drawString(font, "§7Отношения с другими фракциями:", rx + 4, oy + 132, 0xFF888877, false);
            int ry = oy + 145;
            for (FactionData other : others) {
                g.fill(rx + 4, ry + 1, rx + 10, ry + 7, other.color | 0xFF000000);
                g.drawString(font, "§f" + other.name + ":", rx + 16, ry + 1, 0xFFCCCCCC, false);
                ry += 18;
            }
        }
    }

    @Override
    public boolean onMouseClicked(double mx, double my, int btn,
                                  NpcEditorState state, int rx, int oy, int rw) {
        if (!editingFaction || draftFaction == null) return false;
        int swX = rx + 4, swY = oy + 108;
        for (int i = 0; i < SWATCH_COUNT; i++) {
            int x0 = swX + i * 22, x1 = x0 + 18;
            if (mx >= x0 && mx < x1 && my >= swY && my < swY + 18) {
                draftFaction.color = SWATCH_COLORS[i];
                return true;
            }
        }
        return false;
    }

    @Override
    public void pullFields(NpcEditorState state) {
        pullFactionFields();
    }

    // ── Util ──────────────────────────────────────────────────────────────────

    private void pullFactionFields() {
        if (draftFaction == null) return;
        if (factionNameField != null) draftFaction.name = factionNameField.getValue();
        if (factionDescField != null) draftFaction.description = factionDescField.getValue();
    }

    private void closeFactionEditor() {
        editingFaction = false;
        draftFaction = null;
        factionNameField = null;
        factionDescField = null;
    }

    private static String relColorCode(String rel) {
        return switch (rel) {
            case "FRIENDLY" -> "§a";
            case "HOSTILE"  -> "§c";
            default         -> "§7";
        };
    }

    private static int factionIndex(List<FactionData> list, String id) {
        for (int i = 0; i < list.size(); i++) if (list.get(i).id.equals(id)) return i;
        return -1;
    }

    private static int indexOf(String[] arr, String val) {
        for (int i = 0; i < arr.length; i++) if (arr[i].equals(val)) return i;
        return 0;
    }

    private static List<FactionData> mergeFaction(List<FactionData> list, FactionData f) {
        List<FactionData> r = new ArrayList<>();
        boolean found = false;
        for (FactionData e : list) {
            if (e.id.equals(f.id)) { r.add(f); found = true; } else r.add(e);
        }
        if (!found) r.add(f);
        return r;
    }
}

package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.client.npceditor.NpcEditorState;
import com.frametrip.dragonlegacyquesttoast.profession.trader.BuyTradeOffer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TraderBuyOfferEditScreen extends Screen {

    private static final int W = 400;
    private static final int H = 380;

    private final NpcEditorState editorState;
    private final BuyTradeOffer  original;
    private final Screen         parent;

    private BuyTradeOffer draft;

    private EditBox nameField;
    private EditBox descField;
    private EditBox itemIdField;
    private EditBox rewardField;
    private EditBox requiredAmountField;
    private EditBox bonusPercentField;
    private EditBox demandField;
    private EditBox maxDemandField;
    private EditBox restockAmountField;
    private boolean autoImportName = true;

    public TraderBuyOfferEditScreen(NpcEditorState state, BuyTradeOffer offer, Screen parent) {
        super(Component.literal(offer == null ? "Добавить скупку" : "Редактировать скупку"));
        this.editorState = state;
        this.original    = offer;
        this.parent      = parent;
        this.draft       = offer != null ? offer.copy() : new BuyTradeOffer();
    }

    @Override
    protected void init() {
        super.init();
        int ox = ox(), oy = oy();
        int fw = W - 120;
        int itemFieldW = fw - 90;

        itemIdField         = addBox(ox + 110, oy + 36,  itemFieldW, "Предмет (ID)", draft.itemId, 100);
        addRenderableWidget(Button.builder(Component.literal("Выбрать…"), b -> openItemPicker())
                .bounds(ox + 110 + itemFieldW + 6, oy + 36, 84, 16).build());
        nameField           = addBox(ox + 110, oy + 60,  fw, "Название", draft.customName, 64);
        descField           = addBox(ox + 110, oy + 84,  fw, "Описание", draft.description, 128);
        requiredAmountField = addBox(ox + 110, oy + 108, 80, "Кол. предметов", String.valueOf(draft.requiredAmount), 10);
        rewardField         = addBox(ox + 200, oy + 108, 80, "Награда (монет)", String.valueOf(draft.reward), 10);
        bonusPercentField   = addBox(ox + 110, oy + 132, 60, "Бонус %", String.valueOf(draft.bonusPercent), 4);

        addRenderableWidget(Button.builder(
                Component.literal(draft.infiniteDemand ? "§a■ §fБескон. спрос" : "§7□ §fБескон. спрос"),
                b -> { draft.infiniteDemand = !draft.infiniteDemand; rebuildWidgets(); }
        ).bounds(ox + 110, oy + 156, 140, 18).build());

        if (!draft.infiniteDemand) {
            demandField    = addBox(ox + 110, oy + 180, 80, "Лимит", String.valueOf(draft.demandLeft), 10);
            maxDemandField = addBox(ox + 200, oy + 180, 80, "Макс.", String.valueOf(draft.maxDemand), 10);

            addRenderableWidget(Button.builder(
                    Component.literal(draft.restockEnabled ? "§a■ §fАвто-обновление" : "§7□ §fАвто-обновление"),
                    b -> { draft.restockEnabled = !draft.restockEnabled; rebuildWidgets(); }
            ).bounds(ox + 110, oy + 206, 150, 18).build());

            if (draft.restockEnabled) {
                restockAmountField = addBox(ox + 110, oy + 230, 80, "Восстановление", String.valueOf(draft.restockAmount), 10);
            }
        }

        addRenderableWidget(Button.builder(
                Component.literal(autoImportName ? "§a■ §fАвто-импорт названия" : "§7□ §fАвто-импорт названия"),
                b -> { autoImportName = !autoImportName; rebuildWidgets(); }
        ).bounds(ox + 110, oy + 254, 190, 18).build());

        int btnY = oy + H - 30;
        addRenderableWidget(Button.builder(Component.literal("Сохранить"), b -> save())
                .bounds(ox + 8, btnY, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Отмена"),
                b -> { if (minecraft != null) minecraft.setScreen(parent); })
                .bounds(ox + 114, btnY, 80, 20).build());
    }

    private EditBox addBox(int x, int y, int w, String hint, String value, int maxLen) {
        EditBox box = new EditBox(font, x, y, w, 16, Component.literal(hint));
        box.setMaxLength(maxLen);
        box.setValue(value);
        addRenderableWidget(box);
        return box;
    }

    private void save() {
        pullFields();
        var d = editorState.getDraft();
        if (d.professionData == null) return;
        d.professionData.ensureTraderData();
        var offers = d.professionData.traderData.buyOffers;
        if (original == null) {
            offers.add(draft);
        } else {
            int idx = indexOf(offers, original.id);
            if (idx >= 0) offers.set(idx, draft);
            else          offers.add(draft);
        }
        editorState.markDirty();
        if (minecraft != null) minecraft.setScreen(parent);
    }

     private void openItemPicker() {
        pullFields();
        if (minecraft == null) return;
        minecraft.setScreen(new TraderItemPickerScreen(this, draft.itemId, (itemId, displayName) -> {
            draft.itemId = itemId;
            itemIdField.setValue(itemId);

            boolean nameEmpty = val(nameField).isBlank();
            if (autoImportName || nameEmpty) {
                draft.customName = displayName;
                nameField.setValue(displayName);
            }
        }));
    }

    private void pullFields() {
        draft.itemId         = val(itemIdField);
        draft.customName     = val(nameField);
        draft.description    = val(descField);
        draft.requiredAmount = parseInt(val(requiredAmountField), draft.requiredAmount);
        draft.reward         = parseInt(val(rewardField),         draft.reward);
        draft.bonusPercent   = Math.max(0, Math.min(300, parseInt(val(bonusPercentField), draft.bonusPercent)));
        if (!draft.infiniteDemand) {
            draft.demandLeft = parseInt(val(demandField),    draft.demandLeft);
            draft.maxDemand  = parseInt(val(maxDemandField), draft.maxDemand);
            if (draft.restockEnabled)
                draft.restockAmount = parseInt(val(restockAmountField), draft.restockAmount);
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        int ox = ox(), oy = oy();
        g.fill(ox, oy, ox + W, oy + H, 0xEE0A0A14);
        NpcCreatorScreen.brd(g, ox, oy, W, H, 0xFF3A3A55);
        g.fill(ox, oy, ox + W, oy + 26, 0xBB12121E);
        g.drawString(font, "§f" + getTitle().getString(), ox + 8, oy + 8, 0xFFE6D7B5, false);

        label(g, "Предмет:",         ox + 8, oy + 39);
        label(g, "Название:",        ox + 8, oy + 63);
        label(g, "Описание:",        ox + 8, oy + 87);
        label(g, "Кол. / монет:",    ox + 8, oy + 111);
        label(g, "Бонус % (0-300):", ox + 8, oy + 135);
        super.render(g, mx, my, pt);
    }

    private void label(GuiGraphics g, String text, int x, int y) {
        g.drawString(font, "§7" + text, x, y, 0xFF888877, false);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private int ox() { return (width  - W) / 2; }
    private int oy() { return (height - H) / 2; }

    private static String val(EditBox box) { return box == null ? "" : box.getValue(); }
    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }
    private static int indexOf(java.util.List<BuyTradeOffer> list, String id) {
        for (int i = 0; i < list.size(); i++) if (list.get(i).id.equals(id)) return i;
        return -1;
    }
}

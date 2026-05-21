package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.currency.ClientCurrencyState;
import com.frametrip.dragonlegacyquesttoast.currency.CurrencyConfig;
import com.frametrip.dragonlegacyquesttoast.currency.NpcEconomyData;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SaveCurrencyConfigPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Consumer;

// [ECO-1..2]: Economy tab — global currency config + per-NPC wallet and rep pricing.
public class NpcEconomyTab implements NpcEditorTab {

    public static final int ACCENT = 0xFFFFCC00;

    // ECO-1 global currency boxes
    private EditBox currencyNameBox;
    private EditBox currencySymbolBox;
    private EditBox currencyItemBox;

    // ECO-2 NPC economy boxes
    private EditBox walletBox;
    private EditBox highRepBonusBox;
    private EditBox lowRepPenaltyBox;
    private EditBox minRepBox;

    @Override
    public void init(Consumer<AbstractWidget> add, Runnable rebuild,
                     NpcEditorState state, int rx, int oy, int rw) {
        var font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();
        NpcEconomyData eco = ensureEconomy(d);
        CurrencyConfig cfg = ClientCurrencyState.getCurrencyConfig();

        int y = oy + 22;

        // ── [ECO-1] Global currency section ──────────────────────────────────
        currencyNameBox = new EditBox(font, rx + 80, y, rw - 84, 16,
                Component.literal("Название"));
        currencyNameBox.setValue(cfg.name);
        currencyNameBox.setMaxLength(40);
        add.accept(currencyNameBox);
        y += 20;

        currencySymbolBox = new EditBox(font, rx + 80, y, 40, 16,
                Component.literal("Символ"));
        currencySymbolBox.setValue(cfg.symbol);
        currencySymbolBox.setMaxLength(4);
        add.accept(currencySymbolBox);
        y += 20;

        currencyItemBox = new EditBox(font, rx + 80, y, rw - 84, 16,
                Component.literal("Предмет"));
        currencyItemBox.setValue(cfg.itemId);
        currencyItemBox.setMaxLength(128);
        currencyItemBox.setHint(Component.literal("modid:item_name")
                .withStyle(s -> s.withColor(0xFF444455)));
        add.accept(currencyItemBox);
        y += 24;

        add.accept(Button.builder(
                Component.literal("💾 Сохранить глобально"),
                b -> {
                    CurrencyConfig newCfg = new CurrencyConfig();
                    newCfg.name   = currencyNameBox != null ? currencyNameBox.getValue().trim() : cfg.name;
                    newCfg.symbol = currencySymbolBox != null ? currencySymbolBox.getValue().trim() : cfg.symbol;
                    newCfg.itemId = currencyItemBox != null ? currencyItemBox.getValue().trim() : cfg.itemId;
                    ModNetwork.CHANNEL.send(PacketDistributor.SERVER.noArg(),
                            new SaveCurrencyConfigPacket(newCfg));
                }
        ).bounds(rx, y, 150, 18).build());
        y += 28;

        // ── [ECO-2] NPC wallet section ────────────────────────────────────────
        walletBox = new EditBox(font, rx + 90, y, 80, 16, Component.literal("Кошелёк"));
        walletBox.setValue(String.valueOf(eco.npcWallet));
        walletBox.setMaxLength(12);
        add.accept(walletBox);
        y += 24;

        // Rep-affects-price toggle
        add.accept(Button.builder(
                Component.literal(eco.repAffectsPrice
                        ? "§a☑§r Цена от репутации" : "§7☐ Цена от репутации"),
                b -> {
                    eco.repAffectsPrice = !eco.repAffectsPrice;
                    state.markDirty(); rebuild.run();
                }
        ).bounds(rx, y, rw, 18).build());
        y += 22;

        if (eco.repAffectsPrice) {
            highRepBonusBox = new EditBox(font, rx + 130, y, 50, 16,
                    Component.literal("Бонус %"));
            highRepBonusBox.setValue(String.valueOf(eco.highRepBonus));
            highRepBonusBox.setMaxLength(4);
            add.accept(highRepBonusBox);
            y += 20;

            lowRepPenaltyBox = new EditBox(font, rx + 130, y, 50, 16,
                    Component.literal("Штраф %"));
            lowRepPenaltyBox.setValue(String.valueOf(eco.lowRepPenalty));
            lowRepPenaltyBox.setMaxLength(4);
            add.accept(lowRepPenaltyBox);
            y += 20;

            minRepBox = new EditBox(font, rx + 130, y, 60, 16,
                    Component.literal("Мин. реп."));
            minRepBox.setValue(String.valueOf(eco.minRepToTrade));
            minRepBox.setMaxLength(6);
            add.accept(minRepBox);
        }
    }

    @Override
    public void render(GuiGraphics g, NpcEditorState state, int rx, int oy, int rw, int mx, int my) {
        var font = Minecraft.getInstance().font;
        NpcEntityData d = state.getDraft();
        NpcEconomyData eco = ensureEconomy(d);
        CurrencyConfig cfg = ClientCurrencyState.getCurrencyConfig();

        // ECO-1 card
        NpcEditorUtils.sectionCard(g, rx, oy, rw, 106, "ГЛОБАЛЬНАЯ ВАЛЮТА", ACCENT);
        g.drawString(font, "§8(требуется OP)", rx + 4, oy + 8, 0xFF666644, false);
        g.drawString(font, "§7Название:", rx + 4, oy + 26, 0xFF888877, false);
        g.drawString(font, "§7Символ:",   rx + 4, oy + 46, 0xFF888877, false);
        g.drawString(font, "§7ID предмета:", rx + 4, oy + 66, 0xFF888877, false);

        // ECO-2 card
        int eco2Y = oy + 112;
        int eco2H = 54 + (eco.repAffectsPrice ? 64 : 0);
        NpcEditorUtils.sectionCard(g, rx, eco2Y, rw, eco2H, "ЭКОНОМИКА NPC", ACCENT);
        g.drawString(font, "§7Текущая валюта: §e" + cfg.name + " §8(" + cfg.symbol + ")",
                rx + 4, eco2Y + 8, 0xFF888877, false);
        g.drawString(font, "§7Кошелёк NPC:", rx + 4, eco2Y + 26, 0xFF888877, false);

        if (eco.repAffectsPrice) {
            g.drawString(font, "§7Бонус (реп > 500):", rx + 4, eco2Y + 68, 0xFF888877, false);
            g.drawString(font, "§7Штраф (реп < -500):", rx + 4, eco2Y + 88, 0xFF888877, false);
            g.drawString(font, "§7Мин. реп. для торговли:", rx + 4, eco2Y + 108, 0xFF888877, false);
        }
    }

    @Override
    public void pullFields(NpcEditorState state) {
        NpcEntityData d = state.getDraft();
        NpcEconomyData eco = ensureEconomy(d);

        if (walletBox != null) {
            try { eco.npcWallet = Math.max(0, Long.parseLong(walletBox.getValue().trim())); }
            catch (NumberFormatException ignored) {}
        }
        if (highRepBonusBox != null) {
            try { eco.highRepBonus = Math.max(0, Math.min(100, Integer.parseInt(highRepBonusBox.getValue().trim()))); }
            catch (NumberFormatException ignored) {}
        }
        if (lowRepPenaltyBox != null) {
            try { eco.lowRepPenalty = Math.max(0, Math.min(100, Integer.parseInt(lowRepPenaltyBox.getValue().trim()))); }
            catch (NumberFormatException ignored) {}
        }
        if (minRepBox != null) {
            try { eco.minRepToTrade = Math.max(-1000, Math.min(1000, Integer.parseInt(minRepBox.getValue().trim()))); }
            catch (NumberFormatException ignored) {}
        }
    }

    private static NpcEconomyData ensureEconomy(NpcEntityData d) {
        if (d.economyData == null) d.economyData = new NpcEconomyData();
        return d.economyData;
    }
}

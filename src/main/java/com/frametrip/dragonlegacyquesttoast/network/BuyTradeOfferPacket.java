package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.currency.CurrencyManager;
import com.frametrip.dragonlegacyquesttoast.currency.NpcEconomyData;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.profession.NpcProfessionType;
import com.frametrip.dragonlegacyquesttoast.server.PlayerFactionReputationManager;
import com.frametrip.dragonlegacyquesttoast.profession.trader.SellTradeOffer;
import com.frametrip.dragonlegacyquesttoast.profession.trader.TradePriceResult;
import com.frametrip.dragonlegacyquesttoast.server.compat.VaultHook;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;
import java.util.function.Supplier;

/** Client → Server: player wants to buy a sell-offer from a trader NPC. */
public class BuyTradeOfferPacket {

    private final UUID   npcUuid;
    private final String offerId;

    public BuyTradeOfferPacket(UUID npcUuid, String offerId) {
        this.npcUuid = npcUuid;
        this.offerId = offerId;
    }

    public static void encode(BuyTradeOfferPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.npcUuid);
        buf.writeUtf(msg.offerId, 64);
    }

    public static BuyTradeOfferPacket decode(FriendlyByteBuf buf) {
        return new BuyTradeOfferPacket(buf.readUUID(), buf.readUtf(64));
    }

    public static void handle(BuyTradeOfferPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            NpcEntity npc = findNpc(player, msg.npcUuid);
            if (npc == null) {
                player.sendSystemMessage(Component.literal("§cNPC не найден."));
                return;
            }
            if (player.distanceTo(npc) > 8.0) {
                player.sendSystemMessage(Component.literal("§cВы слишком далеко от NPC."));
                return;
            }

            var pd = npc.getNpcData().professionData;
            if (pd == null || pd.type != NpcProfessionType.TRADER || pd.traderData == null) return;

            SellTradeOffer offer = pd.traderData.sellOffers.stream()
                    .filter(o -> o.id.equals(msg.offerId)).findFirst().orElse(null);
            if (offer == null) {
                player.sendSystemMessage(Component.literal("§cТовар не найден."));
                return;
            }

            if (!offer.infiniteStock && offer.stock <= 0) {
                player.sendSystemMessage(Component.literal("§cТовар закончился."));
                return;
            }

            // [ECO-2]: Check minimum reputation to trade
            NpcEntityData npcData = npc.getNpcData();
            NpcEconomyData eco = npcData.economyData;
            if (eco == null) eco = new NpcEconomyData();
            String factionId = npcData.factionId;
            int playerRep = (factionId != null && !factionId.isEmpty())
                    ? PlayerFactionReputationManager.get(player.getUUID(), factionId) : 0;
            if (!eco.canTrade(playerRep)) {
                player.sendSystemMessage(Component.literal("§cВаша репутация слишком низкая для торговли."));
                return;
            }

            // Apply discount + [ECO-2] reputation price multiplier (server-side authoritative)
            int discPct = pd.traderData.getOrCreateDiscounts().effectiveBuyDiscount(offer.discountPercent);
            TradePriceResult price = TradePriceResult.calculate(offer.price, offer.amount, discPct);
            float repMultiplier = eco.getPriceMultiplier(playerRep);
            if (repMultiplier != 1.0f)
                price.finalPrice = Math.max(1, Math.round(price.finalPrice * repMultiplier));

            // [IMM-2]: Mood affects trade price
            if (npcData.immersionData != null && npcData.immersionData.moodEnabled) {
                float moodMult = npcData.immersionData.moodPriceMultiplier();
                if (moodMult != 1.0f)
                    price.finalPrice = Math.max(1, Math.round(price.finalPrice * moodMult));
            }

            // [INT-API-3]: Try Vault economy first, fall back to internal CurrencyManager
            boolean usedVault = false;
            if (VaultHook.isAvailable()) {
                if (!VaultHook.withdraw(player, price.finalPrice)) {
                    player.sendSystemMessage(Component.literal("§cНедостаточно средств (Vault)."));
                    return;
                }
                usedVault = true;
            } else if (!CurrencyManager.hasBalance(player.getUUID(), price.finalPrice)) {
                player.sendSystemMessage(Component.literal("§cНедостаточно монет."));
                return;
            }

            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(offer.itemId));
            if (item == null) {
                // Refund Vault if item lookup failed
                player.sendSystemMessage(Component.literal("§cПредмет не существует."));
                return;
            }
            ItemStack result = new ItemStack(item, offer.amount);
            if (!player.getInventory().add(result)) {
                player.sendSystemMessage(Component.literal("§cНет места в инвентаре."));
                return;
            }

            if (!usedVault) CurrencyManager.removeBalance(player, price.finalPrice);

            // [STA-1]: Increment items-sold counter
            NpcEntityData statData = npc.getNpcData();
            if (statData.stats != null) { statData.stats.itemsSold++; npc.setNpcData(statData); }

            if (!offer.infiniteStock) {
                offer.stock = Math.max(0, offer.stock - 1);
                npc.setNpcData(npc.getNpcData());
            }

            String itemName = offer.customName.isBlank() ? offer.itemId : offer.customName;
            if (discPct > 0) {
                player.sendSystemMessage(Component.literal(
                        "§aКуплено: §f" + itemName + " §8(скидка §a-" + discPct + "%§8, цена §e" + price.finalPrice + "§8)"));
            } else {
                player.sendSystemMessage(Component.literal(
                        "§aКуплено: §f" + itemName + " §8(§e" + price.finalPrice + " §8монет)"));
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static NpcEntity findNpc(ServerPlayer player, UUID uuid) {
        var server = player.getServer();
        if (server == null) return null;
        for (ServerLevel level : server.getAllLevels()) {
            Entity e = level.getEntity(uuid);
            if (e instanceof NpcEntity npc) return npc;
        }
        return null;
    }
}


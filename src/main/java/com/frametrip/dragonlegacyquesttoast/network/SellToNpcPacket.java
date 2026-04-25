package com.frametrip.dragonlegacyquesttoast.network;

import com.frametrip.dragonlegacyquesttoast.currency.CurrencyManager;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.profession.NpcProfessionType;
import com.frametrip.dragonlegacyquesttoast.profession.trader.BuyTradeOffer;
import com.frametrip.dragonlegacyquesttoast.profession.trader.TradeRewardResult;
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

/**
 * Client → Server: player sells items to a trader NPC.
 * sellAll=true repeats until items or limit run out.
 */
public class SellToNpcPacket {

    private final UUID    npcUuid;
    private final String  offerId;
    private final boolean sellAll;

    public SellToNpcPacket(UUID npcUuid, String offerId, boolean sellAll) {
        this.npcUuid  = npcUuid;
        this.offerId  = offerId;
        this.sellAll  = sellAll;
    }

    public static void encode(SellToNpcPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.npcUuid);
        buf.writeUtf(msg.offerId, 64);
        buf.writeBoolean(msg.sellAll);
    }

    public static SellToNpcPacket decode(FriendlyByteBuf buf) {
        return new SellToNpcPacket(buf.readUUID(), buf.readUtf(64), buf.readBoolean());
    }

    public static void handle(SellToNpcPacket msg, Supplier<NetworkEvent.Context> ctx) {
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

            BuyTradeOffer offer = pd.traderData.buyOffers.stream()
                    .filter(o -> o.id.equals(msg.offerId)).findFirst().orElse(null);
            if (offer == null) {
                player.sendSystemMessage(Component.literal("§cПредложение скупки не найдено."));
                return;
            }

            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(offer.itemId));
            if (item == null) {
                player.sendSystemMessage(Component.literal("§cПредмет не существует: " + offer.itemId));
                return;
            }

            int times = msg.sellAll ? maxTimes(player, item, offer) : 1;
            if (times <= 0) {
                // provide specific feedback
                if (countItems(player, item) < offer.requiredAmount) {
                    player.sendSystemMessage(Component.literal("§cНедостаточно предметов."));
                } else {
                    player.sendSystemMessage(Component.literal("§cЛимит скупки исчерпан."));
                }
                return;
            }

            int bonusPct = pd.traderData.getOrCreateDiscounts().effectiveSellBonus(offer.bonusPercent);

            long totalReward = 0;
            int  actualTimes = 0;
            for (int t = 0; t < times; t++) {
                if (countItems(player, item) < offer.requiredAmount) break;
                if (!offer.infiniteDemand && offer.demandLeft <= 0) break;

                removeItems(player, item, offer.requiredAmount);
                TradeRewardResult rew = TradeRewardResult.calculate(offer.reward, 1, bonusPct);
                totalReward += rew.finalReward;
                actualTimes++;
                if (!offer.infiniteDemand) offer.demandLeft = Math.max(0, offer.demandLeft - 1);
            }

            if (totalReward > 0) {
                CurrencyManager.addBalance(player, totalReward);
                npc.setNpcData(npc.getNpcData());
                if (bonusPct > 0) {
                    player.sendSystemMessage(Component.literal(
                            "§aПродано ×" + actualTimes + " §8(бонус §a+" + bonusPct + "%§8). Получено: §e" + totalReward + " §aмонет."));
                } else {
                    player.sendSystemMessage(Component.literal(
                            "§aПродано ×" + actualTimes + ". Получено: §e" + totalReward + " §aмонет."));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static int maxTimes(ServerPlayer player, Item item, BuyTradeOffer offer) {
        int byItems = countItems(player, item) / Math.max(1, offer.requiredAmount);
        if (offer.infiniteDemand) return byItems;
        return Math.min(byItems, offer.demandLeft);
    }

    private static int countItems(ServerPlayer player, Item item) {
        int count = 0;
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && s.is(item)) count += s.getCount();
        }
        return count;
    }

    private static void removeItems(ServerPlayer player, Item item, int amount) {
        var inv = player.getInventory();
        int remaining = amount;
        for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && s.is(item)) {
                int take = Math.min(s.getCount(), remaining);
                s.shrink(take);
                remaining -= take;
            }
        }
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


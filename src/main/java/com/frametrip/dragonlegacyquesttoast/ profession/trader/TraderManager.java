package com.frametrip.dragonlegacyquesttoast.profession.trader;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.profession.NpcProfessionType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TraderManager {

    private static int tickCounter = 0;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        tickCounter++;
        if (tickCounter % 100 != 0) return;  // check every 5 seconds

        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        long worldDay = server.overworld().getDayTime() / 24000L;
        long timeOfDay = server.overworld().getDayTime() % 24000L;

        for (var level : server.getAllLevels()) {
            for (var entity : level.getAllEntities()) {
                if (!(entity instanceof NpcEntity npc)) continue;
                NpcEntityData data = npc.getNpcData();
                if (data.professionData == null
                        || data.professionData.type != NpcProfessionType.TRADER
                        || data.professionData.traderData == null) continue;

                TraderProfessionData td = data.professionData.traderData;
                TradeRestockSettings rs = td.restockSettings;

                if (rs.mode == RestockMode.DISABLED) continue;
                if (rs.lastRestockDay == worldDay) continue;

                boolean shouldRestock = false;
                if (rs.mode == RestockMode.EVERY_DAY_AT_TIME) {
                    shouldRestock = timeOfDay >= rs.timeOfDay;
                } else if (rs.mode == RestockMode.EVERY_N_DAYS_AT_TIME) {
                    shouldRestock = timeOfDay >= rs.timeOfDay
                            && (worldDay - rs.lastRestockDay) >= rs.everyNDays;
                }

                if (!shouldRestock) continue;

                rs.lastRestockDay = worldDay;
                for (SellTradeOffer o : td.sellOffers) {
                    if (o.restockEnabled && !o.infiniteStock) {
                        o.stock = Math.min(o.stock + o.restockAmount, o.maxStock);
                    }
                }
                for (BuyTradeOffer o : td.buyOffers) {
                    if (o.restockEnabled && !o.infiniteDemand) {
                        o.demandLeft = Math.min(o.demandLeft + o.restockAmount, o.maxDemand);
                    }
                }
                npc.setNpcData(data);
            }
        }
    }
}

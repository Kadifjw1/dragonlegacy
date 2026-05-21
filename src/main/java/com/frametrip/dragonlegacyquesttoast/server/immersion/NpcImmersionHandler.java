package com.frametrip.dragonlegacyquesttoast.server.immersion;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.NpcDialoguePacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@Mod.EventBusSubscriber
public class NpcImmersionHandler {

    // [IMM-2]: Mood falls by 1 hit when NPC is attacked
    @SubscribeEvent
    public static void onNpcHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof NpcEntity npc)) return;
        NpcEntityData data = npc.getNpcData();
        NpcImmersionData imm = data.immersionData;
        if (imm == null || !imm.moodEnabled) return;
        imm.mood = Math.max(-100, imm.mood - 20);
        npc.setNpcData(data);
    }

    // [IMM-6]: Record killer player UUID on NPC death
    @SubscribeEvent
    public static void onNpcDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof NpcEntity npc)) return;
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        NpcEntityData data = npc.getNpcData();
        NpcImmersionData imm = data.immersionData;
        if (imm == null || !imm.rememberDeath) return;
        imm.killerPlayerUuid = player.getUUID().toString();
        npc.setNpcData(data);
    }

    // [IMM-4] + [IMM-5]: Server level tick — schedule and item reactions
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level)) return;

        long gameTime = level.getGameTime();
        long dayTime  = level.getDayTime() % 24000;
        int  hour     = (int)(dayTime / 1000);

        level.getAllEntities().forEach(entity -> {
            if (!(entity instanceof NpcEntity npc)) return;
            NpcEntityData data = npc.getNpcData();
            NpcImmersionData imm = data.immersionData;
            if (imm == null) return;

            // [IMM-2]: Mood decays by 1 every 5 minutes (6000 ticks)
            if (imm.moodEnabled && imm.mood != 0 && gameTime % 6000 == 0) {
                imm.mood += imm.mood > 0 ? -1 : 1;
                npc.setNpcData(data);
            }

            // [IMM-4]: Daily schedule — fire on hour change
            if (gameTime % 1000 == 0 && !imm.dailySchedule.isEmpty()) {
                for (NpcScheduleEvent ev : imm.dailySchedule) {
                    if (ev.hour == hour) {
                        executeScheduleEvent(npc, data, ev, level);
                        break;
                    }
                }
            }

            // [IMM-5]: Item reactions — check every second (20 ticks)
            if (gameTime % 20 != 0 || imm.itemReactions.isEmpty()) return;
            for (ServerPlayer sp : level.players()) {
                if (npc.distanceTo(sp) > 8.0) continue;
                ItemStack held = sp.getMainHandItem();
                if (held.isEmpty()) continue;
                String heldId = ForgeRegistries.ITEMS.getKey(held.getItem()).toString();
                for (NpcItemReaction reaction : imm.itemReactions) {
                    if (heldId.equals(reaction.itemId)) {
                        executeItemReaction(npc, data, reaction, sp);
                        break;
                    }
                }
            }
        });
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private static void executeScheduleEvent(NpcEntity npc, NpcEntityData data,
                                             NpcScheduleEvent ev, ServerLevel level) {
        switch (ev.action) {
            case "GOTO" -> {
                try {
                    String[] parts = ev.target.split(",");
                    if (parts.length >= 3) {
                        double x = Double.parseDouble(parts[0].trim());
                        double y = Double.parseDouble(parts[1].trim());
                        double z = Double.parseDouble(parts[2].trim());
                        npc.getNavigation().moveTo(x, y, z, 1.0);
                    }
                } catch (NumberFormatException ignored) {}
            }
            case "SAY" -> {
                if (!ev.dialog.isEmpty()) {
                    level.players().forEach(p -> {
                        if (npc.distanceTo(p) <= 16) {
                            p.sendSystemMessage(Component.literal(
                                    "§e" + data.displayName + "§7: §f" + ev.dialog));
                        }
                    });
                }
            }
            case "SLEEP", "WORK", "IDLE", "EMOTE" -> {
                // Reserved — future animation state hooks
            }
        }
    }

    private static void executeItemReaction(NpcEntity npc, NpcEntityData data,
                                            NpcItemReaction reaction, ServerPlayer player) {
        // [IMM-2]: Apply mood change from item reaction
        NpcImmersionData imm = data.immersionData;
        if (imm != null && imm.moodEnabled && reaction.moodChange != 0) {
            imm.mood = Math.min(100, Math.max(-100, imm.mood + reaction.moodChange));
            npc.setNpcData(data);
        }

        switch (reaction.reactionType) {
            case "FEAR" -> npc.getNavigation().moveTo(
                    npc.getX() - (player.getX() - npc.getX()),
                    npc.getY(),
                    npc.getZ() - (player.getZ() - npc.getZ()), 1.2);
            case "INTEREST" -> npc.getNavigation().moveTo(player, 1.0);
            case "AGGRO"    -> npc.setTarget(player);
            case "DIALOG"   -> {
                if (!reaction.dialog.isEmpty()) {
                    ModNetwork.CHANNEL.send(
                            PacketDistributor.PLAYER.with(() -> player),
                            new NpcDialoguePacket(data.displayName, reaction.dialog));
                }
            }
        }
    }

    /** [IMM-1]: Call from interact handler to record player visit and select dialog. */
    public static String resolveMemoryDialog(NpcEntity npc, NpcEntityData data, ServerPlayer player) {
        NpcImmersionData imm = data.immersionData;
        if (imm == null || !imm.rememberPlayers) return null;

        String uid = player.getUUID().toString();
        int visits = imm.playerVisits.getOrDefault(uid, 0);
        imm.playerVisits.put(uid, visits + 1);
        npc.setNpcData(data);

        if (visits == 0 && !imm.firstVisitDialog.isEmpty())   return imm.firstVisitDialog;
        if (visits < 5  && !imm.returningDialog.isEmpty())    return imm.returningDialog;
        if (!imm.regularDialog.isEmpty())                      return imm.regularDialog;
        return null;
    }

    /** [IMM-2]: Call on gift interaction (player shift+right-clicks with gift item). */
    public static void applyGiftMoodBonus(NpcEntity npc, NpcEntityData data, Item gift) {
        NpcImmersionData imm = data.immersionData;
        if (imm == null || !imm.moodEnabled) return;
        String giftId = ForgeRegistries.ITEMS.getKey(gift).toString();
        if (imm.moodGiftItems.contains(giftId)) {
            imm.mood = Math.min(100, imm.mood + 10);
            npc.setNpcData(data);
        }
    }

    /** [IMM-6]: Check if the interacting player killed this NPC; return vengeance dialog. */
    public static String resolveDeathDialog(NpcEntityData data, ServerPlayer player) {
        NpcImmersionData imm = data.immersionData;
        if (imm == null || !imm.rememberDeath || imm.killerPlayerUuid.isEmpty()) return null;
        if (!imm.killerPlayerUuid.equals(player.getUUID().toString())) return null;
        return imm.deathReactionDialog.isEmpty() ? null : imm.deathReactionDialog;
    }
}

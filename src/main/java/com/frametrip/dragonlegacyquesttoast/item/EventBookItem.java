package com.frametrip.dragonlegacyquesttoast.item;

import com.frametrip.dragonlegacyquesttoast.client.EventChainScreen;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class EventBookItem extends Item {

    public EventBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            HitResult hit = Minecraft.getInstance().hitResult;
            if (hit instanceof EntityHitResult ehr && ehr.getEntity() instanceof NpcEntity npc) {
                Minecraft.getInstance().setScreen(new EventChainScreen(npc));
            } else {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(
                                "item.dragonlegacyquesttoast.event_book.hint"),
                        true);
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}

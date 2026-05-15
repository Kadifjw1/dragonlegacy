package com.frametrip.dragonlegacyquesttoast.item;

import com.frametrip.dragonlegacyquesttoast.client.ClientUtil;
import com.frametrip.dragonlegacyquesttoast.client.EventChainScreen;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EventBookItem extends Item {

    public EventBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            NpcEntity npc = ClientUtil.getLookedAtNpc();
            if (npc != null) {
                Minecraft.getInstance().setScreen(new EventChainScreen(npc));
            } else {
                player.displayClientMessage(
                        Component.translatable("item.dragonlegacyquesttoast.event_book.hint"), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}

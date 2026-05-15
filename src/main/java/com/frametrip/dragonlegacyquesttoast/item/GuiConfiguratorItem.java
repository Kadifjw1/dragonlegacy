package com.frametrip.dragonlegacyquesttoast.item;

import com.frametrip.dragonlegacyquesttoast.client.ClientUtil;
import com.frametrip.dragonlegacyquesttoast.client.GuiEditorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GuiConfiguratorItem extends Item {

    public GuiConfiguratorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            Minecraft.getInstance().setScreen(new GuiEditorScreen(ClientUtil.getLookedAtNpc()));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}

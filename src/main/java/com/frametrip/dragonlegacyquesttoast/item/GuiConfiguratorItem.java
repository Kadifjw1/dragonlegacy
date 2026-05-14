package com.frametrip.dragonlegacyquesttoast.item;

import com.frametrip.dragonlegacyquesttoast.client.GuiEditorScreen;
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

public class GuiConfiguratorItem extends Item {

    public GuiConfiguratorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            HitResult hit = Minecraft.getInstance().hitResult;
            NpcEntity npc = (hit instanceof EntityHitResult ehr && ehr.getEntity() instanceof NpcEntity n)
                    ? n : null;
            Minecraft.getInstance().setScreen(new GuiEditorScreen(npc));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}

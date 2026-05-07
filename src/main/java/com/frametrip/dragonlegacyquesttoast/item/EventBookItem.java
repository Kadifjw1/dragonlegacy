package com.frametrip.dragonlegacyquesttoast.item;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

/** Shift + right-click on an NPC → opens the visual event-chain editor. */
public class EventBookItem extends Item {

    public EventBookItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player,
                                                   LivingEntity target, InteractionHand hand) {
        if (player.isShiftKeyDown() && target instanceof NpcEntity npc) {
            if (player.level().isClientSide) {
                openScreen(npc);
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        return InteractionResult.PASS;
    }

    @OnlyIn(Dist.CLIENT)
    private static void openScreen(NpcEntity npc) {
        net.minecraft.client.Minecraft.getInstance().setScreen(
                new com.frametrip.dragonlegacyquesttoast.client.EventChainScreen(npc)
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Shift + ПКМ§r на NPC — редактор событий"));
    }
}


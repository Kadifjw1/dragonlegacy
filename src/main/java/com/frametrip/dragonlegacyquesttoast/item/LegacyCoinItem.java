package com.frametrip.dragonlegacyquesttoast.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LegacyCoinItem extends Item {

    public LegacyCoinItem(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Основная валюта торговцев."));
        tooltip.add(Component.literal("§8При попадании в инвентарь"));
        tooltip.add(Component.literal("§8автоматически зачисляется в банк."));
    }
}

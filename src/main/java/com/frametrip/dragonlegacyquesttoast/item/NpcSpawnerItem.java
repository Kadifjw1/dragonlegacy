package com.frametrip.dragonlegacyquesttoast.item;
 
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
 
import javax.annotation.Nullable;
import java.util.List;
 
public class NpcSpawnerItem extends Item {
 
    public NpcSpawnerItem(Properties props) {
        super(props);
    }
 
    // Right-click on ground → spawn NPC (only if NOT sneaking)
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
 
        if (player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }
 
        if (!level.isClientSide) {
            HitResult hit = player.pick(6.0, 0.0f, false);
            BlockPos spawnBlock;
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) hit;
                spawnBlock = blockHit.getBlockPos().relative(blockHit.getDirection());
            } else {
                spawnBlock = BlockPos.containing(
                    player.getEyePosition().add(player.getLookAngle().scale(3.5))
                );
            }
 
            NpcEntity npc = ModEntities.NPC.get().create(level);
            if (npc != null) {
                npc.moveTo(spawnBlock.getX() + 0.5, spawnBlock.getY(), spawnBlock.getZ() + 0.5,
                           player.getYRot(), 0);
                level.addFreshEntity(npc);
            }
        }
 
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
 
    // Shift + right-click on NPC → open creator screen (client-side)
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player,
                                                   LivingEntity target, InteractionHand hand) {
        if (player.isShiftKeyDown() && target instanceof NpcEntity npc) {
            if (player.level().isClientSide) {
                openCreator(npc);
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        return InteractionResult.PASS;
    }
 
    @OnlyIn(Dist.CLIENT)
    private static void openCreator(NpcEntity npc) {
        net.minecraft.client.Minecraft.getInstance().setScreen(
            new com.frametrip.dragonlegacyquesttoast.client.NpcCreatorScreen(npc)
        );
    }
 
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7ПКМ§r — спавн NPC"));
        tooltip.add(Component.literal("§7Shift + ПКМ§r на NPC — настройка"));
    }
}

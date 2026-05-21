package com.frametrip.dragonlegacyquesttoast.entity.goal;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.server.world.FarmerData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

// [WLD-2]: Full farming FSM — IDLE → HARVEST → PLANT → STORE → IDLE.
public class NpcFarmerGoal extends Goal {

    private enum FarmerState { IDLE, HARVEST, PLANT, STORE }

    private final NpcEntity npc;
    private FarmerState state = FarmerState.IDLE;

    // Ticks between idle checks.
    private static final int IDLE_INTERVAL  = 100; // ~5 s
    // Ticks to wait in STORE state before returning to IDLE.
    private static final int STORE_DURATION = 60;

    private int idleTick   = 0;
    private int storeTick  = 0;

    // Pending positions to act on.
    private final List<BlockPos> targets = new ArrayList<>();
    private int targetIdx = 0;

    // Items pending store (simple counter per crop).
    private int harvestCount = 0;

    public NpcFarmerGoal(NpcEntity npc) {
        this.npc = npc;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (npc.level().isClientSide) return false;
        NpcEntityData data = npc.getNpcData();
        FarmerData fd = data.farmerData;
        return fd != null && fd.farmerEnabled;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        Level level = npc.level();
        if (level.isClientSide) return;

        NpcEntityData data = npc.getNpcData();
        FarmerData fd = data.farmerData;
        if (fd == null || !fd.farmerEnabled) { state = FarmerState.IDLE; return; }

        switch (state) {
            case IDLE -> tickIdle(fd);
            case HARVEST -> tickHarvest(level, fd);
            case PLANT   -> tickPlant(level, fd);
            case STORE   -> tickStore(level, fd);
        }
    }

    // ── Idle ─────────────────────────────────────────────────────────────────

    private void tickIdle(FarmerData fd) {
        if (++idleTick < IDLE_INTERVAL) return;
        idleTick = 0;

        // Build harvest target list.
        targets.clear();
        targetIdx = 0;
        harvestCount = 0;
        BlockPos origin = npc.blockPosition();
        int r = fd.plotRadius;
        Block cropBlock = resolveCropBlock(fd.cropType);

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                BlockPos p = origin.offset(dx, 0, dz);
                BlockState bs = npc.level().getBlockState(p);
                if (bs.getBlock() == cropBlock && bs.getBlock() instanceof CropBlock cb) {
                    if (cb.isMaxAge(bs)) targets.add(p);
                }
            }
        }

        if (!targets.isEmpty()) {
            state = FarmerState.HARVEST;
        } else {
            // Check if there's bare farmland to plant.
            scanPlantTargets(fd);
            if (!targets.isEmpty()) state = FarmerState.PLANT;
        }
    }

    // ── Harvest ───────────────────────────────────────────────────────────────

    private void tickHarvest(Level level, FarmerData fd) {
        if (targetIdx >= targets.size()) {
            // Done harvesting — move to plant phase.
            scanPlantTargets(fd);
            state = targets.isEmpty() ? FarmerState.STORE : FarmerState.PLANT;
            targetIdx = 0;
            return;
        }

        BlockPos target = targets.get(targetIdx);
        // Walk toward target.
        npc.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 0.8);
        npc.getLookControl().setLookAt(Vec3.atCenterOf(target));

        double dist = npc.position().distanceTo(Vec3.atCenterOf(target));
        if (dist < 2.5) {
            BlockState bs = level.getBlockState(target);
            if (bs.getBlock() instanceof CropBlock cb && cb.isMaxAge(bs)) {
                level.destroyBlock(target, true); // drops naturally
                harvestCount++;
                npc.swing(InteractionHand.MAIN_HAND);
            }
            targetIdx++;
        }
    }

    // ── Plant ─────────────────────────────────────────────────────────────────

    private void tickPlant(Level level, FarmerData fd) {
        if (targetIdx >= targets.size()) {
            state = FarmerState.STORE;
            storeTick = 0;
            return;
        }

        BlockPos target = targets.get(targetIdx);
        npc.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 0.8);
        npc.getLookControl().setLookAt(Vec3.atCenterOf(target));

        double dist = npc.position().distanceTo(Vec3.atCenterOf(target));
        if (dist < 2.5) {
            BlockState bs = level.getBlockState(target);
            if (bs.is(Blocks.FARMLAND)) {
                // Plant appropriate crop seed above farmland.
                BlockPos cropPos = target.above();
                if (level.getBlockState(cropPos).isAir()) {
                    Block cropBlock = resolveCropBlock(fd.cropType);
                    if (cropBlock != Blocks.AIR) {
                        level.setBlock(cropPos, cropBlock.defaultBlockState(), 3);
                        npc.swing(InteractionHand.MAIN_HAND);
                    }
                }
            }
            targetIdx++;
        }
    }

    // ── Store ─────────────────────────────────────────────────────────────────

    private void tickStore(Level level, FarmerData fd) {
        if (!fd.storagePos.isEmpty()) {
            try {
                String[] parts = fd.storagePos.split(",");
                double cx = Double.parseDouble(parts[0].trim());
                double cy = Double.parseDouble(parts[1].trim());
                double cz = Double.parseDouble(parts[2].trim());
                npc.getNavigation().moveTo(cx, cy, cz, 0.8);

                if (npc.position().distanceTo(new Vec3(cx, cy, cz)) < 2.5 || ++storeTick >= STORE_DURATION) {
                    tryStoreInChest(level, new BlockPos((int)cx, (int)cy, (int)cz), fd);
                    state = FarmerState.IDLE;
                }
                return;
            } catch (Exception ignored) {}
        }
        // No storage configured — just drop items in place and go idle.
        if (++storeTick >= STORE_DURATION / 2) {
            state = FarmerState.IDLE;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void scanPlantTargets(FarmerData fd) {
        targets.clear();
        targetIdx = 0;
        BlockPos origin = npc.blockPosition();
        int r = fd.plotRadius;
        Block cropBlock = resolveCropBlock(fd.cropType);

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                BlockPos p = origin.offset(dx, 0, dz);
                BlockState bs = npc.level().getBlockState(p);
                if (bs.is(Blocks.FARMLAND)) {
                    BlockPos above = p.above();
                    if (npc.level().getBlockState(above).isAir()) {
                        targets.add(p);
                    }
                }
            }
        }
    }

    private static void tryStoreInChest(Level level, BlockPos pos, FarmerData fd) {
        BlockState bs = level.getBlockState(pos);
        if (level instanceof ServerLevel sl) {
            var be = sl.getBlockEntity(pos);
            if (be instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest) {
                Item seedItem = resolveSeedItem(fd.cropType);
                if (seedItem != null) {
                    ItemStack seeds = new ItemStack(seedItem, 1);
                    for (int slot = 0; slot < chest.getContainerSize(); slot++) {
                        if (chest.getItem(slot).isEmpty()) {
                            chest.setItem(slot, seeds);
                            break;
                        }
                    }
                }
            }
        }
    }

    private static Block resolveCropBlock(String cropType) {
        return switch (cropType.toLowerCase()) {
            case "carrot"   -> Blocks.CARROTS;
            case "potato"   -> Blocks.POTATOES;
            case "beetroot" -> Blocks.BEETROOTS;
            default         -> Blocks.WHEAT;
        };
    }

    private static Item resolveSeedItem(String cropType) {
        return switch (cropType.toLowerCase()) {
            case "carrot"   -> net.minecraft.world.item.Items.CARROT;
            case "potato"   -> net.minecraft.world.item.Items.POTATO;
            case "beetroot" -> net.minecraft.world.item.Items.BEETROOT_SEEDS;
            default         -> net.minecraft.world.item.Items.WHEAT_SEEDS;
        };
    }
}

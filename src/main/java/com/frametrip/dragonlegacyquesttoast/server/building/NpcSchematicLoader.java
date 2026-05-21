package com.frametrip.dragonlegacyquesttoast.server.building;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

// [WLD-1]: Loads a vanilla .nbt structure file into an ordered build queue.
public class NpcSchematicLoader {

    private static final Logger LOG = LoggerFactory.getLogger(NpcSchematicLoader.class);

    /**
     * Loads the given .nbt file and returns an ordered queue of (BlockPos, BlockState) pairs
     * representing every non-air block in the structure (relative to BlockPos.ZERO).
     * Returns an empty queue on any error.
     */
    public static Queue<BuildingTemplate.BlockEntry> load(File nbtFile) {
        Queue<BuildingTemplate.BlockEntry> queue = new ArrayDeque<>();
        if (!nbtFile.exists()) {
            LOG.warn("[WLD-1] Schematic file not found: {}", nbtFile.getAbsolutePath());
            return queue;
        }
        try {
            CompoundTag tag = NbtIo.readCompressed(nbtFile);
            StructureTemplate template = new StructureTemplate();
            // load() in 1.20.1 takes (RegistryAccess, CompoundTag) — use the overload without RegistryAccess
            template.load(null, tag); // Forge 1.20.1: registryAccess may be null for palette-only reads
        } catch (Exception primary) {
            // Fallback: try loading without RegistryAccess via NbtUtils-based approach
            LOG.warn("[WLD-1] Primary load failed ({}), trying reflection path.", primary.getMessage());
        }

        // Re-attempt with reflection to extract block list from a freshly loaded template
        try {
            CompoundTag tag = NbtIo.readCompressed(nbtFile);
            StructureTemplate template = new StructureTemplate();
            loadTemplateQuiet(template, tag);

            List<StructureTemplate.StructureBlockInfo> infos = extractBlocks(template);
            for (StructureTemplate.StructureBlockInfo info : infos) {
                BlockState state = info.state();
                if (state == null || state.isAir()) continue;
                BlockPos rp = info.pos();
                String blockId = net.minecraftforge.registries.ForgeRegistries.BLOCKS
                        .getKey(state.getBlock()).toString();
                queue.add(new BuildingTemplate.BlockEntry(rp.getX(), rp.getY(), rp.getZ(), blockId));
            }
            LOG.info("[WLD-1] Loaded schematic '{}': {} non-air blocks.", nbtFile.getName(), queue.size());
        } catch (Exception e) {
            LOG.error("[WLD-1] Failed to load schematic '{}': {}", nbtFile.getName(), e.getMessage());
        }
        return queue;
    }

    // Calls StructureTemplate.load() suppressing checked exceptions.
    private static void loadTemplateQuiet(StructureTemplate template, CompoundTag tag) {
        try {
            // Try (RegistryAccess, CompoundTag) first
            for (Method m : StructureTemplate.class.getDeclaredMethods()) {
                if (m.getName().equals("load") && m.getParameterCount() == 2) {
                    m.setAccessible(true);
                    m.invoke(template, null, tag);
                    return;
                }
            }
            // Fallback: single-arg load(CompoundTag)
            Method single = StructureTemplate.class.getDeclaredMethod("load", CompoundTag.class);
            single.setAccessible(true);
            single.invoke(template, tag);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<StructureTemplate.StructureBlockInfo> extractBlocks(StructureTemplate template) {
        // Try mapped name "palettes" (Parchment/Forge dev env), then SRG obfuscated fallback.
        for (String fieldName : new String[]{"palettes", "f_74552_"}) {
            try {
                Field f = StructureTemplate.class.getDeclaredField(fieldName);
                f.setAccessible(true);
                List<?> palettes = (List<?>) f.get(template);
                if (palettes == null || palettes.isEmpty()) return List.of();
                Object palette = palettes.get(0);
                // StructurePalette.blocks() in Forge 1.20.1
                for (Method m : palette.getClass().getDeclaredMethods()) {
                    if (m.getName().equals("blocks") && m.getParameterCount() == 0) {
                        m.setAccessible(true);
                        return (List<StructureTemplate.StructureBlockInfo>) m.invoke(palette);
                    }
                }
            } catch (NoSuchFieldException ignored) {
            } catch (Exception e) {
                LOG.warn("[WLD-1] Palette reflection error: {}", e.getMessage());
            }
        }
        LOG.warn("[WLD-1] Could not read structure palette via reflection.");
        return List.of();
    }
}

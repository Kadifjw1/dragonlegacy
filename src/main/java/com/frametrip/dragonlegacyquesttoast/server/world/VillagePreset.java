package com.frametrip.dragonlegacyquesttoast.server.world;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;

// [WLD-4]: Spawns a preset village NPC set around a given center position.
public class VillagePreset {

    public enum VillageType { MEDIEVAL, FOREST, DESERT }

    public enum VillageSize {
        SMALL(5, 8),
        MEDIUM(8, 14),
        LARGE(12, 20);

        public final int npcCount;
        public final int spreadRadius;

        VillageSize(int npcCount, int spreadRadius) {
            this.npcCount = npcCount;
            this.spreadRadius = spreadRadius;
        }
    }

    /**
     * Spawns a preset village of NPCs around {@code center}.
     * NPCs are placed in a ring at {@code size.spreadRadius} distance.
     */
    public static void spawnVillage(ServerLevel level, BlockPos center, VillageType type, VillageSize size) {
        NpcEntityData[] roles = buildRoles(type, size);
        int count = Math.min(roles.length, size.npcCount);

        for (int i = 0; i < count; i++) {
            double angle = (Math.PI * 2 * i) / count;
            double x = center.getX() + Math.cos(angle) * size.spreadRadius;
            double z = center.getZ() + Math.sin(angle) * size.spreadRadius;
            double y = center.getY();

            NpcEntity npc = ModEntities.NPC.get().create(level);
            if (npc == null) continue;
            npc.moveTo(x, y, z, (float)(Math.toDegrees(angle + Math.PI)), 0f);
            npc.setNpcData(roles[i]);
            level.addFreshEntity(npc);
            npc.finalizeSpawn(level, level.getCurrentDifficultyAt(npc.blockPosition()),
                    MobSpawnType.MOB_SUMMONED, null, null);
        }
    }

    // ── Role templates per village type ──────────────────────────────────────

    private static NpcEntityData[] buildRoles(VillageType type, VillageSize size) {
        return switch (type) {
            case MEDIEVAL -> medievalRoles(size);
            case FOREST   -> forestRoles(size);
            case DESERT   -> desertRoles(size);
        };
    }

    private static NpcEntityData[] medievalRoles(VillageSize size) {
        return new NpcEntityData[] {
            makeNpc("Торговец", "FRIENDLY", false, false, true),
            makeNpc("Стражник I", "NEUTRAL", true, false, false),
            makeNpc("Стражник II", "NEUTRAL", true, false, false),
            makeNpc("Кузнец", "FRIENDLY", false, false, false),
            makeNpc("Фермер", "FRIENDLY", false, true, false),
            makeNpc("Трактирщик", "FRIENDLY", false, false, false),
            makeNpc("Горожанин", "FRIENDLY", false, false, false),
            makeNpc("Горожанка", "FRIENDLY", false, false, false),
        };
    }

    private static NpcEntityData[] forestRoles(VillageSize size) {
        return new NpcEntityData[] {
            makeNpc("Лесной торговец", "FRIENDLY", false, false, true),
            makeNpc("Лесной охранник", "NEUTRAL", true, false, false),
            makeNpc("Лесной охранник", "NEUTRAL", true, false, false),
            makeNpc("Дровосек", "FRIENDLY", false, false, false),
            makeNpc("Травник", "FRIENDLY", false, true, false),
            makeNpc("Лесной житель", "FRIENDLY", false, false, false),
        };
    }

    private static NpcEntityData[] desertRoles(VillageSize size) {
        return new NpcEntityData[] {
            makeNpc("Торговец пустыни", "FRIENDLY", false, false, true),
            makeNpc("Страж пустыни", "NEUTRAL", true, false, false),
            makeNpc("Страж пустыни", "NEUTRAL", true, false, false),
            makeNpc("Гончар", "FRIENDLY", false, false, false),
            makeNpc("Земледелец", "FRIENDLY", false, true, false),
            makeNpc("Житель оазиса", "FRIENDLY", false, false, false),
        };
    }

    private static NpcEntityData makeNpc(String name, String relation,
                                          boolean guardTerritory, boolean farmer, boolean trader) {
        NpcEntityData d = new NpcEntityData();
        d.displayName          = name;
        d.playerRelation       = relation;
        d.showName             = true;
        d.guardTerritoryEnabled = guardTerritory;
        d.guardRadius          = 12.0f;
        d.guardWarnFirst       = true;
        if (farmer) {
            d.farmerData = new FarmerData();
            d.farmerData.farmerEnabled = true;
            d.farmerData.cropType      = "wheat";
            d.farmerData.plotRadius    = 6;
        }
        return d;
    }
}

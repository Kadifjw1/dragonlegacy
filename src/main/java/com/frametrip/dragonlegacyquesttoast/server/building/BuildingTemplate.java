package com.frametrip.dragonlegacyquesttoast.server.building;

import java.util.ArrayList;
import java.util.List;

/** Шаблон здания, которое может построить NPC-строитель. */
public class BuildingTemplate {

    public String id          = "";
    public String name        = "Безымянное здание";
    public String category    = "misc";     // residential, military, farm, civic, misc
    public String description = "";
    public String professionLink = "";     // ID профессии, которой нужно это здание

    // Размеры в блоках
    public int sizeX = 5;
    public int sizeY = 4;
    public int sizeZ = 5;

    // Точка отсчёта (внутри размеров)
    public int originX = 0;
    public int originY = 0;
    public int originZ = 0;

    public List<BlockEntry> blocks = new ArrayList<>();

    /** Один блок в шаблоне. */
    public static class BlockEntry {
        public int    x;
        public int    y;
        public int    z;
        public String block;   // напр. "minecraft:oak_planks"

        public BlockEntry() {}
        public BlockEntry(int x, int y, int z, String block) {
            this.x = x; this.y = y; this.z = z; this.block = block;
        }
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    public String categoryLabel() {
        return switch (category) {
            case "residential" -> "Жильё";
            case "military"    -> "Военное";
            case "farm"        -> "Ферма";
            case "civic"       -> "Гражданское";
            default            -> "Разное";
        };
    }

    public String sizeLabel() {
        return sizeX + "×" + sizeY + "×" + sizeZ;
    }

    public int totalBlocks() {
        return blocks.size();
    }
}

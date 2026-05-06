package com.frametrip.dragonlegacyquesttoast.server.building;

import java.util.ArrayList;
import java.util.List;

/** 10 встроенных шаблонов зданий для NPC-строителей. */
final class BuiltInTemplates {

    private BuiltInTemplates() {}

    static List<BuildingTemplate> all() {
        List<BuildingTemplate> list = new ArrayList<>();
        list.add(woodenHouse());
        list.add(lumberjackHouse());
        list.add(minerHouse());
        list.add(mine());
        list.add(farm());
        list.add(barracks());
        list.add(cattleYard());
        list.add(archerTower());
        list.add(defensiveWalls());
        list.add(townHall());
        return list;
    }

    // ── 1. Жилой деревянный дом ────────────────────────────────────────────
    private static BuildingTemplate woodenHouse() {
        BuildingTemplate t = new BuildingTemplate();
        t.id = "wooden_house";
        t.name = "Жилой деревянный дом";
        t.category = "residential";
        t.description = "Простой деревянный дом с кроватями для двух жителей.";
        t.professionLink = "";
        t.sizeX = 7; t.sizeY = 5; t.sizeZ = 7;
        buildBox(t, 0, 0, 0, 7, 5, 7, "minecraft:oak_planks", "minecraft:oak_log", "minecraft:oak_log");
        // Floor
        fillLayer(t, 0, 0, 7, 7, "minecraft:oak_planks");
        // Door
        t.blocks.add(new BuildingTemplate.BlockEntry(3, 1, 0, "minecraft:air"));
        t.blocks.add(new BuildingTemplate.BlockEntry(3, 2, 0, "minecraft:air"));
        t.blocks.add(new BuildingTemplate.BlockEntry(3, 1, 0, "minecraft:oak_door"));
        // Window
        t.blocks.add(new BuildingTemplate.BlockEntry(1, 2, 0, "minecraft:glass_pane"));
        t.blocks.add(new BuildingTemplate.BlockEntry(5, 2, 0, "minecraft:glass_pane"));
        // Beds
        t.blocks.add(new BuildingTemplate.BlockEntry(1, 1, 5, "minecraft:red_bed"));
        t.blocks.add(new BuildingTemplate.BlockEntry(3, 1, 5, "minecraft:red_bed"));
        // Roof
        buildGableRoof(t, 0, 4, 0, 7, 7, "minecraft:oak_stairs", "minecraft:oak_slab");
        return t;
    }

    // ── 2. Дом лесоруба ───────────────────────────────────────────────────
    private static BuildingTemplate lumberjackHouse() {
        BuildingTemplate t = new BuildingTemplate();
        t.id = "lumberjack_house";
        t.name = "Дом лесоруба";
        t.category = "residential";
        t.description = "Крепкий дом лесоруба с верстаком и складом.";
        t.professionLink = "lumberjack";
        t.sizeX = 6; t.sizeY = 4; t.sizeZ = 6;
        buildBox(t, 0, 0, 0, 6, 4, 6, "minecraft:spruce_planks", "minecraft:spruce_log", "minecraft:spruce_log");
        fillLayer(t, 0, 0, 6, 6, "minecraft:spruce_planks");
        t.blocks.add(new BuildingTemplate.BlockEntry(2, 1, 0, "minecraft:air"));
        t.blocks.add(new BuildingTemplate.BlockEntry(2, 2, 0, "minecraft:air"));
        t.blocks.add(new BuildingTemplate.BlockEntry(1, 2, 0, "minecraft:glass_pane"));
        t.blocks.add(new BuildingTemplate.BlockEntry(4, 2, 0, "minecraft:glass_pane"));
        // Crafting table + chest
        t.blocks.add(new BuildingTemplate.BlockEntry(4, 1, 4, "minecraft:crafting_table"));
        t.blocks.add(new BuildingTemplate.BlockEntry(1, 1, 4, "minecraft:chest"));
        // Bed
        t.blocks.add(new BuildingTemplate.BlockEntry(1, 1, 2, "minecraft:spruce_slab"));
        buildGableRoof(t, 0, 3, 0, 6, 6, "minecraft:spruce_stairs", "minecraft:spruce_slab");
        return t;
    }
  
// ── 3. Дом шахтёра ────────────────────────────────────────────────────
    private static BuildingTemplate minerHouse() {
        BuildingTemplate t = new BuildingTemplate();
        t.id = "miner_house";
        t.name = "Дом шахтёра";
        t.category = "residential";
        t.description = "Каменный дом шахтёра с кузницей и сундуком.";
        t.professionLink = "miner";
        t.sizeX = 6; t.sizeY = 4; t.sizeZ = 6;
        buildBox(t, 0, 0, 0, 6, 4, 6, "minecraft:cobblestone", "minecraft:stone_bricks", "minecraft:stone_bricks");
        fillLayer(t, 0, 0, 6, 6, "minecraft:cobblestone");
        t.blocks.add(new BuildingTemplate.BlockEntry(2, 1, 0, "minecraft:air"));
        t.blocks.add(new BuildingTemplate.BlockEntry(2, 2, 0, "minecraft:air"));
        t.blocks.add(new BuildingTemplate.BlockEntry(1, 2, 0, "minecraft:glass_pane"));
        t.blocks.add(new BuildingTemplate.BlockEntry(4, 2, 0, "minecraft:glass_pane"));
        t.blocks.add(new BuildingTemplate.BlockEntry(4, 1, 4, "minecraft:furnace"));
        t.blocks.add(new BuildingTemplate.BlockEntry(1, 1, 4, "minecraft:chest"));
        t.blocks.add(new BuildingTemplate.BlockEntry(3, 1, 4, "minecraft:smoker"));
        buildGableRoof(t, 0, 3, 0, 6, 6, "minecraft:cobblestone_stairs", "minecraft:cobblestone_slab");
        return t;
    }

    // ── 4. Шахта ─────────────────────────────────────────────────────────
    private static BuildingTemplate mine() {
        BuildingTemplate t = new BuildingTemplate();
        t.id = "mine_entrance";
        t.name = "Шахта";
        t.category = "civic";
        t.description = "Вход в шахту с деревянными укреплениями и факелами.";
        t.professionLink = "miner";
        t.sizeX = 5; t.sizeY = 4; t.sizeZ = 8;
        // Entrance arch
        t.blocks.add(new BuildingTemplate.BlockEntry(0, 0, 0, "minecraft:cobblestone"));
        t.blocks.add(new BuildingTemplate.BlockEntry(4, 0, 0, "minecraft:cobblestone"));
        t.blocks.add(new BuildingTemplate.BlockEntry(0, 1, 0, "minecraft:cobblestone"));
        t.blocks.add(new BuildingTemplate.BlockEntry(4, 1, 0, "minecraft:cobblestone"));
        t.blocks.add(new BuildingTemplate.BlockEntry(0, 2, 0, "minecraft:cobblestone"));
        t.blocks.add(new BuildingTemplate.BlockEntry(1, 2, 0, "minecraft:cobblestone"));
        t.blocks.add(new BuildingTemplate.BlockEntry(2, 2, 0, "minecraft:cobblestone"));
        t.blocks.add(new BuildingTemplate.BlockEntry(3, 2, 0, "minecraft:cobblestone"));
        t.blocks.add(new BuildingTemplate.BlockEntry(4, 2, 0, "minecraft:cobblestone"));
        // Tunnel sides
        for (int z = 1; z <= 7; z++) {
            t.blocks.add(new BuildingTemplate.BlockEntry(0, 0, z, "minecraft:oak_planks"));
            t.blocks.add(new BuildingTemplate.BlockEntry(4, 0, z, "minecraft:oak_planks"));
            t.blocks.add(new BuildingTemplate.BlockEntry(0, 1, z, "minecraft:oak_planks"));
            t.blocks.add(new BuildingTemplate.BlockEntry(4, 1, z, "minecraft:oak_planks"));
            t.blocks.add(new BuildingTemplate.BlockEntry(0, 2, z, "minecraft:oak_log"));
            t.blocks.add(new BuildingTemplate.BlockEntry(4, 2, z, "minecraft:oak_log"));
        }
        // Torches
        for (int z = 2; z <= 7; z += 2) {
            t.blocks.add(new BuildingTemplate.BlockEntry(1, 2, z, "minecraft:torch"));
            t.blocks.add(new BuildingTemplate.BlockEntry(3, 2, z, "minecraft:torch"));
        }
        // Chest + lantern
        t.blocks.add(new BuildingTemplate.BlockEntry(2, 0, 7, "minecraft:chest"));
        t.blocks.add(new BuildingTemplate.BlockEntry(2, 2, 4, "minecraft:lantern"));
        return t;
    }

    // ── 5. Ферма ─────────────────────────────────────────────────────────
    private static BuildingTemplate farm() {
        BuildingTemplate t = new BuildingTemplate();
        t.id = "farm";
        t.name = "Ферма";
        t.category = "farm";
        t.description = "Небольшое хозяйство с полями пшеницы и водяным ирригатором.";
        t.professionLink = "farmer";
        t.sizeX = 9; t.sizeY = 2; t.sizeZ = 9;
        // Irrigation channel
        for (int x = 0; x <= 8; x++) t.blocks.add(new BuildingTemplate.BlockEntry(x, 0, 4, "minecraft:water"));
        // Farmland
        for (int x = 0; x <= 8; x++) {
            for (int z = 0; z <= 8; z++) {
                if (z != 4) {
                    t.blocks.add(new BuildingTemplate.BlockEntry(x, 0, z, "minecraft:farmland"));
                    if (z != 0 && z != 8 && x != 0 && x != 8)
                        t.blocks.add(new BuildingTemplate.BlockEntry(x, 1, z, "minecraft:wheat"));
                }
            }
        }
        // Fences
        for (int x = 0; x <= 8; x++) {
            t.blocks.add(new BuildingTemplate.BlockEntry(x, 1, 0, "minecraft:oak_fence"));
            t.blocks.add(new BuildingTemplate.BlockEntry(x, 1, 8, "minecraft:oak_fence"));
        }
        for (int z = 1; z <= 7; z++) {
            t.blocks.add(new BuildingTemplate.BlockEntry(0, 1, z, "minecraft:oak_fence"));
            t.blocks.add(new BuildingTemplate.BlockEntry(8, 1, z, "minecraft:oak_fence"));
        }
        // Gate
        t.blocks.add(new BuildingTemplate.BlockEntry(4, 1, 0, "minecraft:oak_fence_gate"));
        return t;
    }

// ── 6. Казарма ────────────────────────────────────────────────────────
    private static BuildingTemplate barracks() {
        BuildingTemplate t = new BuildingTemplate();
        t.id = "barracks";
        t.name = "Казарма";
        t.category = "military";
        t.description = "Военные казармы для стражи с кроватями и стойками для оружия.";
        t.professionLink = "guard";
        t.sizeX = 9; t.sizeY = 5; t.sizeZ = 7;
        buildBox(t, 0, 0, 0, 9, 5, 7, "minecraft:stone_bricks", "minecraft:stone_bricks", "minecraft:stone_bricks");
        fillLayer(t, 0, 0, 9, 7, "minecraft:stone_bricks");
        // Door + windows
        t.blocks.add(new BuildingTemplate.BlockEntry(4, 1, 0, "minecraft:air"));
        t.blocks.add(new BuildingTemplate.BlockEntry(4, 2, 0, "minecraft:air"));
        for (int x = 1; x <= 7; x += 3) {
            t.blocks.add(new BuildingTemplate.BlockEntry(x, 2, 0, "minecraft:iron_bars"));
        }
        // Beds (6 total)
        for (int i = 0; i < 3; i++) {
            t.blocks.add(new BuildingTemplate.BlockEntry(1 + i * 3, 1, 5, "minecraft:red_bed"));
            t.blocks.add(new BuildingTemplate.BlockEntry(1 + i * 3, 1, 1, "minecraft:red_bed"));
        }
        // Armor stands
        t.blocks.add(new BuildingTemplate.BlockEntry(7, 1, 3, "minecraft:armor_stand"));
        // Chest
        t.blocks.add(new BuildingTemplate.BlockEntry(1, 1, 3, "minecraft:chest"));
        buildFlatRoof(t, 0, 4, 0, 9, 7, "minecraft:stone_brick_slab");
        return t;
    }

    // ── 7. Скотоводческий двор ────────────────────────────────────────────
    private static BuildingTemplate cattleYard() {
        BuildingTemplate t = new BuildingTemplate();
        t.id = "cattle_yard";
        t.name = "Скотоводческий двор";
        t.category = "farm";
        t.description = "Загон для скота с сеном и поилками.";
        t.professionLink = "farmer";
        t.sizeX = 10; t.sizeY = 3; t.sizeZ = 8;
        // Outer fence
        for (int x = 0; x <= 9; x++) {
            t.blocks.add(new BuildingTemplate.BlockEntry(x, 0, 0, "minecraft:cobblestone"));
            t.blocks.add(new BuildingTemplate.BlockEntry(x, 1, 0, "minecraft:oak_fence"));
            t.blocks.add(new BuildingTemplate.BlockEntry(x, 0, 7, "minecraft:cobblestone"));
            t.blocks.add(new BuildingTemplate.BlockEntry(x, 1, 7, "minecraft:oak_fence"));
        }
        for (int z = 1; z <= 6; z++) {
            t.blocks.add(new BuildingTemplate.BlockEntry(0, 0, z, "minecraft:cobblestone"));
            t.blocks.add(new BuildingTemplate.BlockEntry(0, 1, z, "minecraft:oak_fence"));
            t.blocks.add(new BuildingTemplate.BlockEntry(9, 0, z, "minecraft:cobblestone"));
            t.blocks.add(new BuildingTemplate.BlockEntry(9, 1, z, "minecraft:oak_fence"));
        }
        // Gate
        t.blocks.add(new BuildingTemplate.BlockEntry(4, 1, 0, "minecraft:oak_fence_gate"));
        t.blocks.add(new BuildingTemplate.BlockEntry(5, 1, 0, "minecraft:oak_fence_gate"));
        // Hay bales and water trough
        t.blocks.add(new BuildingTemplate.BlockEntry(1, 0, 5, "minecraft:hay_block"));
        t.blocks.add(new BuildingTemplate.BlockEntry(2, 0, 5, "minecraft:hay_block"));
        t.blocks.add(new BuildingTemplate.BlockEntry(7, 0, 5, "minecraft:water"));
        t.blocks.add(new BuildingTemplate.BlockEntry(8, 0, 5, "minecraft:water"));
        // Shelter roof corner
        for (int x = 0; x <= 3; x++) {
            t.blocks.add(new BuildingTemplate.BlockEntry(x, 2, 4, "minecraft:oak_planks"));
            t.blocks.add(new BuildingTemplate.BlockEntry(x, 2, 5, "minecraft:oak_planks"));
            t.blocks.add(new BuildingTemplate.BlockEntry(x, 2, 6, "minecraft:oak_planks"));
        }
        return t;
    }

 // ── 8. Башня для лучников ─────────────────────────────────────────────
    private static BuildingTemplate archerTower() {
        BuildingTemplate t = new BuildingTemplate();
        t.id = "archer_tower";
        t.name = "Башня для лучников";
        t.category = "military";
        t.description = "Каменная башня с бойницами и смотровой площадкой.";
        t.professionLink = "archer";
        t.sizeX = 5; t.sizeY = 10; t.sizeZ = 5;
        // Tower walls (hollow)
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 5; x++) {
                for (int z = 0; z < 5; z++) {
                    if (x == 0 || x == 4 || z == 0 || z == 4) {
                        String block = (y % 3 == 2) ? "minecraft:stone_bricks" : "minecraft:cobblestone";
                        t.blocks.add(new BuildingTemplate.BlockEntry(x, y, z, block));
                    }
                }
            }
        }
        // Floor at base
        fillLayer(t, 0, 0, 5, 5, "minecraft:cobblestone");
        // Top platform
        for (int x = 0; x < 5; x++) for (int z = 0; z < 5; z++)
            t.blocks.add(new BuildingTemplate.BlockEntry(x, 9, z, "minecraft:stone_brick_slab"));
        // Battlements
        for (int i = 0; i < 5; i += 2) {
            t.blocks.add(new BuildingTemplate.BlockEntry(i, 10, 0, "minecraft:stone_brick_wall"));
            t.blocks.add(new BuildingTemplate.BlockEntry(i, 10, 4, "minecraft:stone_brick_wall"));
            t.blocks.add(new BuildingTemplate.BlockEntry(0, 10, i, "minecraft:stone_brick_wall"));
            t.blocks.add(new BuildingTemplate.BlockEntry(4, 10, i, "minecraft:stone_brick_wall"));
        }
        // Arrow slits
        t.blocks.add(new BuildingTemplate.BlockEntry(2, 5, 0, "minecraft:air"));
        t.blocks.add(new BuildingTemplate.BlockEntry(0, 5, 2, "minecraft:air"));
        t.blocks.add(new BuildingTemplate.BlockEntry(4, 5, 2, "minecraft:air"));
        // Ladder
        for (int y = 1; y < 9; y++)
            t.blocks.add(new BuildingTemplate.BlockEntry(2, y, 3, "minecraft:ladder"));
        // Chest at top
        t.blocks.add(new BuildingTemplate.BlockEntry(2, 9, 2, "minecraft:chest"));
        return t;
    }

    // ── 9. Оборонительные стены ──────────────────────────────────────────
    private static BuildingTemplate defensiveWalls() {
        BuildingTemplate t = new BuildingTemplate();
        t.id = "defensive_walls";
        t.name = "Оборонительные стены";
        t.category = "military";
        t.description = "Секция каменной стены с зубцами и воротами.";
        t.professionLink = "";
        t.sizeX = 16; t.sizeY = 6; t.sizeZ = 3;
        // Wall body
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 5; y++) {
                t.blocks.add(new BuildingTemplate.BlockEntry(x, y, 0, "minecraft:stone_bricks"));
                t.blocks.add(new BuildingTemplate.BlockEntry(x, y, 1, "minecraft:stone_bricks"));
                t.blocks.add(new BuildingTemplate.BlockEntry(x, y, 2, "minecraft:stone_bricks"));
            }
        }
        // Gate (3 wide, 3 tall at center)
        for (int y = 0; y < 4; y++) {
            for (int z = 0; z < 3; z++) {
                t.blocks.add(new BuildingTemplate.BlockEntry(7, y, z, "minecraft:air"));
                t.blocks.add(new BuildingTemplate.BlockEntry(8, y, z, "minecraft:air"));
            }
        }
        // Gate arch
        for (int z = 0; z < 3; z++) {
            t.blocks.add(new BuildingTemplate.BlockEntry(6, 4, z, "minecraft:stone_brick_stairs"));
            t.blocks.add(new BuildingTemplate.BlockEntry(9, 4, z, "minecraft:stone_brick_stairs"));
        }
        // Battlements
        for (int x = 0; x < 16; x += 2) {
            t.blocks.add(new BuildingTemplate.BlockEntry(x, 5, 0, "minecraft:stone_brick_wall"));
        }
        // Torches
        for (int x = 2; x < 16; x += 4) {
            t.blocks.add(new BuildingTemplate.BlockEntry(x, 4, 2, "minecraft:torch"));
        }
        return t;
    }

// ── 10. Ратуша ────────────────────────────────────────────────────────
    private static BuildingTemplate townHall() {
        BuildingTemplate t = new BuildingTemplate();
        t.id = "town_hall";
        t.name = "Ратуша";
        t.category = "civic";
        t.description = "Большое каменное здание ратуши — центр управления поселением.";
        t.professionLink = "mayor";
        t.sizeX = 11; t.sizeY = 7; t.sizeZ = 9;
        buildBox(t, 0, 0, 0, 11, 7, 9, "minecraft:stone_bricks", "minecraft:chiseled_stone_bricks", "minecraft:stone_brick_wall");
        fillLayer(t, 0, 0, 11, 9, "minecraft:stone_bricks");
        // Large door
        t.blocks.add(new BuildingTemplate.BlockEntry(5, 1, 0, "minecraft:air"));
        t.blocks.add(new BuildingTemplate.BlockEntry(5, 2, 0, "minecraft:air"));
        t.blocks.add(new BuildingTemplate.BlockEntry(5, 3, 0, "minecraft:air"));
        t.blocks.add(new BuildingTemplate.BlockEntry(5, 1, 0, "minecraft:oak_door"));
        // Windows
        for (int x : new int[]{2, 4, 6, 8}) {
            t.blocks.add(new BuildingTemplate.BlockEntry(x, 3, 0, "minecraft:glass_pane"));
            t.blocks.add(new BuildingTemplate.BlockEntry(x, 3, 8, "minecraft:glass_pane"));
        }
        // Interior: table + chairs
        t.blocks.add(new BuildingTemplate.BlockEntry(5, 1, 4, "minecraft:lectern"));
        t.blocks.add(new BuildingTemplate.BlockEntry(3, 1, 4, "minecraft:bookshelf"));
        t.blocks.add(new BuildingTemplate.BlockEntry(7, 1, 4, "minecraft:bookshelf"));
        t.blocks.add(new BuildingTemplate.BlockEntry(5, 1, 2, "minecraft:crafting_table"));
        t.blocks.add(new BuildingTemplate.BlockEntry(5, 1, 6, "minecraft:chest"));
        // Bell
        t.blocks.add(new BuildingTemplate.BlockEntry(5, 1, 8, "minecraft:bell"));
        // Roof
        buildGableRoof(t, 0, 6, 0, 11, 9, "minecraft:stone_brick_stairs", "minecraft:stone_brick_slab");
        return t;
    }

    // ── Строительные утилиты ─────────────────────────────────────────────

    /** Строит пустотелый ящик: стены+пол с заданными материалами. */
    private static void buildBox(BuildingTemplate t, int x0, int y0, int z0, int w, int h, int d,
                                  String wall, String corner, String base) {
        for (int x = x0; x < x0 + w; x++) {
            for (int y = y0; y < y0 + h; y++) {
                for (int z = z0; z < z0 + d; z++) {
                    boolean isEdgeX = (x == x0 || x == x0 + w - 1);
                    boolean isEdgeZ = (z == z0 || z == z0 + d - 1);
                    boolean isEdgeY = (y == y0);
                    if (isEdgeX || isEdgeZ) {
                        String block = (isEdgeX && isEdgeZ) ? corner : (isEdgeY ? base : wall);
                        t.blocks.add(new BuildingTemplate.BlockEntry(x, y, z, block));
                    }
                }
            }
        }
    }

    /** Заполняет горизонтальный слой блоками. */
    private static void fillLayer(BuildingTemplate t, int x0, int z0, int w, int d, String block) {
        for (int x = x0; x < x0 + w; x++)
            for (int z = z0; z < z0 + d; z++)
                t.blocks.add(new BuildingTemplate.BlockEntry(x, 0, z, block));
    }

    /** Добавляет двускатную крышу. */
    private static void buildGableRoof(BuildingTemplate t, int x0, int baseY, int z0, int w, int d,
                                        String stair, String slab) {
        int cx = x0 + w / 2;
        for (int layer = 0; layer < w / 2; layer++) {
            int y = baseY + layer;
            for (int z = z0; z < z0 + d; z++) {
                int lx = cx - layer;
                int rx = cx + layer;
                if (lx >= x0) t.blocks.add(new BuildingTemplate.BlockEntry(lx, y, z, stair));
                if (rx < x0 + w && rx != lx) t.blocks.add(new BuildingTemplate.BlockEntry(rx, y, z, stair));
            }
        }
        // Ridge
        for (int z = z0; z < z0 + d; z++)
            t.blocks.add(new BuildingTemplate.BlockEntry(cx, baseY + w / 2, z, slab));
    }

    /** Добавляет плоскую крышу. */
    private static void buildFlatRoof(BuildingTemplate t, int x0, int y, int z0, int w, int d, String block) {
        for (int x = x0; x < x0 + w; x++)
            for (int z = z0; z < z0 + d; z++)
                t.blocks.add(new BuildingTemplate.BlockEntry(x, y, z, block));
    }
}

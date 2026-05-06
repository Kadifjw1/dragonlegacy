package com.frametrip.dragonlegacyquesttoast.server.building;

import java.util.ArrayList;
import java.util.List;

/** Конфигурация строителя, хранится в NpcEntityData. */
public class NpcBuildingData {

    /** Является ли NPC строителем. */
    public boolean isBuilder = false;

    /** ID шаблонов зданий, разрешённых для этого NPC. */
    public List<String> allowedBuildingIds = new ArrayList<>();

    /** Рабочая зона: центр и радиус (X, Y, Z, radius). */
    public int workZoneX      = 0;
    public int workZoneY      = 0;
    public int workZoneZ      = 0;
    public int workZoneRadius = 32;

    /** Скорость строительства (блоков за тик). */
    public int blocksPerTick = 1;

    public NpcBuildingData copy() {
        NpcBuildingData c = new NpcBuildingData();
        c.isBuilder           = this.isBuilder;
        c.allowedBuildingIds  = new ArrayList<>(this.allowedBuildingIds);
        c.workZoneX           = this.workZoneX;
        c.workZoneY           = this.workZoneY;
        c.workZoneZ           = this.workZoneZ;
        c.workZoneRadius      = this.workZoneRadius;
        c.blocksPerTick       = this.blocksPerTick;
        return c;
    }
}

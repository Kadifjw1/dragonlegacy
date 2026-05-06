package com.frametrip.dragonlegacyquesttoast.server.building;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

/** Текущее состояние строительства для конкретного NPC. */
public class NpcBuildingState {

    public enum Status { IDLE, BUILDING, PAUSED, DONE, CANCELLED }

    public final UUID     npcId;
    public final String   templateId;
    public       Status   status = Status.IDLE;

    /** Очередь блоков для размещения. */
    public final Deque<BuildingTemplate.BlockEntry> queue = new ArrayDeque<>();

    /** Смещение начала строительства в мире. */
    public int originX;
    public int originY;
    public int originZ;

    /** Всего блоков в очереди изначально. */
    public int totalBlocks;
    /** Уже размещено. */
    public int placedBlocks = 0;

    public NpcBuildingState(UUID npcId, String templateId, int ox, int oy, int oz,
                             BuildingTemplate template) {
        this.npcId      = npcId;
        this.templateId = templateId;
        this.originX    = ox;
        this.originY    = oy;
        this.originZ    = oz;
        if (template != null) {
            queue.addAll(template.blocks);
            this.totalBlocks = queue.size();
        }
        this.status = Status.BUILDING;
    }

    public float progress() {
        if (totalBlocks == 0) return 1f;
        return placedBlocks / (float) totalBlocks;
    }

    public String progressLabel() {
        return placedBlocks + "/" + totalBlocks;
    }

    public void pause()  { if (status == Status.BUILDING)  status = Status.PAUSED; }
    public void resume() { if (status == Status.PAUSED)     status = Status.BUILDING; }
    public void cancel() { status = Status.CANCELLED; queue.clear(); }
}


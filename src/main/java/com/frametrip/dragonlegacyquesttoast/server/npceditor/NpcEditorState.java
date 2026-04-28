package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.SaveNpcEntityDataPacket;

/**
 * Single source of truth for the NPC editor session.
 * Tracks the draft data and whether it diverges from the saved state.
 */
public class NpcEditorState {

    private final NpcEntity npcEntity;
    private final NpcEntityData originalData;
    private NpcEntityData draftData;
    private boolean dirty = false;

    public NpcEditorState(NpcEntity entity) {
        this.npcEntity    = entity;
        this.originalData = entity.getNpcData().copy();
        this.draftData    = originalData.copy();
    }

    public NpcEntity getEntity() {
        return npcEntity;
    }

    public NpcEntityData getDraft() {
        return draftData;
    }

    public boolean isDirty() {
        return dirty;
    }

    /** Call whenever the draft is mutated from outside. */
    public void markDirty() {
        dirty = true;
    }

    /** Directly replace the draft (marks dirty automatically). */
    public void setDraft(NpcEntityData data) {
        this.draftData = data;
        this.dirty = true;
    }

    /** Send to server, apply locally, clear dirty flag. */
    public void save() {
        NpcEditorValidator.sanitize(draftData);

        ModNetwork.CHANNEL.sendToServer(
                new SaveNpcEntityDataPacket(npcEntity.getUUID(), draftData)
        );
        npcEntity.setNpcData(draftData);
        dirty = false;
    }

    /** Discard draft, restore to original saved state. */
    public void reset() {
        draftData = originalData.copy();
        dirty = false;
    }
}

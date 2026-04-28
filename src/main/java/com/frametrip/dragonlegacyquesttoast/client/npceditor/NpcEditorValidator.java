package com.frametrip.dragonlegacyquesttoast.client.npceditor;

import com.frametrip.dragonlegacyquesttoast.client.ClientDialogueState;
import com.frametrip.dragonlegacyquesttoast.client.ClientFactionState;
import com.frametrip.dragonlegacyquesttoast.client.ClientQuestState;
import com.frametrip.dragonlegacyquesttoast.client.dialogue.ClientNpcSceneState;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Basic data validation/sanitization for NPC editor draft data. */
public final class NpcEditorValidator {

    public static final class Issue {
        public final String message;
        public final boolean error;

        public Issue(String message, boolean error) {
            this.message = message;
            this.error = error;
        }
    }

    private NpcEditorValidator() {}

    public static List<Issue> validate(NpcEntityData d) {
        List<Issue> issues = new ArrayList<>();
        if (d == null) {
            issues.add(new Issue("Данные NPC отсутствуют", true));
            return issues;
        }
        if (d.displayName == null || d.displayName.isBlank()) {
            issues.add(new Issue("Имя NPC пустое", true));
        }
        if (d.sceneId != null && !d.sceneId.isBlank() && ClientNpcSceneState.get(d.sceneId) == null) {
            issues.add(new Issue("Сцена не найдена: " + d.sceneId, true));
        }
        if (d.dialogueId != null && !d.dialogueId.isBlank()
                && ClientDialogueState.getAll().stream().noneMatch(x -> d.dialogueId.equals(x.id))) {
            issues.add(new Issue("Диалог не найден: " + d.dialogueId, true));
        }
        if (d.factionId != null && !d.factionId.isBlank() && ClientFactionState.get(d.factionId) == null) {
            issues.add(new Issue("Фракция не найдена: " + d.factionId, false));
        }
        if (d.questIds != null) {
            for (String q : d.questIds) {
                if (ClientQuestState.getAll().stream().noneMatch(x -> q.equals(x.id))) {
                    issues.add(new Issue("Квест не найден: " + q, false));
                }
            }
        }
        return issues;
    }

    public static void sanitize(NpcEntityData d) {
        if (d == null) return;
        if (d.displayName == null || d.displayName.isBlank()) d.displayName = "NPC";
        if (d.textureLayers == null) d.textureLayers = new LinkedHashMap<>();
        if (d.bodyParts == null) d.bodyParts = new LinkedHashMap<>();
        if (d.questIds == null) d.questIds = new ArrayList<>();
        if (d.sceneId == null) d.sceneId = "";
        if (d.dialogueId == null) d.dialogueId = "";
        if (d.factionId == null) d.factionId = "";
        if (d.skinId == null || d.skinId.isBlank()) d.skinId = "default";
    }
}

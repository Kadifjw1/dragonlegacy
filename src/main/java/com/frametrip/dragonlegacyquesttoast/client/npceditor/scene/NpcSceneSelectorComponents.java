package com.frametrip.dragonlegacyquesttoast.client.npceditor.scene;

import com.frametrip.dragonlegacyquesttoast.client.ClientFactionState;
import com.frametrip.dragonlegacyquesttoast.client.ClientNpcProfileState;
import com.frametrip.dragonlegacyquesttoast.client.ClientQuestState;
import com.frametrip.dragonlegacyquesttoast.client.dialogue.ClientNpcSceneState;
import com.frametrip.dragonlegacyquesttoast.server.NpcProfile;
import com.frametrip.dragonlegacyquesttoast.server.QuestDefinition;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcScene;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

final class NpcSceneSelectorComponents {
    private NpcSceneSelectorComponents() {}

    record SelectorOption(String id, String title, String subtitle) {
        String uiLabel() {
            String sub = subtitle == null || subtitle.isBlank() ? "" : " §8(" + subtitle + ")";
            return title + sub;
        }
    }

    static void addSelector(NpcSceneEditorScreen scr, int x, int y, int w,
                            String title, String currentId,
                            List<SelectorOption> options,
                            Consumer<String> onSelect,
                            Runnable onOpen) {
        int current = -1;
        for (int i = 0; i < options.size(); i++) if (options.get(i).id.equals(currentId)) { current = i; break; }
        String label = current >= 0 ? options.get(current).uiLabel() : "§7не выбрано";
        if (label.length() > 34) label = label.substring(0, 34) + "…";
        final int cur = current;

        scr.addRenderableWidget(Button.builder(Component.literal(title + ": " + label), b -> {
            int next = options.isEmpty() ? -1 : (cur + 1 + options.size()) % options.size();
            onSelect.accept(next < 0 ? "" : options.get(next).id);
            scr.rebuildAll();
        }).bounds(x, y, w - 48, 14).build());

        scr.addRenderableWidget(Button.builder(Component.literal("✕"), b -> {
            onSelect.accept("");
            scr.rebuildAll();
        }).bounds(x + w - 46, y, 14, 14).build());

        scr.addRenderableWidget(Button.builder(Component.literal("↗"), b -> {
            if (onOpen != null) onOpen.run();
        }).bounds(x + w - 30, y, 14, 14).build());
    }

    static List<SelectorOption> questOptions() {
        List<SelectorOption> out = new ArrayList<>();
        for (QuestDefinition q : ClientQuestState.getAll()) {
            out.add(new SelectorOption(q.id, q.title == null || q.title.isBlank() ? q.id : q.title, q.id));
        }
        out.sort(Comparator.comparing(SelectorOption::title));
        return out;
    }

    static List<SelectorOption> sceneOptions() {
        List<SelectorOption> out = new ArrayList<>();
        for (NpcScene s : ClientNpcSceneState.getAll()) {
            out.add(new SelectorOption(s.id, s.name == null || s.name.isBlank() ? s.id : s.name, s.type));
        }
        out.sort(Comparator.comparing(SelectorOption::title));
        return out;
    }

    static List<SelectorOption> npcOptions() {
        List<SelectorOption> out = new ArrayList<>();
        for (NpcProfile n : ClientNpcProfileState.getAll()) {
            out.add(new SelectorOption(n.id, n.displayName == null || n.displayName.isBlank() ? n.id : n.displayName, n.dialogueId));
        }
        out.sort(Comparator.comparing(SelectorOption::title));
        return out;
    }

    static List<SelectorOption> factionOptions() {
        List<SelectorOption> out = new ArrayList<>();
        ClientFactionState.getAll().forEach(f -> out.add(new SelectorOption(f.id, f.name, f.id)));
        out.sort(Comparator.comparing(SelectorOption::title));
        return out;
    }

    static List<SelectorOption> animationOptions() {
        return List.of(
                new SelectorOption("idle", "Idle", "base"),
                new SelectorOption("wave", "Wave", "gesture"),
                new SelectorOption("point", "Point", "gesture"),
                new SelectorOption("bow", "Bow", "greet")
        );
    }

    static List<SelectorOption> itemOptions() {
        List<SelectorOption> out = new ArrayList<>();
        net.minecraft.core.registries.BuiltInRegistries.ITEM.keySet().forEach(key -> {
            String id = key.toString();
            out.add(new SelectorOption(id, id.substring(id.indexOf(':') + 1), id));
        });
        out.sort(Comparator.comparing(SelectorOption::title));
        if (out.size() > 400) return out.subList(0, 400); // lightweight in editor UI
        return out;
    }
    
    static List<SelectorOption> buildingOptions() {
        List<SelectorOption> out = new ArrayList<>();
        com.frametrip.dragonlegacyquesttoast.server.building.BuildingTemplateManager.getAll()
                .forEach(t -> out.add(new SelectorOption(t.id, t.name, t.categoryLabel())));
        out.sort(Comparator.comparing(SelectorOption::title));
        return out;
    }
}

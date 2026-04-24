package com.frametrip.dragonlegacyquesttoast.client.dialogue;

import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcScene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientNpcSceneState {

    private static final List<NpcScene> scenes = new ArrayList<>();

    public static void sync(List<NpcScene> incoming) {
        scenes.clear();
        if (incoming != null) scenes.addAll(incoming);
    }

    public static List<NpcScene> getAll() {
        return Collections.unmodifiableList(scenes);
    }

    public static NpcScene get(String id) {
        if (id == null || id.isEmpty()) return null;
        return scenes.stream().filter(s -> id.equals(s.id)).findFirst().orElse(null);
    }
}

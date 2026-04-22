package com.frametrip.dragonlegacyquesttoast.client;
 
import com.frametrip.dragonlegacyquesttoast.server.QuestDefinition;
 
import java.util.ArrayList;
import java.util.List;
 
public class ClientQuestState {
 
    private static List<QuestDefinition> quests = new ArrayList<>();
 
    public static void sync(List<QuestDefinition> incoming) {
        quests = incoming != null ? new ArrayList<>(incoming) : new ArrayList<>();
    }
 
    public static List<QuestDefinition> getAll() {
        return new ArrayList<>(quests);
    }
}

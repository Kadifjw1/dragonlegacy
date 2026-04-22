package com.frametrip.dragonlegacyquesttoast.client;
 
import com.frametrip.dragonlegacyquesttoast.server.DialogueDefinition;
 
import java.util.ArrayList;
import java.util.List;
 
public class ClientDialogueState {
 
    private static List<DialogueDefinition> dialogues = new ArrayList<>();
 
    public static void sync(List<DialogueDefinition> incoming) {
        dialogues = incoming != null ? new ArrayList<>(incoming) : new ArrayList<>();
    }
 
    public static List<DialogueDefinition> getAll() {
        return new ArrayList<>(dialogues);
    }
}

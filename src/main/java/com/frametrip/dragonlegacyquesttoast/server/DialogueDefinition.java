package com.frametrip.dragonlegacyquesttoast.server;
 
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
 
public class DialogueDefinition {
 
    public String id;
    public String npcName;
    public List<String> lines;
 
    public DialogueDefinition() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.npcName = "NPC";
        this.lines = new ArrayList<>();
    }
 
    public DialogueDefinition copy() {
        DialogueDefinition c = new DialogueDefinition();
        c.id = this.id;
        c.npcName = this.npcName;
        c.lines = new ArrayList<>(this.lines);
        return c;
    }
}

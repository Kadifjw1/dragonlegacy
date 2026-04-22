package com.frametrip.dragonlegacyquesttoast.server;
 
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
 
public class QuestDefinition {
 
    public String id;
    public String title;
    public String description;
    public List<String> objectives;
    public String giverNpcId;
    public String rewardText;
    public String questType; // "main", "side", "daily"
 
    public QuestDefinition() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.title = "Новый квест";
        this.description = "";
        this.objectives = new ArrayList<>();
        this.giverNpcId = "";
        this.rewardText = "";
        this.questType = "side";
    }
 
    public QuestDefinition copy() {
        QuestDefinition c = new QuestDefinition();
        c.id = this.id;
        c.title = this.title;
        c.description = this.description;
        c.objectives = new ArrayList<>(this.objectives);
        c.giverNpcId = this.giverNpcId;
        c.rewardText = this.rewardText;
        c.questType = this.questType;
        return c;
    }
 
    public static final String[] TYPES = {"main", "side", "daily"};
    public static final String[] TYPE_LABELS = {"Основной", "Доп.", "Ежедневный"};
 
    public String typeLabel() {
        for (int i = 0; i < TYPES.length; i++) {
            if (TYPES[i].equals(questType)) return TYPE_LABELS[i];
        }
        return questType;
    }
}

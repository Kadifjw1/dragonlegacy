package com.frametrip.dragonlegacyquesttoast.server.dialogue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NpcSceneNode {

    public static final String TYPE_SPEECH   = "speech";
    public static final String TYPE_QUESTION = "question";
    public static final String TYPE_ACTION   = "action";

    // action types
    public static final String ACTION_GIVE_QUEST     = "give_quest";
    public static final String ACTION_COMPLETE_QUEST = "complete_quest";
    public static final String ACTION_SET_RELATION   = "set_relation";

    public String id;
    public String type = TYPE_SPEECH;

    // speech / question text
    public String text = "";

    // speech: go to next node after display
    public String nextNodeId = "";

    // question: list of player choices
    public List<NpcChoiceOption> choices = new ArrayList<>();

    // action: what action to perform
    public String actionType  = ACTION_GIVE_QUEST;
    public String actionParam = "";
    public String actionNextNodeId = "";

    public NpcSceneNode() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
    }

    public NpcSceneNode copy() {
        NpcSceneNode c = new NpcSceneNode();
        c.id             = this.id;
        c.type           = this.type;
        c.text           = this.text;
        c.nextNodeId     = this.nextNodeId;
        c.choices        = new ArrayList<>();
        for (NpcChoiceOption o : this.choices) c.choices.add(o.copy());
        c.actionType     = this.actionType;
        c.actionParam    = this.actionParam;
        c.actionNextNodeId = this.actionNextNodeId;
        return c;
    }

    public String displayLabel() {
        return switch (type) {
            case TYPE_SPEECH   -> "💬 " + (text.length() > 28 ? text.substring(0, 28) + "…" : text);
            case TYPE_QUESTION -> "❓ " + (text.length() > 28 ? text.substring(0, 28) + "…" : text);
            case TYPE_ACTION   -> "⚡ " + actionType + (actionParam.isEmpty() ? "" : ": " + actionParam);
            default            -> id;
        };
    }
}

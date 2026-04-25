package com.frametrip.dragonlegacyquesttoast.server.dialogue;

import java.util.UUID;

public class NpcChoiceOption {
    
    public String id;
    public String text = "";
    public String nextNodeId = "";

    // Optional condition for when this choice is shown/selectable.
    // Uses NpcSceneNode.COND_* ids. Empty string = always available.
    public String conditionType  = "";
    public String conditionParam = "";

    // Optional action executed after the choice is picked (before transition).
    // Uses NpcSceneNode.ACTION_* ids. Empty string = no action.
    public String actionType  = "";
    public String actionParam = "";

    public NpcChoiceOption() {
        this.id = UUID.randomUUID().toString().substring(0, 6);
    }

    public NpcChoiceOption(String text, String nextNodeId) {
        this();
        this.text = text;
        this.nextNodeId = nextNodeId;
    }

    public NpcChoiceOption copy() {
        NpcChoiceOption c = new NpcChoiceOption();
        c.id             = this.id;
        c.text           = this.text;
        c.nextNodeId     = this.nextNodeId;
        c.conditionType  = this.conditionType;
        c.conditionParam = this.conditionParam;
        c.actionType     = this.actionType;
        c.actionParam    = this.actionParam;
        return c;
    }
}

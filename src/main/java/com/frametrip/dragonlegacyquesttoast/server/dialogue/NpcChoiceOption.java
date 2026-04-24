package com.frametrip.dragonlegacyquesttoast.server.dialogue;

public class NpcChoiceOption {
    public String text = "";
    public String nextNodeId = "";

    public NpcChoiceOption() {}

    public NpcChoiceOption(String text, String nextNodeId) {
        this.text = text;
        this.nextNodeId = nextNodeId;
    }

    public NpcChoiceOption copy() {
        return new NpcChoiceOption(this.text, this.nextNodeId);
    }
}

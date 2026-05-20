package com.frametrip.dragonlegacyquesttoast.server.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// [QST-2]: Branching dialogue tree node.
public class BranchingDialog {

    public String id;
    public String text;
    public List<DialogOption> options = new ArrayList<>();

    public BranchingDialog() {
        this.id   = UUID.randomUUID().toString().substring(0, 8);
        this.text = "";
    }

    public static class DialogOption {
        public String text         = "";
        public String nextDialogId = ""; // empty = end of conversation
        public String condition    = ""; // e.g. "questDone:abc123" or "" = always visible
        public String questIdToGive  = ""; // optional: give a quest when chosen
        public String questIdToFail  = ""; // optional: fail a quest when chosen
    }

    public BranchingDialog copy() {
        BranchingDialog c = new BranchingDialog();
        c.id   = this.id;
        c.text = this.text;
        for (DialogOption o : this.options) {
            DialogOption oc = new DialogOption();
            oc.text          = o.text;
            oc.nextDialogId  = o.nextDialogId;
            oc.condition     = o.condition;
            oc.questIdToGive = o.questIdToGive;
            oc.questIdToFail = o.questIdToFail;
            c.options.add(oc);
        }
        return c;
    }
}

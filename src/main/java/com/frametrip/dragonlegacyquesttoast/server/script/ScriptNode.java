package com.frametrip.dragonlegacyquesttoast.server.script;

import java.util.ArrayList;
import java.util.List;

// [SCR-1]: Single node in the visual script graph.
public class ScriptNode {

    public String         id         = "";
    public ScriptNodeType type       = ScriptNodeType.ACTION;
    public float          posX       = 100f;
    public float          posY       = 100f;
    /** Enum name for sub-type: EventTriggerType / EventConditionType / EventActionType */
    public String         subType    = "";
    /** Single key for the primary parameter (e.g. "phrase", "itemId", "ticks"). */
    public String         paramKey   = "";
    public String         paramValue = "";
    /** IDs of nodes wired from this node's output port. */
    public List<String>   outputTo   = new ArrayList<>();

    public ScriptNode copy() {
        ScriptNode c = new ScriptNode();
        c.id         = this.id;
        c.type       = this.type;
        c.posX       = this.posX;
        c.posY       = this.posY;
        c.subType    = this.subType;
        c.paramKey   = this.paramKey;
        c.paramValue = this.paramValue;
        c.outputTo   = new ArrayList<>(this.outputTo);
        return c;
    }
}

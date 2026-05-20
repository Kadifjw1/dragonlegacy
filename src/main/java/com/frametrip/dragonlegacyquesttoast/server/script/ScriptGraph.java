package com.frametrip.dragonlegacyquesttoast.server.script;

import java.util.ArrayList;
import java.util.List;

// [SCR-1]: Named graph of script nodes; compiled to EventChain on save.
public class ScriptGraph {

    public String           name  = "Граф";
    public List<ScriptNode> nodes = new ArrayList<>();

    public ScriptNode findById(String id) {
        for (ScriptNode n : nodes) {
            if (id.equals(n.id)) return n;
        }
        return null;
    }

    public ScriptGraph copy() {
        ScriptGraph c = new ScriptGraph();
        c.name = this.name;
        for (ScriptNode n : this.nodes) c.nodes.add(n.copy());
        return c;
    }
}

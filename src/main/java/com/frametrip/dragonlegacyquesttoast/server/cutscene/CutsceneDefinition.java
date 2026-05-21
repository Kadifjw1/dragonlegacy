package com.frametrip.dragonlegacyquesttoast.server.cutscene;

import java.util.ArrayList;
import java.util.List;

public class CutsceneDefinition {
    public String id                   = "";
    public boolean disablePlayerControl = true;
    public List<CutsceneEvent> events  = new ArrayList<>();

    public CutsceneDefinition() {}
}

package com.frametrip.dragonlegacyquesttoast.server.cutscene;

import com.google.gson.JsonObject;

public class CutsceneEvent {
    public int    tick   = 0;
    public String type   = ""; // CAMERA_MOVE, NPC_MOVE, NPC_SAY, CAMERA_SHAKE, END_SCENE
    public JsonObject params = new JsonObject();

    public CutsceneEvent() {}
}

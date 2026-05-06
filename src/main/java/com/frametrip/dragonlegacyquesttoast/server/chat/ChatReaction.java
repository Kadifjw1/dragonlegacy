package com.frametrip.dragonlegacyquesttoast.server.chat;

public class ChatReaction {
    public ChatReactionType type  = ChatReactionType.SPEECH;
    /** Primary param: speech text, sceneId, questId, animId, soundId, stateKey, etc. */
    public String param  = "";
    /** Secondary param: nodeId for START_NODE; value for SET_STATE. */
    public String param2 = "";

    public ChatReaction copy() {
        ChatReaction c = new ChatReaction();
        c.type   = this.type;
        c.param  = this.param;
        c.param2 = this.param2;
        return c;
    }

package com.frametrip.dragonlegacyquesttoast.server.chat;

import java.util.ArrayList;
import java.util.List;

public class NpcChatConfig {
    public boolean enabled           = false;
    public float   radius            = 16f;
    public int     globalCooldownTicks = 100;
    public List<ChatTrigger> triggers = new ArrayList<>();

    public NpcChatConfig copy() {
        NpcChatConfig c = new NpcChatConfig();
        c.enabled             = this.enabled;
        c.radius              = this.radius;
        c.globalCooldownTicks = this.globalCooldownTicks;
        for (ChatTrigger t : triggers) c.triggers.add(t.copy());
        return c;
    }
}

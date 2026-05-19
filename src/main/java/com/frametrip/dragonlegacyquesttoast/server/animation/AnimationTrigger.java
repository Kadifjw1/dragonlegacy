package com.frametrip.dragonlegacyquesttoast.server.animation;

// [ANI-2]: Condition that forces a specific NPC animation when met server-side.
public class AnimationTrigger {

    public enum TriggerType {
        PLAYER_NEARBY("Игрок рядом"),    // param = range (blocks)
        TIME_OF_DAY("Время суток"),      // param = 0 day-only, 1 night-only
        WEATHER("Погода"),               // param = 0 rain, 1 clear
        HEALTH_BELOW("HP ниже %"),       // param = threshold (0-100)
        NEARBY_MOB("Моб рядом");         // param = range (blocks)

        private final String label;
        TriggerType(String label) { this.label = label; }
        public String label() { return label; }
    }

    public TriggerType type        = TriggerType.PLAYER_NEARBY;
    public float       param       = 8f;
    public String      targetAnimId = "";
    public boolean     enabled     = true;

    public AnimationTrigger copy() {
        AnimationTrigger c = new AnimationTrigger();
        c.type         = this.type;
        c.param        = this.param;
        c.targetAnimId = this.targetAnimId;
        c.enabled      = this.enabled;
        return c;
    }
}

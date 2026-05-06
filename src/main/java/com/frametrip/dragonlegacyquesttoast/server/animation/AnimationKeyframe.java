package com.frametrip.dragonlegacyquesttoast.server.animation;

public class AnimationKeyframe {
    public float tick;
    public float x, y, z;
    public String easing = "linear"; // linear, easeIn, easeOut, easeInOut, step

    public static final String[] EASING_IDS    = { "linear", "easeIn", "easeOut", "easeInOut", "step" };
    public static final String[] EASING_LABELS = { "Линейно", "Разгон", "Торможение", "Плавно", "Шаг" };

    public AnimationKeyframe() {}
    public AnimationKeyframe(float tick, float x, float y, float z) {
        this.tick = tick; this.x = x; this.y = y; this.z = z;
    }

    public AnimationKeyframe copy() {
        AnimationKeyframe c = new AnimationKeyframe(tick, x, y, z);
        c.easing = this.easing;
        return c;
    }
}

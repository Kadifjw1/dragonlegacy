package com.frametrip.dragonlegacyquesttoast.server.animation;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** In-memory library of NPC animations for the current session. */
public class NpcAnimationLibrary {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NpcAnimationLibrary.class);

    private static final Map<String, NpcAnimationData> LIBRARY = new LinkedHashMap<>();
    
    static {
        registerBuiltins();
    }

    private static void registerBuiltins() {
        register(builtin("builtin.idle",       "Idle",       AnimationState.IDLE,   40f, true));
        register(builtin("builtin.walk",       "Walk",       AnimationState.WALK,   20f, true));
        register(builtin("builtin.talk",       "Talk",       AnimationState.TALK,   30f, true));
        register(builtin("builtin.work",       "Work",       AnimationState.WORK,   40f, true));
        register(builtin("builtin.attack",     "Attack",     AnimationState.ATTACK, 15f, false));
        register(builtin("builtin.guard_idle", "Guard Idle", AnimationState.GUARD,  30f, true));
        register(builtin("builtin.sleep",      "Sleep",      AnimationState.SLEEP,  10f, false));
        register(builtin("builtin.sit",        "Sit",        AnimationState.SIT,    10f, false));
    }

    private static NpcAnimationData builtin(String id, String name, AnimationState state,
                                            float duration, boolean loop) {
        NpcAnimationData anim = new NpcAnimationData();
        anim.id           = id;
        anim.name         = name;
        anim.durationTicks = duration;
        anim.loop          = loop;
        anim.stateBinding  = state;
        anim.ensureBones();
        return anim;
    }

    public static void register(NpcAnimationData anim) {
        LIBRARY.put(anim.id, anim);
    }

    public static NpcAnimationData get(String id) {
        return LIBRARY.get(id);
    }

    public static List<NpcAnimationData> getAll() {
        return new ArrayList<>(LIBRARY.values());
    }

    public static void remove(String id) {
        LIBRARY.remove(id);
    }

    public static void clear() {
        LIBRARY.clear();
    }

    /** Returns animations bound to a specific state. */
    public static List<NpcAnimationData> getByState(AnimationState state) {
        return LIBRARY.values().stream()
                .filter(a -> a.stateBinding == state)
                .toList();
    }

    /** Get the first animation for a state, or null. */
    public static NpcAnimationData getDefaultForState(AnimationState state) {
        return LIBRARY.values().stream()
                .filter(a -> a.stateBinding == state)
                .findFirst()
                .orElse(null);
    }

    public static int size() {
        return LIBRARY.size();
    }
}


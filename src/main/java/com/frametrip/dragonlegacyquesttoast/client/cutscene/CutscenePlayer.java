package com.frametrip.dragonlegacyquesttoast.client.cutscene;

import com.frametrip.dragonlegacyquesttoast.server.cutscene.CutsceneDefinition;
import com.frametrip.dragonlegacyquesttoast.server.cutscene.CutsceneEvent;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// [VFX-3]: Client-side cutscene player. Ticked in DragonLegacyQuestToastMod client tick.
public class CutscenePlayer {

    private static CutsceneDefinition active;
    private static int  currentTick  = 0;
    private static int  nextEventIdx = 0;
    private static boolean playing   = false;

    // Camera shake state
    private static float shakeIntensity = 0f;
    private static int   shakeTicks     = 0;

    public static void start(CutsceneDefinition def) {
        active       = def;
        currentTick  = 0;
        nextEventIdx = 0;
        playing      = true;
        shakeIntensity = 0f;
        shakeTicks     = 0;

        if (def.disablePlayerControl) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) mc.player.input = new FrozenInput();
        }
    }

    public static void stop() {
        playing = false;
        active  = null;
        shakeIntensity = 0f;
        shakeTicks     = 0;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.input instanceof FrozenInput) {
            mc.player.input = new net.minecraft.client.player.KeyboardInput(mc.options);
        }
    }

    public static boolean isPlaying() { return playing; }

    // Returns current shake offset (X) for camera rendering
    public static float getShakeOffsetX() {
        if (shakeTicks <= 0) return 0f;
        return (float) (Math.sin(currentTick * 1.3) * shakeIntensity);
    }

    public static void tick() {
        if (!playing || active == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            stop();
            return;
        }

        // Fire all events at current tick
        List<CutsceneEvent> events = active.events;
        while (nextEventIdx < events.size() && events.get(nextEventIdx).tick <= currentTick) {
            processEvent(events.get(nextEventIdx), mc);
            nextEventIdx++;
        }

        // Decay shake
        if (shakeTicks > 0) shakeTicks--;
        if (shakeTicks == 0) shakeIntensity = 0f;

        currentTick++;
    }

    private static void processEvent(CutsceneEvent ev, Minecraft mc) {
        JsonObject p = ev.params != null ? ev.params : new JsonObject();
        switch (ev.type) {
            case "CAMERA_MOVE" -> {
                double x   = p.has("x")   ? p.get("x").getAsDouble()   : mc.player.getX();
                double y   = p.has("y")   ? p.get("y").getAsDouble()   : mc.player.getY();
                double z   = p.has("z")   ? p.get("z").getAsDouble()   : mc.player.getZ();
                float  yaw = p.has("yaw") ? p.get("yaw").getAsFloat()  : mc.player.getYRot();
                float  pit = p.has("pitch") ? p.get("pitch").getAsFloat() : mc.player.getXRot();
                mc.player.moveTo(x, y, z, yaw, pit);
            }
            case "NPC_SAY" -> {
                String text = p.has("text") ? p.get("text").getAsString() : "";
                mc.player.sendSystemMessage(Component.literal("§d[Кат-сцена] §f" + text));
            }
            case "CAMERA_SHAKE" -> {
                shakeIntensity = p.has("intensity") ? p.get("intensity").getAsFloat() : 0.3f;
                shakeTicks     = p.has("duration")  ? p.get("duration").getAsInt()    : 20;
            }
            case "END_SCENE" -> stop();
            // NPC_MOVE: server-side; client just ignores it
        }
    }
}

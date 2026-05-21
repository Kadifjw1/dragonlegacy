package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntity;
import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.List;

// [EDT-3]: HUD overlay showing AI debug info for NPCs with debugAi = true.
public class NpcAiDebugOverlay {

    public static final IGuiOverlay OVERLAY = NpcAiDebugOverlay::render;

    private static void render(ForgeGui gui, GuiGraphics g, float pt, int screenW, int screenH) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (mc.options.renderDebug) return; // don't overlap vanilla F3 debug

        List<NpcEntity> debugNpcs = mc.level.getEntitiesOfClass(NpcEntity.class,
                mc.player.getBoundingBox().inflate(32),
                npc -> {
                    NpcEntityData d = npc.getNpcData();
                    return d != null && d.debugAi;
                });

        if (debugNpcs.isEmpty()) return;

        // Find nearest
        NpcEntity nearest = debugNpcs.stream()
                .min(java.util.Comparator.comparingDouble(n -> n.distanceTo(mc.player)))
                .orElse(null);
        if (nearest == null) return;

        NpcEntityData data = nearest.getNpcData();
        Font font = mc.font;

        int panelX = 4;
        int panelY = 4;
        int panelW = 180;
        int lineH  = 10;

        String[] lines = buildDebugLines(nearest, data);
        int panelH = lines.length * lineH + 8;

        g.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xAA000022);
        g.fill(panelX, panelY, panelX + panelW, panelY + 12, 0xBB003366);
        g.drawString(font, "§b[DEBUG] §f" + data.displayName, panelX + 4, panelY + 2, 0xFFFFFFFF, false);

        for (int i = 0; i < lines.length; i++) {
            g.drawString(font, lines[i], panelX + 4, panelY + 12 + i * lineH, 0xFFCCCCCC, false);
        }
    }

    private static String[] buildDebugLines(NpcEntity npc, NpcEntityData data) {
        String target = "—";
        LivingEntity t = npc.getTarget();
        if (t != null) {
            target = t.getClass().getSimpleName() + " @" +
                    String.format("%.0f,%.0f,%.0f", t.getX(), t.getY(), t.getZ());
        }
        Vec3 pos = npc.position();
        float hp    = npc.getHealth();
        float maxHp = npc.getMaxHealth();
        String mood = data.immersionData != null ? String.valueOf(data.immersionData.mood) : "?";

        return new String[] {
            "§7Pos:    §f" + String.format("%.1f, %.1f, %.1f", pos.x, pos.y, pos.z),
            "§7HP:     §f" + String.format("%.0f / %.0f", hp, maxHp),
            "§7Target: §f" + target,
            "§7Mood:   §f" + mood,
            "§7Relate: §f" + data.playerRelation,
            "§7Guard:  §f" + (data.guardTerritoryEnabled ? "§aвкл." : "§8выкл."),
            "§7Farmer: §f" + (data.farmerData != null && data.farmerData.farmerEnabled ? "§aвкл." : "§8выкл."),
        };
    }
}

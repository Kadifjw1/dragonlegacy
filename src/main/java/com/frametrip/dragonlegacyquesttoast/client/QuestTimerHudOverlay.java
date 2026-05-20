package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.server.QuestDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.Map;

// [QST-3]: HUD overlay showing countdown timers for active timed quests.
public class QuestTimerHudOverlay {

    public static final IGuiOverlay OVERLAY = (gui, g, partialTick, screenWidth, screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        Map<String, Long> deadlines = ClientQuestDeadlineState.getAll();
        if (deadlines.isEmpty()) return;

        int x = 4;
        int y = screenHeight / 2 - 10;
        int lineH = 11;
        int drawn = 0;

        for (Map.Entry<String, Long> entry : deadlines.entrySet()) {
            int remaining = ClientQuestDeadlineState.getRemainingSeconds(entry.getKey());
            if (remaining < 0) continue;

            // Look up title from client quest cache.
            String title = entry.getKey();
            for (QuestDefinition def : ClientQuestState.getAll()) {
                if (def.id.equals(entry.getKey())) {
                    title = def.title != null ? def.title : entry.getKey();
                    break;
                }
            }

            int mins = remaining / 60;
            int secs = remaining % 60;
            String timeStr = String.format("%d:%02d", mins, secs);
            int color = remaining > 60 ? 0xFFFFFF55 : remaining > 10 ? 0xFFFF9900 : 0xFFFF3333;

            // Translucent background strip.
            g.fill(x - 1, y + drawn * lineH - 1,
                   x + 120, y + drawn * lineH + 9, 0x55000000);
            g.drawString(mc.font, "§7⏱ " + title + " §r" + timeStr,
                         x, y + drawn * lineH, color, true);
            drawn++;
            if (drawn >= 5) break; // cap at 5 timers visible
        }
    };
}

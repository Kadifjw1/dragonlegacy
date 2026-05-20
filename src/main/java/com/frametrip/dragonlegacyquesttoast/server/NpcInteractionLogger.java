package com.frametrip.dragonlegacyquesttoast.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

// [SRV-4]: Logs NPC interaction events to a CSV file in the world folder.
public class NpcInteractionLogger {

    private static final Logger LOG = LoggerFactory.getLogger(NpcInteractionLogger.class);
    private static final String LOG_FILE_NAME = "dragonlegacyquesttoast-interactions.csv";

    // CSV header: timestamp,playerName,playerUUID,npcName,npcUUID,action
    private static final String HEADER = "timestamp,playerName,playerUUID,npcName,npcUUID,action\n";

    public static void log(ServerPlayer player, String npcName, UUID npcUuid, String action) {
        Path logPath = resolveLogPath();
        if (logPath == null) return;
        try {
            boolean exists = Files.exists(logPath);
            String line = String.format("%s,%s,%s,%s,%s,%s\n",
                    Instant.now(),
                    escape(player.getGameProfile().getName()),
                    player.getStringUUID(),
                    escape(npcName),
                    npcUuid,
                    action);
            if (!exists) {
                Files.writeString(logPath, HEADER + line,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } else {
                Files.writeString(logPath, line,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            LOG.warn("[SRV-4] Failed to write interaction log: {}", e.getMessage());
        }
    }

    // Returns the last `count` log lines matching `npcName` (case-insensitive).
    public static List<String> getLogs(String npcName, int count) {
        Path logPath = resolveLogPath();
        if (logPath == null || !Files.exists(logPath)) return Collections.emptyList();
        try {
            List<String> all = Files.readAllLines(logPath);
            List<String> matching = new ArrayList<>();
            String nameLower = npcName.toLowerCase();
            for (String line : all) {
                if (line.startsWith("timestamp")) continue; // skip header
                // npcName is the 4th column (index 3)
                String[] parts = line.split(",", -1);
                if (parts.length >= 5 && parts[3].toLowerCase().contains(nameLower)) {
                    matching.add(line);
                }
            }
            int from = Math.max(0, matching.size() - count);
            return matching.subList(from, matching.size());
        } catch (IOException e) {
            LOG.warn("[SRV-4] Failed to read interaction log: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private static Path resolveLogPath() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;
        File worldDir = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).toFile();
        return new File(worldDir, LOG_FILE_NAME).toPath();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace(",", " ").replace("\n", " ");
    }
}

package com.frametrip.dragonlegacyquesttoast.server.integration;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * [INT-API-4]: Sends async Discord webhook notifications for key mod events.
 * Never blocks the game thread — always uses sendAsync.
 */
public class DiscordWebhook {

    private static final Logger LOGGER = LogManager.getLogger(DragonLegacyQuestToastMod.MODID);
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    public static void send(String message) {
        String url = IntegrationConfigManager.get().discordWebhookUrl;
        if (url == null || url.isBlank()) return;
        send(message, url);
    }

    public static void send(String message, String webhookUrl) {
        if (webhookUrl == null || webhookUrl.isBlank()) return;
        String sanitized = message.replace("\\", "\\\\").replace("\"", "\\\"");
        String body = "{\"content\":\"" + sanitized + "\"}";
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HTTP.sendAsync(req, HttpResponse.BodyHandlers.discarding())
                    .exceptionally(ex -> {
                        LOGGER.debug("Discord webhook failed: {}", ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            LOGGER.warn("Discord webhook error: {}", e.getMessage());
        }
    }

    // ── Convenience helpers ───────────────────────────────────────────────────

    public static void notifyNpcDeath(String npcName, String killerName) {
        if (!IntegrationConfigManager.get().discordNotifyNpcDeath) return;
        send("💀 NPC **" + npcName + "** был убит игроком **" + killerName + "**");
    }

    public static void notifyBossKill(String bossName, String killerName) {
        if (!IntegrationConfigManager.get().discordNotifyBossKill) return;
        send("🏆 Босс **" + bossName + "** повержен игроком **" + killerName + "**!");
    }

    public static void notifyQuestGiven(String playerName, String npcName, String questName) {
        if (!IntegrationConfigManager.get().discordNotifyQuest) return;
        send("📜 **" + playerName + "** получил квест «" + questName + "» от **" + npcName + "**");
    }
}

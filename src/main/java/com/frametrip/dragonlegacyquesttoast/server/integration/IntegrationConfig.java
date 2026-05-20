package com.frametrip.dragonlegacyquesttoast.server.integration;

public class IntegrationConfig {

    // [INT-API-1]
    public boolean usePlaceholderApi = true;

    // [INT-API-2]
    public boolean useLuckPerms = true;

    // [INT-API-3]
    public boolean useVault = true;

    // [INT-API-4]: Discord webhook
    public String  discordWebhookUrl    = "";
    public boolean discordNotifyNpcDeath = false;
    public boolean discordNotifyQuest    = false;
    public boolean discordNotifyBossKill = true;

    // Performance
    public int     maxAiTicksPerFrame   = 10;
    public boolean logInteractions      = false;
    public int     cutsceneRenderDist   = 64;
}

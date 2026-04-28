package com.frametrip.dragonlegacyquesttoast.client.dialogue;

import com.frametrip.dragonlegacyquesttoast.client.ClientNpcDialogueManager;
import com.frametrip.dragonlegacyquesttoast.client.ClientPlayerAbilityState;
import com.frametrip.dragonlegacyquesttoast.client.ClientQuestProgressState;
import com.frametrip.dragonlegacyquesttoast.network.ModNetwork;
import com.frametrip.dragonlegacyquesttoast.network.RequestOpenNpcShopPacket;
import com.frametrip.dragonlegacyquesttoast.network.QuestStateActionPacket;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcChoiceOption;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcScene;
import com.frametrip.dragonlegacyquesttoast.server.dialogue.NpcSceneNode;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Client-side runtime controller for NPC scenes.
 * Processes nodes in order: speech → overlay, question → NpcChoiceScreen,
 * action → auto-execute, condition → branch, end → finish.
 *
 * Supports a preview mode where destructive side-effects (give_quest, etc.)
 * are replaced with informational overlays — for the editor's "Preview scene".
 */
public class NpcSceneController {

    private static String  currentNpcName = "";
    private static NpcScene currentScene  = null;
    private static String  pendingNodeId  = null;
    private static boolean previewMode    = false;
    private static String currentRelation = "NEUTRAL";
    private static UUID currentNpcUuid = null;
    private static final java.util.Set<String> talkedSceneKeys = new java.util.HashSet<>();
    
    private NpcSceneController() {}

    public static void startScene(String npcName, String sceneId) {
        NpcScene scene = ClientNpcSceneState.get(sceneId);
        if (scene == null) return;
         startScene(npcName, scene, scene.startNodeId, false, "NEUTRAL");
    }

    public static void startScene(String npcName, String sceneId, String relation) {
        startScene(npcName, sceneId, relation, null);
    }

    public static void startScene(String npcName, String sceneId, String relation, UUID npcUuid) {
        NpcScene scene = ClientNpcSceneState.get(sceneId);
        if (scene == null) return;
        startScene(npcName, scene, scene.startNodeId, false, relation, npcUuid);
    }

    /** Start a (possibly unsaved) scene from the given node, optionally in preview mode. */
    public static void startScene(String npcName, NpcScene scene, String startNodeId, boolean preview) {
        startScene(npcName, scene, startNodeId, preview, "NEUTRAL");
    }

    public static void startScene(String npcName, NpcScene scene, String startNodeId, boolean preview,
                                  String relation) {
        startScene(npcName, scene, startNodeId, preview, relation, null);
    }

    public static void startScene(String npcName, NpcScene scene, String startNodeId, boolean preview,
                                  String relation, UUID npcUuid) {
        if (scene == null) return;
        finish();
        currentNpcName = npcName == null ? "" : npcName;        
        currentScene   = scene;
        previewMode    = preview;
        currentRelation = relation == null ? "NEUTRAL" : relation.toUpperCase();
        currentNpcUuid = npcUuid;
        String start = (startNodeId == null || startNodeId.isEmpty()) ? scene.startNodeId : startNodeId;
        processNode(start);    
    }

    public static void processNode(String nodeId) {
        if (currentScene == null) return;
        if (nodeId == null || nodeId.isEmpty()) { finish(); return; }

        NpcSceneNode node = currentScene.getNode(nodeId);
        if (node == null) { finish(); return; }

        switch (node.type) {
            case NpcSceneNode.TYPE_SPEECH    -> processSpeech(node);
            case NpcSceneNode.TYPE_QUESTION  -> processQuestion(node);
            case NpcSceneNode.TYPE_ACTION    -> processAction(node);
            case NpcSceneNode.TYPE_CONDITION -> processCondition(node);
            case NpcSceneNode.TYPE_DELAY     -> processDelay(node);
            case NpcSceneNode.TYPE_BRANCH    -> processBranch(node);
            case NpcSceneNode.TYPE_END       -> finish();
            default                          -> finish();
        }
    }

    private static void processSpeech(NpcSceneNode node) {
         String speaker = (node.speakerName != null && !node.speakerName.isBlank())
                ? node.speakerName : currentNpcName;
        ClientNpcDialogueManager.show(speaker, node.text);

        if (node.soundId != null && !node.soundId.isBlank()) playSound(node.soundId);
        else playSound("minecraft:block.note_block.hat");
        if (node.animationId != null && !node.animationId.isBlank()) {
            ClientNpcDialogueManager.show("[анимация]", node.animationId);
        }

        pendingNodeId = node.nextNodeId;
        schedulePending();
    }

    private static void processQuestion(NpcSceneNode node) {
        ClientNpcDialogueManager.clear();
        String speaker = (node.speakerName != null && !node.speakerName.isBlank())
                ? node.speakerName : currentNpcName;
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new NpcChoiceScreen(speaker, node, NpcSceneController::onChoicePicked));
    }

    private static void onChoicePicked(String choiceId) {
        if (currentScene == null) { finish(); return; }
        // Find the choice in the scene by id (UUID-based) and execute its optional action.
        NpcChoiceOption picked = null;
        for (NpcSceneNode n : currentScene.nodes) {
            if (!NpcSceneNode.TYPE_QUESTION.equals(n.type) || n.choices == null) continue;
            for (NpcChoiceOption o : n.choices) {
                if (choiceId != null && choiceId.equals(o.id)) { picked = o; break; }
            }
            if (picked != null) break;
        }

        if (picked == null) { processNode(choiceId); return; } // fallback: treat as nextNodeId

        if (picked.actionType != null && !picked.actionType.isBlank()) {
            executeAction(picked.actionType, picked.actionParam);
        }
        processNode(picked.nextNodeId);
    }

    private static void processAction(NpcSceneNode node) {
        executeAction(node.actionType, node.actionParam);
        if (NpcSceneNode.ACTION_CLOSE_SCENE.equals(node.actionType)) { finish(); return; }
        if (NpcSceneNode.ACTION_OPEN_SCENE.equals(node.actionType)) {
            NpcScene target = ClientNpcSceneState.get(node.actionParam);
            if (target != null) { currentScene = target; processNode(target.startNodeId); return; }
        }
        processNode(node.actionNextNodeId);
    }

    private static void processCondition(NpcSceneNode node) {
        boolean result = evaluateCondition(node.conditionType, node.conditionParam);
        processNode(result ? node.trueNextNodeId : node.falseNextNodeId);
    }

    
    private static void processDelay(NpcSceneNode node) {
        int delay = Math.max(0, node.delayTicks);
        if (delay <= 0) {
            processNode(node.nextNodeId);
            return;
        }
        pendingNodeId = node.nextNodeId;
        NpcSceneTickHandler.scheduleDeferredNode(pendingNodeId, delay);
        pendingNodeId = null;
    }

    private static void processBranch(NpcSceneNode node) {
        if (node.branchOptions == null || node.branchOptions.isEmpty()) {
            finish();
            return;
        }
        int total = 0;
        for (NpcChoiceOption option : node.branchOptions) {
            int weight = parseWeight(option.actionParam);
            total += Math.max(1, weight);
        }
        int roll = Minecraft.getInstance().level == null
                ? new java.util.Random().nextInt(Math.max(1, total))
                : Minecraft.getInstance().level.random.nextInt(Math.max(1, total));
        for (NpcChoiceOption option : node.branchOptions) {
            roll -= Math.max(1, parseWeight(option.actionParam));
            if (roll < 0) {
                processNode(option.nextNodeId);
                return;
            }
        }
        processNode(node.branchOptions.get(0).nextNodeId);
    }

    private static int parseWeight(String raw) {
        try { return Integer.parseInt(raw == null || raw.isBlank() ? "1" : raw.trim()); }
        catch (Exception ignored) { return 1; }
    }
    
    // ── Condition evaluator (client-side approximations) ────────────────────

    private static boolean evaluateCondition(String type, String param) {
        if (type == null || type.isBlank()) return true;
        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        Level lvl = mc.level;
        return switch (type) {
            case NpcSceneNode.COND_TIME_DAY   -> lvl != null && lvl.isDay();
            case NpcSceneNode.COND_TIME_NIGHT -> lvl != null && lvl.isNight();
            case NpcSceneNode.COND_HAS_ITEM   -> p != null && playerHasItem(p, param);
            case NpcSceneNode.COND_NOT_HAS_ITEM -> p == null || !playerHasItem(p, param);
            case NpcSceneNode.COND_QUEST_ACTIVE -> ClientQuestProgressState.isActive(param);
            case NpcSceneNode.COND_QUEST_COMPLETE -> ClientQuestProgressState.isComplete(param);
            case NpcSceneNode.COND_QUEST_NOT_TAKEN ->
                    !ClientQuestProgressState.isActive(param)
                            && !ClientQuestProgressState.isComplete(param)
                            && !ClientQuestProgressState.isFailed(param);
            case NpcSceneNode.COND_RELATION ->
                    param != null && !param.isBlank() && currentRelation.equalsIgnoreCase(param.trim());
            case NpcSceneNode.COND_HAS_ABILITY -> ClientPlayerAbilityState.hasAbility(param);
            case NpcSceneNode.COND_FIRST_TALK -> !talkedSceneKeys.contains(sceneTalkKey());
            case NpcSceneNode.COND_RE_TALK    -> talkedSceneKeys.contains(sceneTalkKey());
            // Faction/path stage are server-authoritative; assume true.
            default -> true;
        };
    }

    private static boolean playerHasItem(Player p, String itemId) {
        if (itemId == null || itemId.isBlank()) return false;
        ResourceLocation rl = tryParse(itemId);
        if (rl == null) return false;
        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(rl);
        if (item == null) return false;
        return p.getInventory().contains(new net.minecraft.world.item.ItemStack(item));
    }

    // ── Action executor ─────────────────────────────────────────────────────

    private static void executeAction(String actionType, String actionParam) {
        if (actionType == null || actionType.isBlank()) return;
        // In preview mode all destructive actions become informational overlays only.
        if (previewMode) {
            String label = switch (actionType) {
                case NpcSceneNode.ACTION_GIVE_QUEST           -> "⏵ Выдан квест";
                case NpcSceneNode.ACTION_COMPLETE_QUEST       -> "⏵ Квест выполнен";
                case NpcSceneNode.ACTION_FAIL_QUEST           -> "⏵ Квест провален";
                case NpcSceneNode.ACTION_SET_RELATION         -> "⏵ Отношение изменено";
                case NpcSceneNode.ACTION_SET_FACTION_RELATION -> "⏵ Отношение фракции";
                case NpcSceneNode.ACTION_GIVE_ITEM            -> "⏵ Выдан предмет";
                case NpcSceneNode.ACTION_TAKE_ITEM            -> "⏵ Забран предмет";
                case NpcSceneNode.ACTION_PLAY_SOUND           -> "⏵ Звук";
                case NpcSceneNode.ACTION_PLAY_ANIMATION       -> "⏵ Анимация";
                case NpcSceneNode.ACTION_LOOK_AT              -> "⏵ LookAt";
                case NpcSceneNode.ACTION_MOVE_TO              -> "⏵ Move";
                case NpcSceneNode.ACTION_CAMERA               -> "⏵ Камера";
                case NpcSceneNode.ACTION_EFFECT               -> "⏵ Эффект";
                case NpcSceneNode.ACTION_EMOTE                -> "⏵ Эмоция";
                case NpcSceneNode.ACTION_TELEPORT             -> "⏵ Телепорт";
                case NpcSceneNode.ACTION_SET_VARIABLE         -> "⏵ Переменная";
                case NpcSceneNode.ACTION_OPEN_SCENE           -> "⏵ Переход в сцену";
                case NpcSceneNode.ACTION_CLOSE_SCENE          -> "⏵ Завершение сцены";
                default                                       -> "⏵ " + actionType;
            };
            String extra = (actionParam == null || actionParam.isBlank()) ? "" : ": " + actionParam;
            ClientNpcDialogueManager.show("[предпросмотр]", label + extra);
            return;
        }
        
        switch (actionType) {
            case NpcSceneNode.ACTION_GIVE_QUEST -> {
                ModNetwork.CHANNEL.sendToServer(new QuestStateActionPacket(QuestStateActionPacket.ACTION_ACCEPT, actionParam));
                ClientNpcDialogueManager.show("", labelFor(actionType) + ": " + actionParam);
            }
            case NpcSceneNode.ACTION_COMPLETE_QUEST -> {
                ModNetwork.CHANNEL.sendToServer(new QuestStateActionPacket(QuestStateActionPacket.ACTION_COMPLETE, actionParam));
                ClientNpcDialogueManager.show("", labelFor(actionType) + ": " + actionParam);
            }
            case NpcSceneNode.ACTION_FAIL_QUEST -> {
                ModNetwork.CHANNEL.sendToServer(new QuestStateActionPacket(QuestStateActionPacket.ACTION_FAIL, actionParam));
                ClientNpcDialogueManager.show("", labelFor(actionType) + ": " + actionParam);
            }
            case NpcSceneNode.ACTION_OPEN_SHOP -> {
                if (currentNpcUuid != null) {
                    ModNetwork.CHANNEL.sendToServer(new RequestOpenNpcShopPacket(currentNpcUuid));
                }
            }
            case NpcSceneNode.ACTION_SET_RELATION, NpcSceneNode.ACTION_SET_FACTION_RELATION,
                 NpcSceneNode.ACTION_GIVE_ITEM, NpcSceneNode.ACTION_TAKE_ITEM ->
                    ClientNpcDialogueManager.show("", labelFor(actionType) + ": " + actionParam);
            case NpcSceneNode.ACTION_PLAY_SOUND -> playSound(actionParam);
            case NpcSceneNode.ACTION_PLAY_ANIMATION ->
                    ClientNpcDialogueManager.show("", "Анимация: " + actionParam);
            case NpcSceneNode.ACTION_LOOK_AT, NpcSceneNode.ACTION_MOVE_TO,
                 NpcSceneNode.ACTION_CAMERA, NpcSceneNode.ACTION_EFFECT,
                 NpcSceneNode.ACTION_EMOTE, NpcSceneNode.ACTION_TELEPORT,
                 NpcSceneNode.ACTION_SET_VARIABLE ->
                    ClientNpcDialogueManager.show("", labelFor(actionType) + ": " + actionParam);
            // OPEN_SCENE / CLOSE_SCENE are handled in processAction() flow.
            default -> {}
        }
    }

    private static String labelFor(String actionType) {
        return NpcSceneNode.actionLabel(actionType);
    }

    private static void playSound(String soundId) {
        if (soundId == null || soundId.isBlank()) return;
        ResourceLocation rl = tryParse(soundId);
        if (rl == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        SoundEvent evt = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(rl);
        if (evt == null) return;
        BlockPos pos = mc.player.blockPosition();
        mc.level.playLocalSound(pos, evt, SoundSource.NEUTRAL, 1.0f, 1.0f, false);
    }

    private static ResourceLocation tryParse(String s) {
        try { return new ResourceLocation(s); } catch (Exception e) { return null; }
    }

    /** Called after overlay finishes ticking to proceed to the next pending node. */
    private static void schedulePending() {
        if (pendingNodeId == null) return;
        String next = pendingNodeId;
        pendingNodeId = null;
       if (next.isEmpty()) { finish(); return; }

        NpcScene scene = currentScene;
        NpcSceneNode nextNode = scene.getNode(next);
        if (nextNode == null) { finish(); return; }
        int delay = nextNode.type.equals(NpcSceneNode.TYPE_SPEECH) ? Math.max(0, nextNode.speechDelayTicks) : 0;
        if (nextNode.type.equals(NpcSceneNode.TYPE_SPEECH) && delay <= 0) {
            processNode(next);
        } else {
        NpcSceneTickHandler.scheduleDeferredNode(next, delay);
        }
    }

    private static void finish() {
        if (!previewMode && currentScene != null && currentScene.id != null && !currentScene.id.isBlank()) {
            talkedSceneKeys.add(sceneTalkKey());
        }
        ClientNpcDialogueManager.clear();
        NpcSceneTickHandler.clearScheduled();
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof NpcChoiceScreen) {
            mc.setScreen(null);
        }
        currentScene   = null;
        currentNpcName = "";
        pendingNodeId  = null;
        previewMode    = false;
        currentRelation = "NEUTRAL";
        currentNpcUuid = null;
    }

    public static boolean isActive() {
        return currentScene != null;
    }
    
    public static boolean isPreview() {
        return previewMode;
    }
    
    private static String sceneTalkKey() {
        return currentNpcName + "|" + (currentScene == null ? "" : currentScene.id);
    }
}

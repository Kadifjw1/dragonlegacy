package com.frametrip.dragonlegacyquesttoast.server.dialogue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Static diagnostics for a scene graph. */
public final class NpcSceneValidator {

    public enum Level { ERROR, WARN }

    public static class Issue {
        public final Level level;
        public final String nodeId;   // may be empty for scene-level issues
        public final String message;
        public Issue(Level l, String nodeId, String msg) {
            this.level = l; this.nodeId = nodeId == null ? "" : nodeId; this.message = msg;
        }
    }

    private NpcSceneValidator() {}

    public static List<Issue> validate(NpcScene scene) {
        List<Issue> out = new ArrayList<>();
        if (scene == null) { out.add(new Issue(Level.ERROR, "", "Сцена пустая")); return out; }

        if (scene.name == null || scene.name.isBlank())
            out.add(new Issue(Level.WARN, "", "У сцены нет имени"));

        if (scene.nodes == null || scene.nodes.isEmpty()) {
            out.add(new Issue(Level.ERROR, "", "В сцене нет узлов"));
            return out;
        }

        Set<String> ids = new HashSet<>();
        for (NpcSceneNode n : scene.nodes) ids.add(n.id);

        if (scene.startNodeId == null || scene.startNodeId.isEmpty()) {
            out.add(new Issue(Level.ERROR, "", "Не задан стартовый узел"));
        } else if (!ids.contains(scene.startNodeId)) {
            out.add(new Issue(Level.ERROR, "", "Стартовый узел «" + scene.startNodeId + "» не существует"));
        }

        // Track which nodes are reachable from start
        Set<String> reachable = new HashSet<>();
        if (scene.startNodeId != null && ids.contains(scene.startNodeId)) {
            reach(scene, scene.startNodeId, reachable);
        }

        for (NpcSceneNode n : scene.nodes) {
            validateNode(scene, n, ids, out);
            if (!reachable.contains(n.id) && !n.id.equals(scene.startNodeId)) {
                out.add(new Issue(Level.WARN, n.id, "Узел недостижим из стартового"));
            }
        }
    return out;
    }

    private static void reach(NpcScene s, String nodeId, Set<String> acc) {
        if (nodeId == null || nodeId.isEmpty() || acc.contains(nodeId)) return;
        NpcSceneNode n = s.getNode(nodeId);
        if (n == null) return;
        acc.add(nodeId);
        switch (n.type) {
            case NpcSceneNode.TYPE_SPEECH    -> reach(s, n.nextNodeId, acc);
            case NpcSceneNode.TYPE_ACTION    -> reach(s, n.actionNextNodeId, acc);
            case NpcSceneNode.TYPE_CONDITION -> {
                reach(s, n.trueNextNodeId, acc);
                reach(s, n.falseNextNodeId, acc);
            }
            case NpcSceneNode.TYPE_QUESTION  -> {
                if (n.choices != null) for (NpcChoiceOption o : n.choices) reach(s, o.nextNodeId, acc);
            }
            // TYPE_END — terminal
        }
    }

    private static void validateNode(NpcScene s, NpcSceneNode n, Set<String> ids, List<Issue> out) {
        switch (n.type) {
            case NpcSceneNode.TYPE_SPEECH -> {
                if (n.text == null || n.text.isBlank())
                    out.add(new Issue(Level.WARN, n.id, "Пустой текст у фразы"));
                if (!n.nextNodeId.isEmpty() && !ids.contains(n.nextNodeId))
                    out.add(new Issue(Level.ERROR, n.id, "Следующий узел «" + n.nextNodeId + "» не существует"));
            }
            case NpcSceneNode.TYPE_QUESTION -> {
                if (n.text == null || n.text.isBlank())
                    out.add(new Issue(Level.WARN, n.id, "Пустой текст вопроса"));
                if (n.choices == null || n.choices.isEmpty()) {
                    out.add(new Issue(Level.ERROR, n.id, "У вопроса нет ответов"));
                } else {
                    for (int i = 0; i < n.choices.size(); i++) {
                        NpcChoiceOption o = n.choices.get(i);
                        if (o.text == null || o.text.isBlank())
                            out.add(new Issue(Level.WARN, n.id, "Ответ #" + (i + 1) + ": пустой текст"));
                        if (!o.nextNodeId.isEmpty() && !ids.contains(o.nextNodeId))
                            out.add(new Issue(Level.ERROR, n.id, "Ответ #" + (i + 1) + ": переход в несуществующий узел"));
                    }
                }
            }
            case NpcSceneNode.TYPE_ACTION -> {
                if (n.actionType == null || n.actionType.isBlank())
                    out.add(new Issue(Level.ERROR, n.id, "Тип действия не задан"));
                if (requiresParam(n.actionType) && (n.actionParam == null || n.actionParam.isBlank()))
                    out.add(new Issue(Level.ERROR, n.id, "Параметр действия обязателен"));
                if (!n.actionNextNodeId.isEmpty() && !ids.contains(n.actionNextNodeId))
                    out.add(new Issue(Level.ERROR, n.id, "Следующий узел после действия не существует"));
            }
            case NpcSceneNode.TYPE_CONDITION -> {
                if (n.conditionType == null || n.conditionType.isBlank())
                    out.add(new Issue(Level.ERROR, n.id, "Тип условия не задан"));
                if (n.trueNextNodeId.isEmpty() && n.falseNextNodeId.isEmpty())
                    out.add(new Issue(Level.ERROR, n.id, "Условие без переходов"));
                if (!n.trueNextNodeId.isEmpty() && !ids.contains(n.trueNextNodeId))
                    out.add(new Issue(Level.ERROR, n.id, "Ветка TRUE ведёт в несуществующий узел"));
                if (!n.falseNextNodeId.isEmpty() && !ids.contains(n.falseNextNodeId))
                    out.add(new Issue(Level.ERROR, n.id, "Ветка FALSE ведёт в несуществующий узел"));
            }
            case NpcSceneNode.TYPE_END -> { /* terminal */ }
            default -> out.add(new Issue(Level.ERROR, n.id, "Неизвестный тип узла: " + n.type));
        }
    }

    private static boolean requiresParam(String actionType) {
        if (actionType == null) return false;
        return switch (actionType) {
            case NpcSceneNode.ACTION_GIVE_QUEST,
                 NpcSceneNode.ACTION_COMPLETE_QUEST,
                 NpcSceneNode.ACTION_FAIL_QUEST,
                 NpcSceneNode.ACTION_SET_RELATION,
                 NpcSceneNode.ACTION_SET_FACTION_RELATION,
                 NpcSceneNode.ACTION_GIVE_ITEM,
                 NpcSceneNode.ACTION_TAKE_ITEM,
                 NpcSceneNode.ACTION_PLAY_SOUND,
                 NpcSceneNode.ACTION_PLAY_ANIMATION,
                 NpcSceneNode.ACTION_OPEN_SCENE -> true;
            default -> false;
        };
    }
}

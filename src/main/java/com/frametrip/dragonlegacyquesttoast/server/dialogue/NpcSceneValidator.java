package com.frametrip.dragonlegacyquesttoast.server.dialogue;

import java.util.ArrayDeque;
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

        if (!scene.allowCycles && hasCycle(scene)) {
            out.add(new Issue(Level.ERROR, "", "Обнаружены циклы, но они запрещены в настройках сцены"));
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
            if (!scene.allowCycles && hasCycle(scene)) {
            out.add(new Issue(Level.ERROR, "", "Обнаружены циклы, но они запрещены в настройках сцены"));
            }
        }
        
   return out;
    }

    private static boolean hasCycle(NpcScene scene) {
        Set<String> visited = new HashSet<>();
        Set<String> stack = new HashSet<>();
        for (NpcSceneNode node : scene.nodes) {
            if (dfsCycle(scene, node.id, visited, stack)) return true;
        }
        return false;
    }

    private static boolean dfsCycle(NpcScene scene, String nodeId, Set<String> visited, Set<String> stack) {
        if (stack.contains(nodeId)) return true;
        if (visited.contains(nodeId)) return false;
        visited.add(nodeId);
        stack.add(nodeId);
        NpcSceneNode node = scene.getNode(nodeId);
        if (node != null) {
            for (String next : nextNodes(node)) {
                if (next == null || next.isEmpty()) continue;
                if (dfsCycle(scene, next, visited, stack)) return true;
            }
        }
        stack.remove(nodeId);
        return false;
    }

    private static void reach(NpcScene s, String nodeId, Set<String> acc) {
        if (nodeId == null || nodeId.isEmpty() || acc.contains(nodeId)) return;
        NpcSceneNode n = s.getNode(nodeId);
        if (n == null) return;
        acc.add(nodeId);
        for (String next : nextNodes(n)) {
            reach(s, next, acc);
        }
    }

    private static List<String> nextNodes(NpcSceneNode n) {
        List<String> out = new ArrayList<>();
        switch (n.type) {
            case NpcSceneNode.TYPE_SPEECH, NpcSceneNode.TYPE_DELAY -> out.add(n.nextNodeId);
            case NpcSceneNode.TYPE_ACTION -> out.add(n.actionNextNodeId);
            case NpcSceneNode.TYPE_CONDITION -> {
               out.add(n.trueNextNodeId);
                out.add(n.falseNextNodeId);
            }
            case NpcSceneNode.TYPE_QUESTION -> {
                if (n.choices != null) for (NpcChoiceOption o : n.choices) out.add(o.nextNodeId);
            }
            case NpcSceneNode.TYPE_BRANCH -> {
                if (n.branchOptions != null) for (NpcChoiceOption o : n.branchOptions) out.add(o.nextNodeId);
            }
            default -> { }
        }
         return out;
    }

    private static void validateNode(NpcScene s, NpcSceneNode n, Set<String> ids, List<Issue> out) {
        switch (n.type) {
            case NpcSceneNode.TYPE_SPEECH -> {
                if (n.text == null || n.text.isBlank())
                    out.add(new Issue(Level.WARN, n.id, "Пустой текст у фразы"));
                if (n.speechDelayTicks < 0)
                    out.add(new Issue(Level.ERROR, n.id, "Задержка фразы не может быть отрицательной"));
                requireNext(ids, out, n.id, n.nextNodeId, "У фразы не задан следующий узел", "Следующий узел «%s» не существует");
            }
            case NpcSceneNode.TYPE_DELAY -> {
                if (n.delayTicks < 0) out.add(new Issue(Level.ERROR, n.id, "Пауза не может быть отрицательной"));
                requireNext(ids, out, n.id, n.nextNodeId, "У паузы не задан следующий узел", "Следующий узел «%s» не существует" );
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
                         requireNext(ids, out, n.id, o.nextNodeId,
                                "Ответ #" + (i + 1) + ": не задан nextNodeId",
                                "Ответ #" + (i + 1) + ": переход в несуществующий узел");
                    }
                }
            }
            case NpcSceneNode.TYPE_BRANCH -> {
                if (n.branchOptions == null || n.branchOptions.isEmpty()) {
                    out.add(new Issue(Level.ERROR, n.id, "У ветвления нет выходов"));
                } else {
                    for (int i = 0; i < n.branchOptions.size(); i++) {
                        NpcChoiceOption o = n.branchOptions.get(i);
                        requireNext(ids, out, n.id, o.nextNodeId,
                                "Ветка #" + (i + 1) + ": не задан переход",
                                "Ветка #" + (i + 1) + ": переход в несуществующий узел");
                    }
                }
            }
            case NpcSceneNode.TYPE_ACTION -> {
                if (n.actionType == null || n.actionType.isBlank())
                    out.add(new Issue(Level.ERROR, n.id, "Тип действия не задан"));
                if (requiresParam(n.actionType) && (n.actionParam == null || n.actionParam.isBlank()))
                    out.add(new Issue(Level.ERROR, n.id, "Параметр действия обязателен"));
                if (!NpcSceneNode.ACTION_CLOSE_SCENE.equals(n.actionType)) {
                    requireNext(ids, out, n.id, n.actionNextNodeId,
                            "У действия не задан следующий узел",
                            "Следующий узел после действия не существует");
                }
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
        
        if (!NpcSceneNode.TYPE_END.equals(n.type) && nextNodes(n).stream().noneMatch(v -> v != null && !v.isEmpty())) {
            out.add(new Issue(Level.WARN, n.id, "У узла нет исходящих связей"));
        }
    }

    private static void requireNext(Set<String> ids, List<Issue> out, String nodeId, String next,
                                    String missingMsg, String brokenMsgFmt) {
        if (next == null || next.isEmpty()) {
            out.add(new Issue(Level.ERROR, nodeId, missingMsg));
            return;
        }
        if (!ids.contains(next)) out.add(new Issue(Level.ERROR, nodeId, brokenMsgFmt.formatted(next)));
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
                 NpcSceneNode.ACTION_LOOK_AT,
                 NpcSceneNode.ACTION_MOVE_TO,
                 NpcSceneNode.ACTION_CAMERA,
                 NpcSceneNode.ACTION_EFFECT,
                 NpcSceneNode.ACTION_EMOTE,
                 NpcSceneNode.ACTION_TELEPORT,
                 NpcSceneNode.ACTION_SET_VARIABLE,
                 NpcSceneNode.ACTION_OPEN_SCENE -> true;
            default -> false;
        };
    }
}

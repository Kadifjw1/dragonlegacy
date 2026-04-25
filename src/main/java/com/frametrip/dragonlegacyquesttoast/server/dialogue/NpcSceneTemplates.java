package com.frametrip.dragonlegacyquesttoast.server.dialogue;

/** Factory for common scene shapes used by the "Create scene" menu. */
public final class NpcSceneTemplates {

    public static final String TPL_EMPTY           = "empty";
    public static final String TPL_GREETING        = "greeting";
    public static final String TPL_SIMPLE          = "simple";
    public static final String TPL_WITH_QUESTION   = "with_question";
    public static final String TPL_QUEST_OFFER     = "quest_offer";
    public static final String TPL_QUEST_COMPLETE  = "quest_complete";
    public static final String TPL_REFUSAL         = "refusal";
    public static final String TPL_REPEATABLE      = "repeatable";
    public static final String TPL_HOSTILE_WARNING = "hostile_warning";

    public static final String[] TEMPLATE_IDS = {
            TPL_EMPTY, TPL_GREETING, TPL_SIMPLE, TPL_WITH_QUESTION,
            TPL_QUEST_OFFER, TPL_QUEST_COMPLETE, TPL_REFUSAL,
            TPL_REPEATABLE, TPL_HOSTILE_WARNING
    };
    public static final String[] TEMPLATE_LABELS = {
            "Пустая сцена",
            "Приветствие",
            "Простой разговор",
            "Разговор с вопросом",
            "Выдача квеста",
            "Завершение квеста",
            "Отказ",
            "Повторяемый разговор",
            "Предупреждение врагу"
    };

    private NpcSceneTemplates() {}

    public static NpcScene create(String templateId) {
        return switch (templateId) {
            case TPL_GREETING        -> greeting();
            case TPL_SIMPLE          -> simple();
            case TPL_WITH_QUESTION   -> withQuestion();
            case TPL_QUEST_OFFER     -> questOffer();
            case TPL_QUEST_COMPLETE  -> questComplete();
            case TPL_REFUSAL         -> refusal();
            case TPL_REPEATABLE      -> repeatable();
            case TPL_HOSTILE_WARNING -> hostileWarning();
            default                  -> empty();
        };
    }

    public static String label(String id) {
        for (int i = 0; i < TEMPLATE_IDS.length; i++) if (TEMPLATE_IDS[i].equals(id)) return TEMPLATE_LABELS[i];
        return id;
    }

    private static NpcScene empty() {
        NpcScene s = new NpcScene();
        s.name = "Новая сцена";
        s.type = NpcScene.TYPE_CUSTOM;
        return s;
    }

    private static NpcScene greeting() {
        NpcScene s = new NpcScene();
        s.name = "Приветствие";
        s.type = NpcScene.TYPE_GREETING;
        NpcSceneNode a = s.addNode(NpcSceneNode.TYPE_SPEECH);
        a.text = "Здравствуй, путник.";
        NpcSceneNode b = s.addNode(NpcSceneNode.TYPE_SPEECH);
        b.text = "Что привело тебя в наши края?";
        NpcSceneNode end = s.addNode(NpcSceneNode.TYPE_END);
        a.nextNodeId = b.id;
        b.nextNodeId = end.id;
        return s;
    }

    private static NpcScene simple() {
        NpcScene s = new NpcScene();
        s.name = "Разговор";
        s.type = NpcScene.TYPE_IDLE;
        NpcSceneNode a = s.addNode(NpcSceneNode.TYPE_SPEECH);
        a.text = "Давненько я не видел новых лиц.";
        NpcSceneNode end = s.addNode(NpcSceneNode.TYPE_END);
        a.nextNodeId = end.id;
        return s;
    }

    private static NpcScene withQuestion() {
        NpcScene s = new NpcScene();
        s.name = "Разговор с вопросом";
        s.type = NpcScene.TYPE_IDLE;
        NpcSceneNode a = s.addNode(NpcSceneNode.TYPE_SPEECH);
        a.text = "Рад тебя видеть.";
        NpcSceneNode q = s.addNode(NpcSceneNode.TYPE_QUESTION);
        q.text = "Как твои дела?";
        NpcSceneNode good = s.addNode(NpcSceneNode.TYPE_SPEECH);
        good.text = "Замечательно! Я тоже рад.";
        NpcSceneNode bad = s.addNode(NpcSceneNode.TYPE_SPEECH);
        bad.text = "Держись, всё наладится.";
        NpcSceneNode end = s.addNode(NpcSceneNode.TYPE_END);
        a.nextNodeId = q.id;
        q.choices.add(new NpcChoiceOption("Отлично", good.id));
        q.choices.add(new NpcChoiceOption("Плохо",   bad.id));
        good.nextNodeId = end.id;
        bad.nextNodeId  = end.id;
        return s;
    }

    private static NpcScene questOffer() {
        NpcScene s = new NpcScene();
        s.name = "Выдача квеста";
        s.type = NpcScene.TYPE_QUEST_OFFER;
        NpcSceneNode intro = s.addNode(NpcSceneNode.TYPE_SPEECH);
        intro.text = "У меня есть к тебе поручение.";
        NpcSceneNode details = s.addNode(NpcSceneNode.TYPE_SPEECH);
        details.text = "Дело опасное, но хорошо оплачиваемое.";
        NpcSceneNode q = s.addNode(NpcSceneNode.TYPE_QUESTION);
        q.text = "Возьмёшься?";
        NpcSceneNode give = s.addNode(NpcSceneNode.TYPE_ACTION);
        give.actionType = NpcSceneNode.ACTION_GIVE_QUEST;
        give.actionParam = "";
        NpcSceneNode yes = s.addNode(NpcSceneNode.TYPE_SPEECH);
        yes.text = "Отлично, на тебя можно положиться.";
        NpcSceneNode no = s.addNode(NpcSceneNode.TYPE_SPEECH);
        no.text = "Жаль. Если передумаешь — возвращайся.";
        NpcSceneNode end = s.addNode(NpcSceneNode.TYPE_END);
        intro.nextNodeId   = details.id;
        details.nextNodeId = q.id;
        q.choices.add(new NpcChoiceOption("Да, возьмусь", give.id));
        q.choices.add(new NpcChoiceOption("Нет, не сейчас", no.id));
        give.actionNextNodeId = yes.id;
        yes.nextNodeId = end.id;
        no.nextNodeId  = end.id;
        return s;
    }

    private static NpcScene questComplete() {
        NpcScene s = new NpcScene();
        s.name = "Завершение квеста";
        s.type = NpcScene.TYPE_QUEST_COMPLETE;
        NpcSceneNode praise = s.addNode(NpcSceneNode.TYPE_SPEECH);
        praise.text = "Ты справился! Я восхищён.";
        NpcSceneNode complete = s.addNode(NpcSceneNode.TYPE_ACTION);
        complete.actionType = NpcSceneNode.ACTION_COMPLETE_QUEST;
        NpcSceneNode reward = s.addNode(NpcSceneNode.TYPE_SPEECH);
        reward.text = "Прими свою награду.";
        NpcSceneNode end = s.addNode(NpcSceneNode.TYPE_END);
        praise.nextNodeId = complete.id;
        complete.actionNextNodeId = reward.id;
        reward.nextNodeId = end.id;
        return s;
    }

    private static NpcScene refusal() {
        NpcScene s = new NpcScene();
        s.name = "Отказ";
        s.type = NpcScene.TYPE_REFUSAL;
        NpcSceneNode a = s.addNode(NpcSceneNode.TYPE_SPEECH);
        a.text = "Мне нечего сказать тебе.";
        NpcSceneNode end = s.addNode(NpcSceneNode.TYPE_END);
        a.nextNodeId = end.id;
        return s;
    }

    private static NpcScene repeatable() {
        NpcScene s = new NpcScene();
        s.name = "Повторный разговор";
        s.type = NpcScene.TYPE_IDLE;
        s.repeatable = true;
        NpcSceneNode cond = s.addNode(NpcSceneNode.TYPE_CONDITION);
        cond.conditionType = NpcSceneNode.COND_FIRST_TALK;
        NpcSceneNode first = s.addNode(NpcSceneNode.TYPE_SPEECH);
        first.text = "О, это ты. Рад знакомству.";
        NpcSceneNode again = s.addNode(NpcSceneNode.TYPE_SPEECH);
        again.text = "Снова ты? Что-то хотел?";
        NpcSceneNode end = s.addNode(NpcSceneNode.TYPE_END);
        cond.trueNextNodeId  = first.id;
        cond.falseNextNodeId = again.id;
        first.nextNodeId = end.id;
        again.nextNodeId = end.id;
        return s;
    }

    private static NpcScene hostileWarning() {
        NpcScene s = new NpcScene();
        s.name = "Предупреждение";
        s.type = NpcScene.TYPE_HOSTILE_WARNING;
        NpcSceneNode a = s.addNode(NpcSceneNode.TYPE_SPEECH);
        a.text = "Отойди, пока не пострадал.";
        a.emotion = "THREAT";
        NpcSceneNode end = s.addNode(NpcSceneNode.TYPE_END);
        a.nextNodeId = end.id;
        return s;
    }
}

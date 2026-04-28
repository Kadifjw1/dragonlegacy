package com.frametrip.dragonlegacyquesttoast.server.dialogue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NpcSceneNode {

    // ── Node types ───────────────────────────────────────────────────────────
    public static final String TYPE_SPEECH    = "speech";
    public static final String TYPE_QUESTION  = "question";
    public static final String TYPE_ACTION    = "action";
    public static final String TYPE_CONDITION = "condition";
    public static final String TYPE_END       = "end";

    public static final String[] TYPE_IDS = {
            TYPE_SPEECH, TYPE_QUESTION, TYPE_ACTION, TYPE_CONDITION, TYPE_END
    };
    public static final String[] TYPE_LABELS = {
            "Фраза", "Вопрос", "Действие", "Условие", "Конец"
    };
    
// ── Action types ─────────────────────────────────────────────────────────
    public static final String ACTION_GIVE_QUEST           = "give_quest";
    public static final String ACTION_COMPLETE_QUEST       = "complete_quest";
    public static final String ACTION_FAIL_QUEST           = "fail_quest";
    public static final String ACTION_SET_RELATION         = "set_relation";
    public static final String ACTION_SET_FACTION_RELATION = "set_faction_relation";
    public static final String ACTION_GIVE_ITEM            = "give_item";
    public static final String ACTION_TAKE_ITEM            = "take_item";
    public static final String ACTION_PLAY_SOUND           = "play_sound";
    public static final String ACTION_PLAY_ANIMATION       = "play_animation";
    public static final String ACTION_OPEN_SHOP            = "open_shop";
    public static final String ACTION_OPEN_SCENE           = "open_scene";
    public static final String ACTION_CLOSE_SCENE          = "close_scene";

    public static final String[] ACTION_IDS = {
            ACTION_GIVE_QUEST, ACTION_COMPLETE_QUEST, ACTION_FAIL_QUEST,
            ACTION_SET_RELATION, ACTION_SET_FACTION_RELATION,
            ACTION_GIVE_ITEM, ACTION_TAKE_ITEM,
            ACTION_PLAY_SOUND, ACTION_PLAY_ANIMATION,
            ACTION_OPEN_SHOP,
            ACTION_OPEN_SCENE, ACTION_CLOSE_SCENE
    };
    public static final String[] ACTION_LABELS = {
            "Выдать квест", "Завершить квест", "Провалить квест",
            "Отношение к игроку", "Отношение фракции",
            "Выдать предмет", "Забрать предмет",
            "Проиграть звук", "Проиграть анимацию",
            "Открыть магазин",
            "Открыть сцену", "Закрыть сцену"
    };

    // ── Condition types ──────────────────────────────────────────────────────
    public static final String COND_QUEST_ACTIVE    = "quest_active";
    public static final String COND_QUEST_COMPLETE  = "quest_complete";
    public static final String COND_QUEST_NOT_TAKEN = "quest_not_taken";
    public static final String COND_HAS_ITEM        = "has_item";
    public static final String COND_NOT_HAS_ITEM    = "not_has_item";
    public static final String COND_RELATION        = "relation";
    public static final String COND_FACTION         = "faction";
    public static final String COND_TIME_DAY        = "time_day";
    public static final String COND_TIME_NIGHT      = "time_night";
    public static final String COND_PATH_STAGE      = "path_stage";
    public static final String COND_HAS_ABILITY     = "has_ability";
    public static final String COND_FIRST_TALK      = "first_talk";
    public static final String COND_RE_TALK         = "re_talk";

    public static final String[] COND_IDS = {
            COND_QUEST_ACTIVE, COND_QUEST_COMPLETE, COND_QUEST_NOT_TAKEN,
            COND_HAS_ITEM, COND_NOT_HAS_ITEM,
            COND_RELATION, COND_FACTION,
            COND_TIME_DAY, COND_TIME_NIGHT,
            COND_PATH_STAGE, COND_HAS_ABILITY,
            COND_FIRST_TALK, COND_RE_TALK
    };
    public static final String[] COND_LABELS = {
            "Квест активен", "Квест завершён", "Квест не взят",
            "Есть предмет", "Нет предмета",
            "Отношение к игроку", "Фракция",
            "Сейчас день", "Сейчас ночь",
            "Стадия пути", "Есть способность",
            "Первый разговор", "Повторный разговор"
    };

    // ── Emotions ─────────────────────────────────────────────────────────────
    public static final String[] EMOTION_IDS = {
            "NEUTRAL", "HAPPY", "ANGRY", "SAD", "THREAT", "MYSTERY"
    };
    public static final String[] EMOTION_LABELS = {
            "Обычная", "Радость", "Злость", "Печаль", "Угроза", "Тайна"
    };

    // ── Fields ───────────────────────────────────────────────────────────────
    public String id;
    public String type = TYPE_SPEECH;

    // speech / question text
    public String text = "";

    // speech-only cosmetics
    public String speakerName = "";
    public String displayMode = "PLAQUE";       // reserved for future
    public String emotion     = "NEUTRAL";
    public String animationId = "";
    public String soundId     = "";
    public int speechDelayTicks = 0;

    // speech / action: transition after execution
    public String nextNodeId = "";

    // question: list of player choices
    public List<NpcChoiceOption> choices = new ArrayList<>();

    // action fields
    public String actionType  = ACTION_GIVE_QUEST;
    public String actionParam = "";
    public String actionNextNodeId = "";

    // condition fields
    public String conditionType   = COND_FIRST_TALK;
    public String conditionParam  = "";
    public String trueNextNodeId  = "";
    public String falseNextNodeId = "";

    public NpcSceneNode() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
    }

    public NpcSceneNode copy() {
        NpcSceneNode c = new NpcSceneNode();
        c.id              = this.id;
        c.type            = this.type;
        c.text            = this.text;
        c.speakerName     = this.speakerName;
        c.displayMode     = this.displayMode;
        c.emotion         = this.emotion;
        c.animationId     = this.animationId;
        c.soundId         = this.soundId;
        c.speechDelayTicks = this.speechDelayTicks;
        c.nextNodeId      = this.nextNodeId;
        c.choices         = new ArrayList<>();
        if (this.choices != null) for (NpcChoiceOption o : this.choices) c.choices.add(o.copy());
        c.actionType      = this.actionType;
        c.actionParam     = this.actionParam;
        c.actionNextNodeId = this.actionNextNodeId;        
        c.conditionType   = this.conditionType;
        c.conditionParam  = this.conditionParam;
        c.trueNextNodeId  = this.trueNextNodeId;
        c.falseNextNodeId = this.falseNextNodeId;
        return c;
    }

    public String displayLabel() {
        String previewText = truncate(text, 28);
        return switch (type) {
             case TYPE_SPEECH    -> "💬 " + previewText;
            case TYPE_QUESTION  -> "❓ " + previewText;
            case TYPE_ACTION    -> "⚡ " + actionLabel(actionType)
                    + (actionParam.isEmpty() ? "" : ": " + truncate(actionParam, 20));
            case TYPE_CONDITION -> "❖ " + condLabel(conditionType)
                    + (conditionParam.isEmpty() ? "" : ": " + truncate(conditionParam, 18));
            case TYPE_END       -> "■ Конец";
            default             -> id;
        };
    }
    
    public static String actionLabel(String id) {
        for (int i = 0; i < ACTION_IDS.length; i++) if (ACTION_IDS[i].equals(id)) return ACTION_LABELS[i];
        return id == null ? "" : id;
    }

    public static String condLabel(String id) {
        for (int i = 0; i < COND_IDS.length; i++) if (COND_IDS[i].equals(id)) return COND_LABELS[i];
        return id == null ? "" : id;
    }

    public static String emotionLabel(String id) {
        for (int i = 0; i < EMOTION_IDS.length; i++) if (EMOTION_IDS[i].equals(id)) return EMOTION_LABELS[i];
        return id == null ? "" : id;
    }

    public static String typeLabel(String id) {
        for (int i = 0; i < TYPE_IDS.length; i++) if (TYPE_IDS[i].equals(id)) return TYPE_LABELS[i];
        return id == null ? "" : id;
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }
}

package com.frametrip.dragonlegacyquesttoast.client.lang;

import com.frametrip.dragonlegacyquesttoast.DragonLegacyQuestToastMod;

/**
 * Translation key constants for all user-facing strings in the NPC editor.
 * Use these with Component.translatable(key) or I18n.get(key) in UI code.
 */
public final class ModLang {

    private static final String MOD = DragonLegacyQuestToastMod.MODID;
    private static final String GUI = "gui." + MOD + ".";

    // ── Common buttons ────────────────────────────────────────────────────────
    public static final String SAVE      = GUI + "save";
    public static final String DELETE    = GUI + "delete";
    public static final String ADD       = GUI + "add";
    public static final String CANCEL    = GUI + "cancel";
    public static final String DUPLICATE = GUI + "duplicate";
    public static final String ENABLED   = GUI + "enabled";
    public static final String DISABLED  = GUI + "disabled";
    public static final String SEARCH    = GUI + "search";
    public static final String NAME      = GUI + "name";
    public static final String CLOSE     = GUI + "close";

    // ── NPC editor tabs ───────────────────────────────────────────────────────
    public static final String TAB_INFO        = GUI + "tab.info";
    public static final String TAB_PROFESSION  = GUI + "tab.profession";
    public static final String TAB_ANIMATIONS  = GUI + "tab.animations";
    public static final String TAB_RELATIONS   = GUI + "tab.relations";
    public static final String TAB_STEALTH     = GUI + "tab.stealth";
    public static final String TAB_BUILDING    = GUI + "tab.building";
    public static final String TAB_INTERACTION = GUI + "tab.interaction";
    public static final String TAB_MODEL       = GUI + "tab.model";

    // ── Animation editor ──────────────────────────────────────────────────────
    public static final String ANIM_NEW         = GUI + "animation.new";
    public static final String ANIM_DURATION    = GUI + "animation.duration";
    public static final String ANIM_LOOP        = GUI + "animation.loop";
    public static final String ANIM_ROTATION    = GUI + "animation.rotation";
    public static final String ANIM_POSITION    = GUI + "animation.position";
    public static final String ANIM_KEYFRAME    = GUI + "animation.keyframe";
    public static final String ANIM_TICK        = GUI + "animation.tick";
    public static final String ANIM_IMPORT_JSON = GUI + "animation.import_json";
    public static final String ANIM_LOAD_PRESET = GUI + "animation.load_preset";
    public static final String ANIM_STATE       = GUI + "animation.state";

    // ── GUI editor ────────────────────────────────────────────────────────────
    public static final String GUI_EDITOR_TITLE       = GUI + "gui_editor.title";
    public static final String GUI_EDITOR_ADD_SHOP    = GUI + "gui_editor.add_shop";
    public static final String GUI_EDITOR_ADD_JOURNAL = GUI + "gui_editor.add_journal";
    public static final String GUI_EDITOR_ADD_EMPTY   = GUI + "gui_editor.add_empty";
    public static final String GUI_EDITOR_LOAD_PRESET = GUI + "gui_editor.load_preset";
    public static final String GUI_EDITOR_CANVAS      = GUI + "gui_editor.canvas";
    public static final String GUI_EDITOR_ELEMENTS    = GUI + "gui_editor.elements";
    public static final String GUI_EDITOR_PROPERTIES  = GUI + "gui_editor.properties";

    // ── Event chain editor ────────────────────────────────────────────────────
    public static final String EVENT_CHAIN_TITLE   = GUI + "event_chain.title";
    public static final String EVENT_CHAIN_TRIGGER = GUI + "event_chain.trigger";
    public static final String EVENT_CHAIN_CONDITION = GUI + "event_chain.condition";
    public static final String EVENT_CHAIN_ACTION   = GUI + "event_chain.action";
    public static final String EVENT_CHAIN_AND      = GUI + "event_chain.and";
    public static final String EVENT_CHAIN_OR       = GUI + "event_chain.or";

    // ── Companion mode ────────────────────────────────────────────────────────
    public static final String COMPANION_FOLLOW  = GUI + "companion.follow";
    public static final String COMPANION_WAIT    = GUI + "companion.wait";
    public static final String COMPANION_GUARD   = GUI + "companion.guard";
    public static final String COMPANION_PROTECT = GUI + "companion.protect";
    public static final String COMPANION_COMBAT  = GUI + "companion.combat";
    public static final String COMPANION_STEALTH = GUI + "companion.stealth";
    public static final String COMPANION_PASSIVE = GUI + "companion.passive";
    public static final String COMPANION_FOLLOW_DIST  = GUI + "companion.follow_distance";
    public static final String COMPANION_AGGRESSION    = GUI + "companion.aggressiveness";
    public static final String COMPANION_GUARD_RADIUS  = GUI + "companion.guard_radius";

    // ── Stealth tab ───────────────────────────────────────────────────────────
    public static final String STEALTH_ACTIVE        = GUI + "stealth.active";
    public static final String STEALTH_TYPE          = GUI + "stealth.type";
    public static final String STEALTH_VISION_RADIUS = GUI + "stealth.vision_radius";
    public static final String STEALTH_VISION_ANGLE  = GUI + "stealth.vision_angle";
    public static final String STEALTH_HEAR_RADIUS   = GUI + "stealth.hear_radius";
    public static final String STEALTH_DETECT_TICKS  = GUI + "stealth.detect_ticks";
    public static final String STEALTH_SENSITIVITY   = GUI + "stealth.sensitivity";
    public static final String STEALTH_ALERT_SCENE   = GUI + "stealth.alert_scene";
    public static final String STEALTH_SPOT_SCENE    = GUI + "stealth.spot_scene";
    public static final String STEALTH_PATROL_CYCLE  = GUI + "stealth.patrol_cycle";

    // ── NPC info tab ──────────────────────────────────────────────────────────
    public static final String INFO_NPC_NAME     = GUI + "info.npc_name";
    public static final String INFO_SKIN         = GUI + "info.skin";
    public static final String INFO_LAYERS       = GUI + "info.layers";
    public static final String INFO_BODY_PARTS   = GUI + "info.body_parts";
    public static final String INFO_OPEN_FOLDER  = GUI + "info.open_folder";
    public static final String INFO_REFRESH      = GUI + "info.refresh";
    public static final String INFO_PRESET       = GUI + "info.preset";
    public static final String INFO_APPLY        = GUI + "info.apply";
    public static final String INFO_SAVE_PRESET  = GUI + "info.save_preset";

    // ── Professions ───────────────────────────────────────────────────────────
    public static final String PROF_NONE      = "npc.profession." + MOD + ".none";
    public static final String PROF_TRADER    = "npc.profession." + MOD + ".trader";
    public static final String PROF_BUILDER   = "npc.profession." + MOD + ".builder";
    public static final String PROF_COMPANION = "npc.profession." + MOD + ".companion";

    private ModLang() {}
}

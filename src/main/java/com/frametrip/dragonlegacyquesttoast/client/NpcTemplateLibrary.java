package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.entity.NpcEntityData;
import com.frametrip.dragonlegacyquesttoast.profession.NpcProfessionData;
import com.frametrip.dragonlegacyquesttoast.profession.NpcProfessionType;
import com.frametrip.dragonlegacyquesttoast.server.world.FarmerData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

// [EDT-2]: Built-in NPC template library.
public class NpcTemplateLibrary {

    public record Template(String id, String icon, String label, String description,
                           Supplier<NpcEntityData> factory) {}

    public static final Template[] TEMPLATES = {
        new Template("trader",    "🛒", "Торговец",    "Дружелюбный NPC с профессией Trader",     NpcTemplateLibrary::trader),
        new Template("guard",     "⚔",  "Стражник",    "Нейтральный охранник с охраной территории", NpcTemplateLibrary::guard),
        new Template("quest_npc", "❗", "Квестодатель","NPC с иконкой квеста и диалогом",          NpcTemplateLibrary::questNpc),
        new Template("farmer",    "🌾", "Фермер",      "Фермер с циклом сбора урожая пшеницы",     NpcTemplateLibrary::farmer),
        new Template("innkeeper", "🍺", "Трактирщик",  "Дружелюбный трактирщик, ночная смена",     NpcTemplateLibrary::innkeeper),
    };

    // ── Templates ─────────────────────────────────────────────────────────────

    private static NpcEntityData trader() {
        NpcEntityData d = base("Торговец", "FRIENDLY");
        d.nameplateIcon = 2; // 🛒
        d.professionData = new NpcProfessionData();
        d.professionData.type = NpcProfessionType.TRADER;
        d.showName = true;
        return d;
    }

    private static NpcEntityData guard() {
        NpcEntityData d = base("Стражник", "NEUTRAL");
        d.nameplateIcon = 1; // ⚔ Страж
        d.guardTerritoryEnabled = true;
        d.guardRadius = 15.0f;
        d.guardWarnFirst = true;
        d.maxHealth = 30;
        return d;
    }

    private static NpcEntityData questNpc() {
        NpcEntityData d = base("Путник", "FRIENDLY");
        d.nameplateIcon = 4; // ❗ Квест
        d.showName = true;
        return d;
    }

    private static NpcEntityData farmer() {
        NpcEntityData d = base("Фермер", "FRIENDLY");
        d.farmerData = new FarmerData();
        d.farmerData.farmerEnabled = true;
        d.farmerData.cropType = "wheat";
        d.farmerData.plotRadius = 8;
        return d;
    }

    private static NpcEntityData innkeeper() {
        NpcEntityData d = base("Трактирщик", "FRIENDLY");
        d.nameplateIcon = 7; // 🔧 Мастер (craftsman)
        d.greetEnabled = true;
        d.greetRange = 6.0f;
        d.greetMessage = "Добро пожаловать, путник!";
        d.greetCooldownSec = 60;
        return d;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static NpcEntityData base(String name, String relation) {
        NpcEntityData d = new NpcEntityData();
        d.displayName   = name;
        d.playerRelation = relation;
        d.showName      = true;
        d.maxHealth     = 20;
        return d;
    }
}

package com.frametrip.dragonlegacyquesttoast.server;
 
import com.frametrip.dragonlegacyquesttoast.client.AwakeningPathType;
 
import java.util.*;
import java.util.stream.Collectors;
 
public class AbilityRegistry {
 
    private static final Map<String, AbilityDefinition> ALL = new LinkedHashMap<>();
 
    static {
        // ─── FIRE ────────────────────────────────────────────────────────────────────
        reg("fire_ember",           "Искра касания",        "Каждые 5 ударов: взрыв огня в радиусе 3 блоков (8 урона).",
                AwakeningPathType.FIRE, 1, 1,  List.of(),                                          1);
        reg("fire_aura",            "Аура Пламени",         "Каждые 4 сек: 3 урона врагам в радиусе 3 блоков, поджигание 2 сек.",
                AwakeningPathType.FIRE, 2, 2,  List.of("fire_ember"),                              2);
        reg("fire_trail",           "Огненный след",        "Спринт: каждые 0.5 сек поджигает врагов в радиусе 2 блоков у следа.",
                AwakeningPathType.FIRE, 2, 2,  List.of("fire_ember"),                              3);
        reg("fire_magma_shield",    "Магматический щит",    "Получая >4 урона: снижает урон на 30%, поджигает атакующего на 4 сек.",
                AwakeningPathType.FIRE, 3, 3,  List.of("fire_aura"),                               4);
        reg("fire_inferno_wave",    "Волна Инферно",        "Каждые 10 ударов: кольцо огня 4 урона в радиусе 4 блоков.",
                AwakeningPathType.FIRE, 3, 3,  List.of("fire_trail"),                              5);
        reg("fire_phoenix_rush",    "Рывок Феникса",        "Убийство: Скорость III + Сила I на 8 сек, восстановление 2 HP.",
                AwakeningPathType.FIRE, 4, 4,  List.of("fire_magma_shield"),                       6);
        reg("fire_blazing_gaze",    "Пылающий взор",        "Каждые 1 сек: поджигает врагов в направлении взгляда (радиус 4).",
                AwakeningPathType.FIRE, 4, 4,  List.of("fire_inferno_wave"),                       7);
        reg("fire_vengeance_flash", "Вспышка возмездия",   "Получая >6 урона: огненный взрыв 10 урона в радиусе 5 (КД 30 сек).",
                AwakeningPathType.FIRE, 5, 5,  List.of("fire_phoenix_rush"),                       8);
        reg("fire_ashen_armor",     "Пепельная броня",      "Горя: −40% получаемого урона + 1 урон врагам в радиусе 2 каждые 0.5 сек.",
                AwakeningPathType.FIRE, 5, 5,  List.of("fire_blazing_gaze"),                       9);
        reg("fire_eternal_flame",   "Вечный огонь",         "Иммунитет к огню. Аура пламени 2× сильнее. Взрыв при каждом убийстве.",
                AwakeningPathType.FIRE, 6, 10, List.of("fire_vengeance_flash", "fire_ashen_armor"), 10);
 
        // ─── ICE ─────────────────────────────────────────────────────────────────────
        reg("ice_frost_touch",    "Прикосновение мороза",  "Каждый удар: Замедление I на 3 сек.",
                AwakeningPathType.ICE, 1, 1,  List.of(),                                         1);
        reg("ice_shards",         "Ледяная крошка",        "Каждые 5 ударов: взрыв льда, Замедление II в радиусе 4 блоков.",
                AwakeningPathType.ICE, 2, 2,  List.of("ice_frost_touch"),                        2);
        reg("ice_freeze_guard",   "Ледяной щит",           "Получая >5 урона: Замедление III атакующему на 4 сек.",
                AwakeningPathType.ICE, 2, 2,  List.of("ice_frost_touch"),                        3);
        reg("ice_blizzard_cloak", "Метельный плащ",        "Каждые 3 сек: Замедление I врагам в радиусе 4 блоков.",
                AwakeningPathType.ICE, 3, 3,  List.of("ice_shards"),                             4);
        reg("ice_cryo_burst",     "Крио удар",             "Каждые 3 сек: Замедление III врагу вплотную + исцеление 2 HP.",
                AwakeningPathType.ICE, 3, 3,  List.of("ice_freeze_guard"),                       5);
        reg("ice_reflect",        "Ледяное отражение",     "25% шанс при получении удара: 2 урона атакующему, заморозка.",
                AwakeningPathType.ICE, 4, 4,  List.of("ice_blizzard_cloak"),                     6);
        reg("ice_path",           "Ледяная тропа",         "Спринт: каждые 0.5 сек Замедление I врагам у следа.",
                AwakeningPathType.ICE, 4, 4,  List.of("ice_cryo_burst"),                         7);
        reg("ice_armor",          "Ледяная броня",         "Каждые 6 ед. урона = 1 заряд. На 5 зарядах — AoE заморозка r=5.",
                AwakeningPathType.ICE, 5, 5,  List.of("ice_reflect"),                            8);
        reg("ice_permafrost",     "Вечная мерзлота",       "Каждые 2 сек: Замедление II всем в радиусе 6 блоков.",
                AwakeningPathType.ICE, 5, 5,  List.of("ice_path"),                               9);
        reg("ice_absolute_zero",  "Абсолютный ноль",       "Каждые 5 сек: Замедление III всем в радиусе 8 + исцеление 3 HP.",
                AwakeningPathType.ICE, 6, 10, List.of("ice_armor", "ice_permafrost"),             10);
 
        // ─── STORM ───────────────────────────────────────────────────────────────────
        reg("storm_static",         "Статический разряд",  "Каждые 5 ударов: молния бьёт в цель (5 урона).",
                AwakeningPathType.STORM, 1, 1,  List.of(),                                              1);
        reg("storm_thunder_step",   "Громовой шаг",        "Спринт: каждые 0.5 сек удар током врагам в радиусе 3 (2 урона).",
                AwakeningPathType.STORM, 2, 2,  List.of("storm_static"),                               2);
        reg("storm_electric_shield","Электрический щит",   "30% шанс: оглушить атакующего на 2 сек + 3 урона.",
                AwakeningPathType.STORM, 2, 2,  List.of("storm_static"),                               3);
        reg("storm_chain",          "Цепная молния",        "30% шанс при ударе: молния перепрыгивает на ближайшего врага (4 урона).",
                AwakeningPathType.STORM, 3, 3,  List.of("storm_thunder_step"),                         4);
        reg("storm_leap",           "Скачок шторма",       "Убийство: телепорт к ближайшему врагу, молния на прежнем месте.",
                AwakeningPathType.STORM, 3, 3,  List.of("storm_electric_shield"),                      5);
        reg("storm_ball",           "Шаровая молния",      "Каждые 15 ударов: электрический взрыв 6 урона в радиусе 3.",
                AwakeningPathType.STORM, 4, 4,  List.of("storm_chain"),                                6);
        reg("storm_overcharge",     "Перегрузка",          "20 ударов без получения урона: Скорость II + Сила I на 12 сек.",
                AwakeningPathType.STORM, 4, 4,  List.of("storm_leap"),                                 7);
        reg("storm_cyclone",        "Вихрь",               "Каждые 2 сек: отбрасывает всех в радиусе 5 блоков.",
                AwakeningPathType.STORM, 5, 5,  List.of("storm_ball"),                                 8);
        reg("storm_vengeful",       "Буря возмездия",      "Получая >8 урона: молнии по всем врагам в радиусе 6 (КД 60 сек).",
                AwakeningPathType.STORM, 5, 5,  List.of("storm_overcharge"),                           9);
        reg("storm_thundergod",     "Бог грозы",           "Каждые 5 сек: молнии на 3 врагов в радиусе 12. Вечная электроаура.",
                AwakeningPathType.STORM, 6, 10, List.of("storm_cyclone", "storm_vengeful"),            10);
 
        // ─── VOID ────────────────────────────────────────────────────────────────────
        reg("void_touch",          "Прикосновение пустоты","Каждый удар: Слепота на 2 сек.",
                AwakeningPathType.VOID, 1, 1,  List.of(),                                            1);
        reg("void_shadow",         "Теневое ускользание",  "HP<30% + получение урона: невидимость 5 сек, телепорт (КД 60 сек).",
                AwakeningPathType.VOID, 2, 2,  List.of("void_touch"),                               2);
        reg("void_null_aura",      "Нулевая аура",         "Каждые 4 сек: высасывает 1 HP у всех в радиусе 5 + исцеление.",
                AwakeningPathType.VOID, 2, 2,  List.of("void_touch"),                               3);
        reg("void_rift",           "Разрыв пустоты",       "Каждые 6 ударов: притягивает врагов к цели, Слабость II на 4 сек.",
                AwakeningPathType.VOID, 3, 3,  List.of("void_shadow"),                              4);
        reg("void_phase_shift",    "Фазовый сдвиг",        "Смертельный удар: обнуляет урон, телепорт, Поглощение II (КД 120 сек).",
                AwakeningPathType.VOID, 3, 3,  List.of("void_null_aura"),                           5);
        reg("void_soul_drain",     "Похищение душ",        "Удар по врагу с HP<50%: восстанавливает 0.5 HP игроку.",
                AwakeningPathType.VOID, 4, 4,  List.of("void_rift"),                                6);
        reg("void_phantom_trail",  "Призрачный след",      "Приседание: Слабость I врагам в радиусе 2 каждые 0.5 сек.",
                AwakeningPathType.VOID, 4, 4,  List.of("void_phase_shift"),                         7);
        reg("void_mirror",         "Зеркало пустоты",      "40% шанс при получении урона: 50% урона отражается на атакующего.",
                AwakeningPathType.VOID, 5, 5,  List.of("void_soul_drain"),                          8);
        reg("void_event_horizon",  "Горизонт событий",     "Каждые 25 ударов: гравитационное поле, Слабость II всем рядом.",
                AwakeningPathType.VOID, 5, 5,  List.of("void_phantom_trail"),                       9);
        reg("void_annihilation",   "Аннигиляция",          "Убийство: 15 урона в радиусе 8 + исцеление 5 HP. Вечная аура слабости.",
                AwakeningPathType.VOID, 6, 10, List.of("void_mirror", "void_event_horizon"),         10);
    }
 
    private static void reg(String id, String name, String desc,
                             AwakeningPathType path, int tier, int cost,
                             List<String> requires, int treeIndex) {
        ALL.put(id, new AbilityDefinition(id, name, desc, path, tier, cost, requires, treeIndex));
    }
 
    public static AbilityDefinition get(String id) {
        return ALL.get(id);
    }
 
    public static List<AbilityDefinition> getForPath(AwakeningPathType path) {
        return ALL.values().stream()
                .filter(d -> d.path == path)
                .sorted(Comparator.comparingInt(d -> d.treeIndex))
                .collect(Collectors.toList());
    }
 
    public static Set<String> getAllIds() {
        return Collections.unmodifiableSet(ALL.keySet());
    }
}

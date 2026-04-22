package com.frametrip.dragonlegacyquesttoast.client;
 
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
 
public class ClientPlayerAbilityState {
    private static Set<String> unlockedAbilities = new HashSet<>();
    private static Set<String> disabledAbilities = new HashSet<>();
    private static int awakeningPoints = 0;
 
    public static void sync(Set<String> abilities, Set<String> disabled, int points) {
        unlockedAbilities = new HashSet<>(abilities);
        disabledAbilities = new HashSet<>(disabled);
        awakeningPoints = points;
    }
 
    public static boolean hasAbility(String abilityId) {
        return unlockedAbilities.contains(abilityId);
    }
 
    public static Set<String> getAbilities() {
        return Collections.unmodifiableSet(unlockedAbilities);
    }

    public static boolean isEnabled(String abilityId) {
        return unlockedAbilities.contains(abilityId) && !disabledAbilities.contains(abilityId);
    }
 
    public static int getPoints() {
        return awakeningPoints;
    }
}

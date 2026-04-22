package com.frametrip.dragonlegacyquesttoast.client;
 
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
 
public class ClientPlayerAbilityState {
    private static Set<String> unlockedAbilities = new HashSet<>();
    private static int awakeningPoints = 0;
 
    public static void sync(Set<String> abilities, int points) {
        unlockedAbilities = new HashSet<>(abilities);
        awakeningPoints = points;
    }
 
    public static boolean hasAbility(String abilityId) {
        return unlockedAbilities.contains(abilityId);
    }
 
    public static Set<String> getAbilities() {
        return Collections.unmodifiableSet(unlockedAbilities);
    }
 
    public static int getPoints() {
        return awakeningPoints;
    }
}
    }
 
    public static int getPoints() {
        return awakeningPoints;
    }
}
 

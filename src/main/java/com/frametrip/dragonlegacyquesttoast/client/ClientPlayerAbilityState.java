package com.frametrip.dragonlegacyquesttoast.client;
 
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
 
public class ClientPlayerAbilityState {
    private static Set<String> unlockedAbilities = new HashSet<>();
 
    public static void setAbilities(Set<String> abilities) {
        unlockedAbilities = new HashSet<>(abilities);
    }
 
    public static boolean hasAbility(String abilityId) {
        return unlockedAbilities.contains(abilityId);
    }
 
    public static Set<String> getAbilities() {
        return Collections.unmodifiableSet(unlockedAbilities);
    }
}

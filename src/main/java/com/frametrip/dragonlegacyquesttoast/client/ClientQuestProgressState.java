package com.frametrip.dragonlegacyquesttoast.client;
 
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
 
public class ClientQuestProgressState {
 
    private static Map<String, Integer> progress  = new HashMap<>();
    private static Set<String>          active = new HashSet<>();
    private static Set<String>          completed = new HashSet<>();
    private static Set<String>          failed = new HashSet<>();

    public static void sync(Map<String, Integer> prog, Set<String> activeQuests,
                            Set<String> done, Set<String> failedQuests) {
        progress  = prog  != null ? new HashMap<>(prog)  : new HashMap<>();
        active = activeQuests != null ? new HashSet<>(activeQuests) : new HashSet<>();
        completed = done  != null ? new HashSet<>(done)  : new HashSet<>();
        failed = failedQuests != null ? new HashSet<>(failedQuests) : new HashSet<>();
    }
 
    public static int  getProgress (String questId) { return progress.getOrDefault(questId, 0); }
    public static boolean isActive(String questId) { return active.contains(questId); }
    public static boolean isComplete(String questId) { return completed.contains(questId); }
    public static boolean isFailed(String questId) { return failed.contains(questId); }
 
    public static Map<String, Integer> getAllProgress()  { return Collections.unmodifiableMap(progress);  }
    public static Set<String>          getActive()      { return Collections.unmodifiableSet(active); }
    public static Set<String>          getCompleted()   { return Collections.unmodifiableSet(completed); }
    public static Set<String>          getFailed()      { return Collections.unmodifiableSet(failed); }
}

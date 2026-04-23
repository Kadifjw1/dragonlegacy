package com.frametrip.dragonlegacyquesttoast.client;
 
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
 
public class ClientQuestProgressState {
 
    private static Map<String, Integer> progress  = new HashMap<>();
    private static Set<String>          completed = new HashSet<>();
 
    public static void sync(Map<String, Integer> prog, Set<String> done) {
        progress  = prog  != null ? new HashMap<>(prog)  : new HashMap<>();
        completed = done  != null ? new HashSet<>(done)  : new HashSet<>();
    }
 
    public static int  getProgress (String questId) { return progress.getOrDefault(questId, 0); }
    public static boolean isComplete(String questId) { return completed.contains(questId); }
 
    public static Map<String, Integer> getAllProgress()  { return Collections.unmodifiableMap(progress);  }
    public static Set<String>          getCompleted()   { return Collections.unmodifiableSet(completed); }
}

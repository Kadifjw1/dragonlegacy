package com.frametrip.dragonlegacyquesttoast.client;
 
import com.frametrip.dragonlegacyquesttoast.entity.FactionData;
 
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
 
public class ClientFactionState {
 
    private static final List<FactionData> factions = new ArrayList<>();
 
    public static void sync(List<FactionData> data) {
        factions.clear();
        if (data != null) factions.addAll(data);
    }
 
    public static List<FactionData> getAll() {
        return Collections.unmodifiableList(factions);
    }
 
    public static FactionData get(String id) {
        return factions.stream().filter(f -> f.id.equals(id)).findFirst().orElse(null);
    }
}

package com.frametrip.dragonlegacyquesttoast.client;
 
import com.frametrip.dragonlegacyquesttoast.server.NpcProfile;
 
import java.util.ArrayList;
import java.util.List;
 
public class ClientNpcProfileState {
 
    private static List<NpcProfile> profiles = new ArrayList<>();
 
    public static void sync(List<NpcProfile> incoming) {
        profiles = incoming != null ? new ArrayList<>(incoming) : new ArrayList<>();
    }
 
    public static List<NpcProfile> getAll() {
        return new ArrayList<>(profiles);
    }
}

package com.frametrip.dragonlegacyquesttoast.client;

import com.frametrip.dragonlegacyquesttoast.server.quest.QuestChain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// [QST-1]: Client-side cache of quest chains synced from the server.
public class ClientQuestChainState {

    private static List<QuestChain> chains = new ArrayList<>();

    public static void sync(List<QuestChain> incoming) {
        chains = incoming != null ? new ArrayList<>(incoming) : new ArrayList<>();
    }

    public static List<QuestChain> getAll() {
        return Collections.unmodifiableList(chains);
    }

    public static QuestChain get(String chainId) {
        for (QuestChain c : chains) if (c.chainId.equals(chainId)) return c;
        return null;
    }
}

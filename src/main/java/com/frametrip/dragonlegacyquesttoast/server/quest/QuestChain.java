package com.frametrip.dragonlegacyquesttoast.server.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// [QST-1]: Quest chain data — ordered list of quests with unlock gates.
public class QuestChain {

    public String chainId;
    public String chainName;
    public List<QuestChainLink> links = new ArrayList<>();

    public QuestChain() {
        this.chainId   = UUID.randomUUID().toString().substring(0, 8);
        this.chainName = "Новая цепочка";
    }

    public static class QuestChainLink {
        public String questId = "";
        // Quest IDs that become available once this link's quest is completed.
        public List<String> unlocksOnComplete = new ArrayList<>();
    }

    public QuestChain copy() {
        QuestChain c = new QuestChain();
        c.chainId   = this.chainId;
        c.chainName = this.chainName;
        for (QuestChainLink l : this.links) {
            QuestChainLink lc = new QuestChainLink();
            lc.questId = l.questId;
            lc.unlocksOnComplete = new ArrayList<>(l.unlocksOnComplete);
            c.links.add(lc);
        }
        return c;
    }
}

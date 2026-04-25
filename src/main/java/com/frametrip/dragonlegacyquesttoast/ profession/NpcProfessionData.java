package com.frametrip.dragonlegacyquesttoast.profession;

import com.frametrip.dragonlegacyquesttoast.profession.trader.TraderProfessionData;

public class NpcProfessionData {

    public NpcProfessionType   type       = NpcProfessionType.NONE;
    public TraderProfessionData traderData = null;

    public NpcProfessionData copy() {
        NpcProfessionData c = new NpcProfessionData();
        c.type       = this.type;
        c.traderData = (this.traderData != null) ? this.traderData.copy() : null;
        return c;
    }

    public void ensureTraderData() {
        if (traderData == null) traderData = new TraderProfessionData();
    }
}

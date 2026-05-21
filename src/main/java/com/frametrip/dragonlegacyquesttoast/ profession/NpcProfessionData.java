package com.frametrip.dragonlegacyquesttoast.profession;

import com.frametrip.dragonlegacyquesttoast.profession.trader.TraderProfessionData;
import com.frametrip.dragonlegacyquesttoast.server.stealth.PatrolPoint;

import java.util.ArrayList;
import java.util.List;

public class NpcProfessionData {

    public NpcProfessionType    type          = NpcProfessionType.NONE;
    public TraderProfessionData traderData    = null;

    // [JOB-1] Work schedule
    public WorkSchedule  workSchedule = new WorkSchedule();

    // [JOB-2] Work patrol route (reuses PatrolPoint infrastructure)
    public List<PatrolPoint> workRoute    = new ArrayList<>();
    public boolean           loopWorkRoute = true;

    // [JOB-3] Job conditions
    public JobConditions jobConditions = new JobConditions();

    public NpcProfessionData copy() {
        NpcProfessionData c = new NpcProfessionData();
        c.type          = this.type;
        c.traderData    = (this.traderData != null) ? this.traderData.copy() : null;
        c.workSchedule  = this.workSchedule.copy();
        c.loopWorkRoute  = this.loopWorkRoute;
        c.jobConditions = this.jobConditions.copy();
        if (this.workRoute != null) {
            c.workRoute = new ArrayList<>();
            for (PatrolPoint p : this.workRoute) c.workRoute.add(p.copy());
        }
        return c;
    }

    public void ensureTraderData() {
        if (traderData == null) traderData = new TraderProfessionData();
    }
}

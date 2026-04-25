package com.frametrip.dragonlegacyquesttoast.profession.trader;

public class TradeRestockSettings {

    public RestockMode mode          = RestockMode.DISABLED;
    public int         timeOfDay     = 6000;
    public int         everyNDays    = 1;
    public long        lastRestockDay = -1L;

    public TradeRestockSettings copy() {
        TradeRestockSettings c = new TradeRestockSettings();
        c.mode           = this.mode;
        c.timeOfDay      = this.timeOfDay;
        c.everyNDays     = this.everyNDays;
        c.lastRestockDay = this.lastRestockDay;
        return c;
    }

    public String timeOfDayLabel() {
        int hours   = (int)(timeOfDay / 1000.0 * (24.0 / 24.0));
        int h       = (hours + 6) % 24;
        return String.format("%02d:00", h);
    }
}

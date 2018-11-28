package com.funstat.ohlc;

import com.funstat.domain.InstrId;

import java.util.List;

public class Metadata{
    List<InstrId> instrIds;
    int period;

    public Metadata(List<InstrId> instrIds, int period) {
        this.instrIds = instrIds;
        this.period = period;
    }

    public List<InstrId> getInstrIds() {
        return instrIds;
    }

    public int getPeriod() {
        return period;
    }
}

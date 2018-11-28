package com.funstat.vantage;

import com.funstat.domain.Ohlc;
import com.funstat.domain.InstrId;

import java.time.LocalDateTime;
import java.util.List;

public interface Source{
    List<InstrId> symbols();
    List<Ohlc> load(InstrId instrId);
    List<Ohlc> load(InstrId instrId, LocalDateTime dateTime);
    String getName();
}

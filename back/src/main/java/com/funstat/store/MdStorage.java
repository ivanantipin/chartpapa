package com.funstat.store;

import com.funstat.domain.Ohlc;
import com.funstat.finam.InstrId;

import java.util.List;

public interface MdStorage {
    List<Ohlc> read(InstrId instrId, String interval);

    void save(String code, String source, String interval, List<firelib.domain.Ohlc> data);

    List<InstrId> getMeta();

    void updateSymbolsMeta();
}

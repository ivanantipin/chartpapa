package com.funstat.store;

import com.funstat.domain.Ohlc;
import com.funstat.finam.Symbol;

import java.util.List;

public interface MdStorage {
    List<Ohlc> read(Symbol symbol, String interval);

    void save(String code, String source, String interval, List<firelib.domain.Ohlc> data);

    List<Symbol> getMeta();

    void updateSymbolsMeta();
}

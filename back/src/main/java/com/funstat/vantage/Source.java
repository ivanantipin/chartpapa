package com.funstat.vantage;

import com.funstat.domain.Ohlc;
import com.funstat.finam.Symbol;

import java.time.LocalDateTime;
import java.util.List;

public interface Source{
    List<Symbol> symbols();
    List<Ohlc> load(Symbol symbol);
    List<Ohlc> load(Symbol symbol, LocalDateTime dateTime);
    String getName();
}

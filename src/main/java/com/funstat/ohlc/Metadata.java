package com.funstat.ohlc;

import com.iaa.finam.Symbol;

import java.util.List;

public class Metadata{
    List<Symbol> symbols;
    int period;

    public Metadata(List<Symbol> symbols, int period) {
        this.symbols = symbols;
        this.period = period;
    }
}

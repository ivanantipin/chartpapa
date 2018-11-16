package com.funstat;

import com.funstat.finam.Symbol;

public class Tables{
    public static final PersistDescriptor<Symbol> REQUESTED = new PersistDescriptor<>("requested",Symbol.class, s->s.code);
    public static final PersistDescriptor<Symbol> SYMBOLS = new PersistDescriptor<>("symbols",Symbol.class, s->s.code);
    public static final PersistDescriptor<Pair> PAIRS = new PersistDescriptor<>("pairs", Pair.class, s -> s.key);
}


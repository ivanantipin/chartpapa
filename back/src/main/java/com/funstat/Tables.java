package com.funstat;

import com.funstat.finam.Symbol;
import org.apache.commons.lang3.tuple.MutablePair;

public class Tables{
    public static final PersistDescriptor<Symbol> REQUESTED = new PersistDescriptor<>("requested",Symbol.class, s->s.code);
    public static final PersistDescriptor<Symbol> SYMBOLS = new PersistDescriptor<>("symbols",Symbol.class, s->s.code);
    public static final PersistDescriptor<MutablePair> PAIRS = new PersistDescriptor<>("pairs", MutablePair.class, s -> s.getLeft().toString());

}

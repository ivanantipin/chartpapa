package com.funstat;

import com.funstat.domain.InstrId;

public class Tables{
    public static final PersistDescriptor<InstrId> REQUESTED = new PersistDescriptor<>("requested", InstrId.class, s->s.code);
    public static final PersistDescriptor<InstrId> SYMBOLS = new PersistDescriptor<>("symbols", InstrId.class, s->s.code);
    public static final PersistDescriptor<Pair> PAIRS = new PersistDescriptor<>("pairs", Pair.class, s -> s.key);
}


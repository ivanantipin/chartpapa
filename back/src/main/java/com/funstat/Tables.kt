package com.funstat

import com.funstat.domain.InstrId

object Tables {
    val REQUESTED = PersistDescriptor("requested", InstrId::class.java) { it.code }
    val SYMBOLS = PersistDescriptor("symbols", InstrId::class.java) { it.code }
    val PAIRS = PersistDescriptor("pairs", Pair::class.java) { it.key }
}


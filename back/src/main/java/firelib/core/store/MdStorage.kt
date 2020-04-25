package firelib.core.store

import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc

interface MdStorage {

    fun meta(): List<InstrId>

    fun read(instrId: InstrId, interval: Interval, targetInterval: Interval): List<Ohlc>

}



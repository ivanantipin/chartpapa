package firelib.core.store

import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc

interface MdStorage {

    fun meta(): List<InstrId>
    fun read(instrId: InstrId, interval: Interval): List<Ohlc>

    fun save(code: String, source: SourceName, interval: Interval, data: List<Ohlc>)

    fun updateRequested(code: String)
}



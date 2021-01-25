package firelib.core.store

import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import java.time.LocalDateTime

interface MdStorage {
    fun read(instrId: InstrId, interval: Interval, targetInterval: Interval): List<Ohlc>
    fun insert(instrId: InstrId, interval: Interval, ohlcs : List<Ohlc>)
    fun read(instrId: InstrId, interval: Interval, start: LocalDateTime): List<Ohlc>
}



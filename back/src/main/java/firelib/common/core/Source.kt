package firelib.common.core

import com.funstat.domain.InstrId
import firelib.common.interval.Interval
import firelib.domain.Ohlc
import java.time.LocalDateTime

interface Source {
    fun symbols(): List<InstrId>
    fun load(instrId: InstrId): Sequence<Ohlc>
    fun load(instrId: InstrId, dateTime: LocalDateTime): Sequence<Ohlc>
    fun getName(): String;
    fun getDefaultInterval(): Interval

    fun listen(instrId: InstrId, callback : (Ohlc)->Unit){}

}
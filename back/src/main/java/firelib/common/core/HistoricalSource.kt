package firelib.common.core

import firelib.domain.InstrId
import firelib.common.interval.Interval
import firelib.domain.Ohlc
import java.time.LocalDateTime

interface HistoricalSource {
    fun symbols(): List<InstrId>
    fun load(instrId: InstrId): Sequence<Ohlc>
    fun load(instrId: InstrId, dateTime: LocalDateTime): Sequence<Ohlc>
    fun getName(): SourceName
    fun getDefaultInterval(): Interval

    fun mapSecurity(security : String) : InstrId {
        return InstrId(code = security)
    }
}

interface RealtimeSource{
    fun listen(instrId: InstrId, callback : (Ohlc)->Unit){}
}



enum class SourceName{
    FINAM,TRANSAQ, TCS, DUMMY, VANTAGE, IQFEED
}

enum class RealtimeSourceName{
    TRANSAQ
}
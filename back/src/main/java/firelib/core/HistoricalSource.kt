package firelib.core

import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import java.time.LocalDateTime

interface HistoricalSource {
    fun symbols(): List<InstrId>
    fun load(instrId: InstrId, interval: Interval): Sequence<Ohlc>
    fun load(instrId: InstrId, dateTime: LocalDateTime, interval: Interval): Sequence<Ohlc>
    fun getName(): SourceName

    fun mapSecurity(security : String) : InstrId {
        return InstrId(code = security)
    }
}

enum class SourceName{
    FINAM,TRANSAQ, TCS, DUMMY, VANTAGE, IQFEED,MT5,MOEX, EODHIST
}
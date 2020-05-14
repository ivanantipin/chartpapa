package firelib.mt5

import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import java.time.LocalDateTime

class MT5SourceSafe : HistoricalSource {

    var del : MT5Source

    fun reinit() {
        del = MT5Source()
    }

    init {
        del = MT5Source()
    }

    override fun symbols(): List<InstrId> {
        return del.symbols()
    }

    override fun load(instrId: InstrId, interval: Interval): Sequence<Ohlc> {
        return del.load(instrId, interval)
    }

    override fun load(instrId: InstrId, dateTime: LocalDateTime, interval: Interval): Sequence<Ohlc> {

        try {
            return del.load(instrId, dateTime, interval)
        }catch (e : Exception){
            del.close()
            reinit()
            return del.load(instrId, dateTime, interval)
        }
    }

    override fun getName(): SourceName {
        return del.getName()
    }

}
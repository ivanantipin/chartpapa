package firelib.poligon

import firelib.core.HistoricalSource
import firelib.core.HistoricalSourceAsync
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime

class PoligonSource(val poligonSourceAsync: PoligonSourceAsync) : HistoricalSource {

    override fun symbols(): List<InstrId> {
        return runBlocking {  poligonSourceAsync.symbols()}
    }

    override fun load(instrId: InstrId, interval: Interval): Sequence<Ohlc> {
        return load(instrId, LocalDateTime.now().minusDays(600), Interval.Min10)
    }

    override fun load(instrId: InstrId, dateTime: LocalDateTime, interval: Interval): Sequence<Ohlc> {
        return runBlocking {
            val ret = mutableListOf<Ohlc>()
            poligonSourceAsync.load(instrId, dateTime, interval).toCollection(ret)
            ret.asSequence()
        }
    }

    override fun getName(): SourceName {
        return SourceName.POLIGON
    }

    override fun getAsyncInterface(): HistoricalSourceAsync? {
        return poligonSourceAsync
    }
}
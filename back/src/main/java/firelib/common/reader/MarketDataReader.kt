package firelib.common.reader

import firelib.common.misc.atUtc
import firelib.common.model.Div
import firelib.domain.Ohlc
import firelib.domain.Timed
import java.time.Instant
import java.time.ZoneOffset

/**

 */
interface MarketDataReader<out T : Timed> : AutoCloseable{

    fun seek(time:Instant) : Boolean

    fun current(): T

    fun read(): Boolean

    fun startTime() : Instant

    fun endTime() : Instant
}

class ReaderDivAdjusted(val delegate: MarketDataReader<Ohlc>, val divs : List<Div>) : MarketDataReader<Ohlc> by delegate{
    var nextDt : Instant = Instant.MAX
    var currentAdjustment : Double = 0.0
    init {
        reindex()
    }

    fun reindex(){
        val curr = delegate.current()
        val cidx = divs.indexOfFirst { it.lastDayWithDivs.plusDays(1).atStartOfDay().isAfter(curr.dtGmtEnd.atUtc()) }
        if(cidx < 0){
            currentAdjustment = divs.sumByDouble { it.div }
            nextDt = Instant.MAX
            return
        }
        nextDt = divs[cidx].lastDayWithDivs.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        if(cidx > 0){
            currentAdjustment = divs.subList(0,cidx).sumByDouble { it.div }
        }
    }

    var cachedValue = Ohlc()

    override fun current(): Ohlc {
        val ret = delegate.current();
        if(ret.dtGmtEnd.isAfter(nextDt)){
            reindex()
        }
        if(cachedValue.dtGmtEnd != ret.dtGmtEnd){
            cachedValue = ret.copy(open = ret.open + currentAdjustment,
                    high = ret.high + currentAdjustment,
                    low = ret.low + currentAdjustment,
                    close = ret.close + currentAdjustment, volume = ret.volume)
        }
        return cachedValue
    }
}

class MarketDataReaderSql(val ohlcs : List<Ohlc>) : MarketDataReader<Ohlc>{

    var cind = 0;

    override fun seek(time: Instant): Boolean {
        val idx = ohlcs.indexOfFirst { it.dtGmtEnd.isAfter(time) }
        cind = idx
        return idx > 0
    }

    override fun current(): Ohlc {
        return ohlcs[cind]
    }

    override fun read(): Boolean {
        return ++cind < ohlcs.size
    }

    override fun startTime(): Instant {
        return ohlcs[0].dtGmtEnd
    }

    override fun endTime(): Instant {
        return ohlcs[ohlcs.size - 1].dtGmtEnd
    }

    override fun close() {
    }

}


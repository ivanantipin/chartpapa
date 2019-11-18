package firelib.common.reader

import firelib.common.misc.atUtc
import firelib.common.model.Div
import firelib.domain.Ohlc
import java.time.Instant
import java.time.ZoneOffset

class ReaderDivAdjusted(val delegate: MarketDataReader<Ohlc>, val divs : List<Div>) : MarketDataReader<Ohlc> by delegate{
    var nextDt : Instant = Instant.MAX
    var currentAdjustment : Double = 0.0
    init {
        reindex()
    }

    fun reindex(){
        val curr = delegate.current()
        val cidx = divs.indexOfFirst { it.lastDayWithDivs.plusDays(1).atStartOfDay().isAfter(curr.endTime.atUtc()) }
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
        if(ret.endTime.isAfter(nextDt)){
            reindex()
        }
        if(cachedValue.endTime != ret.endTime){
            cachedValue = ret.copy(open = ret.open + currentAdjustment,
                    high = ret.high + currentAdjustment,
                    low = ret.low + currentAdjustment,
                    close = ret.close + currentAdjustment, volume = ret.volume)
        }
        return cachedValue
    }
}
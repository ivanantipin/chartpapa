package firelib.core.store.reader

import firelib.core.misc.atUtc
import firelib.model.Div
import firelib.core.domain.Ohlc
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

class ReaderSimpleDivAdjusted(val delegate: SimplifiedReader, val divsin : List<Div>) : SimplifiedReader {

    var divs = divsin.sortedBy { it.lastDayWithDivs }

    var nextDt : Instant = Instant.MAX
    var currentAdjustment : Double = 0.0
    init {
        reindex()
    }

    fun reindex(){
        val curr = delegate.peek()
        if(curr == null){
            return
        }
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

    override fun peek(): Ohlc? {
        val ret = delegate.peek();
        if(ret == null){
            return null;
        }
        if(ret.endTime.isAfter(nextDt)){
            reindex()
        }
        return adjust(ret)
    }

    private fun adjust(ret: Ohlc) : Ohlc{
        if (cachedValue.endTime != ret.endTime) {
            cachedValue = ret.copy(
                open = ret.open + currentAdjustment,
                high = ret.high + currentAdjustment,
                low = ret.low + currentAdjustment,
                close = ret.close + currentAdjustment, volume = ret.volume
            )
        }
        return cachedValue
    }

    override fun poll(): Ohlc {
        return adjust(delegate.poll())
    }
}
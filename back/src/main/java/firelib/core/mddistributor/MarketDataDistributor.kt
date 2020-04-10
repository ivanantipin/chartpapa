package firelib.core.mddistributor

import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.timeseries.TimeSeries
import java.time.Instant

/**

 */
interface MarketDataDistributor {
    fun getOrCreateTs(tickerId: Int, interval: Interval, len: Int): TimeSeries<Ohlc>
    fun price(idx: Int): Ohlc
    fun addListener(interval: Interval, action: (Instant, MarketDataDistributor) -> Unit)
    val size : Int
}

fun MarketDataDistributor.getOrCreatTss(interval: Interval, len : Int) : List<TimeSeries<Ohlc>>{
    return (0 until size).map { getOrCreateTs(it,interval,len) }
}
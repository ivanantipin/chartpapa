package firelib.common.mddistributor

import firelib.domain.Interval
import firelib.common.core.timeseries.TimeSeries
import firelib.domain.Ohlc
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
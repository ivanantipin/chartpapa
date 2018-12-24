package firelib.common.mddistributor

import firelib.common.interval.Interval
import firelib.common.timeseries.TimeSeries
import firelib.domain.Ohlc

/**

 */
interface MarketDataDistributor {
    fun getOrCreateTs(tickerId: Int, interval: Interval, len: Int): TimeSeries<Ohlc>
    fun listenOhlc(idx : Int, lsn : (Ohlc)->Unit) : Unit
}
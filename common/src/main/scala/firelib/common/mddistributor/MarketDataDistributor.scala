package firelib.common.mddistributor

import firelib.common.interval.Interval
import firelib.common.misc.SubChannel
import firelib.common.timeseries.TimeSeries
import firelib.domain.{Ohlc, Tick}

/**

 */
trait MarketDataDistributor {

    def activateOhlcTimeSeries(tickerId: Int, interval: Interval, len: Int): TimeSeries[Ohlc]

    def listenTicks(idx : Int, lsn : Tick=>Unit) : Unit

    def listenOhlc(idx : Int, lsn : Ohlc=>Unit) : Unit

    def tickTopic(idx : Int) : SubChannel[Tick]

    def getTs(tickerId: Int, interval: Interval) : TimeSeries[Ohlc]

    def setTickTransformFunction(fun : (Tick) => Tick) : Unit
}

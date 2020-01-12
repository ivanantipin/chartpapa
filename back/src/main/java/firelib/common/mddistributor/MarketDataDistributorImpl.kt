package firelib.common.mddistributor

import firelib.common.interval.Interval
import firelib.common.interval.IntervalServiceImpl
import firelib.common.timeseries.TimeSeries
import firelib.domain.Ohlc
import java.time.Instant


class MarketDataDistributorImpl(override val size: Int, val startTime: Instant) : MarketDataDistributor {


    val intervalService = IntervalServiceImpl()

    val timeseries = (0 until size).map { TimeSeriesContainer(intervalService, startTime ) }.toTypedArray()


    fun addOhlc(idx : Int, ohlc: Ohlc){
        timeseries[idx].addOhlc(ohlc)
    }


    override fun price(idx: Int): Ohlc {
        return timeseries[idx].latestOhlc
    }


    override fun getOrCreateTs(idx: Int, interval: Interval, capacity: Int): TimeSeries<Ohlc> {
        return timeseries[idx].getOrCreateTs(interval, capacity)
    }

    fun roll(dt: Instant) {
        val rolledIntervals = intervalService.onStep(dt)
        rolledIntervals.forEach({ interval ->
            timeseries.forEach { ts -> ts.roll(interval, dt) }
        })
    }

    override fun addListener(interval: Interval, action: (Instant, MarketDataDistributor) -> Unit) {
        intervalService.addListener(interval) { time -> action(time, this) }
    }
}
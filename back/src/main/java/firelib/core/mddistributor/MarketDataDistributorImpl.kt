package firelib.core.mddistributor

import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.interval.IntervalServiceImpl
import firelib.core.timeseries.TimeSeries
import java.time.Instant


class MarketDataDistributorImpl(override val size: Int, val startTime: Instant, val rootInterval : Interval = Interval.Min10) : MarketDataDistributor {


    val intervalService = IntervalServiceImpl()

    val timeseries = (0 until size).map { TimeSeriesContainer(intervalService, startTime ) }.toTypedArray()


    fun addOhlc(idx : Int, ohlc: Ohlc){
        timeseries[idx].addOhlc(ohlc)
    }


    override fun price(idx: Int): Ohlc {
        return timeseries[idx].latestOhlc
    }


    override fun getOrCreateTs(idx: Int, interval: Interval, capacity: Int): TimeSeries<Ohlc> {
        require(interval.durationMs % rootInterval.durationMs == 0L) {"series interval ${interval} has to be divisible by root interval ${rootInterval}"}
        return timeseries[idx].getOrCreateTs(interval, capacity)
    }

    fun roll(dt: Instant) {
        val rolledIntervals = intervalService.onStep(dt)
        rolledIntervals.forEach { interval ->
            timeseries.forEach { ts -> ts.roll(interval, dt) }
        }
    }

    override fun addListener(interval: Interval, action: (Instant, MarketDataDistributor) -> Unit) {
        intervalService.addListener(interval) { time -> action(time, this) }
    }
}
package firelib.common.mddistributor

import firelib.common.interval.Interval
import firelib.common.interval.IntervalServiceImpl
import firelib.common.reader.MarketDataReader
import firelib.common.timeseries.TimeSeries
import firelib.common.timeseries.TimeSeriesImpl
import firelib.domain.Ohlc
import java.time.Instant


class MarketDataDistributorImpl(
        val readers: List<MarketDataReader<Ohlc>>
) : MarketDataDistributor {


    val timeseries = readers.map { TimeSeriesContainer(it) }.toTypedArray()

    val intervalService = IntervalServiceImpl()

    private val finalUpdates = ArrayList<() -> Unit>()

    fun initTimes(startTime: Instant) {
        timeseries.forEach { c ->
            for (ts in c.iterator()) {
                val start = ts.first.ceilTime(startTime)
                ts.second[0] = ts.second[0].copy(endTime = start)
            }
        }
    }

    override fun price(idx: Int): Ohlc {
        return readers[idx].current()
    }


    override fun getOrCreateTs(idx: Int, interval: Interval, capacity: Int): TimeSeries<Ohlc> {
        if (!timeseries[idx].contains(interval)) {
            return createTimeSeries(idx, interval, capacity)
        }
        val hist = timeseries[idx][interval]
        if (capacity > hist.capacity()) {
            hist.adjustCapacity(capacity)
        }
        return hist
    }

    fun readTillIncluding(prevTime: Instant, dt: Instant): Boolean {
        return timeseries.all { it.readTillIncluding(prevTime, dt) }
    }

    fun roll(dt: Instant) {
        intervalService.onStep(dt)
        finalUpdates.forEach { it() }
        finalUpdates.clear()
    }

    override fun addListener(interval: Interval, action: (Instant, MarketDataDistributor) -> Unit) {
        intervalService.addListener(interval) { action(it, this) }
    }

    private fun createTimeSeries(idx: Int, interval: Interval, length: Int = 100): TimeSeriesImpl<Ohlc> {
        val timeSeries = TimeSeriesImpl(length) { Ohlc() }

        timeseries[idx][interval] = timeSeries

        intervalService.addListener(interval) { time ->
            timeSeries.channel.publish(timeSeries)
            finalUpdates += {
                timeSeries += timeSeries[0].copy(endTime = time.plusMillis(interval.durationMs), interpolated = true)
            }
        }
        return timeSeries
    }

}
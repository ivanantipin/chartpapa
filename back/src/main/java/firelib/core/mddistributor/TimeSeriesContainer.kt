package firelib.core.mddistributor

import firelib.core.domain.Interval
import firelib.core.interval.IntervalService
import firelib.core.timeseries.TimeSeries
import firelib.core.timeseries.TimeSeriesImpl
import firelib.core.domain.Ohlc
import java.time.Instant

class TimeSeriesContainer(val intervalService: IntervalService, val startTime : Instant) {

    private val tss = mutableListOf<TimeSeriesImpl<Ohlc>>()

    private val map = mutableMapOf<Interval, TimeSeriesImpl<Ohlc>>()

    fun iterator(): List<Pair<Interval, TimeSeries<Ohlc>>> {
        return map.map { Pair(it.key, it.value) }
    }


    var latestOhlc : Ohlc = Ohlc()

    fun addOhlc(ohlc: Ohlc) {
        latestOhlc = ohlc

        tss.forEach { ts ->
            ts[0] = mergeOhlc(ts[0], ohlc)
        }
    }

    fun roll(interval: Interval, dt: Instant){
        val ts = map[interval]
        if(ts != null){
            ts += ts[0].copy(endTime = dt.plusMillis(interval.durationMs), interpolated = true)
        }
    }


    fun contains(interval: Interval): Boolean {
        return map.contains(interval)
    }

    fun getOrCreateTs(interval: Interval, capacity: Int): TimeSeries<Ohlc> {
        if (!contains(interval)) {
            return createTimeSeries(interval, capacity)
        }
        val hist = get(interval)
        if (capacity > hist.capacity()) {
            hist.adjustCapacity(capacity)
        }
        return hist
    }

    private fun createTimeSeries(interval: Interval, length: Int = 100): TimeSeriesImpl<Ohlc> {
        val timeSeries = TimeSeriesImpl(length) { Ohlc() }
        timeSeries[0] = Ohlc(endTime = interval.ceilTime(startTime), interpolated = true)
        map[interval] = timeSeries
        tss += timeSeries

        intervalService.addListener(interval) { time ->
            timeSeries.channel.publish(timeSeries)
        }
        return timeSeries
    }



    operator fun get(interval: Interval): TimeSeriesImpl<Ohlc> {
        return map[interval] as TimeSeriesImpl<Ohlc>
    }

    fun mergeOhlc(currOhlc: Ohlc, ohlc: Ohlc): Ohlc {
        require(!ohlc.interpolated, { "should not be interpolated" })

        if (currOhlc.interpolated) {
            require(!ohlc.endTime.isAfter(currOhlc.endTime), { "curr ohlc ${currOhlc.endTime} < to be merged  ${ohlc.endTime}" })
            return ohlc.copy(endTime = currOhlc.endTime, interpolated = false)
        } else {
            require(!ohlc.endTime.isAfter(currOhlc.endTime), { "curr ohlc ${currOhlc.endTime} < to be merged  ${ohlc.endTime}" })

            return currOhlc.copy(high = Math.max(ohlc.high, currOhlc.high),
                    low = Math.min(ohlc.low, currOhlc.low),
                    close = ohlc.close,
                    Oi = currOhlc.Oi + ohlc.Oi,
                    volume = currOhlc.volume + ohlc.volume
            )
        }
    }


}


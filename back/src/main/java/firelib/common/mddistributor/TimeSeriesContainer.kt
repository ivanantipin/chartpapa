package firelib.common.mddistributor

import firelib.common.interval.Interval
import firelib.common.timeseries.TimeSeries
import firelib.common.timeseries.TimeSeriesImpl
import firelib.domain.Ohlc

class TimeSeriesContainer() {

    private val timeSeries = ArrayList<TimeSeries<Ohlc>>()

    private val map = HashMap<Interval, TimeSeries<Ohlc>>()

    fun iterator(): List<Pair<Interval, TimeSeries<Ohlc>>> {
        return map.map { Pair(it.key, it.value) }
    }


    fun addOhlc(ohlc: Ohlc) {
        timeSeries.forEach { it[0] = mergeOhlc(it[0], ohlc) }
    }

    operator fun set(interval: Interval, ts: TimeSeries<Ohlc>) {
        map[interval] = ts
        timeSeries += ts
    }

    fun contains(interval: Interval): Boolean {
        return map.contains(interval)
    }

    operator fun get(interval: Interval): TimeSeriesImpl<Ohlc> {
        return map[interval] as TimeSeriesImpl<Ohlc>
    }

    fun mergeOhlc(currOhlc: Ohlc, ohlc: Ohlc): Ohlc {
        assert(!ohlc.interpolated, {"should not be interpolated"})

        if (currOhlc.interpolated) {
            return ohlc.copy(dtGmtEnd = currOhlc.dtGmtEnd, interpolated = false)
        } else {
            return currOhlc.copy(high = Math.max(ohlc.high, currOhlc.high),
                    low = Math.min(ohlc.low, currOhlc.low),
                    close = ohlc.close,
                    Oi = currOhlc.Oi + ohlc.Oi,
                    volume = currOhlc.volume + ohlc.volume
            )
        }
    }


}
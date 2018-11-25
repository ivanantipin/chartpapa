package firelib.common.mddistributor

import firelib.common.interval.Interval
import firelib.common.misc.OhlcBuilderFromOhlc
import firelib.common.timeseries.TimeSeries
import firelib.common.timeseries.TimeSeriesImpl
import firelib.domain.Ohlc

class TimeSeriesContainer() {

    private val timeSeries = ArrayList<TimeSeries<Ohlc>>()

    private val map = HashMap<Interval, TimeSeries<Ohlc>>()

    fun iterator(): List<Pair<Interval, TimeSeries<Ohlc>>> {
        return map.map { Pair(it.key, it.value) }
    }

    val ohlcFromOhlc = OhlcBuilderFromOhlc()

    fun addOhlc(ohlc: Ohlc) {
        timeSeries.forEach({ it[0] = ohlcFromOhlc.mergeOhlc(it[0], ohlc) })
    }

    fun addTs(interval: Interval, ts: TimeSeries<Ohlc>): Unit {
        map[interval] = ts
        timeSeries += ts
    }

    fun contains(interval: Interval): Boolean {
        return map.contains(interval)
    }

    fun getTs(interval: Interval): TimeSeriesImpl<Ohlc> {
        return map[interval] as TimeSeriesImpl<Ohlc>
    }

}
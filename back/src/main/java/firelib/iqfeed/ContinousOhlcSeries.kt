package firelib.iqfeed

import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.domain.merge
import java.util.concurrent.atomic.AtomicReference

class ContinousOhlcSeries(val interval: Interval) {
    var lastFetchedTs: Long = -1L

    private val data = mutableListOf<Ohlc>()

    val cache = AtomicReference<Pair<List<Ohlc>, Long>>()

    fun getDataSafe(): List<Ohlc> {
        val ret = cache.get()
        if (ret == null || ret.second != lastFetchedTs) {
            val dt = mutableListOf<Ohlc>().apply {
                addAll(data)
            }
            cache.set(dt to lastFetchedTs)
        }
        return cache.get().first
    }

    fun add(ohlcsIn: List<Ohlc>) {
        val ohlcs = ohlcsIn.filter { it.endTime.toEpochMilli() > lastFetchedTs }
        if (ohlcs.isEmpty()) {
            return
        }

        if (data.isEmpty()) {
            data.add(ohlcs[0].copy(volume = 0, endTime = interval.ceilTime(ohlcs[0].endTime)))
        }

        var nextTime = interval.ceilTime(data.last().endTime)

        for (ohlc in ohlcs) {
            if (ohlc.endTime.isAfter(nextTime)) {
                nextTime = interval.ceilTime(ohlc.endTime)
                data.add(ohlc.copy(endTime = nextTime))
            } else {
                data[data.size - 1] = data.last().merge(ohlc)
            }
        }

        lastFetchedTs = ohlcs.last().endTime.toEpochMilli()
    }
}
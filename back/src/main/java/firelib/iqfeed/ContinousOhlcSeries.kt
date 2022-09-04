package firelib.iqfeed

import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.domain.merge

class ContinousOhlcSeries(val interval: Interval) {
    var lastFetchedTs: Long = -1L
    var data = mutableListOf<Ohlc>()

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
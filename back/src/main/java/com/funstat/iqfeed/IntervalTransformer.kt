package com.funstat.iqfeed

import firelib.domain.Ohlc
import firelib.common.interval.Interval
import firelib.domain.merge

import java.util.*

object IntervalTransformer {

    fun transform(interval: Interval, ohlcs: List<Ohlc>): List<Ohlc> {
        if (ohlcs.isEmpty()) {
            return ohlcs
        }
        var init = ohlcs[0]
        var nextTime = interval.ceilTime(ohlcs[0].endTime)
        val ret = ArrayList<Ohlc>()
        for (ohlc in ohlcs) {
            if (ohlc.endTime.isAfter(nextTime)) {
                ret.add(init.copy(endTime = nextTime))
                while (ohlc.endTime.isAfter(nextTime)){
                    //ret.add(init.copy(dtGmtEnd = nextTime))
                    nextTime = nextTime.plus(interval.duration)
                }
                init = ohlc
            }
            init = init.merge(ohlc)
        }
        ret.add(init.copy(endTime = nextTime))

        return ret
    }
}

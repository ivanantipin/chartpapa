package com.funstat.iqfeed

import firelib.domain.Ohlc
import firelib.common.interval.Interval
import java.time.Instant

import java.time.LocalDateTime
import java.util.*

object IntervalTransformer {

    fun transform(interval: Interval, ohlcs: List<Ohlc>): List<Ohlc> {
        if (ohlcs.isEmpty()) {
            return ohlcs
        }
        val start = System.currentTimeMillis()
        println((System.currentTimeMillis() - start).toString() + " number is " + ohlcs.size)
        var init = ohlcs[0].copy()
        var nextTime = interval.ceilTime(ohlcs[0].dtGmtEnd)
        val ret = ArrayList<Ohlc>()
        for (ohlc in ohlcs) {
            if (ohlc.dtGmtEnd.isAfter(nextTime)) {
                nextTime = interpolateTillEnd(interval, init, nextTime, ret, ohlc.dtGmtEnd)
                init = ohlc
            }
            init = init.merge(ohlc)
        }

        return ret
    }

    private fun interpolateTillEnd(interval: Interval, init: Ohlc, nextTime: Instant, ret: MutableList<Ohlc>, currTime: Instant): Instant {
        var nextTime = nextTime
        while (currTime.isAfter(nextTime)) {
            ret.add(init.copy(dtGmtEnd = nextTime))
            nextTime = nextTime.plus(interval.duration)
        }
        return nextTime
    }


}

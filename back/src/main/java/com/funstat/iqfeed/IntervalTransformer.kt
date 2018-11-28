package com.funstat.iqfeed

import com.funstat.domain.Ohlc
import firelib.common.interval.Interval

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
        var nextTime = interval.ceilTime(ohlcs[0].dateTime)
        val ret = ArrayList<Ohlc>()
        for (ohlc in ohlcs) {
            if (ohlc.dateTime.isAfter(nextTime)) {
                nextTime = interpolateTillEnd(interval, init, nextTime, ret, ohlc.dateTime)
                init = ohlc
            }
            init = init.merge(ohlc)
        }

        return ret
    }

    private fun interpolateTillEnd(interval: Interval, init: Ohlc, nextTime: LocalDateTime, ret: MutableList<Ohlc>, currTime: LocalDateTime): LocalDateTime {
        var nextTime = nextTime
        while (currTime.isAfter(nextTime)) {
            ret.add(init.copy(dateTime = nextTime))
            nextTime = nextTime.plus(interval.duration)
        }
        return nextTime
    }


}

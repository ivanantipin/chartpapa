package com.funstat.domain


import java.time.LocalDateTime

data class TimePoint(
                     val time: LocalDateTime,

                     val value: Double) : Comparable<TimePoint> {

    override fun compareTo(timePoint: TimePoint): Int {
        return this.time.compareTo(timePoint.time)
    }
}
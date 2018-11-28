package com.funstat.domain

import io.swagger.annotations.ApiModelProperty

import java.time.LocalDateTime

data class TimePoint(@get:ApiModelProperty(required = true)
                     val time: LocalDateTime,
                     @get:ApiModelProperty(required = true)
                     val value: Double) : Comparable<TimePoint> {

    override fun compareTo(timePoint: TimePoint): Int {
        return this.time.compareTo(timePoint.time)
    }
}
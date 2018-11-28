package com.funstat.domain

import io.swagger.annotations.ApiModelProperty

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Optional

data class Ohlc(@get:ApiModelProperty(required = true)
           val dateTime: LocalDateTime, @get:ApiModelProperty(required = true)
           val open: Double, @get:ApiModelProperty(required = true)
           val high: Double, @get:ApiModelProperty(required = true)
           val low: Double, @get:ApiModelProperty(required = true)
           val close: Double) : Comparable<Ohlc> {

    val range: Double
        get() = high - low

    val volume: Double
        @ApiModelProperty(required = true)
        get() = 0.0

    init {
        assert(high > Math.max(Math.max(close, open), low)) {"not valid high " + this}
        assert(low < Math.min(Math.min(close, open), high)) {"not valid low " + this}
    }

    constructor(ohlc: Ohlc) : this(ohlc.dateTime, ohlc.open, ohlc.high, ohlc.low, ohlc.close) {}

    fun merge(ohlc: Ohlc): Ohlc {
        return Ohlc(ohlc.dateTime, open,
                Math.max(high, ohlc.high),
                Math.min(low, ohlc.low),
                ohlc.close
        )
    }


    override fun compareTo(ohlc: Ohlc): Int {
        return this.dateTime.compareTo(ohlc.dateTime)
    }

    companion object {

        internal var pattern = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss")

        fun parse(str: String): Optional<Ohlc> {
            return parseWithPattern(str, pattern)
        }

        fun parseWithPattern(str: String, pattern: DateTimeFormatter): Optional<Ohlc> {
            try {
                val arr = str.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                return Optional.of(Ohlc(LocalDateTime.parse(arr[0] + " " + arr[1], pattern),
                        java.lang.Double.parseDouble(arr[2]),
                        java.lang.Double.parseDouble(arr[3]), java.lang.Double.parseDouble(arr[4]), java.lang.Double.parseDouble(arr[5])
                ))
            } catch (e: Exception) {
                println("not valid entry " + str + " because " + e.message)
                return Optional.empty()
            }

        }
    }

}


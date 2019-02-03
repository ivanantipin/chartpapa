package firelib.domain

import firelib.common.misc.toStandardString
import io.swagger.annotations.ApiModelProperty
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

data class Ohlc(
        @get:ApiModelProperty(required = true)
        var dtGmtEnd: Instant = Instant.ofEpochSecond(0),
        @get:ApiModelProperty(required = true)
        var open: Double = Double.NaN,
        @get:ApiModelProperty(required = true)
        var high: Double = Double.MIN_VALUE,
        @get:ApiModelProperty(required = true)
        var low: Double = Double.MAX_VALUE,
        @get:ApiModelProperty(required = true)
        var close: Double = .0,
        var Oi: Int = 0,
        @get:ApiModelProperty(required = true)
        var volume: Int = 0,
        var interpolated: Boolean = true
) : Timed, Comparable<Ohlc> {

    override fun compareTo(other: Ohlc): Int {
        return this.dtGmtEnd.compareTo(other.dtGmtEnd)
    }


    //FIXME make immutable - require some core refactoring

    fun merge(ohlc: Ohlc): Ohlc {
        return Ohlc(ohlc.dtGmtEnd, open,
                Math.max(high, ohlc.high),
                Math.min(low, ohlc.low),
                ohlc.close
        )
    }

    @get:ApiModelProperty(required = true)
    val dateTimeMs = dtGmtEnd.toEpochMilli()


    fun nprice(): Double = (close + high + low) / 3

    fun medium(): Double = (high + low) / 2

    fun isUpBar(): Boolean = close > open

    fun range(): Double = high - low

    fun upShadow(): Double = high - close

    fun downShadow(): Double = close - low

    fun bodyLength(): Double = Math.abs(close - open)

    fun ret(): Double = close - open

    fun isInRange(vv: Double): Boolean = high > vv && vv > low

    override fun toString(): String {
        val dtStr: String = dtGmtEnd.toStandardString()
        return "OHLC($open/$high/$low/$close/$dtStr/$interpolated)"
    }


    override fun time(): Instant = dtGmtEnd


    companion object {

        internal var pattern = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss")

        fun parse(str: String): Optional<Ohlc> {
            return parseWithPattern(str, pattern)
        }

        fun parseWithPattern(str: String, pattern: DateTimeFormatter): Optional<Ohlc> {
            try {
                val arr = str.split(";")
                return Optional.of(Ohlc(LocalDateTime.parse(arr[0] + " " + arr[1], pattern).toInstant(ZoneOffset.UTC),
                        java.lang.Double.parseDouble(arr[2]),
                        java.lang.Double.parseDouble(arr[3]),
                        java.lang.Double.parseDouble(arr[4]),
                        java.lang.Double.parseDouble(arr[5])
                ))
            } catch (e: Exception) {
                println("not valid entry " + str + " because " + e.message)
                return Optional.empty()
            }

        }
    }


}


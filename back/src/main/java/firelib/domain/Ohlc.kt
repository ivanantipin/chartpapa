package firelib.domain

import firelib.common.misc.atUtc
import firelib.common.misc.toStandardString

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

data class Ohlc(

        var dtGmtEnd: Instant = Instant.ofEpochSecond(0),

        var open: Double = Double.NaN,

        var high: Double = Double.MIN_VALUE,

        var low: Double = Double.MAX_VALUE,

        var close: Double = .0,
        var Oi: Int = 0,

        var volume: Long = 0,
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
                ohlc.close, volume = ohlc.volume + volume
        )
    }

    fun merge(price : Double, vol: Long): Ohlc {
        if(interpolated){
            return Ohlc(dtGmtEnd,price,price,price,price,volume = vol, interpolated = false)
        }
        return Ohlc(dtGmtEnd, open,
                high=Math.max(high, price),
                low=Math.min(low, price),
                close=price, volume = vol + volume
        )
    }



    val dateTimeMs = dtGmtEnd.toEpochMilli()


    fun nprice(): Double = (close + high + low) / 3

    fun medium(): Double = (high + low) / 2

    fun isUpBar(): Boolean = close > open

    fun range(): Double = high - low

    fun upShadow(): Double = high - close

    fun downShadow(): Double = close - low

    fun bodyLength(): Double = Math.abs(close - open)

    fun ret(): Double = (close - open)/open

    fun isInRange(vv: Double): Boolean = high > vv && vv > low

    override fun toString(): String {
        val dtStr: String = dtGmtEnd.toStandardString()
        return "OHLC($open/$high/$low/$close/$dtStr/$interpolated)"
    }


    override fun time(): Instant = dtGmtEnd

    fun date() : LocalDate {
        return dtGmtEnd.atUtc().toLocalDate()!!
    }

    companion object {

        internal var pattern = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss")

        fun parse(str: String): Ohlc? {
            return parseWithPattern(str, pattern)
        }

        fun parseWithPattern(str: String, pattern: DateTimeFormatter): Ohlc? {
            try {
                val arr = str.split(";")
                return Ohlc(LocalDateTime.parse(arr[0] + " " + arr[1], pattern).toInstant(ZoneOffset.UTC),
                        arr[2].toDouble(),
                        arr[3].toDouble(),
                        arr[4].toDouble(),
                        arr[5].toDouble(),
                        volume = arr[6].toLong())
            } catch (e: Exception) {
                println("not valid entry " + str + " because " + e.message)
                return null
            }

        }
    }


}


fun main() {
    Ohlc.parse("20140821;11:30:00;0.0090000;0.0090300;0.0089100;0.0089900;5972800000")
}
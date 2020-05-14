package firelib.core.domain

import firelib.core.misc.atNy
import firelib.core.misc.dateFormat
import firelib.core.misc.toStandardString
import firelib.core.misc.toStringAtMoscow
import java.time.Instant

data class Ohlc(
        var endTime: Instant = Instant.ofEpochSecond(0),
        var open: Double = Double.NaN,
        var high: Double = Double.MIN_VALUE,
        var low: Double = Double.MAX_VALUE,
        var close: Double = .0,
        var Oi: Int = 0,
        var volume: Long = 0,
        var interpolated: Boolean = true
) : Comparable<Ohlc>, Timed {
    override fun time(): Instant {
        return endTime
    }

    override fun compareTo(other: Ohlc): Int {
        return this.endTime.compareTo(other.endTime)
    }

    override fun toString(): String {
        val dtStr: String = endTime.toStandardString()
        return "OHLC($open/$high/$low/$close/$dtStr/${volume}/$interpolated)"
    }

    fun toStrMtDay() : String {
        val dtStr: String = endTime.atNy().format(dateFormat)
        return "${dtStr},${"%.5f".format(open)},${"%.5f".format(high)},${"%.5f".format(low)},${"%.5f".format(close)},${volume}"
    }

}
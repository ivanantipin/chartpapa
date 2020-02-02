package firelib.core.domain

import firelib.common.misc.toStandardString
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
}
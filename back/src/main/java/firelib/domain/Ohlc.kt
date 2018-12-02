package firelib.domain

import firelib.common.misc.toStandardString
import java.time.Instant

data class Ohlc(var dtGmtEnd: Instant = Instant.ofEpochSecond(0),
                var O: Double = Double.NaN,
                var H: Double = Double.MIN_VALUE,
                var L: Double = Double.MAX_VALUE,
                var C: Double = .0,
                var Oi: Int = 0,
                var Volume: Int = 0) : Timed {

    //FIXME make immutable - require some core refactoring

    var interpolated: Boolean = true

    fun nprice(): Double = (C + H + L) / 3

    fun medium(): Double = (H + L) / 2

    fun isUpBar(): Boolean = C > O

    fun range(): Double = H - L

    fun upShadow(): Double = H - C

    fun downShadow(): Double = C - L

    fun bodyLength(): Double = Math.abs(C - O)

    fun ret(): Double = C - O

    fun isInRange(vv: Double): Boolean = H > vv && vv > L

    override fun toString(): String {
        val dtStr: String = dtGmtEnd.toStandardString()
        return "OHLC($O/$H/$L/$C/$dtStr/$interpolated)"
    }


    override fun time(): Instant = dtGmtEnd

}


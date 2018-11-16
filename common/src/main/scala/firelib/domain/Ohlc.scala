package firelib.domain

import java.time.Instant

import firelib.common.misc.dateUtils._

import scala.beans.BeanProperty

class Ohlc() extends Timed {

    //FIXME make immutable - require some core refactoring

    var interpolated: Boolean = true

    def nprice: Double = (C + H + L) / 3

    def medium: Double = (H + L) / 2

    def isUpBar: Boolean = C > O

    def range: Double = H - L

    def upShadow: Double = H - C

    def downShadow: Double = C - L

    def bodyLength: Double = Math.abs(C - O)

    def ret: Double = C - O

    def isInRange(vv: Double): Boolean = H > vv && vv > L

    override def toString: String = {
        val dtStr: String = dtGmtEnd.toStandardString
        return s"OHLC($O/$H/$L/$C/$dtStr/$interpolated)"
    }


    def time: Instant = dtGmtEnd

    @BeanProperty
    var C: Double = .0
    @BeanProperty
    var dtGmtEnd: Instant = Instant.ofEpochSecond(0)
    @BeanProperty
    var H: Double = Integer.MIN_VALUE
    @BeanProperty
    var L: Double = Integer.MAX_VALUE
    @BeanProperty
    var O: Double = Double.NaN
    @BeanProperty
    var Oi: Int = 0
    @BeanProperty
    var Volume: Int = 0
}

object Ohlc{
    def apply(dt : Instant, o : Double, h : Double, l : Double, c : Double) : Ohlc = {
        new Ohlc{
            dtGmtEnd = dt
            O = o
            H = h
            L = c
        }
    }
}

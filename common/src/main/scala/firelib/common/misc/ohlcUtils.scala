package firelib.common.misc

import firelib.domain.Ohlc


object ohlcUtils{

    def interpolate(from: Ohlc) : Ohlc = {
        val to = new Ohlc
        interpolate(from,to)
        to
    }

    def interpolate(from: Ohlc, to : Ohlc) : Unit = {
        to.O = from.O
        to.H = from.H
        to.L = from.L
        to.C = from.C
        to.Oi = from.Oi
        to.interpolated = true
    }

    def copy(from: Ohlc) : Ohlc ={
        val to = interpolate(from)
        to.dtGmtEnd = from.dtGmtEnd
        to.interpolated = from.interpolated
        return to;
    }
}











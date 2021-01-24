package com.firelib.techbot

import com.firelib.techbot.domain.LineType
import firelib.core.domain.Ohlc

data class TdLine(val x0: Int, val x1: Int, val y0: Double, val y1: Double,
                  val lineType: LineType,
                  val xM : Int, val yM : Double) {

    val slope = (y0 - y1) / (x0 - x1)
    val intercept = (x0 * y1 - x1 * y0) / (x0 - x1)

    fun calcValue(idx: Int): Double {
        return slope * idx + intercept
    }

    fun span() : Int{
        return x1 - x0
    }

    var intersectPoint : Pair<Int,Double>? = null

    fun rangeIntersected(
        rangeStart: Int,
        rangeEnd: Int,
        prices: List<Double>
    ): Boolean {
        return (rangeStart until rangeEnd).any { lineType.priceCmp(prices[it], calcValue(it)) }
    }

    fun toDated(ohlcs : List<Ohlc>) : TdLineDated{
        return TdLineDated(ohlcs[x0].endTime.toEpochMilli(), ohlcs[x1].endTime.toEpochMilli(), y0, y1, lineType)
    }
}

data class TdLineDated(val x0 : Long, val x1 : Long, val y0 : Double, val y1 : Double, val lineType: LineType){

    fun toTdLine(ohlcs : List<Ohlc>) : TdLine{
        val first = ohlcs.indexOfFirst{ it.endTime.toEpochMilli() == x0 }
        val last = ohlcs.indexOfFirst { it.endTime.toEpochMilli() == x1 }
        return TdLine(first, last, ohlcs[first].high, ohlcs[last].high, lineType, 0, 0.0)
    }
}
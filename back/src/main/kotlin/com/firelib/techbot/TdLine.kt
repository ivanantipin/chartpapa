package com.firelib.techbot

import com.firelib.techbot.domain.LineType


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
}
package com.firelib.trend

import com.github.kotlintelegrambot.echo.chart.SensitivityConfig
import firelib.telbot.TimeFrame
import com.github.kotlintelegrambot.echo.com.firelib.trend.TdLine
import firelib.core.domain.Ohlc
import org.apache.commons.math3.stat.regression.SimpleRegression
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.math.min


data class LineConfig(val pivotOrder : Int, val rSquare : Double)

object BotConfig{
    var pivotOrders: Map<Pair<String, String>, Int> = emptyMap()
    var rSquares: Map<Pair<String, String>, Double> = emptyMap()
    var lineRSquare: Double = 0.995
    var window: Long = 300
    var pivotOrder = 10

    init {
        transaction {
            rSquares = SensitivityConfig.selectAll()
                .associateBy({ Pair(it[SensitivityConfig.ticker], it[SensitivityConfig.timeframe]) },
                    { it[SensitivityConfig.rSquare] })

            pivotOrders = SensitivityConfig.selectAll()
                .associateBy({ Pair(it[SensitivityConfig.ticker], it[SensitivityConfig.timeframe]) },
                    { it[SensitivityConfig.pivotOrder] })
        }
    }

    fun getConf(ticker: String, timeFrame: TimeFrame) : LineConfig{
        val ret = LineConfig(getPivot(ticker, timeFrame), getRSquare(ticker, timeFrame))
        println("returning config for ${ticker} frame ${timeFrame} : ${ret}")
        return ret
    }

    fun getRSquare(ticker : String, timeFrame : TimeFrame) : Double{
        return rSquares.getOrDefault(Pair(ticker, timeFrame.name), lineRSquare)
    }

    fun getPivot(ticker : String, timeFrame : TimeFrame) : Int{
        return pivotOrders.getOrDefault(Pair(ticker, timeFrame.name), pivotOrder)
    }



}

object TrendsCreator {



    enum class LineType(val priceCmp: (price: Double, other: Double) -> Boolean) {
        Support({ price, other -> price < other }), Resistance({ price, other -> price > other });
    }

    fun makeLines(
        pivots: List<Int>,
        prices: List<Double>,
        intersectPrices: List<Double>,
        lineType: LineType,
        rSquare : Double
    ): List<TdLine> {

        val cache = mutableMapOf<Pair<Int, Int>, Boolean>()


        fun checkRangeNonIntersected(x0: Int, x1: Int): Boolean {
            return cache.computeIfAbsent(Pair(x0, x1), { P ->
                val line = TdLine(x0, x1, prices[x0], prices[x1], lineType, 0, 0.0)
                !line.rangeIntersected(x0, x1, intersectPrices)
            })
        }

        val ret = mutableMapOf<Pair<Int,Int>,TdLine>()



        for (si in pivots.indices) {
            val s = pivots[si]
            for (mi in si + 1 until pivots.size) {
                val m = pivots[mi]
                if (checkRangeNonIntersected(s, m)) {
                    for (ei in mi + 1 until pivots.size) {
                        val e = pivots[ei]
                        if ( !ret.containsKey(Pair(s,e)) && checkRangeNonIntersected(m, e)) {
                            val regr = SimpleRegression()
                            regr.addData(s.toDouble(), prices[s])
                            regr.addData(m.toDouble(), prices[m])
                            regr.addData(e.toDouble(), prices[e])
                            if (regr.rSquare > rSquare) {
                                ret.put(Pair(s,e),TdLine(s, e, prices[s], prices[e], lineType, m, prices[m]))
                            }
                        }
                    }
                }
            }
        }

        for(l in ret.values){
            for(x in l.x1 + 1 until (min(l.x1 + l.span(), intersectPrices.size))){
                if(lineType.priceCmp(intersectPrices[x], l.calcValue(x) )){
                    l.intersectPoint = Pair(x, l.calcValue(x))
                    break
                }
            }
        }

        return ret.values.toList()
    }


    fun findRegresLines(ohlcs: List<Ohlc>, conf : LineConfig): List<TdLine> {
        val highs = ohlcs.map { it.high }
        val lows = ohlcs.map { it.low }
        val closes = ohlcs.map { it.close }
        val resistancePivots = findSimplePivots(highs, conf.pivotOrder, LineType.Resistance)
        val supportPivots = findSimplePivots(lows, conf.pivotOrder, LineType.Support)
        return makeLines(resistancePivots, highs, closes, LineType.Resistance, conf.rSquare) + makeLines(
            supportPivots,
            lows,
            closes,
            LineType.Support, conf.rSquare
        )
    }

    fun findSimplePivots(
        prices: List<Double>,
        pivotOrder: Int,
        lineType: LineType
    ): List<Int> {
        var extremeIndex = 0
        val ret = mutableListOf<Int>()
        for (r in prices.indices) {

            if (lineType.priceCmp(prices[r], prices[extremeIndex])) {
                extremeIndex = r
            }

            if (extremeIndex < r - 2 * pivotOrder) {
                extremeIndex = r - 2 * pivotOrder
                for (i in r - 2 * pivotOrder..r) {
                    if (lineType.priceCmp(prices[i], prices[extremeIndex])) {
                        extremeIndex = i
                    }
                }
            }

            if (r - pivotOrder == extremeIndex && extremeIndex >= pivotOrder) {
                ret += extremeIndex
            }
        }
        return ret
    }


    fun findTdPivots(
        prices: List<Double>,
        pivotOrder: Int
    ): Pair<List<Int>, List<Int>> {
        var counter = 0

        val pivots = prices.mapIndexed { idx, it ->
            if (idx == 0) {
                counter = 0
            } else if (prices[idx] > prices[idx - 1]) {
                if (counter > 0) {
                    counter++
                } else {
                    counter = 1
                }
            } else {
                if (counter < 0) {
                    counter--
                } else {
                    counter = -1
                }
            }
            counter
        }

        val highs = mutableListOf<Int>()
        val lows = mutableListOf<Int>()

        pivots.forEachIndexed { idx, it ->
            if (it == -pivotOrder && pivots[idx - pivotOrder] >= pivotOrder) {
                highs += idx - pivotOrder
            }
            if (it == pivotOrder && pivots[idx - pivotOrder] <= -pivotOrder) {
                lows += idx - pivotOrder
            }

        }
        return Pair(lows, highs)
    }

}

fun main() {
    val regr = SimpleRegression()
    regr.addData(105.0, 240.75)
    regr.addData(130.0, 236.5)
    regr.addData(154.0, 232.97)

    println(regr.rSquare)

}
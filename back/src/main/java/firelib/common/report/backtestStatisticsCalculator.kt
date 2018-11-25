package firelib.common.report

import java.time.Duration

import firelib.common.Trade
import firelib.common.misc.pnlForCase
import org.apache.commons.math3.stat.descriptive.moment.Mean
import org.apache.commons.math3.stat.descriptive.moment.Variance


/**
((List<Pair<Trade, Trade>>) -> Map<StrategyMetric, Double>)
 */
fun statCalculator(tradingCases: List<Pair<Trade, Trade>>): Map<StrategyMetric, Double> {
    val ret = HashMap<StrategyMetric, Double>()

    ret[StrategyMetric.Trades] = tradingCases.size.toDouble()


    var maxPnl = 0.0
    var maxLoss = 0.0
    var pnl = 0.0
    var onlyLoss = 0.0
    var onlyProfit = 0.0
    var maxProfitsInRow = 0
    var maxLossesInRow = 0
    var lossCount = 0
    var currProfitsInRow = 0
    var currLossesInRow = 0
    var maxDrawDown = 0.0
    var maxReachedPnl = 0.0

    var holdingPeriodMins = 0
    var holdingPeriodMinsStat = ArrayList<Int>()
    var holdingPeriodSecs = 0.0


    var pnls : List<Double> = tradingCases.map({pnlForCase(it)})


    val variance = Variance().evaluate(pnls.toDoubleArray())
    val mean = Mean().evaluate(pnls.toDoubleArray())

    ret[StrategyMetric.Sharpe] = mean / variance

    for (cas in tradingCases) {
        if (cas.first.qty != cas.second.qty) {
            throw Exception("trading must have same volume")
        }
        if (cas.first.side() == cas.second.side()) {
            throw Exception("trading must have different sides")
        }
        val pn = pnlForCase(cas)
        pnl += pn

        val ddelta = Duration.between(cas.first.dtGmt, cas.second.dtGmt)
        holdingPeriodMins += ddelta.toMinutes().toInt()

        holdingPeriodMinsStat.add(ddelta.toMinutes().toInt())

        holdingPeriodSecs += ddelta.getSeconds().toInt()

        if (pnl > maxReachedPnl) {
            maxReachedPnl = pnl
        }
        if (maxDrawDown < maxReachedPnl - pnl) {
            maxDrawDown = maxReachedPnl - pnl
        }
        maxPnl = Math.max(pn, maxPnl)
        maxLoss = Math.min(pn, maxLoss)
        if (pn < 0) {
            lossCount += 1
            onlyLoss += pn
            currProfitsInRow = 0
            currLossesInRow += 1
            maxLossesInRow = Math.max(maxLossesInRow, currLossesInRow)
        }
        else {
            onlyProfit += pn
            currLossesInRow = 0
            currProfitsInRow += 1
            maxProfitsInRow = Math.max(maxProfitsInRow, currProfitsInRow)
        }
    }


    ret[StrategyMetric.Pnl] = pnl
    ret[StrategyMetric.Pf] = Math.min(-onlyProfit / onlyLoss, 4.toDouble())
    ret[StrategyMetric.MaxDdStat] = maxDrawDown
    ret[StrategyMetric.AvgPnl] = pnl / tradingCases.size
    ret[StrategyMetric.MaxProfit] = maxPnl
    ret[StrategyMetric.MaxLoss] = maxLoss
    ret[StrategyMetric.MaxLossesInRow] = maxLossesInRow.toDouble()
    ret[StrategyMetric.MaxProfitsInRow] = maxProfitsInRow.toDouble()
    ret[StrategyMetric.ProfitLosses] = (tradingCases.size - lossCount) / tradingCases.size.toDouble()
    ret[StrategyMetric.AvgLoss] = (onlyLoss) / lossCount
    ret[StrategyMetric.AvgProfit] = onlyProfit / (tradingCases.size - lossCount)
    if (tradingCases.size != 0)
        ret[StrategyMetric.AvgHoldingPeriodMin] = holdingPeriodMins.toDouble() / tradingCases.size

    holdingPeriodMinsStat.sort()
    if (holdingPeriodMinsStat.size > 3) {
        ret[StrategyMetric.MedianHoldingPeriodMin] = holdingPeriodMinsStat[holdingPeriodMinsStat.size / 2].toDouble()
    }
    if (tradingCases.size != 0)
        ret[StrategyMetric.AvgHoldingPeriodSec] = holdingPeriodSecs / tradingCases.size
    return ret
}

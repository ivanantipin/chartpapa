package firelib.common.core.report

import firelib.common.Trade
import firelib.common.misc.pnl
import org.apache.commons.math3.stat.descriptive.moment.Mean
import org.apache.commons.math3.stat.descriptive.moment.Variance
import java.time.Duration
import kotlin.math.max
import kotlin.math.min


object StatCalculator{
    fun statCalculator(tradingCases: List<Pair<Trade, Trade>>): Map<StrategyMetric, Double> {
        val ret = mutableMapOf<StrategyMetric, Double>()

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


        var pnls = tradingCases.map({it.pnl()}).toDoubleArray()


        val variance = Variance().evaluate(pnls)
        val mean = Mean().evaluate(pnls)

        ret[StrategyMetric.Sharpe] = mean / variance

        for (cas in tradingCases) {
            if (cas.first.qty != cas.second.qty) {
                throw Exception("trading must have same volume")
            }
            if (cas.first.side() == cas.second.side()) {
                throw Exception("trading must have different sides")
            }
            val pn = cas.pnl()
            pnl += pn

            val ddelta = Duration.between(cas.first.dtGmt, cas.second.dtGmt)
            holdingPeriodMins += ddelta.toMinutes().toInt()

            holdingPeriodMinsStat.add(ddelta.toMinutes().toInt())

            if (pnl > maxReachedPnl) {
                maxReachedPnl = pnl
            }
            if (maxDrawDown < maxReachedPnl - pnl) {
                maxDrawDown = maxReachedPnl - pnl
            }
            maxPnl = max(pn, maxPnl)
            maxLoss = min(pn, maxLoss)
            if (pn < 0) {
                lossCount += 1
                onlyLoss += pn
                currProfitsInRow = 0
                currLossesInRow += 1
                maxLossesInRow = max(maxLossesInRow, currLossesInRow)
            }
            else {
                onlyProfit += pn
                currLossesInRow = 0
                currProfitsInRow += 1
                maxProfitsInRow = max(maxProfitsInRow, currProfitsInRow)
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
        if (tradingCases.isNotEmpty())
            ret[StrategyMetric.AvgHoldingPeriodMin] = holdingPeriodMins.toDouble() / tradingCases.size

        holdingPeriodMinsStat.sort()
        if (holdingPeriodMinsStat.size > 3) {
            ret[StrategyMetric.MedianHoldingPeriodMin] = holdingPeriodMinsStat[holdingPeriodMinsStat.size / 2].toDouble()
        }
        return ret
    }
}



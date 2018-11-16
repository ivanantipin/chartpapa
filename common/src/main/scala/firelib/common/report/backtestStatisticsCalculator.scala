package firelib.common.report

import java.time.Duration

import breeze.stats.{DescriptiveStatsTrait, MeanAndVariance}
import firelib.common.Trade
import firelib.common.misc.utils

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**

 */
object backtestStatisticsCalculator  extends DescriptiveStatsTrait with MetricsCalculator{

    override def apply(tradingCases: Seq[(Trade, Trade)]): Map[StrategyMetric, Double] = {
        val ret = mutable.HashMap[StrategyMetric, Double] {
            StrategyMetric.Trades -> tradingCases.length
        }


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
        var holdingPeriodMinsStat = new ArrayBuffer[Int]()
        var holdingPeriodSecs = 0


        var pnls : Seq[Double] = tradingCases.map(utils.pnlForCase)


        val mAndVar : MeanAndVariance = meanAndVariance(pnls)

        ret(StrategyMetric.Sharpe) = mAndVar.mean / mAndVar.variance

        for (cas <- tradingCases) {
            if (cas._1.qty != cas._2.qty) {
                throw new Exception("trading case must have same volume")
            }
            if (cas._1.side == cas._2.side) {
                throw new Exception("trading case must have different sides")
            }
            val pn = utils.pnlForCase(cas)
            pnl += pn

            val ddelta = Duration.between(cas._1.dtGmt, cas._2.dtGmt)
            holdingPeriodMins += ddelta.toMinutes.toInt

            holdingPeriodMinsStat += ddelta.toMinutes.toInt

            holdingPeriodSecs += ddelta.getSeconds.toInt

            if (pnl > maxReachedPnl) {
                maxReachedPnl = pnl
            }
            if (maxDrawDown < maxReachedPnl - pnl) {
                maxDrawDown = maxReachedPnl - pnl
            }
            maxPnl = math.max(pn, maxPnl)
            maxLoss = math.min(pn, maxLoss)
            if (pn < 0) {
                lossCount += 1
                onlyLoss += pn
                currProfitsInRow = 0
                currLossesInRow += 1
                maxLossesInRow = math.max(maxLossesInRow, currLossesInRow)
            }
            else {
                onlyProfit += pn
                currLossesInRow = 0
                currProfitsInRow += 1
                maxProfitsInRow = math.max(maxProfitsInRow, currProfitsInRow)
            }
        }


        ret(StrategyMetric.Pnl) = pnl
        ret(StrategyMetric.Pf) = math.min(-onlyProfit / onlyLoss, 4)
        ret(StrategyMetric.MaxDdStat) = maxDrawDown
        ret(StrategyMetric.AvgPnl) = pnl / tradingCases.length
        ret(StrategyMetric.MaxProfit) = maxPnl
        ret(StrategyMetric.MaxLoss) = maxLoss
        ret(StrategyMetric.MaxLossesInRow) = maxLossesInRow
        ret(StrategyMetric.MaxProfitsInRow) = maxProfitsInRow
        ret(StrategyMetric.ProfitLosses) = (tradingCases.length - lossCount) / tradingCases.length.toDouble
        ret(StrategyMetric.AvgLoss) = (onlyLoss) / lossCount
        ret(StrategyMetric.AvgProfit) = onlyProfit / (tradingCases.length - lossCount)
        if (tradingCases.length != 0)
            ret(StrategyMetric.AvgHoldingPeriodMin) = holdingPeriodMins / tradingCases.length

        holdingPeriodMinsStat = holdingPeriodMinsStat.sorted
        if (holdingPeriodMinsStat.length > 3) {
            ret(StrategyMetric.MedianHoldingPeriodMin) = holdingPeriodMinsStat(holdingPeriodMinsStat.length / 2)
        }
        if (tradingCases.length != 0)
            ret(StrategyMetric.AvgHoldingPeriodSec) = holdingPeriodSecs / tradingCases.length
        return ret.toMap
    }


}

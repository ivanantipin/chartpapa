package firelib.common.report

object StrategyMetric {
    val Trades = new StrategyMetric("Trades")
    val Sharpe = new StrategyMetric("Sharpe")
    val Pnl = new StrategyMetric("Pnl")
    val Pf = new StrategyMetric("Pf")
    val MaxDdStat = new StrategyMetric("MaxDdStat")
    val AvgPnl = new StrategyMetric("AvgPnl")
    val MaxProfit = new StrategyMetric("MaxProfit")
    val MaxLoss = new StrategyMetric("MaxLoss")
    val MaxLossesInRow = new StrategyMetric("MaxLossesInRow")
    val MaxProfitsInRow = new StrategyMetric("MaxProfitsInRow")
    val ProfitLosses = new StrategyMetric("ProfitLosses")
    val AvgLoss = new StrategyMetric("AvgLoss")
    val AvgProfit = new StrategyMetric("AvgProfit")
    val AvgHoldingPeriodMin = new StrategyMetric("AvgHoldingPeriodMin")
    val MedianHoldingPeriodMin = new StrategyMetric("MedianHoldingPeriodMin")
    val AvgHoldingPeriodSec = new StrategyMetric("AvgHoldingPeriodSec")

}


sealed case class StrategyMetric private (val name: String) {
    override def toString: String = name
}

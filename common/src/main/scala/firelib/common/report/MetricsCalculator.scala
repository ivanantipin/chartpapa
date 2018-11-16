package firelib.common.report

import firelib.common.Trade

/**

 */
trait MetricsCalculator extends ((Seq[(Trade, Trade)]) => Map[StrategyMetric, Double])

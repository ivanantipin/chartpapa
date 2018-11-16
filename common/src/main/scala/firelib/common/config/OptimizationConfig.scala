package firelib.common.config

import firelib.common.opt.OptimizedParameter
import firelib.common.report.StrategyMetric

import scala.collection.mutable.ArrayBuffer


/**
 * config that contains all optimization parameters
 */
class OptimizationConfig{

    /**
     * optimization parameters
     */
    val params = new ArrayBuffer[OptimizedParameter]

    var resourceStrategy : OptResourceStrategy = new DefaultOptResourceStrategy

    /**
    * minimum number of trades to validate strategy output
    */
    var minNumberOfTrades = 50

    /**
     * number of days to optimize params before out of sample run
     * <0 means that whole available period will be used
     */
    var optimizedPeriodDays = -1

    /**
     * optimized metric
     */
    var optimizedMetric = StrategyMetric.Pf


}

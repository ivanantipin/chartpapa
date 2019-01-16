package firelib.common.config

import firelib.common.report.StrategyMetric
import java.time.Instant
import java.time.LocalDateTime

/**
 * configuration for model backtest
 */
class ModelBacktestConfig (){
    /**
     * instruments configuration
     */
    var instruments: List<InstrumentConfig> = emptyList()

    var startDateGmt: Instant = Instant.MIN

    var endDate: Instant = Instant.MAX

    /**
     * market data folder
     * all instrument configs related to that folder
     */
    var dataServerRoot: String = ""

    /*
    * report will be written into this directory
     */
    var reportTargetPath: String = ""


    /*
    * translatest csv data to binary format to speedup backtest
    * that increase read speed from 300k msg/sec -> 10 mio msg/sec
     */
    var precacheMarketData: Boolean = true

    /**
     * params passed to model apply method
     * can not be optimized
     */
    var modelParams : Map<String, String> = emptyMap()

    /*
    * optimization config, used only for BacktestMode.Optimize
     */
    val optConfig: OptimizationConfig = OptimizationConfig()

    /*
    * this metrics will be available for optimization
     */
    val calculatedMetrics = listOf(
            StrategyMetric.Pf,
            StrategyMetric.Pnl,
            StrategyMetric.Sharpe,
            StrategyMetric.AvgPnl
    )

}
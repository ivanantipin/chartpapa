package firelib.common.config

import firelib.common.core.BacktestMode
import firelib.common.report.StrategyMetric

/**
 * configuration for model backtest
 */
class ModelBacktestConfig {
    /**
     * instruments configuration
     */
    val instruments = ArrayList<InstrumentConfig>()

    var startDateGmt: String = ""

    var endDate: String = ""

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
    var precacheMarketData : Boolean = true

    /*
    * simulates roundtrip delay between market and strategy
    */
    var networkSimulatedDelayMs = 30L

    /**
    * dump ohlc data for backtest reporting
    */
    var dumpOhlcData = true

    /**
     * params passed to model apply method
     * can not be optimized
     */
    val modelParams = HashMap<String, String>()

    /*
    * optimization config, used only for BacktestMode.Optimize
     */
    val optConfig : OptimizationConfig= OptimizationConfig()

    var backtestMode = BacktestMode.SimpleRun


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
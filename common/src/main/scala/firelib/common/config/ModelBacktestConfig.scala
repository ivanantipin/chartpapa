package firelib.common.config

import firelib.common.core.BacktestMode
import firelib.common.misc.utils
import firelib.common.model.Model
import firelib.common.report.StrategyMetric
import firelib.common.ticknorm.NormBidAskTickFunc

import scala.collection.mutable._

/**
 * configuration for model backtest
 */
class ModelBacktestConfig {
    /**
     * instruments configuration
     */
    val instruments = ArrayBuffer[InstrumentConfig]()

    var startDateGmt: String = _

    var endDate: String = _

    /**
    * market data folder
     * all instrument configs related to that folder
    */
    var dataServerRoot: String = _

    /*
    * report will be written into this directory
     */
    var reportTargetPath: String = _


    var modelClassName: String = _

    var tickToTickFuncClass : String = classOf[NormBidAskTickFunc].getName

    /*
    * translatest csv data to binary format to speedup backtest
    * that increase read speed from 300k msg/sec => 10 mio msg/sec
     */
    var precacheMarketData : Boolean = true

    /*
    * simulates roundtrip delay between market and strategy
    */
    var networkSimulatedDelayMs = 30l

    /**
    * dump ohlc data for backtest reporting
    */
    var dumpOhlcData = true

    /**
     * params passed to model apply method
     * can not be optimized
     */
    val modelParams = HashMap[String, String]()

    /*
    * optimization config, used only for BacktestMode.Optimize
     */
    val optConfig : OptimizationConfig= new OptimizationConfig

    var backtestMode = BacktestMode.SimpleRun


    /*
    * this metrics will be available for optimization
     */
    val calculatedMetrics = List(
        StrategyMetric.Pf,
        StrategyMetric.Pnl,
        StrategyMetric.Sharpe,
        StrategyMetric.AvgPnl
    )

    def newModelInstance() : Model ={
        return utils.instanceOfClass[Model](modelClassName)
    }

}

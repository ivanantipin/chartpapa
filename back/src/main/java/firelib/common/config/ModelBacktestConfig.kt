package firelib.common.config

import com.fasterxml.jackson.annotation.JsonIgnore
import firelib.common.report.StrategyMetric
import java.nio.file.Path
import java.nio.file.Paths
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

    var endDate: Instant = Instant.now()



    fun makeSpreadAdjuster(koeff : Double) : (Double,Double)->Pair<Double,Double>{
        return {bid : Double, ask : Double->Pair(bid - bid*koeff, ask + ask*koeff)}
    }

    @get:JsonIgnore
    var adjustSpread = makeSpreadAdjuster(0.0)

    /**
     * market data folder
     * all instrument configs related to that folder
     */
    var dataServerRoot: String = ""

    /*
    * report will be written into this directory
     */
    var reportTargetPath: String = ""


    fun getReportDbFile(): Path {
        return Paths.get(reportTargetPath).resolve("report.db").toAbsolutePath()
    }


    var dumpOhlc = false



    val verbose = false

    /*
    * translatest csv data to binary format to speedup backtest
    * that increase read speed from 300k msg/sec -> 10 mio msg/sec
     */
    var precacheMarketData: Boolean = true

    /**
     * params passed to model apply method
     * can not be optimized
     */
    var modelParams : MutableMap<String, String> = mutableMapOf()

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
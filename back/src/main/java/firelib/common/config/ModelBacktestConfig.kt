package firelib.common.config

import com.fasterxml.jackson.annotation.JsonIgnore
import firelib.common.core.Launcher
import firelib.common.core.ModelFactory
import firelib.common.misc.toInstantDefault
import firelib.common.model.Model
import firelib.common.opt.OptimizedParameter
import firelib.common.report.StrategyMetric
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDate


/**
 * configuration for model backtest
 */
class ModelBacktestConfig (){
    /**
     * instruments configuration
     */
    var instruments: List<InstrumentConfig> = emptyList()

    var startDateGmt: Instant = Instant.EPOCH

    var endDate: Instant = Instant.now()


    fun endDate(ed : LocalDate){
        endDate = ed.toInstantDefault()
    }


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
    var precacheMarketData: Boolean = false

    /**
     * params passed to model apply method
     * can not be optimized
     */
    val modelParams : MutableMap<String, String> = mutableMapOf()

    /*
    * optimization config, used only for BacktestMode.Optimize
     */
    val optConfig: OptimizationConfig = OptimizationConfig()

    fun opt(name : String, start : Int, end : Int, step : Int){
        optConfig.params += OptimizedParameter(name,start,end,step)
    }

    fun param(name : String, value : Int){
        modelParams += (name to value.toString())
    }
}

suspend fun ModelBacktestConfig.runStrat(fac : ModelFactory){
    this.runStrat(fac,{})
}

suspend fun ModelBacktestConfig.runStrat(fac : ModelFactory, modelListener : (Model)->Unit){
    if(this.optConfig.params.isNotEmpty()){
        Launcher.runOptimized(this,fac)
    }else{
        Launcher.runSimple(this,fac,modelListener)
    }
}
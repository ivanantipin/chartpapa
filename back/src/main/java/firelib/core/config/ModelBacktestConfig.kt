package firelib.core.config

import com.fasterxml.jackson.annotation.JsonIgnore
import firelib.core.store.GlobalConstants
import firelib.finam.FinamDownloader
import firelib.core.backtest.Backtester
import firelib.core.HistoricalSource
import firelib.core.ModelFactory
import firelib.core.domain.Interval
import firelib.core.misc.toInstantDefault
import firelib.model.Model
import firelib.model.defaultModelFactory
import firelib.core.backtest.opt.OptimizedParameter
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDate
import kotlin.reflect.KClass


/**
 * configuration for model backtest
 */
class ModelBacktestConfig (
    @get:JsonIgnore
    val modelKClass : KClass<out Model>){
    /**
     * instruments configuration
     */
    var instruments: List<String> = emptyList()

    var startDateGmt: Instant = Instant.EPOCH

    var endDate: Instant = Instant.now()

    fun roundedStartTime() : Instant{
        return interval.roundTime(startDateGmt)
    }

    var interval = Interval.Min1


    @get:JsonIgnore
    var backtestHistSource : HistoricalSource = FinamDownloader()

    fun endDate(ed : LocalDate){
        endDate = ed.toInstantDefault()
    }

    fun startDate(ed : LocalDate){
        startDateGmt = ed.toInstantDefault().plusSeconds(13*3600)
    }

    @get:JsonIgnore
    var factory : ModelFactory = defaultModelFactory(modelKClass)



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
    var reportTargetPath: String = GlobalConstants.rootReportPath.resolve(modelKClass.simpleName).toString()


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

fun ModelBacktestConfig.runStrat(){
    if(this.optConfig.params.isNotEmpty()){
        Backtester.runOptimized(this)
    }else{
        Backtester.runSimple(this)
    }
}
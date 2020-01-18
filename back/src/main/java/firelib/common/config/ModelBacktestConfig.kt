package firelib.common.config

import com.fasterxml.jackson.annotation.JsonIgnore
import com.funstat.GlobalConstants
import com.funstat.domain.InstrId
import com.funstat.store.MdStorageImpl
import com.funstat.store.SimplifiedReaderImpl
import firelib.common.core.Launcher
import firelib.common.core.ModelFactory
import firelib.common.interval.Interval
import firelib.common.misc.toInstantDefault
import firelib.common.model.DivHelper
import firelib.common.model.Model
import firelib.common.model.defaultModelFactory
import firelib.common.opt.OptimizedParameter
import firelib.common.reader.MarketDataReaderDb
import firelib.common.reader.ReaderDivAdjusted
import firelib.common.reader.SimplifiedReaderAdapter
import firelib.common.reader.toSequence
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDate
import kotlin.reflect.KClass


/**
 * configuration for model backtest
 */
class ModelBacktestConfig (val modelKClass : KClass<out Model>){
    /**
     * instruments configuration
     */
    var instruments: List<InstrumentConfig> = emptyList()

    var startDateGmt: Instant = Instant.EPOCH

    var endDate: Instant = Instant.now()

    var rootInterval = Interval.Min1


    fun endDate(ed : LocalDate){
        endDate = ed.toInstantDefault()
    }

    fun startDate(ed : LocalDate){
        startDateGmt = ed.toInstantDefault().plusSeconds(13*3600)
    }

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
        Launcher.runOptimized(this)
    }else{
        Launcher.runSimple(this)
    }
}

fun ModelBacktestConfig.instruments(tickers: Iterable<InstrId>, source: String,
                interval: Interval? = null,
                divAdjusted: Boolean = false,
                waitOnEnd: Boolean = false) : List<InstrumentConfig>{

    val storageImpl = MdStorageImpl()

    val interval =  interval ?: storageImpl.getSourceDefaultInterval(source)
    val mdDao = storageImpl.getDao(source,  interval.name)

    if(divAdjusted){
        val divs = DivHelper.getDivs()
        return tickers.map { instr ->
            InstrumentConfig(instr.code, {

                ReaderDivAdjusted(MarketDataReaderDb(mdDao, instr.code, Instant.now().plusSeconds(1000), waitOnEnd),divs[instr.code]!!).toSequence()
            }, instr)
        }

    }else{
        return tickers.map { instr ->
            InstrumentConfig(instr.code, {  SimplifiedReaderImpl(mdDao, instr.code, rootInterval.roundTime(startDateGmt))}, instr)
        }
    }
}


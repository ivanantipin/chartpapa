package firelib.core.config

import com.fasterxml.jackson.annotation.JsonIgnore
import firelib.core.HistoricalSource
import firelib.core.InstrumentMapper
import firelib.core.ModelFactory
import firelib.core.backtest.Backtester
import firelib.core.backtest.opt.OptimizedParameter
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.misc.toInstantDefault
import firelib.core.store.DbReaderFactory
import firelib.core.store.GlobalConstants
import firelib.core.store.ReaderFactory
import firelib.core.store.reader.ReaderSimpleDivAdjusted
import firelib.core.store.reader.SimplifiedReader
import firelib.finam.FinamDownloader
import firelib.model.Div
import firelib.model.Model
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDate
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor


/**
 * configuration for model backtest
 */
data class ModelBacktestConfig(
    @get:JsonIgnore
    val modelKClass: KClass<out Model>
) : Cloneable{
    /**
     * instruments configuration
     */
    var instruments: List<String> = emptyList()

    var startDateGmt: Instant = Instant.EPOCH

    var interval = Interval.Min10

    var disableBacktest = false

    val parallelTickersBacktest = false

    var endDate: Instant = Instant.now()

    fun roundedStartTime(): Instant {
        return interval.roundTime(startDateGmt)
    }

    @get:JsonIgnore
    var backtestHistSource: HistoricalSource = FinamDownloader()

    @get:JsonIgnore
    var gateMapper: InstrumentMapper = object : InstrumentMapper {
        override fun invoke(p1: String): InstrId {
            return InstrId(code = p1)
        }
    }

    @get:JsonIgnore
    var backtestReaderFactory: ReaderFactory = DbReaderFactory(
        backtestHistSource.getName(),
        interval,
        roundedStartTime()
    )


    fun endDate(ed: LocalDate) {
        endDate = ed.toInstantDefault()
    }

    fun startDate(ed: LocalDate) {
        startDateGmt = ed.toInstantDefault().plusSeconds(13 * 3600)
    }

    @get:JsonIgnore
    var factory: ModelFactory = defaultModelFactory(modelKClass)


    var tickerToDiv: Map<String, List<Div>>? = null

    var spreadAdjustKoeff = 0.0

    fun makeBidAdjuster(koeff : Double) : (Double)->Double{
        return {bid->bid - bid*koeff}
    }
    fun makeAskAdjuster(koeff : Double) : (Double)->Double{
        return {ask->ask + ask*koeff}
    }

    /*
    * report will be written into this directory
     */
    var reportTargetPath: String = GlobalConstants.rootReportPath.resolve(modelKClass.simpleName).toString()


    fun getReportDbFile(): Path {
        return Paths.get(reportTargetPath).resolve("report.db").toAbsolutePath()
    }

    fun getProdDbFile(): Path {
        return GlobalConstants.mdFolder.resolve("${modelKClass.simpleName}.db").toAbsolutePath()
    }

    var dumpInterval = Interval.None


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
    val modelParams: MutableMap<String, String> = mutableMapOf()

    /*
    * optimization config, used only for BacktestMode.Optimize
     */
    val optConfig: OptimizationConfig = OptimizationConfig()

    fun opt(name: String, start: Int, end: Int, step: Int) {
        optConfig.params += OptimizedParameter(name, start, end, step)
    }

    fun param(name: String, value: Int) {
        modelParams += (name to value.toString())
    }

    public override fun clone(): ModelBacktestConfig {
        return super.clone() as ModelBacktestConfig
    }
}

fun ModelBacktestConfig.runStrat() {
    if (this.optConfig.params.isNotEmpty()) {
        Backtester.runOptimized(this)
    } else {
        Backtester.runSimple(this)
    }
}

fun ModelBacktestConfig.enableDivs(divs: Map<String, List<Div>>) {
    this.tickerToDiv = divs

    val delegate = this.backtestReaderFactory

    this.backtestReaderFactory = object : ReaderFactory {
        override fun makeReader(security: String): SimplifiedReader {
            return ReaderSimpleDivAdjusted(
                delegate.makeReader(security),
                tickerToDiv!!.getOrDefault(security, emptyList())
            )
        }
    }


}

fun defaultModelFactory(kl: KClass<out Model>): ModelFactory {
    val cons = kl.primaryConstructor!!
    return { a, b ->
        cons.call(a, b)
    }
}



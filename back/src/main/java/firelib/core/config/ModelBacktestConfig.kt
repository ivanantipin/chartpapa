package firelib.core.config

import com.fasterxml.jackson.annotation.JsonIgnore
import firelib.core.*
import firelib.core.backtest.Backtester
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
import firelib.model.prod.runCfgWODivs
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDate
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor


/**
 * configuration for model backtest
 */
class ModelBacktestConfig() : Cloneable {
    /**
     * instruments configuration
     */
    var instruments: List<String> = emptyList()

    var startDateGmt: Instant = Instant.EPOCH

    var interval = Interval.Min10

    var disableBacktest = false

    var endDate: Instant = Instant.now()

    var maxRiskMoney = 15_000_000L

    var market : String = FinamDownloader.SHARES_MARKET

    var maxRiskMoneyPerSec = 10_000_000L

    fun roundedStartTime(): Instant {
        return interval.roundTime(startDateGmt)
    }

    var histSourceName: SourceName = SourceName.MOEX

    @get:JsonIgnore
    var gateMapper: InstrumentMapper = object : InstrumentMapper {
        override fun invoke(p1: String): InstrId {
            return InstrId(code = p1)
        }
    }

    @get:JsonIgnore
    val backtestReaderFactory by lazy {
        if (tickerToDiv != null) {
            val delegate = DbReaderFactory(
                histSourceName,
                interval,
                roundedStartTime(),
                market = market
            )
            object : ReaderFactory {
                override fun makeReader(security: String): SimplifiedReader {
                    return ReaderSimpleDivAdjusted(
                        delegate.makeReader(security),
                        tickerToDiv!!.getOrDefault(security, emptyList())
                    )
                }
            }
        }else{
            DbReaderFactory(
                histSourceName,
                interval,
                roundedStartTime(),
                market = market
            )
        }
    }


    fun endDate(ed: LocalDate) {
        endDate = ed.toInstantDefault()
    }

    fun startDate(ed: LocalDate) {
        startDateGmt = ed.toInstantDefault().plusSeconds(13 * 3600)
    }


    var tickerToDiv: Map<String, List<Div>>? = null

    var spreadAdjustKoeff = 0.0

    fun makeBidAdjuster(koeff: Double): (Double) -> Double {
        return { bid -> bid - bid * koeff }
    }

    fun makeAskAdjuster(koeff: Double): (Double) -> Double {
        return { ask -> ask + ask * koeff }
    }

    /*
    * report will be written into this directory
     */
    var reportTargetPath: String = GlobalConstants.rootReportPath.toString()

    fun getReportDbFile(): Path {
        return Paths.get(reportTargetPath).resolve("report.db").toAbsolutePath()
    }


    var dumpInterval = Interval.None


    public override fun clone(): ModelBacktestConfig {
        return super.clone() as ModelBacktestConfig
    }
}


fun ModelConfig.runStrat(runConfig: ModelBacktestConfig) {
    if (this.optConfig.params.isNotEmpty()) {
        Backtester.runOptimized(this, runConfig)
    } else {
        Backtester.runSimple(this,runConfig)
    }
}

fun ModelBacktestConfig.enableDivs(divs: Map<String, List<Div>>) {
    this.tickerToDiv = divs
}

fun ModelConfig.setTradeSize(tradeSize: Int) {
    this.param("trade_size", tradeSize)
}


fun defaultModelFactory(kl: KClass<out Model>): ModelFactory {
    val cons = kl.primaryConstructor!!
    return { a, b ->
        cons.call(a, b)
    }
}



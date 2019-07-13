package firelib.common.model

import com.funstat.finam.FinamDownloader
import com.funstat.sequenta.Sequenta
import com.funstat.sequenta.SignalType
import com.funstat.store.MdStorageImpl
import firelib.common.config.InstrumentConfig
import firelib.common.config.ManualOptResourceStrategy
import firelib.common.config.ModelBacktestConfig
import firelib.common.core.Launcher.runOptimized
import firelib.common.core.Launcher.runSimple
import firelib.common.interval.Interval
import firelib.common.misc.PositionCloserByTimeOut
import firelib.common.misc.Quantiles
import firelib.common.misc.atUtc
import firelib.common.opt.OptimizedParameter
import firelib.common.ordermanager.OrderManager
import firelib.common.ordermanager.makePositionEqualsTo
import firelib.common.reader.MarketDataReaderSql
import firelib.common.reader.ReaderDivAdjusted
import firelib.common.timeseries.TimeSeries
import firelib.domain.Ohlc
import firelib.indicators.ATR
import firelib.indicators.Quantiled
import java.time.Duration
import java.time.LocalDate


/*

factors brainstorm:

vo

 */

class VolumeIncreaseTrend(val context: ModelContext, val props: Map<String, String>) : Model {

    val oms = makeOrderManagers(context)

    val sequentas = context.instruments.mapIndexed { _, _ ->Sequenta()}
    val weekSequentas = context.instruments.mapIndexed { _, _ ->Sequenta()}


    val daytss = context.instruments.mapIndexed { idx, _ -> context.mdDistributor.getOrCreateTs(idx, Interval.Day, 100)}
    val weektss = context.instruments.mapIndexed { idx, _ -> context.mdDistributor.getOrCreateTs(idx, Interval.Week, 100)}

    val hourtss = context.instruments.mapIndexed { idx, _ -> context.mdDistributor.getOrCreateTs(idx, Interval.Min60, 100)}

    val quantiles = context.instruments.map {
        Quantiles<Double>(1000);
    }

    var currentTrendUp = context.instruments.map { false }.toBooleanArray()


    private var atrs: List<Quantiled>

    private var ohs: List<Quantiled>

    init {

        atrs = daytss.map {
            val atr = ATR(9, it)
            Quantiled(100,it,{
                atr.value()
            })
        }
        ohs = daytss.map { Quantiled(100,it, {it[0].range()}) }

        enableFactor("Trend", {
            if(currentTrendUp[it]) 1.0 else -1.0
        })

        enableFactor("OH",{
            ohs[it].value
        })

        enableFactor("ATR",{
            atrs[it].value
        })

        weektss.mapIndexed({ idx, it ->
            it.preRollSubscribe {
                if(!it[0].interpolated){
                    val signals = weekSequentas[idx].onOhlc(it[0])
                    val find = signals.find { it.type == SignalType.SetupReach }
                    if(find != null){
                        currentTrendUp[idx] = find.reference.up
                    }
                }
            }
        })



        daytss.mapIndexed({ idx, it ->
            it.preRollSubscribe {
                if(!it[0].interpolated){
                    val signals = sequentas[idx].onOhlc(it[0])
                    val find = signals.find { it.type == SignalType.SetupReach }
                    if(find != null){
                        if(find.reference.up){
                            oms[idx].makePositionEqualsTo((1000_000/it[0].close).toInt())
                        }
                    }
                }
            }
        })


        oms.forEachIndexed({ idx, om ->
            PositionCloserByTimeOut(om, Duration.ofDays(props["holddays"]!!.toLong()), context.mdDistributor, Interval.Min10, idx)
        })
    }



    override fun orderManagers(): List<OrderManager> {
        return oms
    }

    override fun update() {


    }

    override fun properties(): Map<String, String> {
        return props
    }
}

suspend fun main(args: Array<String>) {


    val tt = listOf(
            "sber",
            "lkoh",
            "gazp",
            "alrs",
            "moex",
            "gmkn",
            "mgnt",
            "chmf",
            "sberp",
            "nvtk",
            "nlmk",
            "mtss",
            "magn"
    )

    val divsMap = DivHelper.getDivs()

    val divs = divsMap


    val conf = ModelBacktestConfig()
    conf.reportTargetPath = "/home/ivan/projects/fbackend/market_research/report_tmp"

    val mdDao = MdStorageImpl().getDao(FinamDownloader.SOURCE, Interval.Min10.name)

    conf.instruments = divs.keys.filter { it != "irao" && it != "trnfp" }.map { instr ->
        InstrumentConfig(instr, { _ ->
            ReaderDivAdjusted(MarketDataReaderSql(mdDao.queryAll(instr)), divs[instr]!!)
        })
    }

    conf.precacheMarketData = false

    conf.optConfig.params += OptimizedParameter("holddays", 2, 50, 1)
    conf.optConfig.resourceStrategy = ManualOptResourceStrategy(1, 100)
    conf.dumpOhlc = true

    conf.modelParams["holddays"] = "23"

    runSimple(conf, {cfg,fac ->
        VolumeIncreaseTrend(cfg, fac)
    })


//    runOptimized(conf, { cfg, fac ->
//        VolumeIncreaseTrend(cfg, fac)
//    })
    println("done")

}

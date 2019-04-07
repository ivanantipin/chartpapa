package firelib.common.model

import com.funstat.finam.FinamDownloader
import com.funstat.store.MdStorageImpl
import firelib.common.config.InstrumentConfig
import firelib.common.config.ManualOptResourceStrategy
import firelib.common.config.ModelBacktestConfig
import firelib.common.core.runOptimized
import firelib.common.core.runSimple
import firelib.common.interval.Interval
import firelib.common.misc.PositionCloserByTimeOut
import firelib.common.opt.OptimizedParameter
import firelib.common.ordermanager.OrderManager
import firelib.common.ordermanager.makePositionEqualsTo
import firelib.common.reader.MarketDataReaderSql
import firelib.common.reader.ReaderDivAdjusted
import firelib.common.report.GenericDumper
import java.time.Duration


class GapTrading(val context: ModelContext, val fac: Map<String, String>) : Model{

    val oms = makeOrderManagers(context)

    enum class WriteState{ZERO,GAP_WRITTEN,HOUR_WRITTEN}

    val stat = mutableListOf<GapStat>()

    data class GapStat(val ticker: String, val gapPct: Double,
                       val h0: Double,
                       val h1: Double,
                       val h2: Double,
                       val h3: Double,
                       val h4: Double
    )

    init {


        val dayRolled = context.instruments.map { false }.toMutableList()

        val tssDay = context.instruments.mapIndexed{idx,tick->
            val ret = context.mdDistributor.getOrCreateTs(idx, Interval.Day, 10)
            ret.preRollSubscribe {
                if(!ret[0].interpolated){
                    dayRolled[idx] = true
                }
            }
            ret
        }

        val tss10Min = context.instruments.mapIndexed{idx,tick->
            context.mdDistributor.getOrCreateTs(idx, Interval.Min10, 100)
        }



        context.instruments.forEachIndexed{idx,tick->
            val ret = context.mdDistributor.getOrCreateTs(idx, Interval.Min60, 1000)
            ret.preRollSubscribe {
                if(dayRolled[idx] && it[1].interpolated && !it[0].interpolated){
                    dayRolled[idx] = false
                    val gap = (it[0].open - tssDay[idx][1].close)/tssDay[idx][1].close

                    if(gap < -0.02){
                        oms[idx].makePositionEqualsTo(-1000000)
                    }
                    println("written ${it[0].dtGmtEnd} ${tssDay[idx][1].dtGmtEnd} ")
                }
            }
            ret
        }

        oms.forEachIndexed({ idx, om ->
            PositionCloserByTimeOut(om, Duration.ofHours(properties()["holdtime"]!!.toLong()), context.mdDistributor, Interval.Min10, idx)
        })


    }

    override fun orderManagers(): List<OrderManager> {
        return oms
    }

    override fun onBacktestEnd() {
        super.onBacktestEnd()
        val writer = GenericDumper<GapStat>("gaps", context.config.getReportDbFile(), GapStat::class)
        writer.write(stat)
    }

    override fun update() {

    }

    override fun properties(): Map<String, String> {
        return fac
    }

}

fun main(args: Array<String>) {


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
    conf.reportTargetPath = "./report/gapTrading"

    val mdDao = MdStorageImpl().getDao(FinamDownloader.SOURCE, Interval.Min10.name)

    conf.instruments = tt.map { instr ->
        InstrumentConfig(instr, { time ->
            ReaderDivAdjusted(MarketDataReaderSql(mdDao.queryAll(instr)), divs[instr]!!)
        })
    }

    conf.optConfig.params += OptimizedParameter("holdtime", 1,3,1)
    conf.optConfig.resourceStrategy = ManualOptResourceStrategy(1,100)

    conf.precacheMarketData = false

    runOptimized(conf, {cfg,fac->
        GapTrading(cfg,fac)
    })
    println("done")

}

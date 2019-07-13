package firelib.common.model

import com.funstat.finam.FinamDownloader
import com.funstat.store.MdStorageImpl
import firelib.common.config.InstrumentConfig
import firelib.common.config.ModelBacktestConfig
import firelib.common.core.Launcher.runSimple
import firelib.common.interval.Interval
import firelib.common.ordermanager.OrderManager
import firelib.common.reader.MarketDataReaderSql
import firelib.common.reader.ReaderDivAdjusted
import firelib.common.report.GenericDumper


class MarketOpen(val context: ModelContext, val fac: Map<String, String>) : Model{

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
                if(dayRolled[idx] && it[5].interpolated && !it[4].interpolated){
                    dayRolled[idx] = false

                    stat.add(GapStat(ticker = tick,
                            gapPct = (it[4].open - tssDay[idx][1].close)/tssDay[idx][1].close,
                            h0 = it[4].ret(),
                            h1 = it[3].ret(),
                            h2 = it[2].ret(),
                            h3 = it[1].ret(),
                            h4 = it[0].ret()

                    ))

                    println("written ${it[0].dtGmtEnd} ${tssDay[idx][1].dtGmtEnd} ")
                }
            }
            ret
        }




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
    conf.reportTargetPath = "./report/marketOpen"

    val mdDao = MdStorageImpl().getDao(FinamDownloader.SOURCE, Interval.Min10.name)

    conf.instruments = tt.map { instr ->
        InstrumentConfig(instr, { time ->
            ReaderDivAdjusted(MarketDataReaderSql(mdDao.queryAll(instr)), divs[instr]!!)
        })
    }

    conf.precacheMarketData = false

    runSimple(conf, {cfg,fac->
        MarketOpen(cfg,fac)
    })
    println("done")

}

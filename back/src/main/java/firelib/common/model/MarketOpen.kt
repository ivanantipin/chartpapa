package firelib.common.model

import com.funstat.finam.FinamDownloader
import com.funstat.store.MdStorageImpl
import firelib.common.config.InstrumentConfig
import firelib.common.config.ModelBacktestConfig
import firelib.common.config.runStrat
import firelib.common.core.Launcher.runSimple
import firelib.common.interval.Interval
import firelib.common.ordermanager.OrderManager
import firelib.common.reader.MarketDataReaderSql
import firelib.common.reader.ReaderDivAdjusted
import firelib.common.report.GenericDumper


class MarketOpen(context: ModelContext, val fac: Map<String, String>) : Model(context, fac) {

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

        val tssDay = context.instruments.mapIndexed { idx, tick ->
            val ret = context.mdDistributor.getOrCreateTs(idx, Interval.Day, 10)
            ret.preRollSubscribe {
                if (!ret[0].interpolated) {
                    dayRolled[idx] = true
                }
            }
            ret
        }

        context.instruments.forEachIndexed { idx, tick ->
            val ret = context.mdDistributor.getOrCreateTs(idx, Interval.Min60, 1000)
            ret.preRollSubscribe {
                if (dayRolled[idx] && it[5].interpolated && !it[4].interpolated) {
                    dayRolled[idx] = false

                    stat.add(GapStat(ticker = tick,
                            gapPct = (it[4].open - tssDay[idx][1].close) / tssDay[idx][1].close,
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

    override fun onBacktestEnd() {
        super.onBacktestEnd()
        val writer = GenericDumper<GapStat>("gaps", context.config.getReportDbFile(), GapStat::class)
        writer.write(stat)
    }

}

suspend fun main() {


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

    val divs = DivHelper.getDivs()
    val mdDao = MdStorageImpl().getDao(FinamDownloader.SOURCE, Interval.Min10.name)


    val conf = ModelBacktestConfig().apply {
        reportTargetPath = "./report/marketOpen"
        instruments = tt.map { instr ->
            InstrumentConfig(instr, { time ->
                ReaderDivAdjusted(MarketDataReaderSql(mdDao.queryAll(instr)), divs[instr]!!)
            })
        }
    }

    conf.runStrat { cfg, fac ->
        MarketOpen(cfg, fac)
    }
}

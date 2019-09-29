package firelib.common.model

import com.funstat.finam.FinamDownloader
import com.funstat.store.MdStorageImpl
import firelib.common.config.InstrumentConfig
import firelib.common.config.ModelBacktestConfig
import firelib.common.config.runStrat
import firelib.common.interval.Interval
import firelib.common.reader.MarketDataReaderSql
import firelib.common.reader.ReaderDivAdjusted
import firelib.common.report.GenericDumper


class GapTrading(context: ModelContext, fac: Map<String, String>) : Model(context, fac) {

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



        val tssDay = enableSeries(Interval.Day)

        tssDay.forEachIndexed{idx,it->
            it.preRollSubscribe {
                if (!it[0].interpolated) {
                    dayRolled[idx] = true
                }
            }
        }


        context.instruments.forEachIndexed { idx, tick ->
            val ret = context.mdDistributor.getOrCreateTs(idx, Interval.Min60, 1000)
            ret.preRollSubscribe {
                if (dayRolled[idx] && it[1].interpolated && !it[0].interpolated) {
                    dayRolled[idx] = false
                    val gap = (it[0].open - tssDay[idx][1].close) / tssDay[idx][1].close
                    if (gap < -0.02) {
                        sellIfNoPosition(idx, -1000_000)
                    }
                    println("written ${it[0].dtGmtEnd} ${tssDay[idx][1].dtGmtEnd} ")
                }
            }
            ret
        }

        closePositionByTimeout(periods = properties["holdtime"]!!.toInt(), interval = Interval.Min60)

    }

    override fun onBacktestEnd() {
        super.onBacktestEnd()
        val writer = GenericDumper<GapStat>("gaps", context.config.getReportDbFile(), GapStat::class)
        writer.write(stat)
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

    val divs = DivHelper.getDivs()

    val mdDao = MdStorageImpl().getDao(FinamDownloader.SOURCE, Interval.Min10.name)

    val conf = ModelBacktestConfig().apply {
        reportTargetPath = "./report/gapTrading"
        instruments = tt.map { instr ->
            InstrumentConfig(instr, { time ->
                ReaderDivAdjusted(MarketDataReaderSql(mdDao.queryAll(instr)), divs[instr]!!)
            })
        }
        opt("holdtime", 1, 3, 1)
    }

    conf.runStrat{ cfg, fac ->
        GapTrading(cfg, fac)
    }
}

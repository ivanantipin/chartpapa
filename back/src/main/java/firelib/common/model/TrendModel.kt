package firelib.common.model

import com.funstat.finam.FinamDownloader
import com.funstat.store.MdStorageImpl
import firelib.common.config.*
import firelib.common.core.Launcher.runOptimized
import firelib.common.interval.Interval
import firelib.common.opt.OptimizedParameter
import firelib.common.ordermanager.OrderManager
import firelib.common.ordermanager.flattenAll
import firelib.common.reader.MarketDataReaderSql
import firelib.common.reader.ReaderDivAdjusted

class TrendModel(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    init {

        val tss = enableSeries(Interval.Day)

        context.mdDistributor.addListener(Interval.Day, { time, dist ->
            if (tss[0].count() > 40 && (tss[0].count() % 21) == 0) {

                val back = props["period"]!!.toInt()

                val indexed = tss.mapIndexed({ idx, ts -> Pair(idx, (ts[0].close - ts[back].close) / ts[back].close) })

                val sorted = indexed.sortedBy { -it.second }

                sorted.forEachIndexed({ idx, pair ->
                    if (idx < sorted.size - 3) {
                        oms[pair.first].flattenAll()
                    } else {
                        buyIfNoPosition(idx, 1000_000)
                    }
                })
            }
        })

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
        reportTargetPath = "./report/trendModel"
        instruments = tt.map { instr ->
            InstrumentConfig(instr, { time ->
                ReaderDivAdjusted(MarketDataReaderSql(mdDao.queryAll(instr)), divs[instr]!!)
            })
        }
        opt("period", 7, 30, 1)
    }

    conf.runStrat { cfg, fac ->
        TrendModel(cfg, fac)
    }
}

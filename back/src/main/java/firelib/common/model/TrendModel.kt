package firelib.common.model

import com.funstat.domain.InstrId
import com.funstat.finam.FinamDownloader
import com.funstat.store.MdStorageImpl
import firelib.common.config.InstrumentConfig
import firelib.common.config.ModelBacktestConfig
import firelib.common.config.runStrat
import firelib.common.interval.Interval
import firelib.common.ordermanager.flattenAll
import firelib.common.reader.MarketDataReaderSql
import firelib.common.reader.ReaderDivAdjusted
import firelib.common.reader.toSequence

class TrendModel(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    init {

        val tss = enableSeries(Interval.Day)

        context.mdDistributor.addListener(Interval.Day, { _, _ ->
            if (tss[0].count() > 40 && (tss[0].count() % 21) == 0) {

                val back = props["period"]!!.toInt()

                val indexed = tss.mapIndexed({ idx, ts -> Pair(idx, (ts[0].close - ts[back].close) / ts[back].close) })

                val sorted = indexed.sortedBy { -it.second }

                sorted.forEachIndexed({ idx, pair ->
                    if (idx < sorted.size - 3) {
                        oms[pair.first].flattenAll()
                    } else {
                        longForMoneyIfFlat(idx, 1000_000)
                    }
                })
            }
        })

    }
}

fun main() {

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

    val conf = ModelBacktestConfig(TrendModel::class).apply {
        instruments = tt.map { instr ->
            InstrumentConfig(instr, {
                ReaderDivAdjusted(MarketDataReaderSql(mdDao.queryAll(instr)), divs[instr]!!).toSequence()
            }, InstrId.dummyInstrument(instr))
        }
        opt("period", 7, 30, 1)
    }
    conf.runStrat()
}

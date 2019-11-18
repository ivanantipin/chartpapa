package firelib.common.model

import com.funstat.finam.FinamDownloader
import com.funstat.store.MdStorageImpl
import firelib.common.config.InstrumentConfig
import firelib.common.config.ModelBacktestConfig
import firelib.common.config.runStrat
import firelib.common.interval.Interval
import firelib.common.misc.Quantiles
import firelib.common.reader.MarketDataReaderSql
import firelib.common.reader.ReaderDivAdjusted
import firelib.common.report.GenericDumper
import firelib.common.timeseries.TimeSeries
import firelib.domain.Ohlc
import java.nio.file.Paths


/*

investigate how price behaves when volume spikes

take in account following factors

1. price increased/decreased last 3,5,8,13 days
2.


 */

data class HVStat(val volumeQuantile: Double, val ret: Double,
                  val diff3: Double,
                  val diff5: Double,
                  val diff8: Double,
                  val diff13: Double,
                  val ticker: String
)

class HighVolume(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    init {
        val daytss = enableSeries(Interval.Day, interpolated = false)

        val quantiles = context.instruments.map {
            Quantiles<Double>(1000);
        }

        val dumper = GenericDumper("highvolume", Paths.get(context.config.reportTargetPath).resolve("stat.db"), HVStat::class)

        daytss.forEachIndexed{ idx, it ->
            it.preRollSubscribe {
                if (it.count() > 20) {
                    val oh = it[3]
                    val volume = oh.volume.toDouble()
                    quantiles[idx].add(volume)
                    val qq = quantiles[idx].getQuantile(volume)
                    println(qq)
                    if (qq > 0.9) {
                        val hvStat = HVStat(qq,
                                makeRet(it, 0, 3),
                                makeRet(it, 3, 6),
                                makeRet(it, 3, 8),
                                makeRet(it, 3, 11),
                                makeRet(it, 3, 18),
                                context.instruments[idx]
                        )
                        dumper.write(listOf(hvStat))
                    }
                }

            }
        }
    }

    private fun makeRet(it: TimeSeries<Ohlc>, end: Int, start: Int) = (it[end].close - it[start].close) / it[start].close

}

suspend fun main(args: Array<String>) {

    val divs = DivHelper.getDivs()

    val mdDao = MdStorageImpl().getDao(FinamDownloader.SOURCE, Interval.Min10.name)

    val conf = ModelBacktestConfig().apply {
        reportTargetPath = "/home/ivan/projects/chartpapa/market_research/report_tmp"
        instruments = divs.keys.filter { it != "irao" && it != "trnfp" }.map { instr ->
            InstrumentConfig(instr, { _ ->
                ReaderDivAdjusted(MarketDataReaderSql(mdDao.queryAll(instr)), divs[instr]!!)
            })
        }
    }

    conf.runStrat({ cfg, fac ->
        HighVolume(cfg, fac)
    },{})

}

package firelib.model

import firelib.core.config.ModelBacktestConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.misc.Quantiles
import firelib.core.report.dao.GeGeWriter
import firelib.core.timeseries.TimeSeries
import firelib.core.domain.Ohlc
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

        val quantiles = context.config.instruments.map {
            Quantiles<Double>(1000);
        }


        val dumper = GeGeWriter(
            "highvolume",
            Paths.get(context.config.reportTargetPath).resolve("stat.db"),
            HVStat::class
        )
        daytss.forEachIndexed{ idx, it ->
            it.preRollSubscribe {
                if (it.count() > 20) {
                    val oh = it[3]
                    val volume = oh.volume.toDouble()
                    quantiles[idx].add(volume)
                    val qq = quantiles[idx].getQuantile(volume)
                    if (qq > 0.9) {
                        val hvStat = HVStat(qq,
                                makeRet(it, 0, 3),
                                makeRet(it, 3, 6),
                                makeRet(it, 3, 8),
                                makeRet(it, 3, 11),
                                makeRet(it, 3, 18),
                                context.config.instruments[idx]
                        )
                        dumper.write(listOf(hvStat))
                    }
                }

            }
        }
    }

    private fun makeRet(it: TimeSeries<Ohlc>, end: Int, start: Int) = (it[end].close - it[start].close) / it[start].close

}

fun main() {
    val conf = ModelBacktestConfig(HighVolume::class).apply {
        instruments = DivHelper.getDivs().keys.toList()
    }
    conf.runStrat()
}

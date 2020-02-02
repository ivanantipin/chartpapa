package firelib.common.model

import firelib.common.core.config.ModelBacktestConfig
import firelib.common.core.config.runStrat
import firelib.domain.Interval
import firelib.common.misc.Quantiles
import firelib.common.core.timeseries.TimeSeries
import firelib.domain.Ohlc
import java.time.LocalDate


/*

research model to investigate volume spikes q>0.9
on liquid russian stocks
with previos price return as factor before
 */

class HighVolumeTrade(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    init {

        val quantiles = context.config.instruments.map {
            Quantiles<Double>(100);
        }


        val daytss = enableSeries(Interval.Day, interpolated = false)


        // periods of previos price as factors
        val factors = setOf(3, 5, 8, 13).associateBy({ it }, {
            context.config.instruments.map {
                Quantiles<Double>(200);
            }
        })

        factors.forEach { facLen, lst ->
            enableFactor("diff${facLen}") {
                factors[facLen]!![it].getQuantile(makeRet(daytss[it], 0, facLen))
            }
        }


        daytss.forEachIndexed { idx, it ->
            it.preRollSubscribe {
                if (it.count() > 20) {
                    val measure = makeRet(daytss[idx], 0, props["length"]!!.toInt())
                    quantiles[idx].add(measure)
                    factors.forEach { (facLen, lst) ->
                        factors[facLen]!![idx].add(makeRet(daytss[idx], 0, facLen))
                    }
                    // go long whenever volume spikes
                    if (quantiles[idx].getQuantile(measure) > 0.9) {
                        longForMoneyIfFlat(idx, 1000_000)
                    }
                }
            }
        }

        closePositionByTimeout(periods = 3, interval = Interval.Day)
    }

    private fun makeRet(it: TimeSeries<Ohlc>, end: Int, length: Int) = (it[end].close - it[end + length].close) / it[end + length].close

}

fun main() {
    val conf = ModelBacktestConfig(HighVolumeTrade::class).apply {
        endDate(LocalDate.of(2016, 1, 1))
        instruments = DivHelper.getDivs().keys.toList()
        opt("length", 1, 10, 1)
    }

    conf.runStrat()
}
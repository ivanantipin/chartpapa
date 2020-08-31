package firelib.model

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.timeseries.TimeSeries
import java.time.LocalDate


/*

research model to investigate volume spikes q>0.9
on liquid russian stocks
with previos price return as factor before
 */

class HighVolumeTrade(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    init {

        val quantiles = quantiles(100)


        val daytss = enableSeries(Interval.Day, interpolated = false)


        // periods of previos price as factors
        val factors = setOf(3, 5, 8, 13).associateBy({ it }, {
            quantiles(200)
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

    private fun makeRet(it: TimeSeries<Ohlc>, end: Int, length: Int) =
        (it[end].close - it[end + length].close) / it[end + length].close

    companion object {
        fun modelConfig(): ModelConfig {
            return ModelConfig(HighVolumeTrade::class).apply {
                opt("length", 1, 10, 1)
            }
        }

    }


}

fun main() {
    HighVolumeTrade.modelConfig().runStrat(ModelBacktestConfig().apply {
        instruments = tickers
        startDate(LocalDate.now().minusDays(3000))
    })
}
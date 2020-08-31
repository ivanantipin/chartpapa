package firelib.model.google

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import java.time.Instant
import java.time.LocalDate


class GoogTrends(context: ModelContext, val props: Map<String, String>) : Model(context, props) {


    init {
        val buyStocks = GoogleTrendsReader.read("buy stocks")
        val sellStocks = GoogleTrendsReader.read("sell stocks")

        val buyAssoc = buyStocks.associateBy { it.dt }

        val trends = sellStocks.flatMap {
            val buyA = buyAssoc.get(it.dt)
            if (buyA == null) {
                println("null for ${it.dt}")
                emptyList()
            }else{
                listOf(GoogTrend("diff", it.dt, Instant.now(), buyA.idx - it.idx))
            }
        }

        val maDiff =
            GoogMaDiff(trends.map { Pair(it.dt, it.idx.toDouble()) }, 10)

        enableSeries(Interval.Min30, interpolated = false)[0].preRollSubscribe {
            val diff = maDiff.getDiff(currentTime())
            if(diff > 0){

                longForMoneyIfFlat(0,100_000) // risk on
                longForMoneyIfFlat(1,100_000) // risk on
            } else {
                flattenAll(0)
                flattenAll(1)
//                flattenAll(0)
//                //flattenAll(1)
//                longForMoneyIfFlat(1,100_000)  // risk off
            }
        }
    }

    companion object {
        fun modelConfig(): ModelConfig {
            return ModelConfig(GoogTrends::class)
        }

    }

}

fun main() {
    GoogTrends.modelConfig().runStrat(ModelBacktestConfig().apply {
        instruments = listOf("XLY","XLP")
        interval = Interval.Min30
        histSourceName = SourceName.IQFEED
        startDate(LocalDate.now().minusDays(2000))
    })
}
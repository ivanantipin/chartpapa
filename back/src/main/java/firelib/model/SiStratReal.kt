package firelib.model

import firelib.core.config.ModelBacktestConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.domain.ret
import firelib.core.misc.Quantiles
import firelib.core.misc.atMoscow
import firelib.core.timeseries.ret
import firelib.indicators.Ma
import firelib.indicators.SimpleMovingAverage
import java.time.LocalDate
import java.util.*


class SiStratReal(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    init {

        val intraTs = enableSeries(Interval.Min60, 100, false)[0]

        val trend  = GoogleTrendsReader.read("курс доллара")

        val trends = GoogMaDiff(trend.map { Pair(it.dt, it.idx.toDouble()) }, 8)

        intraTs.preRollSubscribe {

            val diff = trends.getDiff(currentTime())
            println("diff is ${diff}")
            println("sko  is ${trends.trendma.sko()}")

            if(diff > 0){
                longForMoneyIfFlat(0,100_000)
            }else if(diff < 0){
                shortForMoneyIfFlat(0,100_000)
            }else{
                flattenAll(0)
            }

        }
    }
}

fun siReal(): ModelBacktestConfig {
    return ModelBacktestConfig(SiStratReal::class).apply {
        instruments = listOf("SPFB_Si")
        //dumpInterval = Interval.Min30
        startDate(LocalDate.now().minusDays(5000))
    }
}

fun main() {
    siReal().runStrat()
}
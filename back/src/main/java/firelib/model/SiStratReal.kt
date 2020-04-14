package firelib.model

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.model.google.GoogMaDiff
import firelib.model.google.GoogleTrendsReader
import java.time.LocalDate


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


    companion object{
        fun modelConfig(): ModelConfig {
            return ModelConfig(SiStratReal::class, ModelBacktestConfig().apply {
                instruments = listOf("SPFB_Si")
                startDate(LocalDate.now().minusDays(5000))
            }).apply {
                param("hold_hours", 30)
            }
        }

    }
}


fun main() {
    SiStratReal.modelConfig().runStrat()
}